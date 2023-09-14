package org.openmrs.module.eptssync.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.apache.commons.logging.LogFactory;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.Logger;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class ProcessStarter implements ControllerStarter {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String[] synConfigFilesPaths;
	
	private ProcessController currentController;
	
	private Logger logger;
	
	public ProcessStarter(String[] synConfigFiles) {
		this.synConfigFilesPaths = synConfigFiles;
		
		this.logger = new Logger(LogFactory.getLog(ProcessStarter.class), SyncConfiguration.determineLogLevel());
	}
	
	public Level getLogLevel() {
		return this.logger.getLevel();
	}
	
	public void run() throws IOException, DBException {
		
		if (getLogLevel().getName().equals(Level.FINE.getName())) {
			TimeCountDown.sleep(10);
		}
		
		List<SyncConfiguration> syncConfigs = loadSyncConfig(this.synConfigFilesPaths);
		
		if (countQtyDestination(syncConfigs) > 1)
			throw new ForbiddenOperationException("You must define only one destination file");
		
		if (syncConfigs.size() > 2) {
			throw new ForbiddenOperationException("The system currently doesn't support parallely processing");
		}
		
		for (SyncConfiguration conf : syncConfigs) {
			if (!conf.isAutomaticStart())
				continue;
			
			this.currentController = new ProcessController(this, conf);
			
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.currentController.getControllerId())
			        .execute(this.currentController);
		}
		
		while (!this.currentController.isFinalized()) {
			TimeCountDown.sleep(60);
			
			logger.logWarn("THE APPLICATION IS STILL RUNING...", 60 * 15);
		}
		
		if (this.currentController.isFinished()) {
			logger.logWarn("ALL JOBS ARE FINISHED");
		} else if (this.currentController.isStopped()) {
			logger.logWarn("ALL JOBS ARE STOPPED");
		}
		
		System.exit(0);
	}
	
	@Override
	public void finalize(Controller c) {
		c.killSelfCreatedThreads();
		
		ProcessController controller = (ProcessController) c;
		
		if (c.isStopped()) {
			logger.logWarn("THE APPLICATION IS STOPPING DUE STOP REQUESTED!");
			controller.finalize();
		} else if (controller.getConfiguration().getChildConfigFilePath() != null) {
			try {
				SyncConfiguration childConfig = SyncConfiguration
				        .loadFromFile(new File(controller.getConfiguration().getChildConfigFilePath()));
				
				ProcessController child = new ProcessController(this, childConfig);
				
				ExecutorService executor = ThreadPoolService.getInstance()
				        .createNewThreadPoolExecutor(child.getControllerId());
				
				executor.execute(child);
				
				ThreadPoolService.getInstance().terminateTread(logger, c.getControllerId(), c);
				
				this.currentController = child;
			}
			catch (DBException e) {
				throw new RuntimeException(e);
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			controller.finalize();
		}
	}
	
	public List<SyncConfiguration> loadSyncConfig(File[] syncConfigFiles) throws ForbiddenOperationException, IOException {
		String[] pathToFiles = new String[syncConfigFiles.length];
		
		for (int i = 0; i < syncConfigFiles.length; i++) {
			pathToFiles[i] = syncConfigFiles[i].getAbsolutePath();
		}
		
		return loadSyncConfig(pathToFiles);
	}
	
	public List<SyncConfiguration> loadSyncConfig(String[] synConfigFiles) throws ForbiddenOperationException, IOException {
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
					logger.logWarn("USING CONFIGURATION FILE " + conf.getRelatedConfFile().getAbsolutePath()
					        + " WITH PROCESS " + conf.getDesignation());
					syncConfigs.add(conf);
				} else
					throw new ForbiddenOperationException(
					        "The configuration [" + conf.getDesignation() + "] exists in more than one files");
				
			}
		}
		
		return syncConfigs;
	}
	
	private int countQtyDestination(List<SyncConfiguration> confs) throws IOException {
		
		int i = 0;
		
		for (SyncConfiguration conf : confs) {
			if (conf.isDataBaseMergeFromJSONProcess()) {
				i++;
			}
		}
		
		return i;
	}
}
