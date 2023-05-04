package org.openmrs.module.eptssync.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.model.OperationProgressInfo;
import org.openmrs.module.eptssync.model.ProcessProgressInfo;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The controller of the whole synchronization process. This class uses {@link OperationController} to do the steps of sync process
 * 
 * @author jpboane
 *
 */
public class ProcessController implements Controller, ControllerStarter{
	private SyncConfiguration configuration;
	private int operationStatus;
	private List<OperationController> operationsControllers;
	private ProcessController childController;
	
	private String controllerId;
	private ProcessProgressInfo progressInfo;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	private static Log logger = LogFactory.getLog(ProcessController.class);
	
	private TimeController timer;
	private boolean progressInfoLoaded;
	
	protected List<AppInfo> appsInfo; 
	
	private ProcessStarter starter;
	
	public ProcessController(){
		this.progressInfo = new ProcessProgressInfo(this);
	}	
	
	public Level getLogLevel() {
		return this.starter.getLogLevel();
	}

	public ProcessController(ProcessStarter starter, SyncConfiguration configuration){
		this();
		
		this.starter = starter;
		
		init(configuration);
	}
	
	public ProcessProgressInfo getProgressInfo() {
		return progressInfo;
	}
	
	public OperationProgressInfo initOperationProgressMeter(OperationController operationController, Connection conn) throws DBException {
		return this.progressInfo.initAndAddProgressMeterToList(operationController, conn);
	}
	
	public void init(File syncCongigurationFile) {
		try {
			init(SyncConfiguration.loadFromFile(syncCongigurationFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void init(SyncConfiguration configuration) {
		this.configuration = configuration;
		this.configuration.setRelatedController(this);
		this.appsInfo = configuration.getAppsInfo();
		
		if (configuration.getChildConfig() != null) {
			this.childController = new ProcessController(this.starter, configuration.getChildConfig());
		}
		
		this.controllerId = configuration.generateControllerId();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		
		this.operationsControllers = new ArrayList<OperationController>();
		
		OpenConnection conn = getDefaultApp().openConnection();
		
		try {
			for (SyncOperationConfig operation : configuration.getOperations()) {
				List<OperationController> controller = operation.generateRelatedController(this, operation.getRelatedSyncConfig().getOriginAppLocationCode(), conn);
				
				this.operationsControllers.addAll(controller);
			}
			
			this.progressInfoLoaded = true;
			
			conn.markAsSuccessifullyTerminected();
		} 
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public void finalize(Controller c) {
		c.killSelfCreatedThreads();
		
		ThreadPoolService.getInstance().terminateTread(logger, getLogLevel(), c.getControllerId(), c);
		
		List<OperationController> nextOperation = ((OperationController)c).getChildren();
		
		logDebug("TRY TO INIT NEXT OPERATION");
		
		//Remember, if one of multiple child is disabled, then all other children are disabled
		while (nextOperation != null && !nextOperation.isEmpty() && nextOperation.get(0).getOperationConfig().isDisabled()) {
			nextOperation = nextOperation.get(0).getChildren();
		}
		
		if (nextOperation != null) {
			if (!stopRequested()) {
				for (OperationController controller : nextOperation) {
					logDebug("STARTING NEXT OPERATION " + controller.getControllerId());
					
					ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId());
					executor.execute(controller);
				}
			}
			else {
				String nextOperations = "[";
				for (OperationController controller : nextOperation) {
					nextOperations += controller.getControllerId() + ";";
				}
				
				nextOperations += "]";
				
				logWarn("THE OPERATION " + nextOperations.toUpperCase() + "NESTED COULD NOT BE INITIALIZED BECAUSE THERE WAS A STOP REQUEST!!!");
			}
		}
		else {
			logWarn("THERE IS NO MORE OPERATION TO EXECUTE... FINALIZING PROCESS... "+this.getControllerId());
		}
	}
	
	@JsonIgnore
	public List<AppInfo> getAppsInfo() {
		return appsInfo;
	}
	
	
	@JsonIgnore
	public SyncConfiguration getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(SyncConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@JsonIgnore
	public ProcessController getChildController() {
		return childController;
	}

	@JsonIgnore
	public AppInfo getDefaultApp() {
		return  getConfiguration().getMainApp();
	}
	
	@Override
	@JsonIgnore
	public TimeController getTimer() {
		return this.timer;
	}

	@Override
	public boolean stopRequested() {
		return generateStopRequestFile().exists();
	}
	
	public File generateStopRequestFile() {
		return new File (getConfiguration().getSyncRootDirectory()+"/process_status/stop_requested_" + getControllerId() + ".info");
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
		if (isNotInitialized()) return false;
		
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
					List<OperationController> children = controller.getChildren();
						
					while(children != null) {
						List<OperationController> grandChildren = null;
						
						for (OperationController child : children) {
							if (!child.isStopped()) {
								return false;
							}
							
							if (child.getChildren() != null) {
								if (grandChildren == null) grandChildren = new ArrayList<OperationController>();
								
								for (OperationController childOfChild : child.getChildren()) {
									grandChildren.add(childOfChild);
								}
							}
						}
						
						children = grandChildren;
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
					List<OperationController> children = controller.getChildren();
					
					while(children != null) {
						List<OperationController> grandChildren = null;
						
						for(OperationController child : children) {
							
							if (!child.isFinished() && !child.getOperationConfig().isDisabled()) {
								return false;
							}
							
							if (child.getChildren() != null) {
								if (grandChildren == null) grandChildren = new ArrayList<OperationController>();
								
								for (OperationController childOfChild : child.getChildren()) {
									grandChildren.add(childOfChild);
								}
							}
						}
						
						children = grandChildren;
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
	public synchronized void requestStop() {
		String fileName = generateStopRequestFile().getAbsolutePath();
		
		FileUtilities.write(fileName, "{\"stopRequestedAt\":" + DateAndTimeUtilities.formatToMilissegundos(DateAndTimeUtilities.getCurrentDate()) + "\"}");
		
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
		
		tryToRemoveOldStopRequested();
		
		if (stopRequested()) {
			logWarn("THE PROCESS COULD NOT BE INITIALIZED DUE STOP REQUESTED!!!!");
			
			changeStatusToStopped();
			
			if (getChildController() != null) {
				getChildController().requestStop();
			}
		}
		else
		if (processIsAlreadyFinished()) {
			logWarn("THE PROCESS "+getControllerId().toUpperCase() + " WAS ALREADY FINISHED!!!");
			onFinish();
		}
		else {
			OpenConnection conn = getDefaultApp().openConnection();
		
			try {
				initOperationsControllers(conn);
				conn.markAsSuccessifullyTerminected();
			}
			finally {
				conn.finalizeConnection();
			}
			
			changeStatusToRunning();
		}
		
		boolean running = true;
		
		while(running) {
			TimeCountDown.sleep(getWaitTimeToCheckStatus());
			
			if (this.isFinished()) {
				this.markAsFinished();
				this.onFinish();
			
				running = false;
			}
			else 
			if (this.isStopped()) {
				running = false;
				
				this.onStop();
			}
		}				
	}
	
	private void tryToRemoveOldStopRequested() {
		File file = new File (getConfiguration().getSyncRootDirectory()+"/process_status/stop_requested.info");
		
		if (file.exists()) file.delete();
	}

	public void initOperationsControllers(Connection conn){
		for (OperationController controller : this.operationsControllers) {
			if (!controller.getOperationConfig().isDisabled()) {
				ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId());
				executor.execute(controller);
			}
		}
	}
	
	@Override
	public void onStart() {
		logInfo("STARTING PROCESS");
	}

	@Override
	public void onSleep() {
	}

	@Override
	public void onStop() {
		logWarn("THE PROCESS "+getControllerId().toUpperCase() + " WAS STOPPED!!!");
		
		FileUtilities.removeFile(generateStopRequestFile().getAbsolutePath());
		
		this.starter.finalize(this);
	}

	@Override
	public void onFinish() {
		markAsFinished();
		
		starter.finalize(this);
	}
	
	@Override
	public void killSelfCreatedThreads() {
		if (this.operationsControllers != null) {
			for (OperationController operationController : this.operationsControllers) {
				operationController.killSelfCreatedThreads();
				
				ThreadPoolService.getInstance().terminateTread(logger, getLogLevel(), operationController.getControllerId(), operationController);
			}
		}
	}
	
	public File generateProcessStatusFile() {
		String operationId = this.getControllerId();
		
		String fileName = generateProcessStatusFolder() + FileUtilities.getPathSeparator() +  operationId;
		
		return new File(fileName);
	}
	
	public String generateProcessStatusFolder() {
		String subFolder = "";
		
		if (getConfiguration().isSupposedToRunInOrigin()) {
			subFolder = "source";
		}
		else
		if (getConfiguration().isSupposedToRunInDestination()) {
			subFolder = "destination"; 
		}
		
		return getConfiguration().getSyncRootDirectory() + FileUtilities.getPathSeparator() +  "process_status" + FileUtilities.getPathSeparator()  + subFolder  + FileUtilities.getPathSeparator() + getConfiguration().getDesignation();
	}
	
	@Override
	public void markAsFinished() {
		logDebug("FINISHING PROCESS...");
		
		if (!generateProcessStatusFile().exists()) {
			logDebug("FINISHING PROCESS... WRITING PROCESS STATUS ON FILE ["+ generateProcessStatusFile().getAbsolutePath() + "]") ;
			
			String desc = "";
			
			desc += "{\n";
			desc += "	processName: \"" + this.getControllerId() + "\",\n";
			desc += "	startTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(this.getTimer().getStartTime()) + "\",\n";
			desc += "	finishTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(DateAndTimeUtilities.getCurrentDate()) + "\",\n";
			desc += "	elapsedTime: \"" + this.getTimer().getDuration(TimeController.DURACAO_IN_HOURS) + "\"\n";
			desc += "}";
			
			FileUtilities.tryToCreateDirectoryStructureForFile(generateProcessStatusFile().getAbsolutePath());
			
			FileUtilities.write(generateProcessStatusFile().getAbsolutePath(), desc);
			
			logDebug("FILE WROTE");
		}
	
		changeStatusToFinished();
	
		logInfo("THE PROCESS IS FINISHED...");
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		return this.controllerId;
	}

	@JsonIgnore
	private boolean processIsAlreadyFinished() {
		return generateProcessStatusFile().exists();
	}
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return 30;
	}

	@JsonIgnore
	public String getControllerId() {
		return this.controllerId;
	}
	
	public void logDebug(String msg) {
		utilities.logDebug(msg, logger, getLogLevel());
	}

	public void logInfo(String msg) {
		utilities.logInfo(msg, logger, getLogLevel());
	}
	
	public void logWarn(String msg) {
		utilities.logWarn(msg, logger, getLogLevel());
	}
		
	public void logErr(String msg) {
		utilities.logErr(msg, logger, getLogLevel());
	}
		
	public boolean isProgressInfoLoaded() {
		return progressInfoLoaded;
	}
	
	public static ProcessController retrieveRunningThread(SyncConfiguration configuration) {
		String controllerId = configuration.generateControllerId();
		
		//Thread runningThread = null;
		
	    for (Thread t : Thread.getAllStackTraces().keySet()) {
	        if (t.getName().equals(controllerId)) {
	        	t.getState();
	        	t.getThreadGroup();
	        	t.isAlive();
	        }
	    }
	    
	    //runningThread.getState()
	    
	    return null;
	}

	public OpenConnection openConnection() {
		return getDefaultApp().openConnection();
	}

}
