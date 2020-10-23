package org.openmrs.module.eptssync;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

public class Main {

	static Logger logger = Logger.getLogger(Main.class);

	public static CommonUtilities utilities = CommonUtilities.getInstance();

	public static void main(String[] synConfigFiles) throws IOException {
		List<SyncConfiguration> syncConfigs = loadSyncConfig(synConfigFiles);

		if (countQtyDestination(syncConfigs) > 1) throw new ForbiddenOperationException("You must define only one destination file");

		List<ProcessController> allController = new ArrayList<ProcessController>();

		for (SyncConfiguration conf : syncConfigs) {
			ProcessController controller = new ProcessController(conf);
			
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId()).execute(controller);
			
			allController.add(controller);
		}

		while (!isAllFinished(allController) && !isAllStoppedFinished(allController)){
			TimeCountDown.sleep(10);
		}
		
		if (isAllFinished(allController)) {
			logger.info("ALL JOBS ARE FINISHED");
		}
		else
		if (isAllStoppedFinished(allController)) {
			logger.info("ALL JOBS ARE STOPPED");
		}
		
	}

	private static List<SyncConfiguration> loadSyncConfig(String[] synConfigFiles) throws ForbiddenOperationException, IOException {
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

				if (conf.isAutomaticStart()) {
					if (!conf.existsOnArray(syncConfigs)) {
						
						logger.info("ADDED " + conf.getDesignation());
						syncConfigs.add(conf);
					} else
						throw new ForbiddenOperationException(
								"The configuration [" + conf.getDesignation() + "] exists in more than one files");
				}
				else {
					logger.info("NOT ADDED " + conf.getDesignation());
				}
			}
		}

		return syncConfigs;
	}

	private static int countQtyDestination(List<SyncConfiguration> confs) throws IOException {

		int i = 0;
	
		for (SyncConfiguration conf : confs) {
			if (conf.isDestinationInstallationType()) {
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
	
	/*
	public static File getProjectRoot() {
		return new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
	}
	*/
	
	public static File getPOJOSourceFilesDirectory() {
		Path root = Paths.get(".").normalize().toAbsolutePath();

		return new File(root.toFile().getAbsoluteFile() + "/src/main/java");
	}
	
	public static File getPOJOCompiledFilesDirectory() {
		Path root = Paths.get(".").normalize().toAbsolutePath();

		return new File(root.toFile().getAbsoluteFile() + "/target/classes");
	}
}
