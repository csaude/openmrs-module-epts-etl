package org.openmrs.module.eptssync;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncConf;
import org.openmrs.module.eptssync.export.controller.SyncExportController;
import org.openmrs.module.eptssync.load.controller.SyncDataLoadController;
import org.openmrs.module.eptssync.synchronization.controller.SynchronizationController;
import org.openmrs.module.eptssync.transport.controller.SyncTransportController;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

public class Main {
	
	public static void main(String[] args) throws IOException {
		SyncConf syncConfSource = SyncConf.loadFromJSON(new String(Files.readAllBytes(Paths.get("sync_config_source.json"))));
		SyncConf syncConfDest = SyncConf.loadFromJSON(new String(Files.readAllBytes(Paths.get("sync_config_dest.json"))));
		
		//SyncConf syncConfSource = SyncConf.loadFromJSON(new String(Files.readAllBytes(Paths.get("sync_config_source_minimal.json"))));
		//SyncConf syncConfDest = SyncConf.loadFromJSON(new String(Files.readAllBytes(Paths.get("sync_config_dest_minimal.json"))));
	
		
		//Performe database configuration
		ThreadPoolService.getInstance().createNewThreadPoolExecutor("SOURCE_SYNC_CONF_INIT").execute(syncConfSource);
		ThreadPoolService.getInstance().createNewThreadPoolExecutor("DEST_SYNC_CONF_INIT").execute(syncConfDest);
		
		
		while(!syncConfSource.isFinished() || !syncConfDest.isFinished()) {
			TimeCountDown.sleep(20);
		}
		
		//Now begin the job
		List<AbstractSyncController> allController = new ArrayList<AbstractSyncController>();
		
		allController.add(new SyncExportController());
		allController.get(allController.size() - 1).init(syncConfSource);
		
		allController.add(new SyncTransportController());
		allController.get(allController.size() - 1).init(syncConfSource);
		
		allController.add(new SyncDataLoadController());
		allController.get(allController.size() - 1).init(syncConfDest);
		
		allController.add(new SynchronizationController());
		allController.get(allController.size() - 1).init(syncConfDest);
			
		//allController.add(new DatabaseIntegrityConsolidationController());
		//allController.get(allController.size() - 1).init(syncConfDest);
		
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
