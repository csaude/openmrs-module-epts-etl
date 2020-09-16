package org.openmrs.module.eptssync.engine;

import java.util.List;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDownInitializer;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * Represent a Synchronization Engine. A Synchronization engine performe the task wich will endup
 * pruducing ou consumming the synchronization info.
 * <p> There are two types of engines: (1) the export engine wich generates the synchronization data from the origin site
 * (2) the import engine, wich retrieve data produced from export engine and reproduce this data in the destination data base
 * @author jpboane
 *
 */
public abstract class SyncEngine implements Runnable, TimeCountDownInitializer{
	private SyncTableInfo syncTableInfo;
	
	private boolean running;
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected AbstractSyncController syncController;
	
	public SyncEngine(SyncTableInfo syncTableInfo, AbstractSyncController syncController) {
		this.syncTableInfo = syncTableInfo;
		
		this.syncController = syncController;
	}
	
	public SyncTableInfo getSyncTableInfo() {
		return syncTableInfo;
	}
	
	public OpenConnection openConnection() {
		return DBConnectionService.getInstance().openConnection();
	}
	
	@Override
	public void run() {
		this.running = true;
		
		while(isRunning()) {
			this.syncController.logInfo("SEARCHING NEXT MIGRATION RECORDS FOR TABLE '" + this.syncTableInfo.getTableName() + "'");
			
			List<SyncRecord> records = searchNextRecords();
			
			this.syncController.logInfo("SERCH NEXT MIGRATION RECORDS FOR TABLE '" + this.syncTableInfo.getTableName() + "' FINISHED.");
			
			this.syncController.logInfo("INITIALIZING SYNC OF '" + records.size() + "' RECORDS OF TABLE '" + this.syncTableInfo.getTableName() + "'");
			
			if (utilities.arrayHasElement(records)) {
				performeSync(records);
			}
			else {
				TimeCountDown t = new TimeCountDown(this, "No '" + this.syncTableInfo.getTableName() + "' records to export" , 18000);
				t.setIntervalForMessage(300);
				
				restart();
				
				t.run();
			}
		}
	}
	
	protected abstract void restart();

	@Override
	public void onFinish() {
	}
	
	@Override
	public String getThreadNamingPattern() {
		return this.syncTableInfo.getTableName() + "_Sleep";
	}
	
	public abstract void performeSync(List<SyncRecord> searchNextRecords);
	
	protected abstract List<SyncRecord> searchNextRecords();
	
	public boolean isRunning() {
		return running;
	}
}
