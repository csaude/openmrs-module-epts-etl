package org.openmrs.module.epts.etl.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Level;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.Logger;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ProcessStarter implements ControllerStarter {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private boolean initialized;
	
	private String[] synConfigFilesPaths;
	
	private ProcessController currentController;
	
	private Logger logger;
	
	private static final String stringLock = new String("LOCK_STRING");
	
	public ProcessStarter(String[] synConfigFiles) {
		this.synConfigFilesPaths = synConfigFiles;
		
		org.apache.log4j.Logger log4jLogger = org.apache.log4j.Logger.getLogger(ProcessStarter.class);
		
		this.logger = new Logger(log4jLogger, SyncConfiguration.determineLogLevel());
	}
	
	public ProcessController getCurrentController() {
		return currentController;
	}
	
	public ProcessStarter(String[] synConfigFiles, org.apache.log4j.Logger logger) {
		this.synConfigFilesPaths = synConfigFiles;
		
		this.logger = new Logger(logger, SyncConfiguration.determineLogLevel());
	}
	
	public Level getLogLevel() {
		return this.logger.getLevel();
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * Initialize this starter. The initialization include the initialization of related
	 * controllers. Not that initialization doesn't start any process
	 * 
	 * @throws ForbiddenOperationException
	 * @throws IOException
	 * @throws DBException
	 */
	public void init() throws ForbiddenOperationException, DBException {
		if (this.initialized) {
			return;
		}
		
		synchronized (stringLock) {
			if (this.initialized) {
				return;
			}
			
			if (this.synConfigFilesPaths.length > 2) {
				throw new ForbiddenOperationException("The system currently doesn't support parallely processing");
			}
			
			List<SyncConfiguration> syncConfigs = loadSyncConfig(this.synConfigFilesPaths);
			
			if (countQtyDestination(syncConfigs) > 1)
				throw new ForbiddenOperationException("You must define only one destination file");
			
			for (SyncConfiguration conf : syncConfigs) {
				if (!conf.isAutomaticStart())
					continue;
				
				this.currentController = new ProcessController(this, conf);
			}
			
			this.initialized = true;
		}
		
	}
	
	@Override
	public void run() {
		
		try {
			if (getLogLevel().equals(Level.DEBUG)) {
				TimeCountDown.sleep(10);
			}
			
			init();
			
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.currentController.getControllerId())
			        .execute(this.currentController);
			
			while (!this.currentController.isFinalized()) {
				TimeCountDown.sleep(60);
				
				logger.logWarn("THE APPLICATION IS STILL RUNING...", 60 * 15);
			}
			
			if (this.currentController.isFinished()) {
				logger.logWarn("ALL JOBS ARE FINISHED");
			} else if (this.currentController.isStopped()) {
				logger.logWarn("ALL JOBS ARE STOPPED");
			}
		}
		catch (ForbiddenOperationException e) {
			throw e;
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
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
	
	public List<SyncConfiguration> loadSyncConfig(String[] synConfigFiles) throws ForbiddenOperationException {
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
				SyncConfiguration conf;
				
				try {
					conf = SyncConfiguration.loadFromFile(file);
					
					conf.validate();
					
					if (!conf.existsOnArray(syncConfigs)) {
						logger.logWarn("USING CONFIGURATION FILE " + conf.getRelatedConfFile().getAbsolutePath()
						        + " WITH PROCESS " + conf.getDesignation());
						syncConfigs.add(conf);
					} else
						throw new ForbiddenOperationException(
						        "The configuration [" + conf.getDesignation() + "] exists in more than one files");
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		return syncConfigs;
	}
	
	private int countQtyDestination(List<SyncConfiguration> confs) {
		
		int i = 0;
		
		for (SyncConfiguration conf : confs) {
			if (conf.isDataBaseMergeFromJSONProcess()) {
				i++;
			}
		}
		
		return i;
	}
}
