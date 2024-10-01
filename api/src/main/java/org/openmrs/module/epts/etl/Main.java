package org.openmrs.module.epts.etl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.DynamicProcessStarter;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.ProcessStarter;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class Main implements Runnable {
	
	static EptsEtlLogger logger = new EptsEtlLogger(Main.class);
	
	public static void main(String[] synConfigFiles) throws IOException, DBException {
		
		int i = 0;
		
		for (EtlConfiguration etlConfig : loadSyncConfig(synConfigFiles)) {
			ProcessStarter p;
			
			if (!etlConfig.isManualStart()) {
				i++;
				
				if (i > 1) {
					throw new ForbiddenOperationException("Currently not supported multiple Configuration files");
				}
				
				p = etlConfig.isDynamic() ? new DynamicProcessStarter(etlConfig) : new ProcessStarter(etlConfig);
				
				p.run();
			}
		}
	}
	
	public static List<EtlConfiguration> loadSyncConfig(File[] syncConfigFiles)
	        throws ForbiddenOperationException, IOException {
		String[] pathToFiles = new String[syncConfigFiles.length];
		
		for (int i = 0; i < syncConfigFiles.length; i++) {
			pathToFiles[i] = syncConfigFiles[i].getAbsolutePath();
		}
		
		return loadSyncConfig(pathToFiles);
	}
	
	public static List<EtlConfiguration> loadSyncConfig(String[] synConfigFiles) throws ForbiddenOperationException {
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
	
	public static void runSync(EtlConfiguration configuration) throws DBException {
		ProcessController controller = new ProcessController(null, configuration);
		ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId()).execute(controller);
	}
	
	@Override
	public void run() {
	}
}
