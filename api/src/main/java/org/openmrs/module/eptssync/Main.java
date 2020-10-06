package org.openmrs.module.eptssync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncConfig;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

public class Main {
	
	static Logger logger = Logger.getLogger(Main.class);
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static void main(String[] synConfigFiles) throws IOException {
		if (synConfigFiles == null || synConfigFiles.length == 0) throw new ForbiddenOperationException("You must especify the source/destination config file. Eg. /sync/conf.json");
	
		List<SyncConfig> syncConfigs = loadSyncConfig(synConfigFiles);
		
		if (countQtyDestination(syncConfigs) > 1) throw new ForbiddenOperationException("You must define only one destination file");
		
		//Performe database and classes adjustment
		for (SyncConfig conf : syncConfigs) {
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(conf.getDesignation()).execute(conf);
		}
		
		//Wait until all configuration been loaded
		for (SyncConfig conf : syncConfigs) {
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
		for (SyncConfig conf : syncConfigs) {
			for (SyncOperationConfig operation : conf.getOperations()) {
				if (!operation.isDisabled()) {
					List<AbstractSyncController> controllers = operation.generateRelatedController();
					
					for (AbstractSyncController controller : controllers) {
						allController.add(controller);
						
						ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId());
						executor.execute(controller);
					}
				}
			}
		}
		
		while(!isAllFinished(allController)) {
			TimeCountDown.sleep(10000);
		}
			
		logger.info("ALL JOBS ARE FINISHED");
	}

	private static List<SyncConfig>  loadSyncConfig(String[] synConfigFiles) throws ForbiddenOperationException, IOException {
		List<SyncConfig> syncConfigs = new ArrayList<SyncConfig>(synConfigFiles.length);
		
		for (String confFile : synConfigFiles) {
			SyncConfig conf = SyncConfig.loadFromFile(new File(confFile));
			
			conf.validate();
			
			syncConfigs.add(conf);
		
		}
		
		return syncConfigs;
	}
	
	private static int countQtyDestination (List<SyncConfig> confs) throws IOException {
		
		int i  = 0;;
		
		for (SyncConfig conf : confs) {
			if(conf.isDestinationInstallationType()) {
				i++;
			}
		}
		
		return i;
	}
	
	
	public static boolean isAllFinished(List<AbstractSyncController> controllers) {
		for (AbstractSyncController c : controllers) {
			if (!c.isFininished()) return false;
		}
		
		return true;
	}
}
