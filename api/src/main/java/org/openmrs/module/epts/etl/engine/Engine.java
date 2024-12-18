package org.openmrs.module.epts.etl.engine;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.types.EtlDstType;
import org.openmrs.module.epts.etl.conf.types.ThreadingMode;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.TableOperationProgressInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.model.pojo.generic.RecordWithDefaultParentInfo;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.EtlThreadFactory;
import org.openmrs.module.epts.etl.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeController;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
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
	
	private String engineId;
	
	private int operationStatus;
	
	private boolean stopRequested;
	
	protected TableOperationProgressInfo tableOperationProgressInfo;
	
	protected List<IntervalExtremeRecord> excludedRecordsLimits;
	
	private AbstractEtlSearchParams<T> searchParams;
	
	private MigrationFinalCheckStatus finalCheckStatus;
	
	private List<TaskProcessor<T>> currentTaskProcessor;
	
	private Map<String, List<EtlDatabaseObject>> recordsToDisplay;
	
	public Engine(OperationController<T> controller, EtlItemConfiguration etlItemConfiguration,
	    TableOperationProgressInfo tableOperationProgressInfo) {
		this.controller = controller;
		this.etlItemConfiguration = etlItemConfiguration;
		
		this.engineId = (getController().getControllerId() + "_" + this.getEtlConfigCode()).toLowerCase();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		this.tableOperationProgressInfo = tableOperationProgressInfo;
		
		this.finalCheckStatus = MigrationFinalCheckStatus.NOT_INITIALIZED;
	}
	
	public Map<String, List<EtlDatabaseObject>> getRecordsToDisplay() {
		return recordsToDisplay;
	}
	
	public void setRecordsToDisplay(Map<String, List<EtlDatabaseObject>> recordsToDisplay) {
		this.recordsToDisplay = recordsToDisplay;
	}
	
	public EtlDstType getGlobalDstType() {
		return getRelatedEtlOperationConfig().getDstType();
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
	
	public boolean isDbDst() {
		return getRelatedEtlOperationConfig().isDbDst();
	}
	
	public boolean isJsonDst() {
		return getRelatedEtlOperationConfig().isJsonDst();
	}
	
	public boolean isDumpDst() {
		return getRelatedEtlOperationConfig().isDumpDst();
	}
	
	public boolean isCsvDst() {
		return getRelatedEtlOperationConfig().isCsvDst();
	}
	
	public boolean isFileDst() {
		return getRelatedEtlOperationConfig().isFileDst();
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
				
				if (dstConn != null && DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
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
	
	public EtlConfiguration getEtlConfiguration() {
		return this.getEtlItemConfiguration().getRelatedEtlConf();
	}
	
	public DBConnectionInfo getDstConnInfo() {
		return this.getRelatedOperationController().getDstConnInfo();
	}
	
	public DBConnectionInfo getSrcConnInfo() {
		return this.getRelatedOperationController().getSrcConnInfo();
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
					logDebug("DETERMINING MAX RECORD FOR CONFIG '" + getEtlItemConfiguration().getConfigCode() + "'");
					
					maxRecId = getController().getMaxRecordId(this);
					
					tableOperationProgressInfo.getProgressMeter().setMaxRecordId(maxRecId);
					
					logDebug("FOUND MAX RECORD " + getEtlItemConfiguration() + " = " + maxRecId);
				} else {
					logDebug("USING SAVED MAX RECORD " + getEtlItemConfiguration() + " = " + maxRecId);
				}
			} else {
				logWarn("MIN RECORD IS ZERO! SKIPING MAX RECORD VERIFICATION...");
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
				
				if (getController().getOperationConfig().getMaxSupportedProcessors() > getMaxRecordsPerProcessing()) {
					setMaxRecordsPerProcessing(getController().getOperationConfig().getMaxSupportedProcessors());
				}
				
				ThreadRecordIntervalsManager<T> t = null;
				
				if (getRelatedOperationController().isResumable()) {
					t = ThreadRecordIntervalsManager.tryToLoadFromFile(getEngineId(), this);
				}
				
				if (getEtlConfiguration().hasTestingItem()) {
					this.getRelatedEtlOperationConfig()
					        .setProcessingBatch((int) tableOperationProgressInfo.getProgressMeter().getMaxRecordId());
				}
				
				if (t == null) {
					t = new ThreadRecordIntervalsManager<>(this);
				}
				
				this.setSearchParams(controller.initMainSearchParams(t, this));
				this.getSearchParams().setRelatedEngine(this);
				
				changeStatusToRunning();
				
				OpenConnection conn = null;
				
				try {
					conn = openSrcConn();
					
					if (getRelatedOperationController().isResumable()) {
						this.getTableOperationProgressInfo().save(conn);
						
						conn.markAsSuccessifullyTerminated();
					}
				}
				finally {
					if (conn != null) {
						conn.finalizeConnection();
					}
				}
				
				calculateStatistics();
				
				doFirstSaveAllLimits();
				
				logDebug("CREATING DEFAULT PARENT OBJECTS");
				
				getEtlItemConfiguration().tryToCreateDefaultRecordsForAllTables();
				
				logTrace("DEFAULT PARENT OBJECTS CREATED");
				
				ThreadingMode threadingMode = this.getRelatedEtlOperationConfig().getThreadingMode();
				
				if (this.getEtlItemConfiguration().hasThreadingMode()) {
					threadingMode = this.getEtlItemConfiguration().getThreadingMode();
				}
				
				if (threadingMode.isMultiThread() && this.getMaxSupportedProcessors() > 1) {
					performeTaskInMultiProcessors();
				} else {
					this.performeTaskInSingleProcessor();
				}
				
				if (mustDoFinalCheck()) {
					perfomeFinalization();
				}
				
				if (this.getRecordsToDisplay() != null) {
					for (DstConf dstConf : this.getEtlItemConfiguration().getDstConf()) {
						if (dstConf.getDstType().isConsole()) {
							displayResultInConsole(this.getRecordsToDisplay().get(dstConf.getTableName()));
						} else if (dstConf.getDstType().isPopUp()) {
							displayResultInPopUp(this.getRecordsToDisplay().get(dstConf.getTableName()));
						} else
							throw new ForbiddenOperationException("Unsupported display method " + dstConf.getDstType());
					}
				}
				
				changeStatusToFinished();
				
				if (getRelatedOperationController().isResumable()) {
					getRelatedOperationController().markTableOperationAsFinished(this.getEtlItemConfiguration());
				}
				
				getRelatedOperationController().finalize(this);
			}
		}
		catch (Exception e) {
			this.changeStatusToStopped();
			
			e.printStackTrace();
			
			logErr(e.getLocalizedMessage());
			logErr(e.getMessage());
			
			getController().requestStopDueError(this, e);
		}
	}
	
	private void displayResultInPopUp(List<EtlDatabaseObject> list) {
		throw new ForbiddenOperationException("Currently popup not supported!");
	}
	
	private void displayResultInConsole(List<EtlDatabaseObject> objs) {
		String header = utilities.generateTabDelimitedHeader(objs.get(0));
		String separator = utilities.maskToken(header, header, '#');
		System.out.println(separator);
		System.out.println(header);
		System.out.println(utilities.parseToTabDelimitedWithoutHeader(objs));
		System.out.println(separator);
	}
	
	/**
	 * @throws DBException
	 */
	public void performeTaskInSingleProcessor() throws DBException {
		ThreadRecordIntervalsManager<T> iManager = getThreadRecordIntervalsManager();
		
		while (iManager.canGoNext() || !iManager.getCurrentLimits().isFullProcessed()) {
			if (iManager.getCurrentLimits().isFullProcessed()) {
				iManager.moveNext();
			}
			
			EtlProgressMeter globalProgressMeter = this.getProgressMeter();
			
			if (globalProgressMeter.getRemain() == 0) {
				if (getRelatedEtlOperationConfig().finishOnNoRemainRecordsToProcess()) {
					logInfo("Finishing operation as there is no more record to process");
					
					return;
				} else {
					logDebug(
					    "No remain records to process but still checking... consider setting finishOnNoRemainRecordsToProcess to true");
				}
			}
			
			for (IntervalExtremeRecord i : iManager.getCurrentLimits().getAllNotProcessed()) {
				TaskProcessor<T> taskProcessor = getController().initRelatedTaskProcessor(this, i, false);
				taskProcessor.setProcessorId(this.getEngineId());
				
				boolean persistTheWork = this.getEtlConfiguration().hasTestingItem() ? false : true;
				boolean useMultiThreadSearch = true;
				
				performeTask(taskProcessor, useMultiThreadSearch, persistTheWork, openSrcConn(), tryToOpenDstConn());
				
				if (taskProcessor.getTaskResultInfo().hasFatalError()) {
					taskProcessor.getTaskResultInfo().throwDefaultExcetions();
				}
			}
			
			tryToProcessSkippedrecords();
		}
	}
	
	public void tryToProcessSkippedrecords() throws DBException, RuntimeException {
		ThreadRecordIntervalsManager<T> iManager = this.getThreadRecordIntervalsManager();
		
		logDebug("TRY TO PROCESS SKIPPED RECORDS ON INTERVAL " + iManager.getCurrentLimits());
		
		String originalExtraCondition = getSearchParams().getExtraCondition();
		String originalExtraConditionForExtract = getSrcConf().getExtraConditionForExtract();
		
		getSrcConf().setExtraConditionForExtract(null);
		
		getSearchParams().setExtraCondition(getSrcConf().generateSkippedRecordInclusionClause());
		
		TaskProcessor<T> taskProcessor = getController()
		        .initRelatedTaskProcessor(this, getThreadRecordIntervalsManager().getCurrentLimits(), false)
		        .initReloadRecordsWithDefaultParentsTaskProcessor(iManager);
		
		taskProcessor.setProcessorId(this.getEngineId());
		
		boolean persistTheWork = this.getEtlConfiguration().hasTestingItem() ? false : true;
		boolean useMultiThreadSearch = true;
		
		performeTask(taskProcessor, useMultiThreadSearch, persistTheWork, openSrcConn(), tryToOpenDstConn());
		
		if (taskProcessor.getTaskResultInfo().hasFatalError()) {
			taskProcessor.getTaskResultInfo().throwDefaultExcetions();
		} else {
			
			OpenConnection srcConn = openSrcConn();
			
			try {
				RecordWithDefaultParentInfo.deleteAllSuccessifulyProcessed(getSrcConf(), srcConn);
				
				srcConn.markAsSuccessifullyTerminated();
			}
			finally {
				srcConn.finalizeConnection();
			}
			
			iManager.getCurrentLimits().markSkippedRecordsAsProcessed();
			iManager.save();
			
			getSrcConf().setExtraConditionForExtract(originalExtraConditionForExtract);
			getSearchParams().setExtraCondition(originalExtraCondition);
		}
	}
	
	/**
	 * @throws DBException
	 */
	public void perfomeFinalization() throws DBException {
		this.finalCheckStatus = MigrationFinalCheckStatus.ONGOING;
		
		logDebug("INITIALIZING FINAL CHECK...");
		
		if (getThreadRecordIntervalsManager().getFinalCheckIntervalsManager() == null) {
			getThreadRecordIntervalsManager().initializeFinalCheckIntervalManager();
		}
		
		getSearchParams().setThreadRecordIntervalsManager(getThreadRecordIntervalsManager().getFinalCheckIntervalsManager());
		
		performeTaskInSingleProcessor();
		
		logDebug("FINAL CHECK FINISHED!");
		
		this.finalCheckStatus = MigrationFinalCheckStatus.DONE;
	}
	
	/**
	 * @param qtyProcessors
	 * @param engineAlocatedRecs
	 * @throws DBException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void performeTaskInMultiProcessors() throws DBException, InterruptedException, ExecutionException {
		
		EtlProgressMeter globalProgressMeter = this.getProgressMeter();
		
		logDebug("INITIALIZING PROCESS IN MULTI-THREAD");
		
		EtlThreadFactory<T> threadFactor = new EtlThreadFactory<>(this);
		boolean useSharedConnection = getRelatedOperationController().getOperationConfig().isUseSharedConnectionPerThread();
		
		ThreadRecordIntervalsManager<T> iManager = this.getThreadRecordIntervalsManager();
		
		while (iManager.canGoNext() || !iManager.getCurrentLimits().isFullProcessed()) {
			
			if (globalProgressMeter.getRemain() == 0) {
				if (getRelatedEtlOperationConfig().finishOnNoRemainRecordsToProcess()) {
					logInfo("Finishing operation as there is no more record to process");
					
					return;
				} else {
					logDebug(
					    "No remain records to process but still checking... consider setting finishOnNoRemainRecordsToProcess to true");
				}
			}
			
			List<IntervalExtremeRecord> avaliableIntervals = iManager.getCurrentLimits().getAllNotProcessed();
			
			if (iManager.getCurrentLimits().isFullProcessed()) {
				logTrace("Moving to next interval from " + iManager.getCurrentLimits());
				
				iManager.moveNext();
				
				avaliableIntervals = iManager.getCurrentLimits().getAllNotProcessed();
				
				logTrace("Moved to next interval " + iManager.getCurrentLimits());
				
			} else {
				if (avaliableIntervals.size() > 0) {
					logDebug("The current interval was not full processed! Still missing " + avaliableIntervals.size()
					        + " to be processed");
				}
			}
			
			if (avaliableIntervals.size() == 0) {
				tryToProcessSkippedrecords();
			} else {
				
				logDebug("Initializing " + avaliableIntervals.size() + " processors to performe task on a interval "
				        + iManager.getCurrentLimits() + "!".toUpperCase());
				
				resetCurrentTaskProcessor(avaliableIntervals.size());
				
				List<CompletableFuture<Void>> tasks = new ArrayList<>(avaliableIntervals.size());
				
				ExecutorService executorService = Executors.newFixedThreadPool(avaliableIntervals.size(), threadFactor);
				
				final OpenConnection sharedSrcConn = openSrcConn();
				final OpenConnection sharedDstConn = tryToOpenDstConn();
				
				try {
					for (int i = 0; i < avaliableIntervals.size(); i++) {
						TaskProcessor<T> taskProcessor = getController().initRelatedTaskProcessor(this,
						    avaliableIntervals.get(i), true);
						
						taskProcessor.setProcessorId(this.getEngineId() + "_" + utilities.garantirXCaracterOnNumber(i, 2));
						
						logDebug("Processor initialized for records between interval: [" + taskProcessor.getLimits()
						        + "]".toUpperCase());
						
						getCurrentTaskProcessor().add(taskProcessor);
						
						tasks.add(CompletableFuture.runAsync(() -> {
							boolean useMultiThreadSearch = false;
							
							if (useSharedConnection) {
								boolean persistTheWork = false;
								
								performeTask(taskProcessor, useMultiThreadSearch, persistTheWork, sharedSrcConn,
								    sharedDstConn);
							} else {
								try {
									boolean persistTheWork = this.getEtlConfiguration().hasTestingItem() ? false : true;
									
									performeTask(taskProcessor, useMultiThreadSearch, persistTheWork, openSrcConn(),
									    tryToOpenDstConn());
								}
								catch (DBException e) {
									taskProcessor.getTaskResultInfo().setFatalException(e);
								}
							}
							
						}, executorService));
						
					}
					
					CompletableFuture<Void> allOf = CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
					
					//Wait until all tasks are finished
					allOf.get();
					
					List<EtlOperationResultHeader<T>> results = new ArrayList<>(getCurrentTaskProcessor().size());
					
					for (TaskProcessor<T> processor : getCurrentTaskProcessor()) {
						results.add(processor.getTaskResultInfo());
					}
					
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
						
						if (useSharedConnection && !this.getEtlConfiguration().hasTestingItem()) {
							OpenConnection.markAllAsSuccessifullyTerminected(sharedSrcConn, sharedDstConn);
							
							getThreadRecordIntervalsManager().getCurrentLimits().markAsProcessed();
							getThreadRecordIntervalsManager().save();
						}
						
						if (!EtlOperationResultHeader.hasAtLeastOneRecordsWithRecursiveRelashionships(results)) {
							getThreadRecordIntervalsManager().getCurrentLimits().markSkippedRecordsAsProcessed();
						}
					}
					
				}
				finally {
					OpenConnection.finalizeAllConnections(sharedSrcConn, sharedDstConn);
					
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
		}
	}
	
	/**
	 * @param persistTheWork
	 * @param sharedSrcConn
	 * @param sharedDstConn
	 * @param interval
	 * @param taskProcessor
	 * @return
	 */
	public void performeTask(TaskProcessor<T> taskProcessor, boolean useMultiTreadSearch, boolean persistTheWork,
	        OpenConnection srcConn, OpenConnection dstConn) {
		
		try {
			
			logTrace("INITIALIZING TASK FOR INTERVAL " + taskProcessor.getLimits());
			
			taskProcessor.performe(useMultiTreadSearch, srcConn, dstConn);
			
			if (!taskProcessor.getTaskResultInfo().hasFatalError()) {
				getController().afterEtl(taskProcessor.getTaskResultInfo().getAllSuccessfulyProcessedRecords(), srcConn,
				    dstConn);
				
				if (taskProcessor.getTaskResultInfo().hasRecordsWithErrors()) {
					logWarn("Some errors where found loading '"
					        + taskProcessor.getTaskResultInfo().getRecordsWithErrorsAsEtlDatabaseObject().size()
					        + "! The errors will be documented");
					
					taskProcessor.getTaskResultInfo().documentErrors(srcConn, dstConn);
				}
				
				refreshProgressMeter(taskProcessor.getTaskResultInfo().countAllSuccessfulyProcessedRecords(), srcConn);
				
				taskProcessor.getLimits().markAsProcessed();
				
				if (persistTheWork) {
					logTrace("PERSISTING WORK OF TASK ON INTERVAL " + taskProcessor.getLimits());
					
					getThreadRecordIntervalsManager().save();
					
					OpenConnection.markAllAsSuccessifullyTerminected(srcConn, dstConn);
					
					logTrace("WORK OF TASK PERSISTED ON INTERVAL " + taskProcessor.getLimits());
				}
			}
		}
		catch (DBException e) {
			taskProcessor.getTaskResultInfo().setFatalException(e);
		}
		finally {
			if (persistTheWork) {
				OpenConnection.finalizeAllConnections(srcConn, dstConn);
			}
		}
	}
	
	private void resetCurrentTaskProcessor(int qtyProcessors) {
		this.currentTaskProcessor = new ArrayList<>(qtyProcessors);
	}
	
	private void calculateStatistics() throws DBException {
		OpenConnection conn = getController().openSrcConnection();
		
		try {
			logInfo("CALCULATING STATISTICS! Using '"
			        + this.getRelatedEtlOperationConfig().getTotalCountStrategy().toString() + "' strategy...");
			
			int remaining = getProgressMeter().getRemain();
			int total = getProgressMeter().getTotal();
			int processed = total - remaining;
			
			if (this.getRelatedEtlOperationConfig().getTotalCountStrategy().isCountAlways()) {
				if (total > 0) {
					logDebug(
					    "Recorded statistic found! But the statistics will be recalculated as per configuration totalCountStrategy set to COUNT_ALWAYS");
					
					total = 0;
				}
			}
			
			if (total == 0) {
				if (this.getRelatedEtlOperationConfig().getTotalCountStrategy().isUseMaxRecordIdAsCount()) {
					total = (int) this.getProgressMeter().getMaxRecordId();
					remaining = total;
				} else if (this.getRelatedEtlOperationConfig().getTotalCountStrategy().isUseProvided()) {
					total = this.getRelatedEtlOperationConfig().getTotalAvaliableRecordsToProcess();
					remaining = total;
				} else {
					logDebug("Loading from Database...");
					
					total = getSearchParams().countAllRecords(conn);
					remaining = getSearchParams().countNotProcessedRecords(conn);
				}
				
				processed = total - remaining;
			}
			
			this.getProgressMeter().refresh(this.getProgressMeter().getStatusMsg(), total, processed,
			    getThreadRecordIntervalsManager().getCurrentLastRecordId());
			
			if (getRelatedOperationController().isResumable()) {
				this.getTableOperationProgressInfo().save(conn);
			}
			
			logInfo("CALCULATION DONE. TOTAL RECORDS " + total + "! PROCESSED RECORDS " + processed);
			
			conn.markAsSuccessifullyTerminated();
			
			reportProgress();
		}
		catch (DBException e) {
			getRelatedOperationController().requestStopDueError(this, e);
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
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
		
		for (ThreadRecordIntervalsManager<T> i : newIntervals) {
			i.save();
		}
	}
	
	public File getThreadsDir() {
		String subFolder = this.getRelatedOperationController().generateOperationStatusFolder();
		
		subFolder += FileUtilities.getPathSeparator() + "threads";
		
		return new File(subFolder);
	}
	
	public File getDataDir() {
		String subFolder = getEtlConfiguration().getEtlRootDirectory();
		
		subFolder += FileUtilities.getPathSeparator() + "data";
		
		subFolder += FileUtilities.getPathSeparator() + getEtlConfiguration().getOriginAppLocationCode();
		
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
		return getController().getOperationConfig().getProcessingBatch();
	}
	
	public int getMaxSupportedProcessors() {
		return getController().getOperationConfig().getMaxSupportedProcessors();
	}
	
	public void setMaxSupportedProcessors(int maxSupportedProcessors) {
		getController().getOperationConfig().setMaxSupportedProcessors(maxSupportedProcessors);
	}
	
	public void setMaxRecordsPerProcessing(int maxRecordsPerProcessing) {
		getController().getOperationConfig().setProcessingBatch(maxRecordsPerProcessing);
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
	
	public EtlOperationConfig getRelatedEtlOperationConfig() {
		return getRelatedOperationController().getOperationConfig();
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
	
	public void logTrace(String msg) {
		getRelatedOperationController().logTrace(msg);
	}
	
	public void logWarn(String msg, long interval) {
		getRelatedOperationController().logWarn(msg, interval);
	}
	
	@Override
	public String toString() {
		return this.getEngineId();
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
	
	public synchronized void refreshProgressMeter(int newlyProcessedRecords, Connection conn) throws DBException {
		logDebug("REFRESHING PROGRESS METER FOR MORE " + newlyProcessedRecords + " RECORDS.");
		this.getProgressMeter().refresh("RUNNING", this.getProgressMeter().getTotal(),
		    this.getProgressMeter().getProcessed() + newlyProcessedRecords,
		    this.getThreadRecordIntervalsManager().getCurrentLastRecordId());
		
		if (getRelatedOperationController().isResumable()) {
			this.getTableOperationProgressInfo().save(conn);
		}
		
		logDebug("PROGRESS METER REFRESHED");
		
		reportProgress();
	}
	
	public void reportProgress() {
		EtlProgressMeter globalProgressMeter = this.getProgressMeter();
		
		String log = "";
		
		log += this.getEtlConfigCode().toUpperCase() + "\n\nPROGRESS (" + getEtlConfigCode().toUpperCase()
		        + "):\n------------------\n";
		
		long diff = globalProgressMeter.getTotalToAnalyze() - globalProgressMeter.getTotal();
		
		if (!utilities.isBetween(diff, -10, 10)) {
			log += "TOTAL TO ANALYZE     : "
			        + utilities.ident(utilities.generateCommaSeparetedNumber(globalProgressMeter.getTotalToAnalyze()), 12)
			        + ", ";
			log += "PROCESSED: " + globalProgressMeter.getDetailedProgressOfAnalyzedRecords() + ", ";
			log += "REMAINING: " + globalProgressMeter.getDetailedRemainingToAnalize() + ",\n";
		}
		
		log += "TOTAL RECS TO PROCESS: "
		        + utilities.ident(utilities.generateCommaSeparetedNumber(globalProgressMeter.getTotal()), 12) + ", ";
		log += "PROCESSED: " + globalProgressMeter.getDetailedProgress() + ", ";
		log += "REMAINING: " + globalProgressMeter.getDetailedRemaining() + ",";
		log += "\nTIME                 : " + utilities.ident(globalProgressMeter.getHumanReadbleTime(), 12);
		log += "\nUSING THREADS: " + this.getThreadRecordIntervalsManager().getMaxSupportedProcessors();
		log += "\n------------------";
		
		this.logInfo(log);
	}
	
	public synchronized void requestDisplayOfEtlResult(DstConf dstConf, List<EtlDatabaseObject> resultObjs) {
		if (this.getRecordsToDisplay() == null) {
			this.setRecordsToDisplay(new HashMap<>());
		}
		
		List<EtlDatabaseObject> dstRecords = this.getRecordsToDisplay().get(dstConf.getTableName());
		
		if (dstRecords == null) {
			dstRecords = new ArrayList<>();
		}
		
		dstRecords.addAll(resultObjs);
		
		this.getRecordsToDisplay().put(dstConf.getTableName(), dstRecords);
	}
	
}
