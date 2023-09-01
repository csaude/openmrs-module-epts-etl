package org.openmrs.module.eptssync;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.ProcessStarter;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class Main implements Runnable {
	
	public static void main(String[] synConfigFiles) throws IOException, DBException {
		BasicConfigurator.configure();
		
		ProcessStarter p = new ProcessStarter(synConfigFiles);
		
		p.run();
	}
	
	public static void runSync(SyncConfiguration configuration) throws DBException {
		ProcessController controller = new ProcessController(null, configuration);
		ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId()).execute(controller);
	}
	
	@Override
	public void run() {
	}
}
