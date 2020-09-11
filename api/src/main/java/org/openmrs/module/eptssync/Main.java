package org.openmrs.module.eptssync;

import org.openmrs.module.eptssync.controller.export.SynchronizationController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;

public class Main {
	
	public static void main(String[] args) {
		String dataBaseUserName = "root";
		String dataBaseUserPassword = "root";
		//String connectionURI = "jdbc:mysql://localhost:3307/openmrs_metadata?autoReconnect=true&sessionVariables=storage_engine=InnoDB&useUnicode=true&ch";
		String connectionURI = "jdbc:mysql://localhost:3307/openmrs_module_eptssync_test?autoReconnect=true&sessionVariables=storage_engine=InnoDB&useUnicode=true&ch";
		String driveClassName = "com.mysql.jdbc.Driver";
		
		DBConnectionService.init(driveClassName, connectionURI, dataBaseUserName, dataBaseUserPassword);
		
		//new SyncExportController().init();
	
		//new SyncDataLoadController().init();
		
		new SynchronizationController().init();
		
		while(true) {
			TimeCountDown.sleep(10000);
		}
	}
}
