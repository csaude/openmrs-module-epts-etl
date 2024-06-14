package org.openmrs.module.epts.etl.monitor;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.EtlProgressMeter;
import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.TableOperationProgressInfo;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeController;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class monitor all {@link TaskProcessor}s of an {@link OperationController}
 * 
 * @author jpboane
 */
public class Engine implements MonitoredOperation {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private OperationController controller;
	
	private EtlItemConfiguration etlItemConfiguration;
	
	private List<TaskProcessor> ownEngines;
	
	private String engineMonitorId;
	
	private String engineId;
	
	private int operationStatus;
	
	private boolean stopRequested;
	
	protected TableOperationProgressInfo tableOperationProgressInfo;
	
	protected List<IntervalExtremeRecord> excludedRecordsLimits;
	
	private AbstractEtlSearchParams<?> searchParams;
	
	public Engine(OperationController controller, EtlItemConfiguration etlItemConfiguration,
	    TableOperationProgressInfo tableOperationProgressInfo) {
		this.controller = controller;
		this.ownEngines = new ArrayList<TaskProcessor>();
		this.etlItemConfiguration = etlItemConfiguration;
		
		this.engineMonitorId = (controller.getControllerId() + "_" + this.getEtlConfigCode() + "_monitor").toLowerCase();
		this.engineId = (getController().getControllerId() + "_" + this.getEtlConfigCode()).toLowerCase();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		this.tableOperationProgressInfo = tableOperationProgressInfo;
		
		this.searchParams = controller.initMainSearchParams();
	}
	
	public AbstractEtlSearchParams<?> getSearchParams() {
		return searchParams;
	}
	
	public ThreadRecordIntervalsManager getLimits() {
		return getSearchParams().getLimits();
	}
	
	public void setSearchParams(AbstractEtlSearchParams<?> searchParams) {
		this.searchParams = searchParams;
	}
	
	public List<IntervalExtremeRecord> getExcludedRecordsIntervals() {
		return excludedRecordsLimits;
	}
	
	public void setExcludedRecordsLimits(List<IntervalExtremeRecord> excludedRecordsLimits) {
		this.excludedRecordsLimits = excludedRecordsLimits;
	}
	
	public long getMinRecordId() {
		return getProgressMeter().getMinRecordId();
	}
	
	public long getMaxRecordId() {
		return getProgressMeter().getMaxRecordId();
	}
	
	public SrcConf getSrcMainTableConf() {
		return this.getEtlConfiguration().getSrcConf();
	}
	
	public List<TaskProcessor> getOwnEngines() {
		return ownEngines;
	}
	
	public String getEngineId() {
		return engineId;
	}
	
	public String getEngineMonitorId() {
		return engineMonitorId;
	}
	
	public EtlItemConfiguration getEtlConfiguration() {
		return this.etlItemConfiguration;
	}
	
	public String getEtlConfigCode() {
		return this.getEtlConfiguration().getConfigCode();
	}
	
	public EtlProgressMeter getProgressMeter() {
		return this.tableOperationProgressInfo != null ? this.tableOperationProgressInfo.getProgressMeter() : null;
	}
	
	public TaskProcessor getMainEngine() {
		for (TaskProcessor taskProcessor : this.ownEngines) {
			if (taskProcessor.getChildren() != null) {
				return taskProcessor;
			}
		}
		
		if (utilities.arrayHasElement(this.ownEngines)) {
			return this.ownEngines.get(0);
		}
		
		return null;
	}
	
	@Override
	public void run() {
		try {
			if (!getEtlConfiguration().isFullLoaded())
				getEtlConfiguration().fullLoad();
			
			initEngine();
			
			if (!utilities.arrayHasElement(ownEngines)) {
				if (!mustRestartInTheEnd()) {
					logWarn(
					    "NO ENGINE FOR '" + getController().getOperationType().name().toLowerCase() + "' FOR ETL CONFIG '"
					            + getEtlConfiguration().getConfigCode().toUpperCase() + "' WAS CREATED...");
					
					this.operationStatus = MonitoredOperation.STATUS_FINISHED;
				} else {
					onStart();
					
					doWait();
				}
			} else {
				onStart();
				
				logDebug("INITIALIZED '" + getController().getOperationType().name().toLowerCase()
				        + "' ENGINE FOR ETL CONFIG'" + getEtlConfiguration().getConfigCode().toUpperCase() + "'");
				
				doWait();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			
			getController().requestStopDueError(this, e);
		}
	}
	
	void doWait() throws DBException {
		while (isRunning()) {
			
			if (mustRestartInTheEnd()) {
				//Sleep more time if must restart in the end is one to prevent
				//Repeatedly retries when there is no more data to process
				TimeCountDown.sleep(5 * 60);
			} else {
				TimeCountDown.sleep(15);
			}
			
			if (!utilities.arrayHasElement(this.ownEngines)) {
				initEngine();
			} else if (getMainEngine().isFinished()) {
				getMainEngine().onFinish();
				
				onFinish();
			} else if (getMainEngine().isStopped()) {
				getMainEngine().onStop();
				
				onStop();
			} else if (getMainEngine().isSleeping()) {
				this.realocateJobToEngines();
			}
		}
	}
	
	public OperationController getController() {
		return controller;
	}
	
	private void initEngine() throws DBException {
		logInfo("INITIALIZING ENGINE FOR ETL CONFIG [" + getEtlConfiguration().getConfigCode().toUpperCase() + "]");
		
		long minRecId = tableOperationProgressInfo.getProgressMeter().getMinRecordId();
		
		if (minRecId == 0) {
			logDebug("DETERMINING MIN RECORD FOR " + getSrcMainTableConf().getTableName());
			
			minRecId = getController().getMinRecordId(getEtlConfiguration());
			
			logDebug("FOUND MIN RECORD " + getEtlConfiguration() + " = " + minRecId);
			
			tableOperationProgressInfo.getProgressMeter().setMinRecordId(minRecId);
			
		} else {
			logDebug("USING SAVED MIN RECORD " + getEtlConfiguration() + " = " + minRecId);
		}
		
		long maxRecId = 0;
		
		if (minRecId != 0) {
			maxRecId = tableOperationProgressInfo.getProgressMeter().getMaxRecordId();
			
			if (maxRecId == 0) {
				logDebug("DETERMINING MAX RECORD FOR CONFIG" + getEtlConfiguration().getConfigCode());
				
				maxRecId = getController().getMaxRecordId(getEtlConfiguration());
				
				tableOperationProgressInfo.getProgressMeter().setMaxRecordId(maxRecId);
				
				logDebug("FOUND MAX RECORD " + getEtlConfiguration() + " = " + maxRecId);
			} else {
				logDebug("USING SAVED MAX RECORD " + getEtlConfiguration() + " = " + maxRecId);
			}
		} else {
			logDebug("MIN RECORD IS ZERO! SKIPING MAX RECORD VERIFICATION...");
		}
		
		if (maxRecId == 0 && minRecId == 0) {
			String msg = "NO RECORD TO PROCESS FOR ETL CONFIG '" + getSrcMainTableConf().getTableName().toUpperCase()
			        + "' NO ENGINE WILL BE CRIETED BY NOW!";
			
			if (mustRestartInTheEnd()) {
				msg += " GOING SLEEP....";
			} else {
				msg += " FINISHING....";
			}
			
			logWarn(msg);
		} else {
			calculateStatistics();
			
			int qtyEngines = getController().getOperationConfig().getMaxSupportedEngines();
			
			if (qtyEngines > getQtyRecordsPerProcessing()) {
				setQtyRecordsPerProcessing(qtyEngines);
			}
			
			ThreadRecordIntervalsManager t = ThreadRecordIntervalsManager.tryToLoadFromFile(getEngineId(), this);
			
			if (t == null) {
				this.getSearchParams().setLimits(new ThreadRecordIntervalsManager(minRecId, maxRecId,
				        this.getQtyRecordsPerProcessing(), getEngineId(), this));
			}
			
			long engineAlocatedRecs = this.getQtyRecordsPerProcessing() / qtyEngines;
			
			performeTask(qtyEngines, engineAlocatedRecs);
			
		}
	}
	
	/**
	 * @param qtyEngines
	 * @param engineAlocatedRecs
	 */
	public void performeTask(int qtyEngines, long engineAlocatedRecs) {
		ThreadRecordIntervalsManager initialLimits = null;
		
		List<Future<?>> futures = new ArrayList<>(qtyEngines);
		
		List<TaskProcessor> generatedEngines = new ArrayList<>(qtyEngines);
		
		for (int i = 0; i < qtyEngines; i++) {
			ThreadRecordIntervalsManager limits;
			
			if (initialLimits == null) {
				limits = new ThreadRecordIntervalsManager(getLimits().getCurrentFirstRecordId(),
				        getLimits().getCurrentFirstRecordId() + engineAlocatedRecs - 1, (int) engineAlocatedRecs);
				initialLimits = limits;
				
			} else {
				// Last processor
				if (i == qtyEngines - 1) {
					long min = initialLimits.getThreadMaxRecordId() + 1;
					long process = getLimits().getCurrentLastRecordId() - min + 1;
					
					limits = new ThreadRecordIntervalsManager(min, getLimits().getCurrentLastRecordId(), (int) process);
				} else {
					limits = new ThreadRecordIntervalsManager(initialLimits.getThreadMaxRecordId() + 1,
					        initialLimits.getThreadMaxRecordId() + engineAlocatedRecs, (int) engineAlocatedRecs);
				}
				initialLimits = limits;
			}
			
			TaskProcessor taskProcessor = getController().initRelatedEngine(this, limits);
			taskProcessor.setEngineId(this.getEngineId() + "_" + utilities.garantirXCaracterOnNumber(i++, 2));
			
			taskProcessor.resetLimits(limits);
			
			limits.setEngine(this);
			limits.setThreadCode(taskProcessor.getEngineId());
			
			ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(taskProcessor.getEngineId());
			
			futures.add(executor.submit(taskProcessor));
			generatedEngines.add(taskProcessor);
		}
		
		for (Future<?> future : futures) {
			try {
				future.get();
			}
			catch (Exception e) {}
		}
	}
	
	private void calculateStatistics() throws DBException {
		OpenConnection conn = getController().openConnection();
		
		try {
			logInfo("CALCULATING STATISTICS...");
			
			int remaining = getProgressMeter().getRemain();
			int total = getProgressMeter().getTotal();
			int processed = total - remaining;
			
			if (total == 0) {
				total = getSearchParams().countAllRecords(conn);
				remaining = getSearchParams().countNotProcessedRecords(conn);
				processed = total - remaining;
			}
			
			this.getProgressMeter().refresh(this.getProgressMeter().getStatusMsg(), total, processed);
			
			if (getRelatedOperationController().isResumable()) {
				this.getTableOperationProgressInfo().save(conn);
			}
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (DBException e) {
			getRelatedOperationController().requestStopDueError(this, e);
			
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void doFirstSaveAllLimits() {
		List<ThreadRecordIntervalsManager> newIntervals = new ArrayList<>();
		
		List<ThreadRecordIntervalsManager> oldLImitsManagers = ThreadRecordIntervalsManager
		        .getAllSavedLimitsOfOperation(this);
		
		newIntervals.add(getSearchParams().getLimits());
		
		if (oldLImitsManagers != null) {
			for (ThreadRecordIntervalsManager limits : oldLImitsManagers) {
				if (!newIntervals.contains(limits)) {
					limits.remove(this);
				}
			}
		}
	}
	
	public File getThreadsDir() {
		String subFolder = this.getRelatedOperationController().generateOperationStatusFolder();
		
		subFolder += FileUtilities.getPathSeparator() + "threads";
		
		return new File(subFolder);
	}
	
	private void tryToLoadExcludedRecordsLimits() {
		List<ThreadRecordIntervalsManager> limitsManagers = ThreadRecordIntervalsManager.getAllSavedLimitsOfOperation(this);
		
		if (utilities.arrayHasElement(limitsManagers)) {
			this.setExcludedRecordsLimits(new ArrayList<>());
			
			for (ThreadRecordIntervalsManager threadLimits : limitsManagers) {
				threadLimits.getCurrentLimits().setMinRecordId(threadLimits.getThreadMinRecordId());
				
				this.getExcludedRecordsIntervals().add(threadLimits.getCurrentLimits());
				
				if (threadLimits.hasExcludedIntervals()) {
					for (IntervalExtremeRecord l : threadLimits.getExcludedIntervals()) {
						if (!this.getExcludedRecordsIntervals().contains(l)) {
							this.getExcludedRecordsIntervals().add(l);
						}
					}
				}
			}
		}
	}
	
	private void addEngineToOwnEgines(TaskProcessor taskProcessor) {
		if (!this.ownEngines.contains(taskProcessor)) {
			this.ownEngines.add(taskProcessor);
		}
	}
	
	public void realocateJobToEngines() throws DBException {
		logDebug("REALOCATING ENGINES FOR '" + getEtlConfigCode().toUpperCase() + "'");
		
		killSelfCreatedThreads();
		
		initEngine();
	}
	
	public int getQtyRecordsPerProcessing() {
		return getController().getOperationConfig().getMaxRecordPerProcessing();
	}
	
	public void setQtyRecordsPerProcessing(int qtyRecordsPerProcessing) {
		getController().getOperationConfig().setMaxRecordPerProcessing(qtyRecordsPerProcessing);
	}
	
	private TaskProcessor retrieveAndRemoveSleepingEngine() {
		TaskProcessor sleepingEngine = null;
		
		for (TaskProcessor taskProcessor : this.ownEngines) {
			if (taskProcessor.isSleeping()) {
				sleepingEngine = taskProcessor;
			}
		}
		
		if (sleepingEngine != null) {
			this.ownEngines.remove(sleepingEngine);
		}
		
		return sleepingEngine;
	}
	
	private TaskProcessor retrieveAndRemoveMainSleepingEngine() {
		TaskProcessor sleepingEngine = null;
		
		for (TaskProcessor taskProcessor : this.ownEngines) {
			if (taskProcessor.isSleeping() && taskProcessor.getChildren() != null) {
				sleepingEngine = taskProcessor;
				
				break;
			}
		}
		
		if (sleepingEngine != null)
			this.ownEngines.remove(sleepingEngine);
		
		return sleepingEngine;
	}
	
	private boolean mustRestartInTheEnd() {
		return getController().mustRestartInTheEnd();
	}
	
	public void logErr(String msg) {
		getRelatedOperationController().logErr(msg);
	}
	
	public OperationController getRelatedOperationController() {
		return controller;
	}
	
	public void logInfo(String msg) {
		getRelatedOperationController().logInfo(msg);
	}
	
	public void logDebug(String msg) {
		getRelatedOperationController().logDebug(msg);
	}
	
	public void logWarn(String msg) {
		getRelatedOperationController().logWarn(msg);
	}
	
	public void logWarn(String msg, long interval) {
		getRelatedOperationController().logWarn(msg, interval);
	}
	
	String generateEngineNewJobRequestStatus() {
		String status = "";
		
		for (TaskProcessor taskProcessor : ownEngines) {
			status += "[" + taskProcessor.getEngineId() + " > " + (taskProcessor.isNewJobRequested() ? "REQUESTED" : "NOT REQUESTED")
			        + "] ";
		}
		
		return status;
	}
	
	@Override
	public String toString() {
		return this.engineMonitorId;
	}
	
	/**
	 * Schedule new job for this job. This is controller by {@link Engine}
	 * 
	 * @param syncEngine
	 */
	public void scheduleNewJobForEngine(TaskProcessor syncEngine) {
		syncEngine.setNewJobRequested(true);
		syncEngine.changeStatusToSleeping();
		logWarn(
		    "THE ENGINE '" + syncEngine.getEngineId() + "' HAS FINISHED ITS JOB AND NOW IS WATING FOR NEW ALOCATION WORK");
	}
	
	boolean isAllEnginesSleeping() {
		for (TaskProcessor taskProcessor : ownEngines) {
			if (!taskProcessor.isSleeping()) {
				return false;
			}
		}
		
		return true;
	}
	
	public void killSelfCreatedThreads() {
		for (TaskProcessor taskProcessor : this.ownEngines) {
			ThreadPoolService.getInstance().terminateTread(getController().getLogger(), taskProcessor.getEngineId(), taskProcessor);
		}
	}
	
	public static Engine init(OperationController controller, EtlItemConfiguration etlItemConfiguration,
	        TableOperationProgressInfo tableOperationProgressInfo) {
		Engine monitor = new Engine(controller, etlItemConfiguration, tableOperationProgressInfo);
		
		return monitor;
	}
	
	@Override
	public boolean isPaused() {
		return this.operationStatus == MonitoredOperation.STATUS_PAUSED;
	}
	
	@Override
	public boolean isSleeping() {
		return this.operationStatus == MonitoredOperation.STATUS_SLEEPING;
	}
	
	@Override
	public void changeStatusToSleeping() {
		this.operationStatus = MonitoredOperation.STATUS_SLEEPING;
	}
	
	@Override
	public void changeStatusToRunning() {
		this.operationStatus = MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public void changeStatusToStopped() {
		this.operationStatus = MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public void changeStatusToFinished() {
		this.operationStatus = MonitoredOperation.STATUS_FINISHED;
	}
	
	@Override
	public void changeStatusToPaused() {
		this.operationStatus = MonitoredOperation.STATUS_PAUSED;
	}
	
	@Override
	public void onStart() {
		this.operationStatus = MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public void onSleep() {
		this.operationStatus = MonitoredOperation.STATUS_SLEEPING;
	}
	
	@Override
	public void onStop() {
		getTimer().stop();
		
		this.operationStatus = MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public void onFinish() {
		getTimer().stop();
		
		this.operationStatus = MonitoredOperation.STATUS_FINISHED;
	}
	
	@Override
	public TimeController getTimer() {
		return getProgressMeter() != null ? getProgressMeter().getTimer() : null;
	}
	
	public TableOperationProgressInfo getTableOperationProgressInfo() {
		return tableOperationProgressInfo;
	}
	
	@Override
	public boolean stopRequested() {
		return this.stopRequested;
	}
	
	public boolean isInitialized() {
		return this.operationStatus != MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	@Override
	public boolean isNotInitialized() {
		return this.operationStatus == MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	@Override
	public boolean isRunning() {
		return this.operationStatus == MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public boolean isStopped() {
		if (isNotInitialized())
			return false;
		
		if (!utilities.arrayHasElement(this.ownEngines)) {
			return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
		} else
			for (TaskProcessor taskProcessor : this.ownEngines) {
				if (!taskProcessor.isStopped()) {
					return false;
				}
			}
		
		return true;
	}
	
	@Override
	public boolean isFinished() {
		if (isNotInitialized()) {
			return false;
		}
		
		if (!utilities.arrayHasElement(this.ownEngines)) {
			return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
		} else
			for (TaskProcessor taskProcessor : this.ownEngines) {
				if (!taskProcessor.isFinished()) {
					return false;
				}
			}
		
		return true;
	}
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return 5;
	}
	
	@Override
	public synchronized void requestStop() {
		if (isNotInitialized()) {
			changeStatusToStopped();
		} else if (!stopRequested() && !isFinished() && !isStopped()) {
			if (getMainEngine() != null)
				getMainEngine().requestStop();
			
			this.stopRequested = true;
		}
	}
	
	public void requestStopDueError() {
		if (getMainEngine() != null) {
			getMainEngine().requestStopDueError();
		} else
			this.operationStatus = MonitoredOperation.STATUS_STOPPED;
		
		this.stopRequested = true;
	}
	
	public synchronized void refreshProgressMeter(int newlyProcessedRecords, Connection conn) throws DBException {
		logDebug("REFRESHING PROGRESS METER FOR MORE " + newlyProcessedRecords + " RECORDS.");
		this.getProgressMeter().refresh("RUNNING", this.getProgressMeter().getTotal(),
		    this.getProgressMeter().getProcessed() + newlyProcessedRecords);
		
		if (getRelatedOperationController().isResumable()) {
			this.getTableOperationProgressInfo().save(conn);
		}
		
		logDebug("PROGRESS METER REFRESHED");
	}
	
	public void reportProgress() {
		EtlProgressMeter globalProgressMeter = this.getProgressMeter();
		
		String log = "";
		
		log += this.getEtlConfigCode().toUpperCase() + " PROGRESS: ";
		log += "[TOTAL RECS: " + utilities.generateCommaSeparetedNumber(globalProgressMeter.getTotal()) + ", ";
		log += "PROCESSED: " + globalProgressMeter.getDetailedProgress() + ", ";
		log += "REMAINING: " + globalProgressMeter.getDetailedRemaining() + ",";
		log += "TIME: " + globalProgressMeter.getHumanReadbleTime() + "]";
		
		this.logInfo(log);
	}
	
}
