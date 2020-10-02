package org.openmrs.module.eptssync.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.SyncConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.RunningEngineInfo;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Synchronization controller. Initialize all synchronization engine
 * 
 * @see SyncEngine
 * 
 * @author jpboane
 *
 */

public abstract class AbstractSyncController implements Runnable{
	protected Logger logger;
	
	protected Map<String, RunningEngineInfo> runnungEngines;
	
	private SyncConfig syncConfig;
	private boolean initialized;

	private List<EnginActivitieMonitor> enginesActivititieMonitor;
	private ControllerActivitieMonitor activitieMonitor;
	
	private AbstractSyncController relatedOperationToBeRunInTheEnd;
	
	private String controllerId;
	
	public AbstractSyncController(SyncConfig syncConfig) {
		this.runnungEngines = new HashMap<String, RunningEngineInfo>();
		
		this.logger = Logger.getLogger(this.getClass());
		
		this.syncConfig = syncConfig;
		
		this.controllerId = getOperationType() + "_" + syncConfig.getOriginAppLocationCode();	
	}

	public AbstractSyncController getRelatedOperationToBeRunInTheEnd() {
		return relatedOperationToBeRunInTheEnd;
	}
	
	public void setRelatedOperationToBeRunInTheEnd(AbstractSyncController relatedOperationToBeRunInTheEnd) {
		this.relatedOperationToBeRunInTheEnd = relatedOperationToBeRunInTheEnd;
	}
	
	@JsonIgnore
	public Map<String, RunningEngineInfo> getRunnungEngines() {
		return runnungEngines;
	}

	public void setRunnungEngines(Map<String, RunningEngineInfo> runnungEngines) {
		this.runnungEngines = runnungEngines;
	}
	
	public void init() {
		this.enginesActivititieMonitor = new ArrayList<EnginActivitieMonitor>();
		
		List<SyncTableInfo> allSync = syncConfig.getSyncTableInfo();
	
		for (SyncTableInfo syncInfo: allSync) {
			initAndStartEngine(syncInfo);
		}
		
		if (this.relatedOperationToBeRunInTheEnd != null) {
			this.activitieMonitor = new ControllerActivitieMonitor(this);
		
			ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.controllerId + "_MONIGTOR");
			executor.execute(this.activitieMonitor);
		}
		
		this.initialized = true;
	}

	public String getControllerId() {
		return controllerId;
	}
	
	protected void initAndStartEngine(SyncTableInfo syncInfo) {
		logInfo("INITIALIZING ENGINE FOR TABLE '" + syncInfo.getTableName() + "'");
		
		SyncEngine mainEngine; 
		
		long maxRecId = getMaxRecordId(syncInfo);
		long minRecId = getMinRecordId(syncInfo);
		
		long qtyEngines = (maxRecId - minRecId)/syncInfo.getQtyRecordsPerEngine(getOperationType());
		
		if (qtyEngines == 0) qtyEngines = 1;
		
		RecordLimits limits = new RecordLimits(minRecId, minRecId + syncInfo.getQtyRecordsPerEngine(getOperationType()));
		
		mainEngine = initRelatedEngine(syncInfo, limits);
		mainEngine.setEngineId(getControllerId()+"_"+syncInfo.getTableName()+""+utilities().garantirXCaracterOnNumber(0, 2));
		mainEngine.setChildren(new ArrayList<SyncEngine>());
		
		int i = 1;
		
		for (i = 1; i < qtyEngines; i++) {
			 limits  = new RecordLimits(limits.getLastRecordId() + 1, limits.getLastRecordId() + syncInfo.getQtyRecordsPerEngine(getOperationType()) + 1);
			
			 if (i == qtyEngines) limits.setLastRecordId(maxRecId);
			 
			 SyncEngine engine = initRelatedEngine(syncInfo, limits);
			 engine.setEngineId(getControllerId()+"_"+syncInfo.getTableName()+""+utilities().garantirXCaracterOnNumber(i, 2));
				
			 engine.setParent(mainEngine);
			 
			 mainEngine.getChildren().add(engine);
		}
	
		ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(mainEngine.getEngineId());
		executor.execute(mainEngine);
		runnungEngines.put(mainEngine.getEngineId(), new RunningEngineInfo(executor, mainEngine));
		
		if (mainEngine.getChildren() != null) {
			for (SyncEngine childEngine : mainEngine.getChildren()) {
				executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(childEngine.getEngineId());
				executor.execute(childEngine);
				runnungEngines.put(childEngine.getEngineId(), new RunningEngineInfo(executor, childEngine));
			}
		}
	
		if (mustRestartInTheEnd()) {
			EnginActivitieMonitor monitor = new EnginActivitieMonitor(this, syncInfo);
			executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(getControllerId() + "_ENGINE_OPERATION_MONITOR");
			executor.execute(monitor);
	
			this.enginesActivititieMonitor.add(monitor);
		}
		
		logInfo("ENGINE FOR TABLE '" + syncInfo.getTableName() + "' INITIALIZED");
	}
	
	public List<EnginActivitieMonitor> getEnginesActivititieMonitor() {
		return enginesActivititieMonitor;
	}
	
	public void setSyncTableInfoSource(SyncConfig syncTableInfoSource) {
		this.syncConfig = syncTableInfoSource;
	}
	
	@JsonIgnore
	public SyncConfig getSyncConfig() {
		return syncConfig;
	}
	
	@JsonIgnore
	public OpenConnection openConnection() {
		return syncConfig.openConnection();
	}
	
	@JsonIgnore
	public CommonUtilities utilities() {
		return CommonUtilities.getInstance();
	}
	
	public void logInfo(String msg) {
		utilities().logInfo(msg, logger);
	}
	
	public void logError(String msg) {
		utilities().logErr(msg, logger);
	}
	
	public void logDebug(String msg) {
		utilities().logDebug(msg, logger);
	}

	@JsonIgnore
	public boolean isFininished() {
		if(!initialized) {
			return false;
		}
		
		for (Map.Entry<String, RunningEngineInfo> engine : runnungEngines.entrySet()) {
			if (!engine.getValue().getEngine().isFinished()) {
				return false;
			}
		}
		
		return true;
	}
	
	public void realocateJobToEngines(EnginActivitieMonitor monitor) {
		SyncTableInfo syncInfo = monitor.getTableInfo();
		
		logInfo("REALOCATING ENGINES FOR '" + syncInfo.getTableName() + "'");
		
		SyncEngine mainEngine = retrieveMainSleepingEngine(syncInfo); 
		
		long maxRecId = getMaxRecordId(syncInfo);
		long minRecId = getMinRecordId(syncInfo);
		
		long qtyEngines = (maxRecId - minRecId)/syncInfo.getQtyRecordsPerEngine(getOperationType());
		
		if (qtyEngines == 0) qtyEngines = 1;
		
		RecordLimits limits = new RecordLimits(minRecId, minRecId + syncInfo.getQtyRecordsPerEngine(getOperationType()));
		
		//If there was no main engine, retrieve onother engine and make it main
		mainEngine =  mainEngine == null ? retrieveSleepingEngine(syncInfo) : mainEngine;
		
		mainEngine.resetLimits(limits);
		mainEngine.changeStatusToPaused();
		
		if (mainEngine.getChildren() == null) {
			mainEngine.setChildren(new ArrayList<SyncEngine>());
			
			mainEngine.setEngineId(getOperationType()+"_"+syncInfo.getTableName()+""+utilities().garantirXCaracterOnNumber(0, 2));
		}
		
		int i = 1;
		
		for (i = 1; i < qtyEngines; i++) {
			 limits  = new RecordLimits(limits.getLastRecordId() + 1, limits.getLastRecordId() + syncInfo.getQtyRecordsPerEngine(getOperationType()) + 1);
			
			 if (i == qtyEngines) limits.setLastRecordId(maxRecId);
			 
			 SyncEngine engine = retrieveSleepingEngine(syncInfo);
			 
			 if (engine == null) {
				 engine = initRelatedEngine(syncInfo, limits);
				 
				 mainEngine.getChildren().add(engine);
				 engine.setEngineId(getOperationType()+"_"+syncInfo.getTableName()+""+utilities().garantirXCaracterOnNumber(i, 2));
				 engine.setParent(mainEngine);
			 }
			 else {
				 engine.resetLimits(limits);
				 engine.changeStatusToPaused();
			 }
		}
	
		ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(mainEngine.getEngineId());
		executor.execute(mainEngine);
		
		//If this engine is new must be not initialized, otherwise must be on STOPPED STATE
		if (mainEngine.isNotInitialized()) {
			runnungEngines.put(mainEngine.getEngineId(), new RunningEngineInfo(executor, mainEngine));
		}
		
		if (mainEngine.getChildren() != null) {
			for (SyncEngine childEngine : mainEngine.getChildren()) {
				executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(childEngine.getEngineId());
				executor.execute(childEngine);
				
				if (mainEngine.isNotInitialized()) {
					runnungEngines.put(childEngine.getEngineId(), new RunningEngineInfo(executor, childEngine));
				}
			}
		}
		
		logInfo("ENGINE FOR TABLE '" + syncInfo.getTableName() + "' RESET");
				
	}
	
	/**
	 * Schedule new job for this job. This is controller by {@link EnginActivitieMonitor}
	 * 
	 * @param syncEngine
	 */
	public void scheduleNewJobForEngine(SyncEngine syncEngine) {
		syncEngine.setNewJobRequested(true);
		syncEngine.changeStatusToSleeping();
		logInfo("THE ENGINE '" + syncEngine.getEngineId() + "' HAS FINISHED ITS JOB AND NOW IS WATING FOR NEW ALOCATION WORK");	
	}

	private SyncEngine retrieveSleepingEngine(SyncTableInfo tableInfo) {
		for ( Entry<String, RunningEngineInfo> engineInfo : this.getRunnungEngines().entrySet()) {
			if (engineInfo.getValue().getEngine().getSyncTableInfo().equals(tableInfo) && engineInfo.getValue().getEngine().isSleeping()) {
				return engineInfo.getValue().getEngine();  
			}
		}
		
		return null;
	}
	
	private SyncEngine retrieveMainSleepingEngine(SyncTableInfo tableInfo) {
		for ( Entry<String, RunningEngineInfo> engineInfo : this.getRunnungEngines().entrySet()) {
			if (engineInfo.getValue().getEngine().getSyncTableInfo().equals(tableInfo) && engineInfo.getValue().getEngine().isSleeping()) {
				
				if (utilities().arrayHasElement(engineInfo.getValue().getEngine().getChildren())) {
					return engineInfo.getValue().getEngine();  
				}
			}
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return this.controllerId;
	}
	
	public void starttRelatedOperationToBeRunInTheEnd() {
		this.relatedOperationToBeRunInTheEnd.init();
	}
	
	@Override
	public void run() {
		init();
	}
	
	@JsonIgnore
	public abstract boolean mustRestartInTheEnd();
	
	@JsonIgnore
	public abstract String getOperationType();
	
	public abstract SyncEngine initRelatedEngine(SyncTableInfo syncInfo, RecordLimits limits) ;

	protected abstract long getMinRecordId(SyncTableInfo tableInfo);

	protected abstract long getMaxRecordId(SyncTableInfo tableInfo);
	
	public void refresh() {
	}
}
