package org.openmrs.module.epts.etl.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.slf4j.Logger;
import org.slf4j.event.Level;

public class ProcessStarter implements ControllerStarter {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private boolean initialized;
	
	private String[] synConfigFilesPaths;
	
	private ProcessController currentController;
	
	private EptsEtlLogger logger;
	
	private static final String stringLock = new String("LOCK_STRING");
	
	public ProcessStarter(String[] synConfigFiles) {
		this.synConfigFilesPaths = synConfigFiles;
		
		this.logger = new EptsEtlLogger(ProcessStarter.class);
	}
	
	public ProcessController getCurrentController() {
		return currentController;
	}
	
	public ProcessStarter(String[] synConfigFiles, Logger logger) {
		this.synConfigFilesPaths = synConfigFiles;
		
		this.logger = new EptsEtlLogger(logger);
	}
	
	public EptsEtlLogger getLogger() {
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
			
			List<EtlConfiguration> syncConfigs = loadSyncConfig(this.synConfigFilesPaths);
			
			for (EtlConfiguration conf : syncConfigs) {
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
			if (EptsEtlLogger.determineLogLevel().equals(Level.DEBUG)) {
				TimeCountDown.sleep(10);
			}
			
			init();
			
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.currentController.getControllerId())
			        .execute(this.currentController);
			
			while (!this.currentController.isFinalized()) {
				TimeCountDown.sleep(60);
				
				logger.warn("THE APPLICATION IS STILL RUNING...", 60 * 15);
			}
			
			if (this.currentController.isFinished()) {
				logger.warn("ALL JOBS ARE FINISHED");
			} else if (this.currentController.isStopped()) {
				logger.warn("ALL JOBS ARE STOPPED");
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
		
		if (c.isFinished()) {
			if (controller.getConfiguration().getChildConfigFilePath() != null) {
				try {
					EtlConfiguration childConfig = EtlConfiguration
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
				finally {
					controller.finalize();
				}
			} else {
				controller.finalize();
			}
		} else if (c.isStopped()) {
			logger.warn("THE APPLICATION IS STOPPING DUE STOP REQUESTED!");
			controller.finalize();
		}
		
	}
	
	public List<EtlConfiguration> loadSyncConfig(File[] syncConfigFiles) throws ForbiddenOperationException, IOException {
		String[] pathToFiles = new String[syncConfigFiles.length];
		
		for (int i = 0; i < syncConfigFiles.length; i++) {
			pathToFiles[i] = syncConfigFiles[i].getAbsolutePath();
		}
		
		return loadSyncConfig(pathToFiles);
	}
	
	public List<EtlConfiguration> loadSyncConfig(String[] synConfigFiles) throws ForbiddenOperationException {
		List<EtlConfiguration> syncConfigs = new ArrayList<EtlConfiguration>(synConfigFiles.length);
		
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
				EtlConfiguration conf;
				
				try {
					conf = EtlConfiguration.loadFromFile(file);
					
					conf.validate();
					
					if (!conf.existsOnArray(syncConfigs)) {
						logger.warn("USING CONFIGURATION FILE " + conf.getRelatedConfFile().getAbsolutePath()
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
}
