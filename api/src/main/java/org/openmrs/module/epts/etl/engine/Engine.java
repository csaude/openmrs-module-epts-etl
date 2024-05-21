package org.openmrs.module.epts.etl.engine;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.conf.EtlOperationType;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeController;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * Represent a Synchronization Engine. A Synchronization engine performes the task which will end up
 * producing or consuming the synchronization info.
 * <p>
 * There are several kinds of engines that performes diferents kind of operations. All the avaliable
 * operations are listed in {@link EtlOperationType} enum
 * 
 * @author jpboane
 */
public abstract class Engine implements Runnable, MonitoredOperation {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected List<Engine> children;
	
	protected Engine parent;
	
	protected EngineMonitor monitor;
	
	protected AbstractEtlSearchParams<? extends EtlObject> searchParams;
	
	private int operationStatus;
	
	private boolean stopRequested;
	
	private String engineId;
	
	private boolean newJobRequested;
	
	private Exception lastException;
	
	protected MigrationFinalCheckStatus finalCheckStatus;
	
	public Engine(EngineMonitor monitr, RecordLimits limits) {
		this.monitor = monitr;
		
		OpenConnection conn;
		try {
			conn = openConnection();
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		
		this.searchParams = initSearchParams(limits, conn);
		
		conn.markAsSuccessifullyTerminated();
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
	
	protected MigrationFinalCheckStatus getFinalCheckStatus() {
		return finalCheckStatus;
	}
	
	public EngineMonitor getMonitor() {
		return monitor;
	}
	
	public boolean mustRestartInTheEnd() {
		return getRelatedOperationController().mustRestartInTheEnd()
		        && !getRelatedOperationController().isParallelModeProcessing();
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
	
	public EtlOperationConfig getRelatedSyncOperationConfig() {
		return getRelatedOperationController().getOperationConfig();
	}
	
	public EtlConfiguration getRelatedSyncConfiguration() {
		return getRelatedOperationController().getConfiguration();
	}
	
	public List<Engine> getChildren() {
		return children;
	}
	
	public void setChildren(List<Engine> children) {
		this.children = children;
	}
	
	public AbstractEtlSearchParams<? extends EtlObject> getSearchParams() {
		return searchParams;
	}
	
	public EtlItemConfiguration getEtlConfiguration() {
		return monitor.getEtlConfiguration();
	}
	
	public String getMainSrcTableName() {
		return getMainSrcTableConf().getTableName();
	}
	
	public SrcConf getMainSrcTableConf() {
		return monitor.getSrcMainTableConf();
	}
	
	public EtlProgressMeter getProgressMeter_() {
		return monitor.getProgressMeter();
	}
	
	public OpenConnection openConnection() throws DBException {
		return getRelatedOperationController().openConnection();
	}
	
	public Engine getParentConf() {
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
		} else {
			
			if (getLimits() != null && !getLimits().isLoadedFromFile()) {
				retriveSavedLimits();
			}
			
			doRun();
		}
	}
	
	private void doRun() {
		
		while (isRunning()) {
			if (stopRequested()) {
				logWarn("STOP REQUESTED... TRYING TO STOP NOW");
				
				if (this.hasChild()) {
					
					boolean allStopped = false;
					
					while (!allStopped) {
						
						String runningThreads = "";
						
						for (Engine child : getChildren()) {
							if (!child.isStopped() && !child.isFinished()) {
								runningThreads = utilities.concatStringsWithSeparator(runningThreads, child.getEngineId(),
								    ";");
							}
						}
						
						if (utilities.stringHasValue(runningThreads)) {
							logWarn("WAITING FOR ALL CHILD ENGINES TO BE STOPPED", 60);
							logDebug("STILL RUNNING THREADS: " + runningThreads);
							
							TimeCountDown.sleep(10);
						} else {
							allStopped = true;
						}
					}
				}
				
				this.changeStatusToStopped();
			} else {
				if (finalCheckStatus.onGoing()) {
					logInfo("PERFORMING FINAL CHECK...");
				}
				
				logDebug(
				    "SEARCHING NEXT MIGRATION RECORDS FOR ETL CONFIG '" + this.getEtlConfiguration().getConfigCode() + "'");
				
				OpenConnection conn = null;
				
				boolean finished = false;
				
				try {
					conn = openConnection();
					
					int processedRecords_ = performe(conn);
					
					refreshProgressMeter(processedRecords_, conn);
					
					conn.markAsSuccessifullyTerminated();
					conn.finalizeConnection();
					
					reportProgress();
					
					if (getLimits() != null && getLimits().canGoNext()) {
						getLimits().moveNext(getQtyRecordsPerProcessing());
						getLimits().save();
					} else {
						if (getRelatedOperationController().mustRestartInTheEnd()) {
							this.requestANewJob();
						} else {
							if (this.isMainEngine() && finalCheckStatus.notInitialized()) {
								//Do the final check before finishing
								
								while (this.hasChild() && !isAllChildFinished()) {
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
									finished = true;
									
									this.finalCheckStatus = MigrationFinalCheckStatus.IGNORED;
								}
							} else {
								logDebug("NO MORE '" + this.getMainSrcTableConf().getTableName() + "' RECORDS TO "
								        + getRelatedOperationController().getOperationType().name().toLowerCase()
								        + " ON LIMITS [" + getLimits() + "]! FINISHING...");
								
								if (this.finalCheckStatus.onGoing()) {
									this.finalCheckStatus = MigrationFinalCheckStatus.DONE;
								}
								
								if (isMainEngine()) {
									finished = true;
								} else {
									this.markAsFinished();
								}
							}
						}
					}
					
					if (finished)
						markAsFinished();
				}
				catch (Exception e) {
					
					if (conn != null)
						conn.finalizeConnection();
					
					reportError(e);
				}
			}
		}
	}
	
	private List<Engine> getRunningChild() {
		if (!hasChild())
			throw new ForbiddenOperationException("This Engine does not have child!!!");
		
		List<Engine> runningChild = new ArrayList<Engine>();
		
		for (Engine child : this.children) {
			if (child.isRunning()) {
				runningChild.add(child);
			}
		}
		
		return runningChild;
	}
	
	protected boolean mustDoFinalCheck() {
		if (getRelatedSyncOperationConfig().skipFinalDataVerification()) {
			return false;
		} else {
			return true;
		}
	}
	
	private void reportError(Exception e) {
		e.printStackTrace();
		
		this.lastException = e;
		
		getRelatedOperationController().requestStopDueError(getMonitor(), e);
	}
	
	public Exception getLastException() {
		return lastException;
	}
	
	public boolean isMainEngine() {
		return this.getParentConf() == null;
	}
	
	private int performe(Connection conn) throws DBException {
		if (getLimits() != null) {
			logDebug("SERCHING NEXT RECORDS FOR LIMITS " + getLimits());
		} else {
			logDebug("SERCHING NEXT RECORDS");
		}
		
		List<EtlObject> records = searchNextRecords(conn);
		
		logDebug("SERCH NEXT MIGRATION RECORDS FOR ETL '" + this.getEtlConfiguration().getConfigCode() + "' ON TABLE '"
		        + getMainSrcTableConf().getTableName() + "' FINISHED. FOUND: '" + utilities.arraySize(records)
		        + "' RECORDS.");
		
		if (utilities.arrayHasElement(records)) {
			logDebug("INITIALIZING " + getRelatedOperationController().getOperationType().name().toLowerCase() + " OF '"
			        + records.size() + "' RECORDS OF TABLE '" + this.getMainSrcTableConf().getTableName() + "'");
			
			beforeSync(records, conn);
			
			performeSync(records, conn);
		}
		
		return utilities.arraySize(records);
	}
	
	private void beforeSync(List<EtlObject> records, Connection conn) {
		for (EtlObject rec : records) {
			if (rec instanceof EtlDatabaseObject) {
				((EtlDatabaseObject) rec).loadObjectIdData(getMainSrcTableConf());
			}
		}
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
				if (engine.isRunning())
					return true;
			}
		}
		
		return this.operationStatus == MonitoredOperation.STATUS_RUNNING;
	}
	
	@Override
	public boolean isStopped() {
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (!engine.isStopped())
					return false;
			}
		}
		
		return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		if (isNotInitialized())
			return false;
		
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (!engine.isFinished())
					return false;
			}
		}
		
		return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
	}
	
	@Override
	public boolean isPaused() {
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (!engine.isPaused())
					return false;
			}
		}
		
		return this.operationStatus == MonitoredOperation.STATUS_PAUSED;
	}
	
	@Override
	public boolean isSleeping() {
		if (utilities.arrayHasElement(this.children)) {
			for (Engine engine : this.children) {
				if (!engine.isSleeping())
					return false;
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
			EtlProgressMeter pm = this.getProgressMeter_();
			
			if (pm != null) {
				pm.changeStatusToStopped();
			}
		}
	}
	
	@Override
	public void changeStatusToFinished() {
		if (this.hasChild()) {
			for (Engine child : getChildren()) {
				while (!child.isFinished()) {
					logDebug("WAITING FOR ALL CHILD ENGINES TO BE FINISHED");
					TimeCountDown.sleep(10);
				}
			}
			
			this.operationStatus = MonitoredOperation.STATUS_FINISHED;
		} else {
			this.operationStatus = MonitoredOperation.STATUS_FINISHED;
		}
		
		if (isMainEngine()) {
			EtlProgressMeter pm = this.getProgressMeter_();
			
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
		} else {
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
					while (!engine.isStopped() && !engine.isFinished()) {
						logError(
						    "AN ERROR OCURRED... WAITING FOR ALL CHILD STOP TO REPORT THE ERROR END STOP THE OPERATION");
						
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
				while (!isFinished()) {
					logDebug(
					    "THE ENGINE " + getEngineId() + " IS WAITING FOR ALL CHILDREN FINISH TO TERMINATE THE OPERATION");
					TimeCountDown.sleep(15);
				}
			}
			
			getTimer().stop();
		}
		
		if (hasChild()) {
			for (Engine child : this.children) {
				ThreadPoolService.getInstance().terminateTread(getRelatedOperationController().getLogger(),
				    child.getEngineId(), this);
			}
		}
		
		ThreadPoolService.getInstance().terminateTread(getRelatedOperationController().getLogger(), getEngineId(), this);
	}
	
	public void markAsFinished() {
		if (!this.hasParent()) {
			if (hasChild()) {
				for (Engine child : this.children) {
					while (!child.isFinished()) {
						logDebug("WATING FOR ALL CHILDREN BEEN TERMINATED!");
						TimeCountDown.sleep(15);
					}
				}
			}
			
			tmp();
		} else
			changeStatusToFinished();
	}
	
	void tmp() {
		this.changeStatusToFinished();
	}
	
	public boolean isAllChildFinished() {
		if (!hasChild())
			throw new ForbiddenOperationException("This Engine does not have child!!!");
		
		for (Engine child : this.children) {
			if (!child.isFinished())
				return false;
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
	
	public void logWarn(String msg, long interval) {
		monitor.logWarn(msg, interval);
	}
	
	protected void retriveSavedLimits() {
		if (!getLimits().hasThreadCode())
			getLimits().setThreadCode(this.getEngineId());
		
		logDebug("Retrieving saved limits for " + getLimits());
		
		getLimits().tryToLoadFromFile(new File(getLimits().generateFilePath()), this);
		
		if (getLimits().isLoadedFromFile()) {
			logDebug("Saved limits found [" + getLimits() + "]");
		} else {
			logDebug("No saved limits found for [" + getLimits() + "]");
		}
	}
	
	public boolean writeOperationHistory() {
		return getRelatedSyncOperationConfig().writeOperationHistory();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Engine)) {
			return false;
		}
		
		Engine e = (Engine) obj;
		
		return this.getEngineId().equals(e.getEngineId());
	}
	
	protected abstract void restart();
	
	protected abstract AbstractEtlSearchParams<? extends EtlObject> initSearchParams(RecordLimits limits, Connection conn);
	
	public abstract void performeSync(List<EtlObject> records, Connection conn) throws DBException;
	
	protected abstract List<EtlObject> searchNextRecords(Connection conn) throws DBException;
	
}
