package org.openmrs.module.eptssync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfoSource;
import org.openmrs.module.eptssync.controller.load.SyncDataLoadController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;

public class Main {
	
	public static void main(String[] args) throws IOException {
		
		String json = new String(Files.readAllBytes(Paths.get("sync_config.json")));
		
		SyncTableInfoSource syncTableInfoSource = SyncTableInfoSource.loadFromJSON(json);
		
		DBConnectionService.init(syncTableInfoSource.getConnInfo());
		
		syncTableInfoSource.fullLoadInfo();
		
		//new SyncExportController().init(syncTableInfoSource);
	
		new SyncDataLoadController().init(syncTableInfoSource);
		
		//new SynchronizationController().init(syncTableInfoSource);
		
		while(true) {
			TimeCountDown.sleep(10000);
		}
	}
}
