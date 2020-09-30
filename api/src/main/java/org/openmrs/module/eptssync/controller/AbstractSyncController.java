package org.openmrs.module.eptssync.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.SyncConf;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.RunningEngineInfo;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * Synchronization controller. Initialize all synchronization engine
 * 
 * @see SyncEngine
 * 
 * @author jpboane
 *
 */

public abstract class AbstractSyncController {
	public static String SYNC_OPERATION_EXPORT = "EXPORT";
	public static String SYNC_OPERATION_SYNCHRONIZATION = "SYNCHRONIZATION";
	public static String SYNC_OPERATION_LOAD = "LOAD";
	public static String SYNC_OPERATION_TRANSPOR = "TRANSPORT";
	public static String SYNC_OPERATION_CONSOLIDATION= "CONSOLIDATION";
	
	protected Logger logger;
	
	
	private Map<String, RunningEngineInfo> runnungEngines;
	
	private SyncConf syncTableInfoSource;
	private boolean initialized;
	
	public AbstractSyncController() {
		this.runnungEngines = new HashMap<String, RunningEngineInfo>();
		
		this.logger = Logger.getLogger(this.getClass());
	}

	public void init(SyncConf syncTableInfoSource) {
		this.syncTableInfoSource = syncTableInfoSource;
		
		List<SyncTableInfo> allSync = syncTableInfoSource.getSyncTableInfo();
	
		for (SyncTableInfo syncInfo: allSync) {
			initAndStartEngine(syncInfo);
		}
		
		this.initialized = true;
	}
	
	protected void initAndStartEngine(SyncTableInfo syncInfo) {
		logInfo("INITIALIZING ENGINE FOR TABLE '" + syncInfo.getTableName() + "'");
		
		SyncEngine mainEngine; 
		
		long maxRecId = getMaxRecordId(syncInfo);
		long minRecId = getMinRecordId(syncInfo);
		
		if (syncInfo.getQtyProcessingEngine() > 1 && minRecId != maxRecId) {
			long qtyRecordsPerEngine = (maxRecId - minRecId)/syncInfo.getQtyProcessingEngine();
			
			RecordLimits limits = new RecordLimits(minRecId, minRecId + qtyRecordsPerEngine);
			
			mainEngine = initRelatedEngine(syncInfo, limits);
			
			mainEngine.setChildren(new ArrayList<SyncEngine>());
			
			int i = 1;
			
			for (i = 1; i < syncInfo.getQtyProcessingEngine() - 1; i++) {
				 limits  = new RecordLimits(limits.getLastRecordId() + 1, limits.getLastRecordId() + qtyRecordsPerEngine + 1);
				
				 SyncEngine engine = initRelatedEngine(syncInfo, limits);
				 engine.setEngineId(getOperationName()+"_"+syncInfo.getTableName()+""+utilities().garantirXCaracterOnNumber(i, 2));
					
				 engine.setParent(mainEngine);
				 
				 mainEngine.getChildren().add(engine);
			}
		
			 limits  = new RecordLimits(limits.getLastRecordId() + 1, maxRecId);
				
			 SyncEngine engine = initRelatedEngine(syncInfo, limits);
			 engine.setEngineId(getOperationName()+"_"+syncInfo.getTableName()+""+utilities().garantirXCaracterOnNumber(i, 2));
				
			 mainEngine.getChildren().add(engine);
		}
		else mainEngine = initRelatedEngine(syncInfo, null);
		
		mainEngine.setEngineId(getOperationName()+"_"+syncInfo.getTableName()+""+utilities().garantirXCaracterOnNumber(0, 2));

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
		
		
		logInfo("ENGINE FOR TABLE '" + syncInfo.getTableName() + "' INITIALIZED");
	}

	public void setSyncTableInfoSource(SyncConf syncTableInfoSource) {
		this.syncTableInfoSource = syncTableInfoSource;
	}
	
	protected SyncConf getSyncTableInfoSource() {
		return syncTableInfoSource;
	}
	
	public OpenConnection openConnection() {
		return syncTableInfoSource.openConnection();
	}
	
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

	public abstract boolean mustRestartInTheEnd();
	
	public abstract String getOperationName();
	
	public abstract SyncEngine initRelatedEngine(SyncTableInfo syncInfo, RecordLimits limits) ;

	protected abstract long getMinRecordId(SyncTableInfo tableInfo);

	protected abstract long getMaxRecordId(SyncTableInfo tableInfo);

}
