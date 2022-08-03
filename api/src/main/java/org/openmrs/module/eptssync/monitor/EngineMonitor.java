package org.openmrs.module.eptssync.monitor;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncProgressMeter;
import org.openmrs.module.eptssync.model.TableOperationProgressInfo;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

/**
 * This class monitor all {@link Engine}s of an {@link OperationController}
 *  
 * @author jpboane
 */
public class EngineMonitor implements MonitoredOperation{
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private OperationController controller;
	private SyncTableConfiguration syncTableInfo;
	
	private List<Engine> ownEngines;
	
	private String engineMonitorId;
	private String engineId;
	
	private int operationStatus;
	private boolean stopRequested;
	protected TableOperationProgressInfo tableOperationProgressInfo;
	
	public EngineMonitor(OperationController controller, SyncTableConfiguration syncTableInfo, TableOperationProgressInfo tableOperationProgressInfo) {
		this.controller = controller;
		this.ownEngines = new ArrayList<Engine>();
		this.syncTableInfo = syncTableInfo;
		
		this.engineMonitorId = (controller.getControllerId() + "_" + syncTableInfo.getTableName() + "_monitor").toLowerCase();
		this.engineId = (getController().getControllerId() + "_" + syncTableInfo.getTableName()).toLowerCase();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		this.tableOperationProgressInfo = tableOperationProgressInfo;
	}
	
	public String getEngineId() {
		return engineId;
	}
	
	public String getEngineMonitorId() {
		return engineMonitorId;
	}
	
	public SyncTableConfiguration getSyncTableInfo() {
		return syncTableInfo;
	}
	
	public SyncProgressMeter getProgressMeter() {
		return this.tableOperationProgressInfo != null ? this.tableOperationProgressInfo.getProgressMeter() : null;
	}
	
	public Engine getMainEngine() {
		for ( Engine engine : this.ownEngines) {
			if (engine.getChildren() != null) {
				return engine;
			}
		}
		
		if (utilities.arrayHasElement(this.ownEngines)) {
			return this.ownEngines.get(0);
		}
		
		
		return null;
		
		//throw new RuntimeException("No engine defined for this monitor "+getController().getControllerId() + "_" + getSyncTableInfo().getTableName());
	}
	
	@Override
	public void run() {
		try {
			if (!getSyncTableInfo().isFullLoaded()) getSyncTableInfo().fullLoad();
			
			initEngine();
			
			//this.controller.getProgressInfo().updateProgressInfo(this);
			
			if (!utilities.arrayHasElement(ownEngines)) {
				logDebug("NO ENGINE FOR '" + getController().getOperationType().name().toLowerCase() + "' FOR TABLE '" + getSyncTableInfo().getTableName().toUpperCase() + "' WAS CREATED...");
				
				this.operationStatus = MonitoredOperation.STATUS_FINISHED;
			}
			else {
				onStart();
				
				logInfo("INITIALIZED '" + getController().getOperationType().name().toLowerCase() + "' ENGINE FOR TABLE '" + getSyncTableInfo().getTableName().toUpperCase() + "'");
				
				while(isRunning()) {
					TimeCountDown.sleep(15);
					
					if (getMainEngine().isFinished()) {
						//getMainEngine().markAsFinished();
						getMainEngine().onFinish();
						
						onFinish();
					}
					else
					if(getMainEngine().isStopped()) {
						getMainEngine().onStop();
						
						onStop();
					}
					else
					if (!isAllEnginesRequestedNewJob()) {
						//this.controller.logInfo(msg);
					}
					else {
						if (utilities.arrayHasElement(this.ownEngines)) {
							for (Engine engine : this.ownEngines) {
								engine.setNewJobRequested(false);
							}
							
							this.realocateJobToEngines();
						}
						else {
							initEngine();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			getController().requestStopDueError(this, e);
		}
	}
	
	public OperationController getController() {
		return controller;
	}
	
	private void initEngine() {
		logInfo("INITIALIZING ENGINE");
		
		SyncTableConfiguration syncInfo = getSyncTableInfo();
		
		logDebug("DETERMINING MIN RECORD FOR " + getSyncTableInfo().getTableName());
		
		long minRecId = getController().getMinRecordId(getSyncTableInfo());
		
		logDebug("FOUND MIN RECORD " + getSyncTableInfo() + " = " + minRecId);
		
		logDebug("DETERMINING MAX RECORD FOR " + getSyncTableInfo().getTableName());
	
		long maxRecId =  getController().getMaxRecordId(getSyncTableInfo());
		logDebug("FOUND MAX RECORD " + getSyncTableInfo() + " = " + maxRecId);
			
		if (maxRecId == 0 && minRecId == 0) {
			String msg = "NO RECORD TO PROCESS FOR TABLE '"+ getSyncTableInfo().getTableName().toUpperCase() + "' NO ENGINE WILL BE CRIETED BY NOW!";
			
			logInfo(msg);
			
			if (mustRestartInTheEnd()) {
				msg += " GOING SLEEP....";
			}
			else {
				msg += " FINISHING....";
			}
			
			logInfo(msg);
		}
		else {
			long qtyRecords = maxRecId - minRecId + 1;
			long qtyEngines = determineQtyEngines(qtyRecords);
			long qtyRecordsPerEngine = 0;
			
			if (qtyEngines == 0) qtyEngines = 1;
			
			qtyRecordsPerEngine = determineQtyRecordsPerEngine(qtyEngines, qtyRecords);
			
			long currMax = minRecId + qtyRecordsPerEngine - 1;
			
			if (qtyEngines == 1) currMax = maxRecId;
			
			RecordLimits limits = generateLimits(minRecId, currMax, null); 
			
			Engine mainEngine = retrieveAndRemoveMainSleepingEngine();
			
			//If there was no main engine, retrieve onother engine and make it main
			mainEngine =  mainEngine == null ? retrieveAndRemoveSleepingEngin() : mainEngine;
			
			mainEngine = mainEngine == null ? controller.initRelatedEngine(this, limits) : mainEngine; 
			mainEngine.setEngineId(this.getEngineId() + "_" + utilities.garantirXCaracterOnNumber(0, 2));
			
			mainEngine.resetLimits(limits);
			
			logDebug("ALLOCATED RECORDS [" + mainEngine.getSearchParams().getLimits() + "] FOR ENGINE [" + mainEngine.getEngineId()  + "]");
			
			if (mainEngine.getChildren() == null) {
				mainEngine.setChildren(new ArrayList<Engine>());
			}
			
			int i = 1;
			
			for (i = 1; i < qtyEngines; i++) {
				 limits  = generateLimits(limits.getThreadMaxRecord() + 1, limits.getThreadMaxRecord() + qtyRecordsPerEngine, null);
				 
				 if (i == qtyEngines - 1) {
					 limits.setThreadMaxRecord(maxRecId);
					 limits.reset();
				 }
				 
				 Engine engine = retrieveAndRemoveSleepingEngin();
				 
				 if (engine == null) {
					 engine = getController().initRelatedEngine(this, limits);
					 engine.resetLimits(limits);
					 mainEngine.getChildren().add(engine);
					 engine.setEngineId(getEngineId() + "_" + utilities.garantirXCaracterOnNumber(i, 2));
					 engine.setParent(mainEngine);
				 }
				 else {
					 engine.resetLimits(limits);
				 }
				 
				 logDebug("REALOCATED NEW RECORDS [" + engine.getSearchParams().getLimits() + "] FOR ENGINE [" + engine.getEngineId()  + "]");
			}
		
			//If this engine is new must be not initialized, otherwise must be on STOPPED STATE
			if (mainEngine.isNotInitialized()) {
				this.ownEngines.add(mainEngine);
			}
			
			ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(mainEngine.getEngineId());
			executor.execute(mainEngine);
			
			if (mainEngine.getChildren() != null) {
				for (Engine childEngine : mainEngine.getChildren()) {
					if (mainEngine.isNotInitialized()) {
						this.ownEngines.add(mainEngine);
					}
			
					executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(childEngine.getEngineId());
					executor.execute(childEngine);
				}
			}
			
			logInfo("ENGINE FOR TABLE '" + syncInfo.getTableName() + "' INITIALIZED!");
		}
	}
	
	protected RecordLimits generateLimits(long minRecId, long currMax, Engine engine) {
		return new RecordLimits(minRecId, currMax, getQtyRecordsPerProcessing(),  engine);
	}

	private long determineQtyRecordsPerEngine(long qtyEngines, long qtyRecords) {
		return qtyRecords/qtyEngines;
	}

	private long determineQtyEngines(long qtyRecords) {
		long qtyRecordsPerEngine = qtyRecords/getController().getOperationConfig().getMaxSupportedEngines();
		
		if (qtyRecordsPerEngine > getController().getOperationConfig().getMinRecordsPerEngine()) {
			return getController().getOperationConfig().getMaxSupportedEngines();
		}
		
		return qtyRecords/getController().getOperationConfig().getMinRecordsPerEngine();
	}

	public void realocateJobToEngines() {
		logDebug("REALOCATING ENGINES FOR '" + getSyncTableInfo().getTableName() + "'");
		
		initEngine();
	}

	public int getQtyRecordsPerProcessing() {
		return getController().getOperationConfig().getMaxRecordPerProcessing();
	}
	
	private Engine retrieveAndRemoveSleepingEngin() {
		Engine sleepingEngine = null;
		
		for ( Engine engine : this.ownEngines) {
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
		
		for ( Engine engine : this.ownEngines) {
			if (engine.isSleeping() && engine.getChildren() != null) {
				sleepingEngine = engine;
				
				break;
			}
		}
		
		if (sleepingEngine != null) this.ownEngines.remove(sleepingEngine);
		
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

	String generateEngineNewJobRequestStatus() {
		String status = "";
		
		for (Engine engine : ownEngines) {
			status += "[" + engine.getEngineId() + " > " + (engine.isNewJobRequested() ? "REQUESTED" : "NOT REQUESTED") + "] ";
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
		logInfo("THE ENGINE '" + syncEngine.getEngineId() + "' HAS FINISHED ITS JOB AND NOW IS WATING FOR NEW ALOCATION WORK");	
	}
	
	boolean isAllEnginesRequestedNewJob() {
		for (Engine engine : ownEngines) {
			if (!engine.isNewJobRequested() ) return false;
		}
		
		return true;
	}

	public void killSelfCreatedThreads() {
		for (Engine engine : this.ownEngines) {
			ThreadPoolService.getInstance().terminateTread(getController().getLogger(), getController().getProcessController().getLogLevel(), engine.getEngineId());
		}
	}

	public static EngineMonitor init(OperationController controller, SyncTableConfiguration syncTableInfo, TableOperationProgressInfo tableOperationProgressInfo) {
		EngineMonitor monitor = new EngineMonitor(controller, syncTableInfo, tableOperationProgressInfo);
		
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
		if (isNotInitialized()) return false;
	
		if (!utilities.arrayHasElement(this.ownEngines)) {
			return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
		}
		else
		for (Engine engine : this.ownEngines ) {
			if (!engine.isStopped()) {
				return false;
			}
		}
			
		return true;
	}
	
	@Override
	public boolean isFinished() {
		if(isNotInitialized()) {
			return false;
		}
		
		if (!utilities.arrayHasElement(this.ownEngines)) {
			return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
		}
		else
		for (Engine engine: this.ownEngines) {
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
		}
		else
		if (!stopRequested()) {
			if (getMainEngine() != null) getMainEngine().requestStop();
			
			this.stopRequested = true;
		}
	}

	public void requestStopDueError() {
		if (getMainEngine() != null) {
			getMainEngine().requestStopDueError();
		}
		else this.operationStatus = MonitoredOperation.STATUS_STOPPED;
			
		this.stopRequested = true;
	}
	
	
	public synchronized void refreshProgressMeter(int newlyProcessedRecords, Connection conn) throws DBException {
		this.getProgressMeter().refresh("RUNNING", this.getProgressMeter().getTotal(), this.getProgressMeter().getProcessed() + newlyProcessedRecords);
			
		this.getTableOperationProgressInfo().save(conn);
	}
	
	public synchronized void doInitProgressMeterRefresh(Engine engine, Connection conn) throws DBException  {
		int remaining = getProgressMeter().getRemain();
		int total = getProgressMeter().getTotal();
		int processed = total - remaining;
	
		if (total == 0) {
			remaining = engine.getSearchParams().countNotProcessedRecords(conn);
			total = engine.getSearchParams().countAllRecords(conn);
			processed = total - remaining;
		}

		this.getProgressMeter().refresh(this.getProgressMeter().getStatusMsg(), total, processed);
		
		this.getProgressMeter().changeStatusToRunning();
		
	}
	
	
	public void reportProgress() {
		SyncProgressMeter globalProgressMeter = this.getProgressMeter();
		
		String log = "";
		
		log += this.syncTableInfo.getTableName() + " PROGRESS: ";
		log += "[TOTAL RECS: " + globalProgressMeter.getTotal() + ", ";
		log += "PROCESSED: " + globalProgressMeter.getDetailedProgress() + ", ";
		log += "REMAINING: " + globalProgressMeter.getDetailedRemaining() + ",";
		log += "TIME: " + globalProgressMeter.getHumanReadbleTime() + "]";
		
		this.logInfo(log);
	}
	
	
}
