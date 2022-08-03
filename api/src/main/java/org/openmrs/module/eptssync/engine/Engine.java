package org.openmrs.module.eptssync.engine;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
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
	
	protected SyncSearchParams<? extends SyncRecord> searchParams;
	
	private int operationStatus;
	private boolean stopRequested;
	
	private String engineId;

	private boolean newJobRequested;
	private Exception lastException;
	
	public Engine(EngineMonitor monitr, RecordLimits limits) {
		this.monitor = monitr;
		
		OpenConnection conn = openConnection();
		
		this.searchParams = initSearchParams(limits, conn);
		
		conn.markAsSuccessifullyTerminected();
		conn.finalizeConnection();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;	
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
	
	boolean finalCheckDone;
	
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
			OpenConnection conn = openConnection();
			
			try {
				
				if (!this.hasParent()) {
					monitor.doInitProgressMeterRefresh(this, conn);
				}
				
				conn.markAsSuccessifullyTerminected();
			} catch (DBException e) {
				reportError(e);
				
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
			finally {
				conn.finalizeConnection();
			}
			
			if (!getLimits().isLoadedFromFile()) {
				RecordLimits saveLimits = retriveSavedLimits();
				
				if (saveLimits != null) {
					this.searchParams.setLimits(saveLimits);
				}
			}
			
			while(isRunning()) {
				if (stopRequested()) {
					logInfo("STOP REQUESTED... STOPPING NOW");
					
					if (this.hasChild()) {
						for (Engine child : getChildren()) {
							while(!child.isStopped() || !child.isFinished()) {
								logInfo("WAITING FOR ALL CHILD ENGINES TO BE STOPPED");
								TimeCountDown.sleep(15);
							}
						}
					}
					
					this.changeStatusToStopped();
				}
				else {
					logInfo("SEARCHING NEXT MIGRATION RECORDS FOR TABLE '" + this.getSyncTableConfiguration().getTableName() + "'");
					
					conn = openConnection();
					
					boolean finished = false;
					
					try {
						int processedRecords_ = performe(conn);
						
						refreshProgressMeter(processedRecords_, conn);
						reportProgress();
						
						conn.markAsSuccessifullyTerminected();
					} catch (Exception e) {
						e.printStackTrace();
						
						reportError(e);
					}
					finally {
						conn.finalizeConnection();
					}
						
					if (getLimits().canGoNext()) {
						getLimits().moveNext(getQtyRecordsPerProcessing());
						getLimits().save();
					}
					else {
						if (getRelatedOperationController().mustRestartInTheEnd()) {
							this.requestANewJob();
						}
						else {
							if (this.isMainEngine() && this.hasChild() && !finalCheckDone) {
								//Do the final check before finishing
								
								while(this.hasChild() && !isAllChildFinished()) {
									List<Engine> runningChild = getRunningChild();
									
									logInfo("WAITING FOR ALL CHILD FINISH JOB TO DO FINAL RECORDS CHECK! RUNNING CHILD " + runningChild);
									
									TimeCountDown.sleep(10);
								}
								
								finalCheckDone = true;
								
								if (mustDoFinalCheck()) {
									this.resetLimits(null);
									
									run();
								} else {
									finished  = true;
								}
							}
							else {
								logInfo("NO MORE '" + this.getSyncTableConfiguration().getTableName() + "' RECORDS TO " + getRelatedOperationController().getOperationType().name().toLowerCase() + " ON LIMITS [" + getLimits() + "]! FINISHING..." );
								
								if (isMainEngine()) {
									finished  = true;
								}
								else this.markAsFinished();
								
							}
						}
					}
					
					
					if (finished) markAsFinished();
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
		this.lastException = e;
		
		getRelatedOperationController().requestStopDueError(getMonitor(), e);
	}

	public Exception getLastException() {
		return lastException;
	}
	
	protected  boolean isMainEngine() {
		return this.getParent() == null;
	}

	private int performe(Connection conn) throws DBException {
		logDebug("SERCHING NEXT RECORDS FOR LIMITS " + getLimits());
		
		List<SyncRecord> records = searchNextRecords(conn);
		
		logInfo("SERCH NEXT MIGRATION RECORDS FOR TABLE '" + this.getSyncTableConfiguration().getTableName() + "' FINISHED. FOUND: '"+ utilities.arraySize(records) + "' RECORDS.");
		
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
		
		SyncProgressMeter pm = this.getProgressMeter_();
		
		if (this.hasParent()) {
			pm = this.parent.getProgressMeter_();
		}
		
		if (pm != null) {
			pm.changeStatusToStopped();
		}
	}
	
	@Override
	public void changeStatusToFinished() {
		if (this.hasChild()) {
			for (Engine child : getChildren()) {
				while(!child.isFinished()) {
					logInfo("WAITING FOR ALL CHILD ENGINES TO BE FINISHED");
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
		limits.setEngine(this);
		
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
						logInfo("AN ERROR OCURRED... WAITING FOR ALL CHILD STOP TO REPORT THE ERROR END STOP THE OPERATION");
						
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
					logInfo("THE ENGINE "+ getEngineId() + " IS WAITING FOR ALL CHILDREN FINISH TO TERMINATE THE OPERATION");
					TimeCountDown.sleep(15);
				}
			}
			
			getTimer().stop();
		}
		
		if (hasChild()) {
			for (Engine child : this.children) {
				ThreadPoolService.getInstance().terminateTread(getRelatedOperationController().getLogger(), getMonitor().getController().getProcessController().getLogLevel(), child.getEngineId());
			}
		}
			
		ThreadPoolService.getInstance().terminateTread(getRelatedOperationController().getLogger(), getMonitor().getController().getProcessController().getLogLevel(), getEngineId());	
	}
	
	public void markAsFinished(){
		if (!this.hasParent()) {
			if (hasChild()) {
				for (Engine child : this.children) {
					while(!child.isFinished()) {
						logInfo("WATING FOR ALL CHILDREN BEEN TERMINATED!");
						TimeCountDown.sleep(15);
					}
				}
			}
		
			getRelatedOperationController().markTableOperationAsFinished(getSyncTableConfiguration(), this, getTimer());
			changeStatusToFinished();
		}
		else changeStatusToFinished();
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
		
		logInfo("Retrieving saved limits for " + getLimits());
		
		RecordLimits savedLimits = RecordLimits.loadFromFile(new File(getLimits().generateFilePath()), this);
	
		if (savedLimits != null) {
			logInfo("Saved limits found [" + savedLimits + "]");
		}
		else {
			logInfo("No saved limits found for [" + getLimits() + "]");
		}
	
		return savedLimits;
	}
	
	protected abstract void restart();

	protected abstract SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn);

	//protected abstract SyncSearchParams<? extends SyncRecord> initSearchParams(Connection conn);

	public abstract void performeSync(List<SyncRecord> searchNextRecords, Connection conn) throws DBException;
	
	protected abstract List<SyncRecord> searchNextRecords(Connection conn) throws DBException;

}
