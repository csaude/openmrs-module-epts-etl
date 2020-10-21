package org.openmrs.module.eptssync.controller;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.monitor.ControllerStatusMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * The controller os whole synchronization process. This class uses {@link OperationController} to do the synchronization process
 * 
 * @author jpboane
 *
 */
public class ProcessController implements Controller{
	private SyncConfiguration configuration;
	private int operationStatus;
	private boolean stopRequested;
	private List<OperationController> operationsControllers;
	private ProcessController childController;
	private ControllerStatusMonitor monitor;
	
	private DBConnectionService connService;
	private String controllerId;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	private static Logger logger = Logger.getLogger(ProcessController.class);
	
	public ProcessController(SyncConfiguration configuration){
		this.configuration = configuration;
		
		
		if (configuration.getChildConfig() != null) {
			this.childController = new ProcessController(configuration.getChildConfig());
		}
		
		this.controllerId = configuration.getDesignation() + "_controller";
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	public SyncConfiguration getConfiguration() {
		return configuration;
	}
	
	public ProcessController getChildController() {
		return childController;
	}

	public OpenConnection openConnection() {
		if (connService == null) connService = DBConnectionService.init(configuration.getConnInfo());
		
		return connService.openConnection();
	}
	
	@Override
	public TimeController getTimer() {
		return null;
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
		if (utilities.arrayHasElement(this.operationsControllers)) {
			
			logInfo("STARTING CHECK IF PROCESS CONTROLLER IS FINICHED");
			
			for (OperationController controller : this.operationsControllers) {
				if (controller.getOperationConfig().isDisabled()) {
					continue;
				}
				else
				if (!controller.isFinished()) {
					logInfo("PROCESS CONTROLLER NOT FINISHED. REASON: OPERATION CONTROLLER " + controller.getControllerId() + " IS STILL NOT FINISHED....");
					
					return false;
				}
				else
				if (controller.getChild() != null && !controller.getChild().isFinished()) {
					logInfo("PROCESS CONTROLLER NOT FINISHED. REASON:  THE CHILD OPERATION CONTROLLER " + controller.getChild().getControllerId() + " IS STILL NOT FINISHED....");
					
					return false;
				}
			}
			
			logInfo("ALL PROCESS OPERATIONS ARE FINISHED!");
			
			return true;
			
			/*
			if (this.childController != null) {
				return this.childController.isFinished();
			}
			else {
				return true;
			}*/
		}
		
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
	public void requestStop() {	
	}

	@Override
	public void run() {
		this.operationStatus = MonitoredOperation.STATUS_RUNNING;
		
		OpenConnection conn = openConnection();
		
		try {
			initOperationsControllers(conn);
			conn.markAsSuccessifullyTerminected();
		}
		finally {
			conn.finalizeConnection();
		}
		
		if (this.childController != null) {
			this.monitor = new ControllerStatusMonitor(this);
		
			ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.controllerId + "_MONITOR");
			executor.execute(this.monitor);
		}
	}
	
	private void initOperationsControllers(Connection conn){
		this.operationsControllers = new ArrayList<OperationController>();
		
		for (SyncOperationConfig operation : configuration.getOperations()) {
			
			SyncOperationConfig operationToAdd = operation;
			
			while(operationToAdd != null && operationToAdd.isDisabled()) {
				if ( operationToAdd.getChild() != null) {
					operationToAdd = operationToAdd.getChild();
				}
				else operationToAdd = null;
			}
			
			if (operationToAdd != null) {
				List<OperationController> controllers = operationToAdd.generateRelatedController(this, conn);
	
				for (OperationController controller : controllers) {
					this.operationsControllers.add(controller);
					
					ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId());
					executor.execute(controller);
				}
			}
		}
		
		if (!utilities.arrayHasElement(this.operationsControllers)) {
			changeStatusToFinished();
		}
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
		if (this.childController != null) {
			if (this.childController != null) {
				ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.childController.getControllerId());
				executor.execute(this.childController);
			}
		}
	}

	@Override
	public int getWaitTimeToCheckStatus() {
		return 15;
	}
	
	public void forceFinish() {
		this.changeStatusToFinished();
	}

	public String getControllerId() {
		return this.controllerId;
	}

	@Override
	public void logInfo(String msg) {
		utilities.logInfo(msg, logger);
	}
}
