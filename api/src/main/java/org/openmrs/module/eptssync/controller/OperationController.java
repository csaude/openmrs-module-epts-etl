	package org.openmrs.module.eptssync.controller;

import java.io.File;
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
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

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
	private OperationController parent;
	
	public OperationController(ProcessController processController, SyncOperationConfig operationConfig) {
		this.logger = Logger.getLogger(this.getClass());
		
		this.processController = processController;
		this.operationConfig = operationConfig;
		
		this.controllerId = processController.getControllerId() + "_" + getOperationType();	
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;	
	}
	
	public OperationController getParent() {
		return parent;
	}
	
	public boolean hasParent() {
		return this.parent != null;
	}
	
	public boolean hasChild() {
		return this.child != null;
	}
	
	public boolean hasNestedController() {
		return hasChild() || hasParent();
	}
	
	public void setParent(OperationController parent) {
		this.parent = parent;
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
		if (operationIsAlreadyFinished()) {
			logInfo("THE OPERATION " + getControllerId() + " WAS ALREADY FINISHED!");
			
			changeStatusToFinished();
			onFinish();
		}
		else {
			this.enginesActivititieMonitor = new ArrayList<EnginActivityMonitor>();
			List<SyncTableConfiguration> allSync = getProcessController().getConfiguration().getTablesConfigurations();
			
			if (isParallelModeProcessing()) {
				for (SyncTableConfiguration syncInfo: allSync) {
					
					if (!operationTableIsAlreadyFinished(syncInfo)) {
						logInfo("INITIALIZING '" + getOperationType() + "' ENGINE FOR TABLE '" + syncInfo.getTableName() + "'");
						
						Engine engine = initAndStartEngine(syncInfo);
						
						if (engine != null) {
							logInfo("INITIALIZED '" + getOperationType() + "' ENGINE FOR TABLE '" + syncInfo.getTableName() + "'");
						}
						else {
							logInfo("NO ENGINE FOR '" + getOperationType() + "' FOR TABLE '" + syncInfo.getTableName() + "' WAS CREATED...");
						}
					}
					else {
						logInfo("The operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "' was already finished!");
					}
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
					
					if (!operationTableIsAlreadyFinished(syncInfo)) {
						logInfo("Starting operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "'");
						
						Engine engine = initAndStartEngine(syncInfo);
						
						while (engine != null && !engine.isFinished()) {
							logInfo("The operation '" + getOperationType() + "' Is still working on table '" + syncInfo.getTableName() + "'");
							TimeCountDown.sleep(15);
						}
						
						logInfo("The operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "' is finished!");
					}
					else logInfo("The operation '" + getOperationType() + "' On table '" + syncInfo.getTableName() + "' was already finished!");
					
				}
				
				changeStatusToFinished();
				
				//There is no controller monitor so do onFinish();
				onFinish();
			}
		}
	}

	private boolean operationTableIsAlreadyFinished(SyncTableConfiguration conf) {
		String operationId = this.getControllerId() + "_" + conf.getTableName();
		
		String fileName = getProcessController().getConfiguration().getSyncRootDirectory() + "/process_status/"+operationId;
		
		return new File(fileName).exists(); 
	}

	private boolean operationIsAlreadyFinished() {
		String operationId = this.getControllerId();
		
		String fileName = getProcessController().getConfiguration().getSyncRootDirectory() + "/process_status/"+operationId;
		
		return new File(fileName).exists(); 
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
		
		if (engine != null) {
			this.enginesActivititieMonitor.add(activitityMonitor);
			
			if (mustRestartInTheEnd()) {
				ThreadPoolService.getInstance().createNewThreadPoolExecutor(getControllerId() + "_ENGINE_OPERATION_MONITOR").execute(activitityMonitor);
			}
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
		
		if (isParallelModeProcessing() && this.enginesActivititieMonitor != null) {
			for (EnginActivityMonitor monitor : this.enginesActivititieMonitor) {
				Engine engine = monitor.getMainEngine();
				
				if (engine == null) throw new RuntimeException("No engine for minitor '" + monitor.getSyncTableInfo().getTableName() + "'");
				
				if (!engine.isFinished()) {
					return false;
				}
			}
			
			return true;
		}
		else {
			return this.operationStatus == MonitoredOperation.STATUS_FINISHED;
		}
	}
	
	
	public void markTableOperationAsFinished(SyncTableConfiguration conf) {
		String operationId = this.getControllerId() + "_" + conf.getTableName();
		
		String fileName = getProcessController().getConfiguration().getSyncRootDirectory() + "/process_status/"+operationId;
		
		logInfo("FINISHING OPERATION... WRITING OPERATION STATUS ON "+ fileName);
		
		String desc = "";
		
		desc += "{\n";
		desc += "	operationName: \"" + this.getControllerId() + "\",\n";
		desc += "	operationTable: \"" + conf.getTableName() + "\"\n";
		desc += "}";
		
		FileUtilities.write(fileName, desc);
		
		logInfo("FILE WROTE");
	}
	
	public void markOperationAsFinished() {
		String operationId = this.getControllerId();
		
		String fileName = getProcessController().getConfiguration().getSyncRootDirectory() + "/process_status/"+operationId;
		
		logInfo("FINISHING OPERATION... WRITING OPERATION STATUS ON "+ fileName);
		
		String desc = "";
		
		desc += "{\n";
		desc += "	operationName: \"" + this.getControllerId() + "\",\n";
		desc += "}";
		
		FileUtilities.write(fileName, desc);
		
		logInfo("FILE WROTE");
		
		changeStatusToFinished();
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
		markOperationAsFinished();
		
		logInfo("FINISHING OPERATION");
		OperationController nextOperation = getChild();
		
		while (nextOperation != null && nextOperation.getOperationConfig().isDisabled()) {
			nextOperation = nextOperation.getChild();
		}
		
		if (nextOperation != null) {
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
