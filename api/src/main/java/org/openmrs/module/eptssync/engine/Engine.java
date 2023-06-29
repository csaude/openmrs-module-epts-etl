package org.openmrs.module.eptssync.engine;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncOperationType;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * Represent a Synchronization Engine. A Synchronization engine performes the task which will end up
 * producing or  consuming the synchronization info.
 * 
 * <p> There are several kinds of engines that performes diferents kind of operations. All the avaliable operations are listed in {@link SyncOperationType} enum
 * 
 * @author jpboane
 *
 */
public abstract class Engine implements Runnable, MonitoredOperation{
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected List<Engine> children;
	protected Engine parent;
	
	protected EngineMonitor monitor;
	
	protected SyncSearchParams<? extends SyncRecord> searchParams;
	
	private int operationStatus;
	private boolean stopRequested;
	
	private String engineId;

	private boolean newJobRequested;
	private Exception lastException;
	
	protected MigrationFinalCheckStatus finalCheckStatus;
	
	public Engine(EngineMonitor monitr, RecordLimits limits) {
		this.monitor = monitr;
		
		OpenConnection conn = openConnection();
		
		this.searchParams = initSearchParams(limits, conn);
		
		conn.markAsSuccessifullyTerminected();
		conn.finalizeConnection();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;	
		this.finalCheckStatus = MigrationFinalCheckStatus.NOT_INITIALIZED;
	}
	
	public AppInfo getDefaultApp() {
		return getRelatedOperationController().getDefaultApp();
	}
	
	public RecordLimits getLimits() {
		return getSearchParams().getLimits();
	}
	
	public int getQtyRecordsPerProcessing() {
		return monitor.getQtyRecordsPerProcessing();
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
	
	public SyncProgressMeter getProgressMeter_() {
		return  monitor.getProgressMeter();
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
	
		if (stopRequested()) {
			changeStatusToStopped();
			
			if (this.hasChild()) {
				for (Engine engine : this.getChildren()) {
					engine.requestStop();
				}
			}
		}
		else {
			
			if (getLimits() != null && !getLimits().isLoadedFromFile()) {
				RecordLimits saveLimits = retriveSavedLimits();
				
				if (saveLimits != null) {
					this.searchParams.setLimits(saveLimits);
				}
			}
			
			doRun();
		}
	}

	private void doRun() {
		
		while(isRunning()) {
			if (stopRequested()) {
				logWarn("STOP REQUESTED... STOPPING NOW");
				
				if (this.hasChild()) {
					for (Engine child : getChildren()) {
						while(!child.isStopped() || !child.isFinished()) {
							logWarn("WAITING FOR ALL CHILD ENGINES TO BE STOPPED");
							TimeCountDown.sleep(15);
						}
					}
				}
				
				this.changeStatusToStopped();
			}
			else {
				if (finalCheckStatus.onGoing()) {
					logInfo("PERFORMING FINAL CHECK...");
				}
				
				logDebug("SEARCHING NEXT MIGRATION RECORDS FOR TABLE '" + this.getSyncTableConfiguration().getTableName() + "'");
					
				OpenConnection conn = openConnection();
				
				boolean finished = false;
			
				try {
					int processedRecords_ = performe(conn);
					
					refreshProgressMeter(processedRecords_, conn);
					
					conn.markAsSuccessifullyTerminected();
					conn.finalizeConnection();
					
					reportProgress();
					
					if (getLimits() != null && getLimits().canGoNext()) {
						getLimits().moveNext(getQtyRecordsPerProcessing());
						getLimits().save();
					}
					else {
						if (getRelatedOperationController().mustRestartInTheEnd()) {
							this.requestANewJob();
						}
						else {
							if (this.isMainEngine() && this.hasChild() && finalCheckStatus.notInitialized()) {
								//Do the final check before finishing
								
								while(this.hasChild() && !isAllChildFinished()) {
									List<Engine> runningChild = getRunningChild();
									
									logDebug("WAITING FOR ALL CHILD FINISH JOB TO DO FINAL RECORDS CHECK! RUNNING CHILD ");
									logDebug(runningChild.toString());
									
									TimeCountDown.sleep(15);
								}
								
								if (mustDoFinalCheck()) {
									this.finalCheckStatus = MigrationFinalCheckStatus.ONGOING;
									
									this.resetLimits(null);
									
									logInfo("INITIALIZING FINAL CHECK...");
									
									doRun();
								} else {
									finished  = true;
									
									this.finalCheckStatus = MigrationFinalCheckStatus.IGNORED;
								}
							}
							else {
								logDebug("NO MORE '" + this.getSyncTableConfiguration().getTableName() + "' RECORDS TO " + getRelatedOperationController().getOperationType().name().toLowerCase() + " ON LIMITS [" + getLimits() + "]! FINISHING..." );
								
								if (this.finalCheckStatus.onGoing()) {
									this.finalCheckStatus = MigrationFinalCheckStatus.DONE;
								}
								
								if (isMainEngine()) {
									finished  = true;
								}
								else {
									this.markAsFinished();
								}
							}
						}
					}
				
					if (finished) markAsFinished();
				}
				catch (Exception e) {
					
					conn.finalizeConnection();
					
					reportError(e);
				}	
			}
		}
	}
	
	private List<Engine> getRunningChild() {
		if (!hasChild()) throw new ForbiddenOperationException("This Engine does not have child!!!");
		
		List<Engine> runningChild = new ArrayList<Engine>();
		
		for (Engine child : this.children) {
			if(child.isRunning()) {
				runningChild.add(child);
			}
		}
		
		return runningChild;
	}

	protected boolean mustDoFinalCheck() {
		return true;
	}

	private void reportError(Exception e) {
		e.printStackTrace();
		
		this.lastException = e;
		
		getRelatedOperationController().requestStopDueError(getMonitor(), e);
	}

	public Exception getLastException() {
		return lastException;
	}
	
	public  boolean isMainEngine() {
		return this.getParent() == null;
	}
	
	private int performe(Connection conn) throws DBException {
		if (getLimits() != null) {
			logDebug("SERCHING NEXT RECORDS FOR LIMITS " + getLimits());
		}
		else {
			logDebug("SERCHING NEXT RECORDS");
		}
		
		List<SyncRecord> records = searchNextRecords(conn);
		
		logDebug("SERCH NEXT MIGRATION RECORDS FOR TABLE '" + this.getSyncTableConfiguration().getTableName() + "' FINISHED. FOUND: '"+ utilities.arraySize(records) + "' RECORDS.");
		
		if (utilities.arrayHasElement(records)) {
			logDebug("INITIALIZING " +  getRelatedOperationController().getOperationType().name().toLowerCase() + " OF '" + records.size() + "' RECORDS OF TABLE '" + this.getSyncTableConfiguration().getTableName() + "'");
			
			performeSync(records, conn);
		}
		
		return utilities.arraySize(records);
	}

	public synchronized void refreshProgressMeter(int newlyProcessedRecords, Connection conn) throws DBException {
		this.monitor.refreshProgressMeter(newlyProcessedRecords, conn);
	}
	

	protected boolean hasChild() {
		return utilities.arrayHasElement(this.children);
	}

	private boolean hasParent() {
		return this.parent != null;
	}
	
	@Override
	public TimeController getTimer() {
		return monitor.getTimer();
	}
	
	@Override
	public boolean stopRequested() {
		return this.stopRequested;
	}
	
	@Override
	public boolean isNotInitialized() {
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (engine.isNotInitialized()) {
					return true;
				}
			}
		}

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
		//if (isFinished()) return true;
		
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (!engine.isStopped()) return false;
			}
		}

		return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		if (isNotInitialized()) return false;
		
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

		return this.operationStatus == MonitoredOperation.STATUS_SLEEPING;
	}

	@Override
	public void changeStatusToSleeping() {
		this.operationStatus = MonitoredOperation.STATUS_SLEEPING;
	}
	
	@Override
	public void changeStatusToRunning() {
		this.operationStatus = MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public void changeStatusToStopped() {
		this.operationStatus = MonitoredOperation.STATUS_STOPPED;	
		
		if (this.isMainEngine()) {
			SyncProgressMeter pm = this.getProgressMeter_();
			
			if (pm != null) {
				pm.changeStatusToStopped();
			}
		}
	}
	
	@Override
	public void changeStatusToFinished() {
		if (this.hasChild()) {
			for (Engine child : getChildren()) {
				while(!child.isFinished()) {
					logDebug("WAITING FOR ALL CHILD ENGINES TO BE FINISHED");
					TimeCountDown.sleep(10);
				}
			}
			
			this.operationStatus = MonitoredOperation.STATUS_FINISHED;	
		}
		else {
			this.operationStatus = MonitoredOperation.STATUS_FINISHED;	
		}
		
		if (isMainEngine()) {
			SyncProgressMeter pm = this.getProgressMeter_();
			
			if (pm != null) {
				pm.changeStatusToFinished();
			}
		}
	}
	
	@Override	
	public void changeStatusToPaused() {
		this.operationStatus = MonitoredOperation.STATUS_PAUSED;	
	
		throw new RuntimeException("Trying to pause engine " + getEngineId());
	}
	
	public void reportProgress() {
		this.monitor.reportProgress();
	}
	
	public void resetLimits(RecordLimits limits) {
		if (limits != null) {
			limits.setEngine(this);
		}
		
		getSearchParams().setLimits(limits);
	}
	
	public void requestANewJob() {
		this.newJobRequested = true;
		
		this.monitor.scheduleNewJobForEngine(this);
	}
	
	@Override
	public String toString() {
		return getEngineId() + " Limits [" + getSearchParams().getLimits() + "]";
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
	
	public synchronized void requestStopDueError() {
		if (this.hasChild()) {
			for (Engine engine : this.getChildren()) {
				engine.requestStop();
			}
		}

		this.stopRequested = true;

		if (lastException != null) {
			if (this.hasChild()) {
				for (Engine engine : this.getChildren()) {
					while(!engine.isStopped() && !engine.isFinished()) {
						logError("AN ERROR OCURRED... WAITING FOR ALL CHILD STOP TO REPORT THE ERROR END STOP THE OPERATION");
						
						TimeCountDown.sleep(5);
					}
				}
			}
			
			changeStatusToStopped(); 
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
					logDebug("THE ENGINE "+ getEngineId() + " IS WAITING FOR ALL CHILDREN FINISH TO TERMINATE THE OPERATION");
					TimeCountDown.sleep(15);
				}
			}
			
			getTimer().stop();
		}
		
		if (hasChild()) {
			for (Engine child : this.children) {
				ThreadPoolService.getInstance().terminateTread(getRelatedOperationController().getLogger(), getMonitor().getController().getProcessController().getLogLevel(), child.getEngineId(), this);
			}
		}
			
		ThreadPoolService.getInstance().terminateTread(getRelatedOperationController().getLogger(), getMonitor().getController().getProcessController().getLogLevel(), getEngineId(), this);	
	}
	
	public void markAsFinished(){
		if (!this.hasParent()) {
			if (hasChild()) {
				for (Engine child : this.children) {
					while(!child.isFinished()) {
						logDebug("WATING FOR ALL CHILDREN BEEN TERMINATED!");
						TimeCountDown.sleep(15);
					}
				}
			}
		
			tmp();
		}
		else changeStatusToFinished();
	}
	
	void tmp() {
		this.changeStatusToFinished();
	}
	
	public boolean isAllChildFinished(){
		if (!hasChild()) throw new ForbiddenOperationException("This Engine does not have child!!!");
		
		for (Engine child : this.children) {
			if(!child.isFinished()) return false;
		}
		
		return true;
	}
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return 5;
	}
	
	public void logError(String msg) {
		monitor.logErr(msg);
	}
	
	public void logInfo(String msg) {
		monitor.logInfo(msg);
	}
	
	public void logDebug(String msg) {
		monitor.logDebug(msg);
	}
	
	public void logWarn(String msg) {
		monitor.logWarn(msg);
	}
	
	
	protected RecordLimits retriveSavedLimits() {
		if (!getLimits().hasThreadCode()) getLimits().setThreadCode(this.getEngineId());
		
		logDebug("Retrieving saved limits for " + getLimits());
		
		RecordLimits savedLimits = RecordLimits.loadFromFile(new File(getLimits().generateFilePath()), this);
	
		if (savedLimits != null) {
			logDebug("Saved limits found [" + savedLimits + "]");
		}
		else {
			logDebug("No saved limits found for [" + getLimits() + "]");
		}
	
		return savedLimits;
	}
	
	protected abstract void restart();

	protected abstract SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn);

	//protected abstract SyncSearchParams<? extends SyncRecord> initSearchParams(Connection conn);

	public abstract void performeSync(List<SyncRecord> searchNextRecords, Connection conn) throws DBException;
	
	protected abstract List<SyncRecord> searchNextRecords(Connection conn) throws DBException;

}
