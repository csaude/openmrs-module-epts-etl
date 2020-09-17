package org.openmrs.module.eptssync.engine;

import java.util.List;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDownInitializer;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * Represent a Synchronization Engine. A Synchronization engine performe the task wich will endup
 * pruducing ou consumming the synchronization info.
 * <p> There are two types of engines: (1) the export engine wich generates the synchronization data from the origin site
 * (2) the import engine, wich retrieve data produced from export engine and reproduce this data in the destination data base
 * @author jpboane
 *
 */
public abstract class SyncEngine implements Runnable, MonitoredOperation, TimeCountDownInitializer{
	protected SyncTableInfo syncTableInfo;
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected List<SyncEngine> children;
	protected SyncEngine parent;
	
	protected AbstractSyncController syncController;
	protected SyncProgressMeter progressMeter;
	protected SyncSearchParams<? extends SyncRecord> searchParams;
	protected RecordLimits limits;
	
	private TimeController timer;
	
	private int operationStatus;
	private boolean stopRequested;
	
	public SyncEngine(SyncTableInfo syncTableInfo, RecordLimits limits, AbstractSyncController syncController) {
		this.syncTableInfo = syncTableInfo;
		
		this.syncController = syncController;
		
		this.limits = limits;
		
		this.searchParams = initSearchParams(limits);
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;	
	}
	
	public RecordLimits getLimits() {
		return limits;
	}
	
	public List<SyncEngine> getChildren() {
		return children;
	}
	
	public void setChildren(List<SyncEngine> children) {
		this.children = children;
	}
	
	public boolean isMultiProcessing() {
		return syncTableInfo.getQtyProcessingEngine() > 1;
	}
	
	public SyncSearchParams<? extends SyncRecord> getSearchParams() {
		return searchParams;
	}
	
	public SyncTableInfo getSyncTableInfo() {
		return syncTableInfo;
	}
	
	public SyncProgressMeter getProgressMeter() {
		return progressMeter;
	}
	
	public OpenConnection openConnection() {
		return DBConnectionService.getInstance().openConnection();
	}
	
	public SyncEngine getParent() {
		return parent;
	}
	
	public void setParent(SyncEngine parent) {
		this.parent = parent;
	}
	
	@Override
	public void run() {
		this.changeStatusToRunning();
		
		if (this.timer == null) {
			this.timer = new TimeController();
			this.timer.start();
		}
		
		initProgressMeter();
		
		reportProgress();
		
		while(isRunning()) {
			this.syncController.logInfo("SEARCHING NEXT MIGRATION RECORDS FOR TABLE '" + this.syncTableInfo.getTableName() + "'");
			
			List<SyncRecord> records = searchNextRecords();
			
			this.syncController.logInfo("SERCH NEXT MIGRATION RECORDS FOR TABLE '" + this.syncTableInfo.getTableName() + "' FINISHED.");
			
			this.syncController.logInfo("INITIALIZING SYNC OF '" + records.size() + "' RECORDS OF TABLE '" + this.syncTableInfo.getTableName() + "'");
			
			if (utilities.arrayHasElement(records)) {
				performeSync(records);
				
				refreshProgressMeter(records.size());
				
				reportProgress();
			}
			else {
				TimeCountDown t = new TimeCountDown(this, "No '" + this.syncTableInfo.getTableName() + "' records to export" , 18000);
				t.setIntervalForMessage(300);
				
				restart();
				
				t.run();
			}
		}
	}

	private synchronized void refreshProgressMeter(int newlyProcessedRecords) {
		this.progressMeter.refresh("RUNNING", this.progressMeter.getTotal(), this.getProgressMeter().getProcessed() + newlyProcessedRecords);
		
		if (this.hasParent()) {
			this.parent.refreshProgressMeter(newlyProcessedRecords);
		}
	}

	private synchronized void initProgressMeter()  {
		OpenConnection conn = openConnection();
		
		try {
			int remaining = this.searchParams.countNotProcessedRecords(conn);
			int total = this.searchParams.countAllRecords(conn);
			int processed = total - remaining;
			
			if (hasChild()) {
				for (SyncEngine child : this.children) {
					while (child.progressMeter == null) {
						TimeCountDown countDown = TimeCountDown.wait(this, 10, "WAINTING FOR CHILD PROGRESS METER TO BE CREATED");
						countDown.setIntervalForMessage(5);
						
						while (countDown.isInExecution()) {TimeCountDown.sleep(10);}
					}
					
					total += child.getProgressMeter().getTotal();
					remaining += child.getProgressMeter().getRemain();
					processed += child.getProgressMeter().getProcessed();
				}
			}
			
			this.progressMeter = new SyncProgressMeter(this, "INITIALIZING", total, processed);
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminected();
		}
	}
	
	private boolean hasChild() {
		return utilities.arrayHasElement(this.children);
	}

	private boolean hasParent() {
		return this.parent != null;
	}
	
	@Override
	public TimeController getTimer() {
		return this.timer;
	}

	@Override
	public void onFinish() {
		syncController.logInfo("FINISHED WAIT");
	}
	
	@Override
	public String getThreadNamingPattern() {
		return this.syncTableInfo.getTableName() + "_Sleep";
	}
	
	@Override
	public boolean stopRequested() {
		return this.stopRequested;
	}
	
	@Override
	public boolean isNotInitialized() {
		return this.operationStatus == MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	@Override
	public boolean isRunning() {
		return this.operationStatus == MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public boolean isStopped() {
		return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
	}
	
	@Override
	public boolean isPaused() {
		return this.operationStatus == MonitoredOperation.STATUS_PAUSED;
	}
	
	@Override
	public boolean isSleeping() {
		return this.operationStatus == MonitoredOperation.STATUS_SLEEPENG;
	}

	@Override
	public void changeStatusToRunning() {
		this.operationStatus = MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public void changeStatusToStopped() {
		this.operationStatus = MonitoredOperation.STATUS_STOPPED;		
	}
	
	@Override
	public void changeStatusToFinished() {
		this.operationStatus = MonitoredOperation.STATUS_FINISHED;	
	}
	
	@Override	
	public void changeStatusToPaused() {
		this.operationStatus = MonitoredOperation.STATUS_PAUSED;	
	}
	
	public void reportProgress() {
		//Report parent progress meter if exists
		SyncProgressMeter globalProgressMeter = this.progressMeter;
		
		if (this.hasParent()) {
			if (this.parent.progressMeter != null) {
				globalProgressMeter = this.parent.progressMeter;
			}
		}
		
		String log = "";
		
		log += syncTableInfo.getTableName() + " PROGRESS: ";
		log += "[TOTAL RECS: " + globalProgressMeter.getTotal() + ", ";
		log += "PROCESSED: " + globalProgressMeter.getDetailedProgress() + ", ";
		log += "REMAINING: " + globalProgressMeter.getDetailedRemaining() + ",";
		log += "TIME: " + globalProgressMeter.getTime() + "]";
		
		this.syncController.logInfo(log);
	}
	
	protected abstract void restart();

	protected abstract SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits);

	public abstract void performeSync(List<SyncRecord> searchNextRecords);
	
	protected abstract List<SyncRecord> searchNextRecords();

}
