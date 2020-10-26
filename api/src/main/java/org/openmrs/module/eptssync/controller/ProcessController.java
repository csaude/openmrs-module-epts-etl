package org.openmrs.module.eptssync.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.monitor.ControllerMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * The controller os whole synchronization process. This class uses {@link OperationController} to do the synchronization process
 * 
 * @author jpboane
 *
 */
public class ProcessController implements Controller{
	private SyncConfiguration configuration;
	private int operationStatus;
	private List<OperationController> operationsControllers;
	private ProcessController childController;
	private ControllerMonitor monitor;
	
	private DBConnectionService connService;
	private String controllerId;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	private static Logger logger = Logger.getLogger(ProcessController.class);
	
	private TimeController timer;
	
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
		return this.timer;
	}

	@Override
	public boolean stopRequested() {
		return new File (getConfiguration().getSyncRootDirectory()+"/process_status/stop_requested.info").exists();
		
		//return this.stopRequested;
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
		if (isFinished()) return true;
		
		if (utilities.arrayHasElement(this.operationsControllers)) {
			for (OperationController controller : this.operationsControllers) {
				if (controller.getOperationConfig().isDisabled()) {
					continue;
				}
				else
				if (!controller.isStopped()) {
					return false;
				}
				else {
					OperationController child = controller.getChild() ;
					
					while(child != null) {
						if (!child.isStopped()) {
							return false;
						}
						
						child = child.getChild();
					}
				}
			}
			
			return true;
		}
		
		
		return this.operationStatus == MonitoredOperation.STATUS_STOPPED;
	}
	
	@Override
	public boolean isFinished() {
		if (utilities.arrayHasElement(this.operationsControllers)) {
			for (OperationController controller : this.operationsControllers) {
				if (controller.getOperationConfig().isDisabled()) {
					continue;
				}
				else
				if (!controller.isFinished()) {
					return false;
				}
				else {
					OperationController child = controller.getChild() ;
					
					while(child != null) {
						if (!child.isFinished() && !child.getOperationConfig().isDisabled()) {
							return false;
						}
						
						child = child.getChild();
					}
				}
			}
			
			return true;
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
		markAsFinished();
	}
	
	@Override	
	public void changeStatusToPaused() {
		this.operationStatus = MonitoredOperation.STATUS_PAUSED;	
	}

	@Override
	public synchronized void requestStop() {	
		if (isNotInitialized()) {
			changeStatusToStopped();
		}
		else
		if (utilities.arrayHasElement(this.operationsControllers)) {
			for (OperationController controller : this.operationsControllers) {
				controller.requestStop();
			}
		}
		
		if (getChildController() != null) {
			getChildController().requestStop();
		}
	}

	@Override
	public void run() {
		this.timer = new TimeController();
		this.timer.start();
	
		this.monitor = new ControllerMonitor(this);
		
		ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.controllerId + "_MONITOR");
		executor.execute(this.monitor);
		
		if (stopRequested()) {
			logInfo("THE PROCESS COULD NOT BE INITIALIZED DUE STOP REQUESTED!!!!");
			
			changeStatusToStopped();
			
			if (getChildController() != null) {
				getChildController().requestStop();
			}
		}
		else
		if (processIsAlreadyFinished()) {
			logInfo("THE PROCESS "+getControllerId().toUpperCase() + " WAS ALREADY FINISHED!!!");
			changeStatusToFinished();
		}
		else {
			OpenConnection conn = openConnection();
			
			try {
				initOperationsControllers(conn);
				conn.markAsSuccessifullyTerminected();
			}
			finally {
				conn.finalizeConnection();
			}
		}
		
		changeStatusToRunning();
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
			logInfo("NO OPERATION TO EXECUTE... FINISHING NOW!!!");
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
		logInfo("THE PROCESS "+getControllerId().toUpperCase() + " WAS STOPPED!!!");
	}

	@Override
	public void onFinish() {
		if (this.childController != null) {
			
			ProcessController child = this.childController;
			
			while (child != null && child.getConfiguration().isDisabled()) {
				child = child.getChildController();
			}
			
			if (child != null) {
				ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(child.getControllerId());
				executor.execute(child);
			}
		}
	}
	
	public void markAsFinished() {
		String operationId = this.getControllerId();
		
		String fileName = getConfiguration().getSyncRootDirectory() + "/process_status/"+operationId;
		
		if (!new File(fileName).exists()) {
			logInfo("FINISHING PROCESS... WRITING PROCESS STATUS ON "+ fileName);
			
			String desc = "";
			
			desc += "{\n";
			desc += "	processName: \"" + this.getControllerId() + "\",\n";
			desc += "	startTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(this.getTimer().getStartTime()) + "\",\n";
			desc += "	finishTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(DateAndTimeUtilities.getCurrentDate()) + "\",\n";
			desc += "	elapsedTime: \"" + this.getTimer().getDuration(TimeController.DURACAO_IN_HOURS) + "\"\n";
			desc += "}";
			
			FileUtilities.write(fileName, desc);
			
			logInfo("FILE WROTE");
		}
	}
	

	private boolean processIsAlreadyFinished() {
		String operationId = this.getControllerId();
		
		String fileName = getConfiguration().getSyncRootDirectory() + "/process_status/"+operationId;
		
		return new File(fileName).exists(); 
	}
	

	@Override
	public int getWaitTimeToCheckStatus() {
		return 5;
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
