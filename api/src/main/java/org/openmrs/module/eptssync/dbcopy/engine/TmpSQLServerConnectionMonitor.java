package org.openmrs.module.eptssync.dbcopy.engine;

import java.sql.SQLException;

import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class TmpSQLServerConnectionMonitor implements Runnable {
	
	OpenConnection conn;
	
	TimeController timer;
	Engine engine;
	
	public TmpSQLServerConnectionMonitor(OpenConnection conn, Engine engine) {
		this.conn = conn;
		this.timer = new TimeController();
		this.engine = engine;
	}
	
	@Override
	public void run() {
		this.timer.start();
		
		try {
			while (!conn.isClosed()) {
				if (conn.getCloseDate() != null) {
					engine.logWarn("Connection was gracefull closed " + this.timer.getDuration(TimeController.DURACAO_IN_SECONDS) + " Seconds");
					
					break;
				}
				
				engine.logInfo("Connection is still open for about " + this.timer.getDuration(TimeController.DURACAO_IN_SECONDS) + " Seconds");
				
				Thread.sleep(1000);
			}
			
			engine.logInfo("Connection Stopped after " + this.timer.getDuration(TimeController.DURACAO_IN_SECONDS) + " Seconds");
			
			this.timer.stop();
			
			this.timer = null;
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
