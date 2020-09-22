package org.openmrs.module.eptssync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfoSource;
import org.openmrs.module.eptssync.synchronization.controller.SynchronizationController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;

public class Main {
	
	public static void main(String[] args) throws IOException {
		
		String json = new String(Files.readAllBytes(Paths.get("sync_config.json")));
		
		SyncTableInfoSource syncTableInfoSource = SyncTableInfoSource.loadFromJSON(json);
		
		DBConnectionService connService = DBConnectionService.init(syncTableInfoSource.getConnInfo());
		
		syncTableInfoSource.fullLoadInfo();
		
		
		List<AbstractSyncController> allController = new ArrayList<AbstractSyncController>();
		
		//allController.add(new SyncExportController());
		//allController.get(allController.size() - 1).init(syncTableInfoSource);
		
		//allController.add(new SyncTransportController());
		//allController.get(allController.size() - 1).init(syncTableInfoSource);
		
		//allController.add(new SyncDataLoadController());
		//allController.get(allController.size() - 1).init(syncTableInfoSource);
		
		allController.add(new SynchronizationController(connService));
		allController.get(allController.size() - 1).init(syncTableInfoSource);
		
		while(!isAllFinished(allController)) {
			TimeCountDown.sleep(10000);
		}
		
		allController.get(0).logInfo("ALL JOBS ARE FINISHED");
	}
	
	public static boolean isAllFinished(List<AbstractSyncController> controllers) {
		for (AbstractSyncController c : controllers) {
			if (!c.isFininished()) return false;
		}
		
		return true;
	}
}
