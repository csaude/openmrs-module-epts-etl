package org.openmrs.module.eptssync.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class ProcessStarter implements ControllerStarter{
	private static Log logger = LogFactory.getLog(ProcessController.class);

	public static CommonUtilities utilities = CommonUtilities.getInstance();

	private String[] synConfigFilesPaths;
	private List<ProcessController> allController;
	private Level logLevel;

	public ProcessStarter(String[] synConfigFiles, Logger logger) {
		this.synConfigFilesPaths = synConfigFiles;
		this.allController = new ArrayList<ProcessController>();
		
		this.logLevel = SyncConfiguration.determineLogLevel();
	}
	
	public Level getLogLevel() {
		return logLevel;
	}
	
	public void run() throws IOException, DBException {
		
		if (getLogLevel().getName().equals(Level.FINE.getName())) {
			TimeCountDown.sleep(10);
		}
		
		List<SyncConfiguration> syncConfigs = loadSyncConfig(this.synConfigFilesPaths);

		if (countQtyDestination(syncConfigs) > 1) throw new ForbiddenOperationException("You must define only one destination file");

		for (SyncConfiguration conf : syncConfigs) {
			if (!conf.isAutomaticStart()) continue;
			
			ProcessController controller = new ProcessController(this, conf);
			
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId()).execute(controller);
			
			allController.add(controller);
				
			ProcessController childController = controller.getChildController();
			
			while (childController != null) {
				allController.add(childController);
			
				childController = childController.getChildController();
			}
		}
		
		while (!isAllFinished(allController) && !isAllStopped(allController)){
			TimeCountDown.sleep(120);
			
			logger.info("THE APPLICATION IS STILL RUNING...");
		}
		
		if (isAllFinished(allController)) {
			logger.info("ALL JOBS ARE FINISHED");
		}
		else
		if (isAllStopped(allController)) {
			logger.info("ALL JOBS ARE STOPPED");
		}
		
		System.exit(0);		
	}
	
	@Override
	public void finalize(Controller c) {
		//c.killSelfCreatedThreads();
		//ThreadPoolService.getInstance().terminateTread(logger, getLogLevel(), c.getControllerId(), c);
		
		ProcessController controller = (ProcessController)c;
		
		
		
		if (controller.getChildController() != null) {
			ProcessController child = controller.getChildController();
			
			while (child != null && child.getConfiguration().isDisabled()) {
				child = child.getChildController();
			}
			
			if (child != null) {
				ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(child.getControllerId());
				
				executor.execute(child);
			}
		}				
	}
	
	public  List<SyncConfiguration> loadSyncConfig(File[] syncConfigFiles) throws ForbiddenOperationException, IOException {
		String[] pathToFiles = new String[syncConfigFiles.length];
		
		
		for (int i = 0; i < syncConfigFiles.length; i++) {
			pathToFiles[i] = syncConfigFiles[i].getAbsolutePath();
		}
	
		return loadSyncConfig(pathToFiles);
	}
	
	public  List<SyncConfiguration> loadSyncConfig(String[] synConfigFiles) throws ForbiddenOperationException, IOException {
		List<SyncConfiguration> syncConfigs = new ArrayList<SyncConfiguration>(synConfigFiles.length);

		for (String confFile : synConfigFiles) {
			File file = new File(confFile);

			if (file.isDirectory()) {
				File[] files = file.listFiles();

				String[] paths = new String[files.length];

				for (int i = 0; i < files.length; i++) {
					paths[i] = files[i].getAbsolutePath();
				}

				syncConfigs.addAll(loadSyncConfig(paths));
			} else {
				SyncConfiguration conf = SyncConfiguration.loadFromFile(file);

				conf.validate();
					
				if (!conf.existsOnArray(syncConfigs)) {
					logger.info("USING CONFIGURATION FILE " + conf.getRelatedConfFile().getAbsolutePath() + " WITH PROCESS " + conf.getDesignation());
					syncConfigs.add(conf);
				} else
						throw new ForbiddenOperationException(
								"The configuration [" + conf.getDesignation() + "] exists in more than one files");

			}
		}

		return syncConfigs;
	}

	private  int countQtyDestination(List<SyncConfiguration> confs) throws IOException {

		int i = 0;
	
		for (SyncConfiguration conf : confs) {
			if (conf.isDataBaseMergeFromJSONProcess()) {
				i++;
			}
		}

		return i;
	}

	public  boolean isAllFinished(List<ProcessController> controllers) {
		for (ProcessController c : controllers) {
			if (!c.isFinished() || c.getChildController() != null && !c.getChildController().isFinished()) {
					return false;
			}
		}
		
		return true;
	}
	
	public  boolean isAllStopped(List<ProcessController> controllers) {
		for (ProcessController c : controllers) {
			if (!c.isStopped() || c.getChildController() != null && !c.getChildController().isStopped()) {
					return false;
			}
		}
		
		return true;
	}
	
}
