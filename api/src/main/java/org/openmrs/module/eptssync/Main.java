package org.openmrs.module.eptssync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncConf;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.export.controller.SyncExportController;
import org.openmrs.module.eptssync.synchronization.controller.SynchronizationController;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

public class Main {
	
	static Logger logger = Logger.getLogger(Main.class);
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static void main(String[] synConfigFiles) throws IOException {
		if (synConfigFiles == null || synConfigFiles.length == 0) throw new ForbiddenOperationException("You must especify the source/destination config file. Eg. /sync/conf.json");
	
		List<SyncConf> syncConfigs = loadSyncConfig(synConfigFiles);
		
		SyncConf destinationConf = determineDestinationSyncConf(syncConfigs);
		
		//Performe database configuration
		for (SyncConf conf : syncConfigs) {
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(conf.getDesignation()).execute(conf);
		}
		
		//Wait until all configuration been loaded
		for (SyncConf conf : syncConfigs) {
			while(!conf.isFinished()) {
				TimeCountDown.sleep(20);
				
				logger.info("Conf["+conf.getDesignation() + "] Still working...");
			}
		}
		
		
		//Wait forever
		/*for (SyncConf conf : syncConfigs) {
			while(conf.isFinished()) {
				TimeCountDown.sleep(20);
				
				logger.info("Conf["+conf.getDesignation() + "] Still working...");
			}
		}*/
		
		
		List<AbstractSyncController> allController = new ArrayList<AbstractSyncController>();
		
		//And now run all operations
		for (SyncConf conf : syncConfigs) {
			if (conf.isDestinationInstallationType()) {
				//allController.add(new SyncDataLoadController());
				//allController.get(allController.size() - 1).init(conf);
				
				allController.add(new SynchronizationController());
				allController.get(allController.size() - 1).init(conf);
			}
			else {
				allController.add(new SyncExportController());
				allController.get(allController.size() - 1).init(conf);
				
				//allController.add(new SyncTransportController());
				//allController.get(allController.size() - 1).init(conf);
			}
		}
		
		while(!isAllFinished(allController)) {
			TimeCountDown.sleep(10000);
		}
		
		allController.add(new DatabaseIntegrityConsolidationController());
		allController.get(allController.size() - 1).init(destinationConf);

		while(!isAllFinished(allController)) {
			TimeCountDown.sleep(10000);
		}
		
		allController.get(0).logInfo("ALL JOBS ARE FINISHED");
	}

	private static List<SyncConf>  loadSyncConfig(String[] synConfigFiles) throws IOException {
		List<SyncConf> syncConfigs = new ArrayList<SyncConf>(synConfigFiles.length);
		
		for (String confFile : synConfigFiles) {
			syncConfigs.add(SyncConf.loadFromFile(new File(confFile)));
		
		}
		
		return syncConfigs;
	}
	
	private static SyncConf  determineDestinationSyncConf(List<SyncConf> confs) throws IOException {
		SyncConf destinationConf = null;
		
		for (SyncConf conf : confs) {
			if(conf.isDestinationInstallationType()) {
				if (destinationConf != null) throw new ForbiddenOperationException("You must define only one destination file");
				
				destinationConf = conf;
			}
		}
		
		return destinationConf;
	}
	
	
	public static boolean isAllFinished(List<AbstractSyncController> controllers) {
		for (AbstractSyncController c : controllers) {
			if (!c.isFininished()) return false;
		}
		
		return true;
	}
}
