package org.openmrs.module.eptssync.problems_solver.model.mozart;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.utilities.concurrent.BackgroundRunner;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.LongTransactionException;

public class MozartRuntaskWithTimeCheck extends BackgroundRunner {
	
	private List<SimpleValue> values;
	
	private MozartTaskType type;
	
	private String query;
	
	private Connection conn;
	
	private MozartRuntaskWithTimeCheck(MozartTaskType type, String query, Connection conn) {
		super("mozart_background_task" + query);
		
		this.type = type;
		this.query = query;
		this.conn = conn;
	}
	
	public static MozartRuntaskWithTimeCheck executeWithTimeCheck(MozartTaskType type, String query, int limitTimeInSecond,
	        Connection conn) throws LongTransactionException {
		
		MozartRuntaskWithTimeCheck runner = new MozartRuntaskWithTimeCheck(type, query, conn);
		runner.runInBackground();
		
		while (runner.isRunning()) {
			try {
				Thread.sleep(1000);
			}
			catch (InterruptedException e) {}
			
			if (runner.getTimer().getDuration(TimeController.DURACAO_IN_SECONDS) > limitTimeInSecond) {
				throw new LongTransactionException();
			}
		}
		
		
		return runner;
	}
	
	public List<SimpleValue> getValues() {
		return values;
	}
	
	@Override
	public void doRun() {
		
		try {
			if (type.isBatch()) {
				DBUtilities.executeBatch(conn, query);
				
				this.values = new ArrayList<SimpleValue>();
			} else if (type.isQuery()) {
				this.values = DatabaseObjectDAO.search(SimpleValue.class, query, null, conn);
			}
		}
		catch (Exception e) {}
	}
	
}
