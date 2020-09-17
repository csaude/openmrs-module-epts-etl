package org.openmrs.module.eptssync.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfoSource;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.RunningEngineInfo;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
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
	protected Logger logger;
	
	private Map<String, RunningEngineInfo> runnungEngines;
	
	private static SyncTableInfoSource syncTableInfoSource;
	
	public AbstractSyncController() {
		this.runnungEngines = new HashMap<String, RunningEngineInfo>();
		
		this.logger = Logger.getLogger(this.getClass());
	}

	public void init() {
		List<SyncTableInfo> allSync = discoverSyncTableInfo();
	
		for (SyncTableInfo syncInfo: allSync) {
			initAndStartEngine(syncInfo);
		}
	}
	
	protected void initAndStartEngine(SyncTableInfo syncInfo) {
		logInfo("INITIALIZING ENGINE FOR TABLE '" + syncInfo.getTableName() + "'");
		
		SyncEngine mainEngine; 
		
		if (syncInfo.getQtyProcessingEngine() > 1) {
			
			int maxRecId = getMaxRecordId(syncInfo);
			int minRecId = getMinRecordId(syncInfo);
			
			int qtyRecordsPerEngine = (maxRecId - minRecId)/syncInfo.getQtyProcessingEngine();
			
			RecordLimits limits = new RecordLimits(minRecId, minRecId + qtyRecordsPerEngine);
			
			mainEngine = initRelatedEngine(syncInfo, limits);
			
			mainEngine.setChildren(new ArrayList<SyncEngine>());
			
			for (int i =0; i < syncInfo.getQtyProcessingEngine() - 2; i++) {
				 limits  = new RecordLimits(limits.getLastRecordId() + 1, limits.getLastRecordId() + qtyRecordsPerEngine + 1);
				
				 SyncEngine engine = initRelatedEngine(syncInfo, limits);
				 
				 engine.setParent(mainEngine);
				 
				 mainEngine.getChildren().add(engine);
			}
		
			 limits  = new RecordLimits(limits.getLastRecordId() + 1, maxRecId);
				
			 mainEngine.getChildren().add(initRelatedEngine(syncInfo, limits));
		}
		else mainEngine = initRelatedEngine(syncInfo, null);
		
		ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(syncInfo.getTableName());
		executor.execute(mainEngine);
		
		if (mainEngine.isMultiProcessing()) {
			for (SyncEngine engine : mainEngine.getChildren()) {
				executor.execute(engine);
			}
		}
		
		runnungEngines.put(syncInfo.getTableName(), new RunningEngineInfo(executor, mainEngine));
		
		logInfo("ENGINE FOR TABLE '" + syncInfo.getTableName() + "' INITIALIZED");
	}

	protected SyncTableInfoSource getSyncTableInfoSource() {
		return syncTableInfoSource;
	}
	
	protected synchronized List<SyncTableInfo> discoverSyncTableInfo() {
		logInfo("DISCOVERY SYNC TABLES FOR '" + this.getClass().getSimpleName() + "'");
		
		try {
			
			String json = new String(Files.readAllBytes(Paths.get("sync_config.json")));
			
			if (syncTableInfoSource == null) {
				syncTableInfoSource = SyncTableInfoSource.loadFromJSON(json);
			}
			
			logInfo("DISCOVERED '" + syncTableInfoSource.getSyncTableInfo().size() + "' Tables for Sync");
			
			return syncTableInfoSource.getSyncTableInfo();
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public abstract SyncEngine initRelatedEngine(SyncTableInfo syncInfo, RecordLimits limits) ;

	protected abstract int getMinRecordId(SyncTableInfo tableInfo);

	protected abstract int getMaxRecordId(SyncTableInfo tableInfo);

	public OpenConnection openConnection() {
		return DBConnectionService.getInstance().openConnection();
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
}
