package org.openmrs.module.epts.etl.monitor;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.EtlProgressMeter;
import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
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
 * This class monitor all {@link Engine}s of an {@link OperationController}
 * 
 * @author jpboane
 */
public class EngineMonitor implements MonitoredOperation {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private OperationController controller;
	
	private EtlItemConfiguration etlItemConfiguration;
	
	private List<Engine> ownEngines;
	
	private String engineMonitorId;
	
	private String engineId;
	
	private int operationStatus;
	
	private boolean stopRequested;
	
	protected TableOperationProgressInfo tableOperationProgressInfo;
	
	protected List<IntervalExtremeRecord> excludedRecordsLimits;
	
	public EngineMonitor(OperationController controller, EtlItemConfiguration etlItemConfiguration,
	    TableOperationProgressInfo tableOperationProgressInfo) {
		this.controller = controller;
		this.ownEngines = new ArrayList<Engine>();
		this.etlItemConfiguration = etlItemConfiguration;
		
		this.engineMonitorId = (controller.getControllerId() + "_" + this.getEtlConfigCode() + "_monitor").toLowerCase();
		this.engineId = (getController().getControllerId() + "_" + this.getEtlConfigCode()).toLowerCase();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		this.tableOperationProgressInfo = tableOperationProgressInfo;
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
	
	public List<Engine> getOwnEngines() {
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
	
	public Engine getMainEngine() {
		for (Engine engine : this.ownEngines) {
			if (engine.getChildren() != null) {
				return engine;
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
		
		tryToLoadExcludedRecordsLimits();
		
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
			long qtyRecords = maxRecId - minRecId + 1;
			long qtyEngines = determineQtyEngines(qtyRecords);
			long qtyRecordsPerEngine = 0;
			
			if (qtyEngines == 0) {
				qtyEngines = 1;
			}
			
			if (qtyEngines > 1 && !controller.canBeRunInMultipleEngines()) {
				throw new ForbiddenOperationException("The " + controller.getOperationType()
				        + " Cannot be run in multiple engines! Please manual set the engines to '1'");
			}
			
			logInfo("STARTING PROCESS FOR TABLE CONFIG [" + getEtlConfiguration().getConfigCode().toUpperCase() + "] WITH "
			        + qtyEngines + " ENGINES [MIN REC: " + minRecId + ", MAX REC: " + maxRecId + "]");
			
			qtyRecordsPerEngine = determineQtyRecordsPerEngine(qtyEngines, qtyRecords);
			
			long currMax = minRecId + qtyRecordsPerEngine - 1;
			
			if (qtyEngines == 1)
				currMax = maxRecId;
			
			ThreadRecordIntervalsManager limits = this.getController().generateLimits(minRecId, currMax, null);
			
			Engine mainEngine = retrieveAndRemoveMainSleepingEngine();
			
			//If there was no main engine, retrieve onother engine and make it main
			mainEngine = mainEngine == null ? retrieveAndRemoveSleepingEngine() : mainEngine;
			
			mainEngine = mainEngine == null ? controller.initRelatedEngine(this, limits) : mainEngine;
			mainEngine.setEngineId(this.getEngineId() + "_" + utilities.garantirXCaracterOnNumber(0, 2));
			
			mainEngine.resetLimits(mainEngine.getLimits());
			
			logDebug("ALLOCATED RECORDS [" + mainEngine.getSearchParams().getLimits() + "] FOR ENGINE ["
			        + mainEngine.getEngineId() + "]");
			
			if (mainEngine.getChildren() == null) {
				mainEngine.setChildren(new ArrayList<Engine>());
			}
			
			int i = 1;
			
			for (i = 1; i < qtyEngines; i++) {
				limits = getController().generateLimits(limits.getThreadMaxRecordId() + 1,
				    limits.getThreadMaxRecordId() + qtyRecordsPerEngine, null);
				
				if (i == qtyEngines - 1) {
					limits.getMaxLimits().setMaxRecordId(maxRecId);
					limits.reset();
				}
				
				Engine engine = retrieveAndRemoveSleepingEngine();
				
				if (engine == null) {
					engine = getController().initRelatedEngine(this, limits);
					engine.resetLimits(engine.getLimits());
					mainEngine.getChildren().add(engine);
					engine.setEngineId(getEngineId() + "_" + utilities.garantirXCaracterOnNumber(i, 2));
					engine.setParent(mainEngine);
				} else {
					engine.resetLimits(limits);
				}
				
				addEngineToOwnEgines(engine);
				
				logDebug("REALOCATED NEW RECORDS [" + engine.getSearchParams().getLimits() + "] FOR ENGINE ["
				        + engine.getEngineId() + "]");
			}
			
			addEngineToOwnEgines(mainEngine);
			
			if (!this.getProgressMeter().isRunning()) {
				this.getProgressMeter().changeStatusToRunning();
			}
			
			OpenConnection conn = controller.openConnection();
			
			try {
				logInfo("CALCULATING STATISTICS...");
				
				int remaining = getProgressMeter().getRemain();
				int total = getProgressMeter().getTotal();
				int processed = total - remaining;
				
				if (total == 0) {
					total = mainEngine.getSearchParams().countAllRecords(conn);
					remaining = mainEngine.getSearchParams().countNotProcessedRecords(conn);
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
			
			doFirstSaveAllLimits();
			
			ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(mainEngine.getEngineId());
			executor.execute(mainEngine);
			
			if (mainEngine.getChildren() != null) {
				for (Engine childEngine : mainEngine.getChildren()) {
					executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(childEngine.getEngineId());
					executor.execute(childEngine);
				}
			}
			
			logInfo("ENGINE FOR TABLE CONFIG [" + getEtlConfiguration().getConfigCode().toUpperCase() + "] INITIALIZED!");
		}
	}
	
	private void doFirstSaveAllLimits() {
		List<ThreadRecordIntervalsManager> newIntervals = new ArrayList<>();
		
		List<ThreadRecordIntervalsManager> oldLImitsManagers = ThreadRecordIntervalsManager
		        .getAllSavedLimitsOfOperation(this);
		
		for (Engine engine : this.getOwnEngines()) {
			engine.getLimits().setEngine(engine);
			engine.getLimits().save(this);
			
			newIntervals.add(engine.getLimits());
		}
		
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
	
	private void addEngineToOwnEgines(Engine engine) {
		if (!this.ownEngines.contains(engine)) {
			this.ownEngines.add(engine);
		}
	}
	
	private long determineQtyRecordsPerEngine(long qtyEngines, long qtyRecords) {
		return qtyRecords / qtyEngines;
	}
	
	private long determineQtyEngines(long qtyRecords) {
		long qtyRecordsPerEngine = qtyRecords / getController().getOperationConfig().getMaxSupportedEngines();
		
		if (qtyRecordsPerEngine > getController().getOperationConfig().getMinRecordsPerEngine()) {
			return getController().getOperationConfig().getMaxSupportedEngines();
		}
		
		return qtyRecords / getController().getOperationConfig().getMinRecordsPerEngine();
	}
	
	public void realocateJobToEngines() throws DBException {
		logDebug("REALOCATING ENGINES FOR '" + getEtlConfigCode().toUpperCase() + "'");
		
		killSelfCreatedThreads();
		
		initEngine();
	}
	
	public int getQtyRecordsPerProcessing() {
		return getController().getOperationConfig().getMaxRecordPerProcessing();
	}
	
	private Engine retrieveAndRemoveSleepingEngine() {
		Engine sleepingEngine = null;
		
		for (Engine engine : this.ownEngines) {
			if (engine.isSleeping()) {
				sleepingEngine = engine;
			}
		}
		
		if (sleepingEngine != null) {
			this.ownEngines.remove(sleepingEngine);
		}
		
		return sleepingEngine;
	}
	
	private Engine retrieveAndRemoveMainSleepingEngine() {
		Engine sleepingEngine = null;
		
		for (Engine engine : this.ownEngines) {
			if (engine.isSleeping() && engine.getChildren() != null) {
				sleepingEngine = engine;
				
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
		
		for (Engine engine : ownEngines) {
			status += "[" + engine.getEngineId() + " > " + (engine.isNewJobRequested() ? "REQUESTED" : "NOT REQUESTED")
			        + "] ";
		}
		
		return status;
	}
	
	@Override
	public String toString() {
		return this.engineMonitorId;
	}
	
	/**
	 * Schedule new job for this job. This is controller by {@link EngineMonitor}
	 * 
	 * @param syncEngine
	 */
	public void scheduleNewJobForEngine(Engine syncEngine) {
		syncEngine.setNewJobRequested(true);
		syncEngine.changeStatusToSleeping();
		logWarn(
		    "THE ENGINE '" + syncEngine.getEngineId() + "' HAS FINISHED ITS JOB AND NOW IS WATING FOR NEW ALOCATION WORK");
	}
	
	boolean isAllEnginesSleeping() {
		for (Engine engine : ownEngines) {
			if (!engine.isSleeping()) {
				return false;
			}
		}
		
		return true;
	}
	
	public void killSelfCreatedThreads() {
		for (Engine engine : this.ownEngines) {
			ThreadPoolService.getInstance().terminateTread(getController().getLogger(), engine.getEngineId(), engine);
		}
	}
	
	public static EngineMonitor init(OperationController controller, EtlItemConfiguration etlItemConfiguration,
	        TableOperationProgressInfo tableOperationProgressInfo) {
		EngineMonitor monitor = new EngineMonitor(controller, etlItemConfiguration, tableOperationProgressInfo);
		
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
			for (Engine engine : this.ownEngines) {
				if (!engine.isStopped()) {
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
			for (Engine engine : this.ownEngines) {
				if (!engine.isFinished()) {
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
