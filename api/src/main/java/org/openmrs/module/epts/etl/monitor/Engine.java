package org.openmrs.module.epts.etl.monitor;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.EtlProgressMeter;
import org.openmrs.module.epts.etl.engine.MigrationFinalCheckStatus;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.EtlException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.TableOperationProgressInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.EtlThreadFactory;
import org.openmrs.module.epts.etl.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeController;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class monitor all {@link TaskProcessor}s of an {@link OperationController}
 * 
 * @author jpboane
 */
public class Engine<T extends EtlDatabaseObject> implements MonitoredOperation {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private OperationController<T> controller;
	
	private EtlItemConfiguration etlItemConfiguration;
	
	private String engineMonitorId;
	
	private String engineId;
	
	private int operationStatus;
	
	private boolean stopRequested;
	
	protected TableOperationProgressInfo tableOperationProgressInfo;
	
	protected List<IntervalExtremeRecord> excludedRecordsLimits;
	
	private AbstractEtlSearchParams<T> searchParams;
	
	private MigrationFinalCheckStatus finalCheckStatus;
	
	private List<TaskProcessor<T>> currentTaskProcessor;
	
	public Engine(OperationController<T> controller, EtlItemConfiguration etlItemConfiguration,
	    TableOperationProgressInfo tableOperationProgressInfo) {
		this.controller = controller;
		this.etlItemConfiguration = etlItemConfiguration;
		
		this.engineMonitorId = (controller.getControllerId() + "_" + this.getEtlConfigCode() + "_monitor").toLowerCase();
		this.engineId = (getController().getControllerId() + "_" + this.getEtlConfigCode()).toLowerCase();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		this.tableOperationProgressInfo = tableOperationProgressInfo;
		
		this.finalCheckStatus = MigrationFinalCheckStatus.NOT_INITIALIZED;
	}
	
	public List<TaskProcessor<T>> getCurrentTaskProcessor() {
		return currentTaskProcessor;
	}
	
	public MigrationFinalCheckStatus getFinalCheckStatus() {
		return finalCheckStatus;
	}
	
	public OpenConnection openSrcConn() throws DBException {
		return getController().openSrcConnection();
	}
	
	public OpenConnection tryToOpenDstConn() throws DBException {
		return getController().tryToOpenDstConn();
	}
	
	public List<OpenConnection> openSrcConn(int qtyConnections) throws DBException {
		List<OpenConnection> conns = new ArrayList<>(qtyConnections);
		
		for (int i = 0; i < qtyConnections; i++) {
			conns.add(this.openSrcConn());
		}
		
		return conns;
	}
	
	public List<OpenConnection> tryToOpenDstConn(int qtyConnections) throws DBException {
		List<OpenConnection> conns = new ArrayList<>(qtyConnections);
		
		for (int i = 0; i < qtyConnections; i++) {
			conns.add(this.tryToOpenDstConn());
		}
		
		return conns;
	}
	
	protected boolean mustDoFinalCheck() {
		if (getRelatedOperationController().getOperationConfig().skipFinalDataVerification()) {
			return false;
		} else {
			OpenConnection srcConn = null;
			OpenConnection dstConn = null;
			
			try {
				srcConn = this.openSrcConn();
				dstConn = this.tryToOpenDstConn();
				
				if (DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
					return utilities.stringHasValue(getSearchParams().generateDestinationExclusionClause(srcConn, dstConn));
				} else {
					return false;
				}
			}
			catch (DBException e) {
				throw new RuntimeException(e);
			}
			finally {
				if (srcConn != null)
					srcConn.finalizeConnection();
				if (dstConn != null)
					dstConn.finalizeConnection();
			}
		}
	}
	
	public AbstractEtlSearchParams<T> getSearchParams() {
		return searchParams;
	}
	
	public ThreadRecordIntervalsManager<T> getThreadRecordIntervalsManager() {
		return getSearchParams().getThreadRecordIntervalsManager();
	}
	
	public void setSearchParams(AbstractEtlSearchParams<T> searchParams) {
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
	
	public SrcConf getSrcConf() {
		return this.getEtlItemConfiguration().getSrcConf();
	}
	
	public String getEngineId() {
		return engineId;
	}
	
	public String getEngineMonitorId() {
		return engineMonitorId;
	}
	
	public EtlItemConfiguration getEtlItemConfiguration() {
		return this.etlItemConfiguration;
	}
	
	public OperationController<T> getController() {
		return controller;
	}
	
	public String getEtlConfigCode() {
		return this.getEtlItemConfiguration().getConfigCode();
	}
	
	public EtlProgressMeter getProgressMeter() {
		return this.tableOperationProgressInfo != null ? this.tableOperationProgressInfo.getProgressMeter() : null;
	}
	
	@Override
	public void run() {
		try {
			logInfo("INITIALIZING ENGINE FOR ETL CONFIG [" + getEtlItemConfiguration().getConfigCode().toUpperCase() + "]");
			
			long minRecId = tableOperationProgressInfo.getProgressMeter().getMinRecordId();
			
			if (minRecId == 0) {
				logDebug("DETERMINING MIN RECORD FOR " + getSrcConf().getTableName());
				
				minRecId = getController().getMinRecordId(this);
				
				logDebug("FOUND MIN RECORD " + getEtlItemConfiguration() + " = " + minRecId);
				
				tableOperationProgressInfo.getProgressMeter().setMinRecordId(minRecId);
				
			} else {
				logDebug("USING SAVED MIN RECORD " + getEtlItemConfiguration() + " = " + minRecId);
			}
			
			long maxRecId = 0;
			
			if (minRecId != 0) {
				maxRecId = tableOperationProgressInfo.getProgressMeter().getMaxRecordId();
				
				if (maxRecId == 0) {
					logDebug("DETERMINING MAX RECORD FOR CONFIG" + getEtlItemConfiguration().getConfigCode());
					
					maxRecId = getController().getMaxRecordId(this);
					
					tableOperationProgressInfo.getProgressMeter().setMaxRecordId(maxRecId);
					
					logDebug("FOUND MAX RECORD " + getEtlItemConfiguration() + " = " + maxRecId);
				} else {
					logDebug("USING SAVED MAX RECORD " + getEtlItemConfiguration() + " = " + maxRecId);
				}
			} else {
				logDebug("MIN RECORD IS ZERO! SKIPING MAX RECORD VERIFICATION...");
			}
			
			if (maxRecId == 0 && minRecId == 0) {
				String msg = "NO RECORD TO PROCESS FOR ETL CONFIG '" + getSrcConf().getTableName().toUpperCase()
				        + "' NO ENGINE WILL BE CRIETED BY NOW!";
				
				if (mustRestartInTheEnd()) {
					msg += " GOING SLEEP....";
				} else {
					msg += " FINISHING....";
					
					changeStatusToFinished();
					
					getRelatedOperationController().markTableOperationAsFinished(getEtlItemConfiguration());
				}
				
				logWarn(msg);
			} else {
				
				if (getController().getOperationConfig().getMaxSupportedEngines() > getMaxRecordsPerProcessing()) {
					setMaxRecordsPerProcessing(getController().getOperationConfig().getMaxSupportedEngines());
				}
				
				ThreadRecordIntervalsManager<T> t = ThreadRecordIntervalsManager.tryToLoadFromFile(getEngineId(), this);
				
				if (t == null) {
					t = new ThreadRecordIntervalsManager<>(this);
				}
				
				this.setSearchParams(controller.initMainSearchParams(t, this));
				this.getSearchParams().setRelatedEngine(this);
				
				changeStatusToRunning();
				
				calculateStatistics();
				
				doFirstSaveAllLimits();
				
				if (getMaxSupportedProcessors() > 1) {
					performeInMultiProcessors();
				} else {
					performeInSingleProcessor();
				}
				
				if (mustDoFinalCheck()) {
					perfomeFinalization();
				}
				
				changeStatusToFinished();
				
			}
		}
		catch (Exception e) {
			this.changeStatusToStopped();
			
			e.printStackTrace();
			
			getController().requestStopDueError(this, e);
		}
	}
	
	/**
	 * @throws DBException
	 */
	public void performeInSingleProcessor() throws DBException {
		
		while (getThreadRecordIntervalsManager().canGoNext()) {
			OpenConnection srcConn = openSrcConn();
			OpenConnection dstConn = tryToOpenDstConn();
			
			try {
				
				if (this.getThreadRecordIntervalsManager().getCurrentLimits().isFullProcessed()) {
					this.getThreadRecordIntervalsManager().moveNext();
				}
				
				List<T> records = getSearchParams().searchNextRecordsInMultiThreads(srcConn, dstConn);
				
				if (!utilities.arrayHasElement(records))
					return;
				
				TaskProcessor<T> taskProcessor = getController().initRelatedEngine(this,
				    getThreadRecordIntervalsManager().getCurrentLimits());
				taskProcessor.setEngineId(this.getEngineId());
				
				EtlOperationResultHeader<T> result = taskProcessor.performeSync(records, srcConn, dstConn);
				
				if (result.hasFatalError()) {
					result.throwDefaultExcetions();
				} else {
					getController().afterEtl(result.getRecordsWithNoError(), srcConn, dstConn);
					
					if (result.hasRecordsWithUnresolvedErrors() || result.hasRecordsWithResolvedErrors()) {
						logWarn("Some errors where found loading '" + result.getRecordsWithUnresolvedErrors().size()
						        + "! The errors will be documented");
						
						result.documentErrors(srcConn, dstConn);
					}
					
					getThreadRecordIntervalsManager().getCurrentLimits().markAsProcessed();
					
					refreshProgressMeter_(records.size(), srcConn);
					
					if (srcConn != null) {
						srcConn.markAsSuccessifullyTerminated();
					}
					if (dstConn != null) {
						dstConn.markAsSuccessifullyTerminated();
					}
				}
				
				getThreadRecordIntervalsManager().save();
			}
			finally {
				if (srcConn != null) {
					srcConn.finalizeConnection();
				}
				if (dstConn != null) {
					dstConn.finalizeConnection();
				}
			}
		}
		
		this.finalCheckStatus = MigrationFinalCheckStatus.DONE;
	}
	
	/**
	 * @throws DBException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void performeInMultiProcessors() throws DBException, InterruptedException, ExecutionException {
		while (this.getThreadRecordIntervalsManager().canGoNext()) {
			
			if (this.getThreadRecordIntervalsManager().getCurrentLimits().isFullProcessed()) {
				this.getThreadRecordIntervalsManager().moveNext();
			}
			
			OpenConnection srcConn = openSrcConn();
			OpenConnection dstConn = tryToOpenDstConn();
			
			try {
				List<EtlOperationResultHeader<T>> results = this.performeTask(srcConn, dstConn);
				
				if (EtlOperationResultHeader.hasAtLeastOneFatalError(results)) {
					logInfo("Some errors where encountered on current processing, the process will be aborted");
					
					for (EtlOperationResultHeader<T> result : results) {
						if (result.hasFatalError()) {
							logInfo("Encountered erros on intervals: " + result.getInterval());
							
							result.printStackErrorOfFatalErrors();
						}
					}
					
					EtlOperationResultHeader<T> r = EtlOperationResultHeader.getDefaultResultWithFatalError(results);
					
					r.throwDefaultExcetions();
					
				} else {
					getThreadRecordIntervalsManager().getCurrentLimits().markAsProcessed();
					
					if (srcConn != null) {
						srcConn.markAsSuccessifullyTerminated();
					}
					if (dstConn != null) {
						dstConn.markAsSuccessifullyTerminated();
					}
				}
				
				getThreadRecordIntervalsManager().save();
			}
			finally {
				if (srcConn != null) {
					srcConn.finalizeConnection();
				}
				if (dstConn != null) {
					dstConn.finalizeConnection();
				}
			}
		}
	}
	
	/**
	 * @throws DBException
	 */
	public void perfomeFinalization() throws DBException {
		this.finalCheckStatus = MigrationFinalCheckStatus.ONGOING;
		
		if (getThreadRecordIntervalsManager().getFinalCheckIntervalsManager() == null) {
			getThreadRecordIntervalsManager().initializeFinalCheckIntervalManager();
		}
		
		getSearchParams().setThreadRecordIntervalsManager(getThreadRecordIntervalsManager().getFinalCheckIntervalsManager());
		
		performeInSingleProcessor();
		
		this.finalCheckStatus = MigrationFinalCheckStatus.DONE;
	}
	
	/**
	 * @param qtyProcessors
	 * @param engineAlocatedRecs
	 * @throws DBException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public List<EtlOperationResultHeader<T>> performeTask(Connection srcConn, Connection dstConn)
	        throws DBException, InterruptedException, ExecutionException {
		List<IntervalExtremeRecord> avaliableIntervals = getThreadRecordIntervalsManager().getCurrentLimits()
		        .getAllNotProcessed();
		
		this.currentTaskProcessor = new ArrayList<>(avaliableIntervals.size());
		
		logDebug("Initializing " + avaliableIntervals.size() + " processors to performe task on a interval "
		        + getThreadRecordIntervalsManager().getCurrentLimits() + "!");
		
		List<CompletableFuture<EtlOperationResultHeader<T>>> tasks = new ArrayList<>(avaliableIntervals.size());
		
		EtlThreadFactory<T> threadFactor = new EtlThreadFactory<>(this);
		
		ExecutorService executorService = Executors.newFixedThreadPool(avaliableIntervals.size(), threadFactor);
		
		try {
			
			for (int i = 0; i < avaliableIntervals.size(); i++) {
				IntervalExtremeRecord interval = avaliableIntervals.get(i);
				
				TaskProcessor<T> taskProcessor = getController().initRelatedEngine(this, interval);
				taskProcessor.setEngineId(this.getEngineId() + "_" + utilities.garantirXCaracterOnNumber(i, 2));
				
				logDebug("Processor initialized for records between interval: [" + interval + "]");
				
				getCurrentTaskProcessor().add(taskProcessor);
				
				tasks.add(CompletableFuture.supplyAsync(() -> {
					
					try {
						EtlOperationResultHeader<T> result = taskProcessor.performe(srcConn, dstConn);
						
						if (!result.hasFatalError()) {
							getController().afterEtl(result.getRecordsWithNoError(), srcConn, dstConn);
							
							if (result.hasRecordsWithUnresolvedErrors() || result.hasRecordsWithResolvedErrors()) {
								logWarn("Some errors where found loading '" + result.getRecordsWithUnresolvedErrors().size()
								        + "! The errors will be documented");
								
								result.documentErrors(srcConn, dstConn);
							}
							
							refreshProgressMeter_(result.countAllSuccessfulyProcessedRecords(), srcConn);
							
						}
						
						return result;
					}
					catch (DBException e) {
						throw new EtlException(e);
					}
					
				}, executorService));
				
			}
			
			CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
			
			//Wait until all tasks are finished
			allOf.get();
			
			return tasks.stream().map(CompletableFuture::join).collect(Collectors.toList());
		}
		finally {
			// Shutdown the executorService service
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
					executorService.shutdownNow();
				}
			}
			catch (InterruptedException e) {
				executorService.shutdownNow();
			}
		}
	}
	
	private void calculateStatistics() throws DBException {
		OpenConnection conn = getController().openSrcConnection();
		
		try {
			logInfo("CALCULATING STATISTICS...");
			
			int remaining = getProgressMeter().getRemain();
			int total = getProgressMeter().getTotal();
			int processed = total - remaining;
			
			if (total == 0) {
				logWarn("No recorded statistic. Loading from Database");
				
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
		
		logDebug("DONE CALCULATING STATISTICS.");
		
	}
	
	private void doFirstSaveAllLimits() {
		List<ThreadRecordIntervalsManager<T>> newIntervals = new ArrayList<>();
		
		List<ThreadRecordIntervalsManager<T>> oldLImitsManagers = ThreadRecordIntervalsManager
		        .getAllSavedLimitsOfOperation(this);
		
		newIntervals.add(getSearchParams().getThreadRecordIntervalsManager());
		
		if (oldLImitsManagers != null) {
			for (ThreadRecordIntervalsManager<T> limits : oldLImitsManagers) {
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
	
	@SuppressWarnings("unused")
	private void tryToLoadExcludedRecordsLimits() {
		List<ThreadRecordIntervalsManager<T>> limitsManagers = ThreadRecordIntervalsManager
		        .getAllSavedLimitsOfOperation(this);
		
		if (utilities.arrayHasElement(limitsManagers)) {
			this.setExcludedRecordsLimits(new ArrayList<>());
			
			for (ThreadRecordIntervalsManager<T> threadLimits : limitsManagers) {
				threadLimits.getCurrentLimits().setMinRecordId(threadLimits.getMinRecordId());
				
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
	
	public int getMaxRecordsPerProcessing() {
		return getController().getOperationConfig().getMaxRecordPerProcessing();
	}
	
	public int getMaxSupportedProcessors() {
		return getController().getOperationConfig().getMaxSupportedEngines();
	}
	
	public void setMaxSupportedProcessors(int maxSupportedProcessors) {
		getController().getOperationConfig().setMaxSupportedEngines(maxSupportedProcessors);
	}
	
	public void setMaxRecordsPerProcessing(int maxRecordsPerProcessing) {
		getController().getOperationConfig().setMaxRecordPerProcessing(maxRecordsPerProcessing);
	}
	
	private boolean mustRestartInTheEnd() {
		return getController().mustRestartInTheEnd();
	}
	
	public void logErr(String msg) {
		getRelatedOperationController().logErr(msg);
	}
	
	public OperationController<T> getRelatedOperationController() {
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
	
	@Override
	public String toString() {
		return this.engineMonitorId;
	}
	
	public static <T extends EtlDatabaseObject> Engine<T> init(OperationController<T> controller,
	        EtlItemConfiguration etlItemConfiguration, TableOperationProgressInfo tableOperationProgressInfo) {
		Engine<T> monitor = new Engine<>(controller, etlItemConfiguration, tableOperationProgressInfo);
		
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
		
		getProgressMeter().changeStatusToRunning();
	}
	
	@Override
	public void changeStatusToStopped() {
		this.operationStatus = MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public void changeStatusToFinished() {
		this.operationStatus = MonitoredOperation.STATUS_FINISHED;
		
		getProgressMeter().changeStatusToFinished();
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
		
		return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		if (isNotInitialized()) {
			return false;
		}
		
		return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
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
			this.stopRequested = true;
		}
	}
	
	public void requestStopDueError() {
		this.operationStatus = MonitoredOperation.STATUS_STOPPED;
		
		this.stopRequested = true;
	}
	
	public synchronized void refreshProgressMeter_(int newlyProcessedRecords, Connection conn) throws DBException {
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
