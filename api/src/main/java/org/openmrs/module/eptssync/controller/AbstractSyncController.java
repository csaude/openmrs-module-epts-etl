package org.openmrs.module.eptssync.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfoSource;
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
	private Map<String, RunningEngineInfo> runnungEngines;
	
	private static SyncTableInfoSource syncTableInfoSource;
	
	public AbstractSyncController() {
		this.runnungEngines = new HashMap<String, RunningEngineInfo>();
	}

	public void init() {
		List<SyncTableInfo> allSync = discoverSyncTableInfo();
	
		for (SyncTableInfo syncInfo: allSync) {
			initAndStartEngine(syncInfo);
		}
	}
	
	private void initAndStartEngine(SyncTableInfo syncInfo) {
		SyncEngine engine = initRelatedEngine(syncInfo);
		
		ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(syncInfo.getTableName());
		executor.execute(engine);
		
		runnungEngines.put(syncInfo.getTableName(), new RunningEngineInfo(executor, engine));
	}
	
	protected SyncTableInfoSource getSyncTableInfoSource() {
		return syncTableInfoSource;
	}
	
	private synchronized List<SyncTableInfo> discoverSyncTableInfo() {
		try {
			String json = new String(Files.readAllBytes(Paths.get("sync_config.json")));
			
			
			/* 
			String json = "{\n" + 
					"				syncRootDirectory: \"/home/jpboane/working/prg/jee/workspace/data/sync\",\n"+
					"				syncTableInfo:[ {\n" + 
					"					tableName: \"person\",\n" + 
					"					mustRecompileTable: true,\n" + 
					"					mainParent: \"\",\n" + 
					"					otherParents: [],\n" + 
					"					intrinsicChild: [\"person_adress\", \"person_attribute\", \"person_name\"]\n" + 
					"				}]}";*/
			
			
			if (syncTableInfoSource == null) {
				syncTableInfoSource = SyncTableInfoSource.loadFromJSON(json);
			}
			
			return syncTableInfoSource.getSyncTableInfo();
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public abstract SyncEngine initRelatedEngine(SyncTableInfo syncInfo) ;

	public OpenConnection openConnection() {
		return DBConnectionService.getInstance().openConnection();
	}
	
	
	public CommonUtilities utilities() {
		return CommonUtilities.getInstance();
	}
}
