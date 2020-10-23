package org.openmrs.module.eptssync.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
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
public abstract class Engine implements Runnable, MonitoredOperation{
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected List<Engine> children;
	protected Engine parent;
	
	protected EngineMonitor monitor;
	
	protected SyncProgressMeter progressMeter;
	protected SyncSearchParams<? extends SyncRecord> searchParams;
	protected RecordLimits limits;
	
	private TimeController timer;
	
	private int operationStatus;
	private boolean stopRequested;
	
	private String engineId;

	private boolean newJobRequested;
	
	public Engine(EngineMonitor monitr, RecordLimits limits) {
		this.monitor = monitr;
		
		this.limits = limits;
		
		OpenConnection conn = openConnection();
		
		this.searchParams = initSearchParams(limits, conn);
		
		conn.markAsSuccessifullyTerminected();
		conn.finalizeConnection();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;	
	}
	
	public int getQtyRecordsPerProcessing() {
		return monitor.getController().getOperationConfig().getMaxRecordPerProcessing();
	}

	public EngineMonitor getMonitor() {
		return monitor;
	}
	
	public boolean mustRestartInTheEnd() {
		return getRelatedOperationController().mustRestartInTheEnd() && !getRelatedOperationController().isParallelModeProcessing();
	}

	public String getEngineId() {
		return engineId;
	}
	
	public void setEngineId(String engineId) {
		this.engineId = engineId;
	}
	
	public OperationController getRelatedOperationController() {
		return this.monitor.getController();
	}
	
	public RecordLimits getLimits() {
		return limits;
	}
	
	public List<Engine> getChildren() {
		return children;
	}
	
	public void setChildren(List<Engine> children) {
		this.children = children;
	}
	
	public SyncSearchParams<? extends SyncRecord> getSearchParams() {
		return searchParams;
	}
	
	public SyncTableConfiguration getSyncTableConfiguration() {
		return monitor.getSyncTableInfo();
	}
	
	public SyncProgressMeter getProgressMeter() {
		return progressMeter;
	}
	
	public OpenConnection openConnection() {
		return getRelatedOperationController().openConnection();
	}
	
	public Engine getParent() {
		return parent;
	}
	
	public void setParent(Engine parent) {
		this.parent = parent;
	}
	
	@Override
	public void run() {
		this.changeStatusToRunning();
		
		if (this.timer == null && !this.hasParent()) {
			this.timer = new TimeController();
			this.timer.start();
		}
		
		if (stopRequested()) {
			changeStatusToStopped();
			
			if (this.hasChild()) {
				for (Engine engine : this.getChildren()) {
					engine.requestStop();
				}
			}
		}
		
		else {
			OpenConnection conn = openConnection();
			
			try {
				initProgressMeter(conn);
				conn.markAsSuccessifullyTerminected();
			} catch (DBException e) {
				e.printStackTrace();
			
				throw new RuntimeException(e);
			}
			finally {
				conn.finalizeConnection();
			}
			
			while(isRunning()) {
				if (stopRequested()) {
					logInfo("STOP REQUESTED... STOPPING NOW");
					
					if (this.hasChild()) {
						for (Engine child : getChildren()) {
							while(!child.isStopped() || !child.isFinished()) {
								logInfo("WAITING FOR ALL CHILD ENGINES TO BE STOPPED");
								TimeCountDown.sleep(10);
							}
						}
					}
					
					this.changeStatusToStopped();
				}
				else {
					logInfo("SEARCHING NEXT MIGRATION RECORDS FOR TABLE '" + this.getSyncTableConfiguration().getTableName() + "'");
					
					conn = openConnection();
					
					try {
						int processedRecords = performe(conn);
						conn.markAsSuccessifullyTerminected();
						
						refreshProgressMeter(processedRecords, conn);
						reportProgress();
						
						if (processedRecords == 0) {
							if (getRelatedOperationController().mustRestartInTheEnd()) {
								this.requestANewJob();
							}
							else {
								getRelatedOperationController().logInfo("NO '" + this.getSyncTableConfiguration().getTableName() + "' RECORDS TO " + getRelatedOperationController().getOperationType() + "! FINISHING..." );
								
								changeStatusToFinished();
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						
						throw new RuntimeException(e);
					}
					finally {
						conn.finalizeConnection();
					}
				}
			}
		}
	}
	
	private int performe(Connection conn) throws DBException {
		List<SyncRecord> records = searchNextRecords(conn);
		
		this.monitor.logInfo("SERCH NEXT MIGRATION RECORDS FOR TABLE '" + this.getSyncTableConfiguration().getTableName() + "' FINISHED.");
		
		if (utilities.arrayHasElement(records)) {
			this.monitor.logInfo("INITIALIZING " +  getRelatedOperationController().getOperationType() + " OF '" + records.size() + "' RECORDS OF TABLE '" + this.getSyncTableConfiguration().getTableName() + "'");
			
			performeSync(records, conn);
		}
		
		return utilities.arraySize(records);
	}

	private synchronized void refreshProgressMeter(int newlyProcessedRecords, Connection conn) {
		if (this.hasParent()) {
			this.parent.refreshProgressMeter(newlyProcessedRecords, conn);
		}
		else this.progressMeter.refresh("RUNNING", this.progressMeter.getTotal(), this.getProgressMeter().getProcessed() + newlyProcessedRecords);
	}

	private synchronized void initProgressMeter(Connection conn) throws DBException  {
		if (this.hasParent()) return;
		
		int remaining = this.searchParams.countNotProcessedRecords(conn);
		int total = this.searchParams.countAllRecords(conn);
		int processed = total - remaining;
		
		/*if (hasChild()) {
			for (SyncEngine child : this.children) {
				while (child.progressMeter == null) {
					TimeCountDown countDown = TimeCountDown.wait(this, 10, "WAINTING FOR CHILD PROGRESS METER TO BE CREATED");
					countDown.setIntervalForMessage(5);
					
					while (countDown.isInExecution()) {TimeCountDown.sleep(10);}
				}
				
				remaining += child.getProgressMeter().getRemain();
				processed += child.getProgressMeter().getProcessed();
			}
		}*/
		
		this.progressMeter = new SyncProgressMeter(this, "INITIALIZING", total, processed);
	}
	
	protected boolean hasChild() {
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
	public boolean stopRequested() {
		return this.stopRequested;
	}
	
	@Override
	public boolean isNotInitialized() {
		//logInfo("CHECK IF ENGINE "+this.getEngineId() + " IS INITIALIZED. CURR STATUS "+ this.operationStatus);
		
		if (utilities.arrayHasElement(this.children)) {
			//logInfo("ENGINE STATUS "+this.getEngineId() + " CHILDREN STATUS: "+ this.operationStatus);
			
			for (Engine engine : this.children) {
				if (engine.isNotInitialized()) {
					//logInfo("CHILD ENGINE "+engine.getEngineId() + " STATUS " + engine.operationStatus);
					
					return true;
				}
			}
		}

		//logInfo("ENGINE STATUS "+this.getEngineId() + this.operationStatus);
		
		return this.operationStatus == MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	@Override
	public boolean isRunning() {
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (engine.isRunning()) return true;
			}
		}

		return this.operationStatus == MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public boolean isStopped() {
		if (isFinished()) return true;
		
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (!engine.isStopped()) return false;
			}
		}

		return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (!engine.isFinished()) return false;
			}
		}
		
		return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
	}
	
	@Override
	public boolean isPaused() {
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (!engine.isPaused()) return false;
			}
		}

		return this.operationStatus == MonitoredOperation.STATUS_PAUSED;
	}
	
	@Override
	public boolean isSleeping() {
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (!engine.isSleeping()) return false;
			}
		}

		return this.operationStatus == MonitoredOperation.STATUS_SLEEPENG;
	}

	@Override
	public void changeStatusToSleeping() {
		this.operationStatus = MonitoredOperation.STATUS_SLEEPENG;
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
	
		throw new RuntimeException("Trying to pause engine " + getEngineId());
	}
	
	public void reportProgress() {
		SyncProgressMeter globalProgressMeter = this.progressMeter;
		
		if (this.hasParent()) {
			globalProgressMeter = this.parent.progressMeter;
		}
		
		if (globalProgressMeter == null) return;
		
		String log = "";
		
		log += getSyncTableConfiguration().getTableName() + " PROGRESS: ";
		log += "[TOTAL RECS: " + globalProgressMeter.getTotal() + ", ";
		log += "PROCESSED: " + globalProgressMeter.getDetailedProgress() + ", ";
		log += "REMAINING: " + globalProgressMeter.getDetailedRemaining() + ",";
		log += "TIME: " + globalProgressMeter.getTime() + "]";
		
		this.monitor.logInfo(log);
	}
	
	public void resetLimits(RecordLimits limits) {
		getSearchParams().setLimits(limits);
	}
	
	public void requestANewJob() {
		this.newJobRequested = true;
		
		this.monitor.scheduleNewJobForEngine(this);
	}
	
	@Override
	public String toString() {
		return getEngineId() + " Limits [" + getLimits() + "]";
	}
	
	@Override
	public synchronized void requestStop() {
		if (isNotInitialized()) {
			changeStatusToStopped();
		}
		else
		if (!stopRequested()) {
			if (this.hasChild()) {
				for (Engine engine : this.getChildren()) {
					engine.requestStop();
				}
			}
			
			this.stopRequested = true;
		}
	}
	
	/**
	 * @return
	 */
	public boolean isNewJobRequested() {
		return newJobRequested;
	}
	
	public void setNewJobRequested(boolean newJobRequested) {
		this.newJobRequested = newJobRequested;
	}
	
	@Override
	public void onStart() {
	}

	@Override
	public void onSleep() {
	}

	@Override
	public void onStop() {
	}

	@Override
	public void onFinish() {
		if (!this.hasParent()) {
			
			if (this.hasChild()) {
				while(!isFinished()) {
					logInfo("THE ENGINE "+ getEngineId() + " IS WAITING FOR ALL CHILDREN FINISH TO TERMINATE THE OPERATION");
					TimeCountDown.sleep(15);
				}
			}
			
			getTimer().stop();
			
			markAsFinished();
		}
	}
	
	public void markAsFinished(){
		getRelatedOperationController().markTableOperationAsFinished(getSyncTableConfiguration(), this, getTimer());
	}
		
	@Override
	public int getWaitTimeToCheckStatus() {
		return 0;
	}
	
	public void logInfo(String msg) {
		getRelatedOperationController().logInfo(msg);
	}

	protected abstract void restart();

	protected abstract SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn);

	public abstract void performeSync(List<SyncRecord> searchNextRecords, Connection conn) throws DBException;
	
	protected abstract List<SyncRecord> searchNextRecords(Connection conn) throws DBException;

}
