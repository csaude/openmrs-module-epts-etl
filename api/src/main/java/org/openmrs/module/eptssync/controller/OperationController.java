package org.openmrs.module.eptssync.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.monitor.ControllerStatusMonitor;
import org.openmrs.module.eptssync.monitor.EnginActivityMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class represent a controller of an synchronization operation. Eg. Export data from tables to JSON files.

 * @author jpboane
 *
 */

public abstract class OperationController implements Controller{
	protected Logger logger;
	
	private ProcessController processController;
	
	private List<EnginActivityMonitor> enginesActivititieMonitor;
	private ControllerStatusMonitor activitieMonitor;
	
	private OperationController child;
	
	private String controllerId;
	
	private int operationStatus;
	private boolean stopRequested;
	
	private SyncOperationConfig operationConfig;
	
	public OperationController(ProcessController processController, SyncOperationConfig operationConfig) {
		this.logger = Logger.getLogger(this.getClass());
		
		this.processController = processController;
		this.operationConfig = operationConfig;
		
		this.controllerId = processController.getControllerId() + "_" + getOperationType();	
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;	
	}
	
	public SyncOperationConfig getOperationConfig() {
		return operationConfig;
	}
	
	public OperationController getChild() {
		return child;
	}
	
	public void setChild(OperationController child) {
		this.child = child;
	}
	
	public ProcessController getProcessController() {
		return processController;
	}
	
	public boolean isParallelModeProcessing() {
		return this.getOperationConfig().isParallelModeProcessing();
	}
	
	public void init() {
		this.enginesActivititieMonitor = new ArrayList<EnginActivityMonitor>();
		List<SyncTableConfiguration> allSync = getProcessController().getConfiguration().getTablesConfigurations();
		
		if (isParallelModeProcessing()) {
			for (SyncTableConfiguration syncInfo: allSync) {
				initAndStartEngine(syncInfo);
			}
		
			if (this.child != null) {
				this.activitieMonitor = new ControllerStatusMonitor(this);
			
				ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.controllerId + "_MONIGTOR");
				executor.execute(this.activitieMonitor);
			}
			
			changeStatusToRunning();
		}
		else {
			for (SyncTableConfiguration syncInfo: allSync) {
				logInfo("Starting operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "'");
				
				Engine engine = initAndStartEngine(syncInfo);
				
				while (engine != null && !engine.isFinished()) {
					logInfo("The operation '" + getOperationType() + "' Is still working on table '" + syncInfo.getTableName() + "'");
					TimeCountDown.sleep(15);
				}
				
				logInfo("The operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "' is finished!");
			}
			
			changeStatusToFinished();
			
			onFinish();
		}
	}

	public String getControllerId() {
		return controllerId;
	}
	
	public List<EnginActivityMonitor> getEnginesActivititieMonitor() {
		return enginesActivititieMonitor;
	}
	
	@JsonIgnore
	public OpenConnection openConnection() {
		return processController.openConnection();
	}
	
	@JsonIgnore
	public CommonUtilities utilities() {
		return CommonUtilities.getInstance();
	}
	
	public void logInfo(String msg) {
		utilities().logInfo(msg, logger);
	}
	
	public void logError(String msg) {
		utilities().logErr(msg, logger);
	}
	
	public void logDebug(String msg) {
		utilities().logDebug(msg, logger);
	}

	protected Engine initAndStartEngine(SyncTableConfiguration syncInfo) {
		EnginActivityMonitor activitityMonitor = new EnginActivityMonitor(this, syncInfo);
		Engine engine = activitityMonitor.initEngine();
		
		if (getOperationConfig().isParallelModeProcessing() &&  mustRestartInTheEnd()) {
			ThreadPoolService.getInstance().createNewThreadPoolExecutor(getControllerId() + "_ENGINE_OPERATION_MONITOR").execute(activitityMonitor);
			
			this.enginesActivititieMonitor.add(activitityMonitor);
		}
		
		return engine;
	}
	
	@Override
	public String toString() {
		return this.controllerId;
	}
	
	public void executeChildOperation() {
		ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.child.getControllerId());
		executor.execute(this.child);
	}
	
	@Override
	public void run() {
		init();
	}
	
	
	@Override
	public TimeController getTimer() {
		return null;
	}
	
	@Override
	public boolean stopRequested() {
		return this.stopRequested;
	}

	public boolean isInitialized() {
		return this.operationStatus != MonitoredOperation.STATUS_NOT_INITIALIZED;
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
		if(isNotInitialized()) {
			return false;
		}
		
		
		if (isParallelModeProcessing()) {
			for (EnginActivityMonitor monitor : this.enginesActivititieMonitor) {
				Engine engine = monitor.getMainEngine();
				
				if (engine != null && !engine.isFinished()) {
						return false;
				}
			}
			
			return true;
		}
		else {
			return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
		}
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
		if (getChild() != null) {
			this.executeChildOperation();
		}
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return 15;
	}
	
	@JsonIgnore
	public abstract boolean mustRestartInTheEnd();
	
	@JsonIgnore
	public abstract String getOperationType();
	
	public abstract Engine initRelatedEngine(EnginActivityMonitor monitor, RecordLimits limits) ;

	public abstract long getMinRecordId(SyncTableConfiguration tableInfo);
	public abstract long getMaxRecordId(SyncTableConfiguration tableInfo);
	
	public void refresh() {
	}
}
