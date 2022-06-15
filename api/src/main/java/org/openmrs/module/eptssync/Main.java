package org.openmrs.module.eptssync;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

public class Main implements Runnable{

	static Logger logger = Logger.getLogger(Main.class);

	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static void main(String[] synConfigFiles) throws IOException {
		BasicConfigurator.configure();

		/*int i = -3;
		
		while(i <= 3) {
			i++;
			
			System.out.println(i);
		
			TimeCountDown.sleep(1);
		}*/
		
		List<SyncConfiguration> syncConfigs = loadSyncConfig(synConfigFiles);

		if (countQtyDestination(syncConfigs) > 1) throw new ForbiddenOperationException("You must define only one destination file");

		List<ProcessController> allController = new ArrayList<ProcessController>();

		for (SyncConfiguration conf : syncConfigs) {
			if (!conf.isAutomaticStart()) continue;
			
			ProcessController controller = new ProcessController(conf);
			
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId()).execute(controller);
			
			allController.add(controller);
		}
		
		while (!isAllFinished(allController) && !isAllStoppedFinished(allController)){
			TimeCountDown.sleep(120);
		}
		
		if (isAllFinished(allController)) {
			logger.info("ALL JOBS ARE FINISHED");
		}
		else
		if (isAllStoppedFinished(allController)) {
			logger.info("ALL JOBS ARE STOPPED");
		}
		
		System.exit(0);
	}
	
	public static void runSync(SyncConfiguration configuration) {
		/*try {
			URL[] classPaths = new URL[] {configuration.getPOJOCompiledFilesDirectory().toURI().toURL()};
			
			URLClassLoader loader = URLClassLoader.newInstance(classPaths);
			
			Class<?> c = null;
			
			c = (Class<?>) loader.loadClass("org.openmrs.module.eptssync.controller.ProcessController");
			
	        loader.close();
	        
	        ProcessController p = (ProcessController) c.newInstance();
			p.init(configuration);
	        
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(p.getControllerId()).execute(p);
		} 
		catch (ClassNotFoundException e) {
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch blockq
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		ProcessController controller = new ProcessController(configuration);
		ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId()).execute(controller);
	}
	
	public static List<SyncConfiguration> loadSyncConfig(File[] syncConfigFiles) throws ForbiddenOperationException, IOException {
		String[] pathToFiles = new String[syncConfigFiles.length];
		
		
		for (int i = 0; i < syncConfigFiles.length; i++) {
			pathToFiles[i] = syncConfigFiles[i].getAbsolutePath();
		}
	
		return loadSyncConfig(pathToFiles);
	}
	
	public static List<SyncConfiguration> loadSyncConfig(String[] synConfigFiles) throws ForbiddenOperationException, IOException {
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

				//if (conf.isAutomaticStart()) {
					if (!conf.existsOnArray(syncConfigs)) {
						logger.info("FOUND CONFIGURATION FILE " + conf.getRelatedConfFile().getAbsolutePath() + " AND ADDED AS " + conf.getDesignation());
						syncConfigs.add(conf);
					} else
						throw new ForbiddenOperationException(
								"The configuration [" + conf.getDesignation() + "] exists in more than one files");
				//}
				//else {
					logger.info("FOUND CONFIGURATION FILE " + conf.getRelatedConfFile().getAbsolutePath() + " AS " + conf.getDesignation() + " BUT WON'T START");
				//}
			}
		}

		return syncConfigs;
	}

	private static int countQtyDestination(List<SyncConfiguration> confs) throws IOException {

		int i = 0;
	
		for (SyncConfiguration conf : confs) {
			if (conf.isDestinationSyncProcess()) {
				i++;
			}
		}

		return i;
	}

	public static boolean isAllFinished(List<ProcessController> controllers) {
		for (ProcessController c : controllers) {
			if (!c.isFinished() || c.getChildController() != null && !c.getChildController().isFinished()) {
					return false;
			}
		}
		
		return true;
	}
	
	public static boolean isAllStoppedFinished(List<ProcessController> controllers) {
		for (ProcessController c : controllers) {
			if (!c.isStopped() || c.getChildController() != null && !c.getChildController().isStopped()) {
					return false;
			}
		}
		
		return true;
	}

	@Override
	public void run() {
	}
	
	/*
	public static File getProjectRoot() {
		return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	}
	*/
	
	/*
	public static File getPOJOSourceFilesDirectory() {
		Path root = Paths.get(".").normalize().toAbsolutePath();

		return new File(root.toFile().getAbsoluteFile() + "/src/main/java");
	}
	
	public static File getPOJOCompiledFilesDirectory() {
		Path root = Paths.get(".").normalize().toAbsolutePath();

		return new File(root.toFile().getAbsoluteFile() + "/target/classes");
	}*/
	
}
