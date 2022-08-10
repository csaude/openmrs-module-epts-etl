package org.openmrs.module.eptssync;

import java.io.IOException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.ProcessStarter;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;

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
		
		ProcessStarter p = new ProcessStarter(synConfigFiles, logger);
		
		p.run();
	}
	
	

	
	public static void runSync(SyncConfiguration configuration) {
		ProcessController controller = new ProcessController(null, configuration);
		ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId()).execute(controller);
	}
	

	@Override
	public void run() {
	}
}
