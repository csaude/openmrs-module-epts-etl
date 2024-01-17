package org.openmrs.module.epts.etl;

import java.io.IOException;

import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.ProcessStarter;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class Main implements Runnable {
	public static void main(String[] synConfigFiles) throws IOException, DBException {
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
