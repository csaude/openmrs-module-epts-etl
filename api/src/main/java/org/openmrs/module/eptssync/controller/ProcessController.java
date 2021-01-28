package org.openmrs.module.eptssync.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.OperationProgressInfo;
import org.openmrs.module.eptssync.model.ProcessProgressInfo;
import org.openmrs.module.eptssync.monitor.ControllerMonitor;
import org.openmrs.module.eptssync.utilities.ClassPathUtilities;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
	
	private String controllerId;
	private ProcessProgressInfo progressInfo;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	private static Log logger = LogFactory.getLog(ProcessController.class);
	
	private TimeController timer;
	private boolean progressInfoLoaded;
	
	public ProcessController(){
		this.progressInfo = new ProcessProgressInfo(this);
	}	
	
	public ProcessController(SyncConfiguration configuration){
		this();
		init(configuration);
	}
	
	public ProcessProgressInfo getProgressInfo() {
		return progressInfo;
	}
	
	public OperationProgressInfo initOperationProgressMeter(OperationController operationController) {
		return this.progressInfo.initAndAddProgressMeterToList(operationController);
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
		
		if (configuration.getChildConfig() != null) {
			this.childController = new ProcessController(configuration.getChildConfig());
		}
		
		this.controllerId = configuration.generateControllerId();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		
		ClassPathUtilities.tryToCopyPOJOToClassPath(this.configuration);
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
	public OpenConnection openConnection() {
		return this.getConfiguration().openConnetion();
	}
	
	@Override
	@JsonIgnore
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
		
		//OpenMRSPOJOGenerator.addToClasspath(getConfiguration().getPOJOCompiledFilesDirectory());
		//OpenMRSPOJOGenerator.tryToAddAllPOJOToClassPath(getConfiguration());
		
		ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.monitor.getMonitorId());
		executor.execute(this.monitor);
		
		tryToRemoveOldStopRequested();
		
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
			
			changeStatusToRunning();
			this.progressInfoLoaded = true;
		}
	}
	
	private void tryToRemoveOldStopRequested() {
		File file = new File (getConfiguration().getSyncRootDirectory()+"/process_status/stop_requested.info");
		
		if (file.exists()) file.delete();
	}

	public void initOperationsControllers(Connection conn){
		this.operationsControllers = new ArrayList<OperationController>();
		
		for (SyncOperationConfig operation : configuration.getOperations()) {
			OperationController controller = operation.generateRelatedController(this, conn);
			
			if (controller instanceof DestinationOperationController) {
				for (String appOriginCode : controller.getOperationConfig().getSourceFolders()) {
					this.operationsControllers.add(((DestinationOperationController)controller).cloneForOrigin(appOriginCode));
				}
			}
			else this.operationsControllers.add(controller);
		}
		
		for (OperationController controller : this.operationsControllers) {
			if (!controller.getOperationConfig().isDisabled()) {
				ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId());
				executor.execute(controller);
			}
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
		
		if (this.operationsControllers != null) {
			for (OperationController operationController : this.operationsControllers) {
				operationController.killSelfCreatedThreads();
				
				ThreadPoolService.getInstance().terminateTread(logger, operationController.getControllerId());
			}
		}
		
		ThreadPoolService.getInstance().terminateTread(logger, this.monitor.getMonitorId());
		ThreadPoolService.getInstance().terminateTread(logger, this.getControllerId());
	}
	
	@JsonIgnore
	private File generateProcessStatusFile() {
		String operationId = this.getControllerId();
		
		String subFolder = "";
		
		if (getConfiguration().isSourceInstallationType()) {
			subFolder = "source"; 
		}
		else {
			throw new ForbiddenOperationException("There is no status folder for destination operation");
		}
		
		String fileName = getConfiguration().getSyncRootDirectory() + FileUtilities.getPathSeparator() +  "process_status" + FileUtilities.getPathSeparator() + subFolder + FileUtilities.getPathSeparator() +  operationId;
		
		return new File(fileName);
	}
	
	public void markAsFinished() {
		logInfo("FINISHING PROCESS...");
		
		if (getConfiguration().isSourceInstallationType()) {
			if (!generateProcessStatusFile().exists()) {
				logInfo("FINISHING PROCESS... WRITING PROCESS STATUS ON FILE ["+ generateProcessStatusFile().getAbsolutePath() + "]") ;
				
				String desc = "";
				
				desc += "{\n";
				desc += "	processName: \"" + this.getControllerId() + "\",\n";
				desc += "	startTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(this.getTimer().getStartTime()) + "\",\n";
				desc += "	finishTime: \"" + DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(DateAndTimeUtilities.getCurrentDate()) + "\",\n";
				desc += "	elapsedTime: \"" + this.getTimer().getDuration(TimeController.DURACAO_IN_HOURS) + "\"\n";
				desc += "}";
				
				FileUtilities.tryToCreateDirectoryStructureForFile(generateProcessStatusFile().getAbsolutePath());
				
				FileUtilities.write(generateProcessStatusFile().getAbsolutePath(), desc);
				
				logInfo("FILE WROTE");
			}
		}
		
		/*ThreadPoolService.getInstance().terminateTread(logger, this.monitor.getMonitorId());
		ThreadPoolService.getInstance().terminateTread(logger, this.getControllerId());*/
		
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
		return  getConfiguration().isSourceInstallationType() ? generateProcessStatusFile().exists() : false; 
	}
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return 30;
	}

	@JsonIgnore
	public String getControllerId() {
		return this.controllerId;
	}

	@Override
	public void logInfo(String msg) {
		utilities.logInfo(msg, logger);
	}

	public boolean isProgressInfoLoaded() {
		return progressInfoLoaded;
	}
	
	public static ProcessController retrieveRunningThread(SyncConfiguration configuration) {
		String controllerId = configuration.generateControllerId();
		
		Thread runningThread = null;
		
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
}
