package org.openmrs.module.epts.etl.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.OperationProgressInfo;
import org.openmrs.module.epts.etl.model.ProcessProgressInfo;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeController;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The controller of the whole synchronization process. This class uses {@link OperationController}
 * to do the steps of sync process
 * 
 * @author jpboane
 */
public class ProcessController implements Controller, ControllerStarter {
	
	private EtlConfiguration configuration;
	
	private int operationStatus;
	
	private List<OperationController<? extends EtlDatabaseObject>> operationsControllers;
	
	private String controllerId;
	
	private ProcessProgressInfo progressInfo;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private TimeController timer;
	
	private boolean progressInfoLoaded;
	
	private ProcessStarter starter;
	
	private boolean finalized;
	
	protected boolean selfTreadKilled;
	
	private ProcessInfo processInfo;
	
	private EptsEtlLogger logger;
	
	private EtlDatabaseObject schemaInfoSrc;
	
	public ProcessController() {
		this.progressInfo = new ProcessProgressInfo(this);
	}
	
	public ProcessController(ProcessStarter starter, EtlConfiguration configuration) throws DBException {
		this();
		
		this.starter = starter;
		
		this.logger = new EptsEtlLogger(ProcessController.class);
		
		init(configuration);
	}
	
	public void setSchemaInfoSrc(EtlDatabaseObject schemaInfoSrc) {
		this.schemaInfoSrc = schemaInfoSrc;
	}
	
	@JsonIgnore
	public EtlDatabaseObject getSchemaInfoSrc() {
		return schemaInfoSrc;
	}
	
	@JsonIgnore
	public List<OperationController<? extends EtlDatabaseObject>> getOperationsControllers() {
		return operationsControllers;
	}
	
	public ProcessProgressInfo getProgressInfo() {
		return progressInfo;
	}
	
	public ProcessInfo getProcessInfo() {
		return processInfo;
	}
	
	public OperationProgressInfo initOperationProgressMeter(
	        OperationController<? extends EtlDatabaseObject> operationController, Connection conn) throws DBException {
		return this.progressInfo.initAndAddProgressMeterToList(operationController, conn);
	}
	
	public void init(File syncCongigurationFile) throws DBException {
		try {
			init(EtlConfiguration.loadFromFile(syncCongigurationFile));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void init(EtlConfiguration configuration) throws DBException {
		this.configuration = configuration;
		this.configuration.setRelatedController(this);
		this.processInfo = new ProcessInfo(getConfiguration());
		
		this.controllerId = configuration.generateProcessId();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		
		this.operationsControllers = new ArrayList<>();
		
		OpenConnection conn = openConnection();
		
		try {
			
			for (EtlOperationConfig operation : configuration.getOperations()) {
				List<OperationController<? extends EtlDatabaseObject>> controller = operation.generateRelatedController(this,
				    operation.getRelatedEtlConfig().getOriginAppLocationCode(), conn);
				
				this.operationsControllers.addAll(controller);
			}
			
			this.progressInfoLoaded = true;
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}
	
	public boolean isFinalized() {
		return finalized;
	}
	
	public void finalize() {
		setFinalized(true);
		
		getConfiguration().finalizeAllApps();
	}
	
	@Override
	public void finalize(Controller c) {
		c.killSelfCreatedThreads();
		
		List<OperationController<? extends EtlDatabaseObject>> nextOperation = ((OperationController<? extends EtlDatabaseObject>) c)
		        .getChildren();
		
		logDebug("TRY TO INIT NEXT OPERATION");
		
		//Remember, if one of multiple child is disabled, then all other children are disabled
		while (nextOperation != null && !nextOperation.isEmpty() && nextOperation.get(0).getOperationConfig().isDisabled()) {
			nextOperation = nextOperation.get(0).getChildren();
		}
		
		if (nextOperation != null) {
			if (!stopRequested()) {
				for (OperationController<? extends EtlDatabaseObject> controller : nextOperation) {
					logDebug("STARTING NEXT OPERATION " + controller.getControllerId());
					
					ExecutorService executor = ThreadPoolService.getInstance()
					        .createNewThreadPoolExecutor(controller.getControllerId());
					executor.execute(controller);
				}
			} else {
				String nextOperations = "[";
				for (OperationController<? extends EtlDatabaseObject> controller : nextOperation) {
					nextOperations += controller.getControllerId() + ";";
				}
				
				nextOperations += "]";
				
				logWarn("THE OPERATION " + nextOperations.toUpperCase()
				        + "NESTED COULD NOT BE INITIALIZED BECAUSE THERE WAS A STOP REQUEST!!!");
			}
		} else {
			logWarn("THERE IS NO MORE OPERATION TO EXECUTE... FINALIZING PROCESS... " + this.getControllerId());
		}
		
		getConfiguration().finalizeAllApps();
	}
	
	@JsonIgnore
	public EtlConfiguration getConfiguration() {
		return configuration;
	}
	
	public void setConfiguration(EtlConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@JsonIgnore
	public DBConnectionInfo getDefaultConnInfo() {
		return getConfiguration().getSrcConnInfo();
	}
	
	@JsonIgnore
	public DBConnectionInfo getDstConnInfo() {
		return getConfiguration().getDstConnInfo();
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
		return new File(
		        getConfiguration().getEtlRootDirectory() + "/process_status/stop_requested_" + getControllerId() + ".info");
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
		if (isNotInitialized())
			return false;
		
		if (utilities.listHasElement(this.operationsControllers)) {
			for (OperationController<? extends EtlDatabaseObject> controller : this.operationsControllers) {
				if (controller.getOperationConfig().isDisabled()) {
					continue;
				} else if (!controller.isStopped() && !controller.isFinished()) {
					return false;
				} else {
					List<OperationController<? extends EtlDatabaseObject>> children = controller.getChildren();
					
					while (children != null) {
						List<OperationController<? extends EtlDatabaseObject>> grandChildren = null;
						
						for (OperationController<? extends EtlDatabaseObject> child : children) {
							if (!child.isStopped() && !child.isFinished()) {
								return false;
							}
							
							if (child.getChildren() != null) {
								if (grandChildren == null)
									grandChildren = new ArrayList<>();
								
								for (OperationController<? extends EtlDatabaseObject> childOfChild : child.getChildren()) {
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
		if (this.operationStatus == STATUS_FINISHED)
			return true;
		
		if (utilities.listHasElement(this.operationsControllers)) {
			for (OperationController<? extends EtlDatabaseObject> controller : this.operationsControllers) {
				if (controller.getOperationConfig().isDisabled()) {
					continue;
				} else if (!controller.isFinished()) {
					return false;
				} else {
					List<OperationController<? extends EtlDatabaseObject>> children = controller.getChildren();
					
					while (children != null) {
						List<OperationController<? extends EtlDatabaseObject>> grandChildren = null;
						
						for (OperationController<? extends EtlDatabaseObject> child : children) {
							
							if (!child.isFinished() && !child.getOperationConfig().isDisabled()) {
								return false;
							}
							
							if (child.getChildren() != null) {
								if (grandChildren == null)
									grandChildren = new ArrayList<>();
								
								for (OperationController<? extends EtlDatabaseObject> childOfChild : child.getChildren()) {
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
		
		FileUtilities.write(fileName, "{\"stopRequestedAt\":"
		        + DateAndTimeUtilities.formatToMilissegundos(DateAndTimeUtilities.getCurrentDate()) + "\"}");
		
		if (isNotInitialized()) {
			changeStatusToStopped();
		} else if (utilities.listHasElement(this.operationsControllers)) {
			for (OperationController<? extends EtlDatabaseObject> controller : this.operationsControllers) {
				controller.requestStop();
			}
			
			for (OperationController<? extends EtlDatabaseObject> controller : this.operationsControllers) {
				while (!controller.isStopped()) {
					logger.warn("WAITING FOR PROCESS " + controller.getControllerId() + " TO STOP...", 120);
					TimeCountDown.sleep(5);
				}
			}
		}
		
		changeStatusToStopped();
	}
	
	@Override
	public void run() {
		this.timer = new TimeController();
		this.timer.start();
		
		tryToRemoveOldStopRequested();
		
		if (stopRequested()) {
			logWarn("THE PROCESS COULD NOT BE INITIALIZED DUE STOP REQUESTED!!!!");
			
			changeStatusToStopped();
			
			return;
		}
		
		boolean wasPreviouslyFinished = processIsAlreadyFinished();
		
		if (wasPreviouslyFinished && (!canBeReRun() || !reRunConditionsAreSatisfied())) {
			logWarn("THE PROCESS " + getControllerId().toUpperCase() + " WAS ALREADY FINISHED!!!");
			onFinish();
		} else {
			
			OpenConnection conn = null;
			
			try {
				if (wasPreviouslyFinished) {
					performePreReRunActions();
				}
				
				conn = getDefaultConnInfo().openConnection();
				
				initOperationsControllers(conn);
				conn.markAsSuccessifullyTerminated();
			}
			catch (DBException e) {
				throw new RuntimeException(e);
			}
			finally {
				if (conn != null) {
					conn.finalizeConnection();
				}
			}
			
			changeStatusToRunning();
			
			boolean running = true;
			
			while (running) {
				TimeCountDown.sleep(getWaitTimeToCheckStatus());
				
				this.logger.warn("The process " + getControllerId() + " is still running...", 60 * 5);
				
				if (this.isFinished()) {
					this.markAsFinished();
					this.onFinish();
					
					running = false;
				} else if (this.isStopped()) {
					running = false;
					
					this.onStop();
				}
			}
			
		}
		
	}
	
	private void performePreReRunActions() throws DBException {
		FileUtilities.removeFile(this.getProcessInfo().generateProcessStatusFile());
		
		FileUtilities.removeFile(this.getProcessInfo().generateProcessStatusFolder());
		
		OpenConnection conn = openConnection();
		
		try {
			this.progressInfo = new ProcessProgressInfo(this);
			
			for (OperationController<? extends EtlDatabaseObject> controller : this.operationsControllers) {
				controller.resetProgressInfo(conn);
			}
			
			FileUtilities.removeFile(this.getProcessInfo().generateProcessStatusFile());
			FileUtilities.removeFile(this.getProcessInfo().generateProcessStatusFolder());
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
		
	}
	
	/**
	 * Check if the conditions for this process to be re-run are satisfied.
	 * 
	 * @return true if the re-run conditions are satisfied
	 */
	public boolean reRunConditionsAreSatisfied() {
		if (!canBeReRun())
			return false;
		
		ProcessInfo processInfoOnDB = this.processInfo.tryToLoadFromFile();
		
		return !this.processInfo.equals(processInfoOnDB);
	}
	
	private boolean canBeReRun() {
		return getConfiguration().reRunable();
	}
	
	public boolean isDBReSyncProcess() {
		return getConfiguration().isDBReSyncProcess();
	}
	
	public boolean isDBQuickExportProcess() {
		return getConfiguration().isDBQuickExportProcess();
	}
	
	public boolean isDBQuickLoadProcess() {
		return getConfiguration().isDBQuickLoadProcess();
	}
	
	private void tryToRemoveOldStopRequested() {
		File file = generateStopRequestFile();
		
		if (file.exists())
			file.delete();
	}
	
	public void initOperationsControllers(Connection conn) throws DBException {
		for (OperationController<? extends EtlDatabaseObject> controller : this.operationsControllers) {
			if (!controller.getOperationConfig().isDisabled()) {
				ExecutorService executor = ThreadPoolService.getInstance()
				        .createNewThreadPoolExecutor(controller.getControllerId());
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
		logWarn("THE PROCESS " + getControllerId().toUpperCase() + " WAS STOPPED!!!");
		
		FileUtilities.removeFile(generateStopRequestFile().getAbsolutePath());
		
		this.starter.finalize(this);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void onFinish() {
		markAsFinished();
		
		if (getConfiguration().hasFinalizer()) {
			Class[] parameterTypes = { ProcessController.class };
			
			try {
				Constructor<? extends ProcessFinalizer> a = getConfiguration().getFinalizer().getFinalizerClazz()
				        .getConstructor(parameterTypes);
				
				ProcessFinalizer finalizer = a.newInstance(this);
				
				finalizer.performeFinalizationTasks();
			}
			catch (Exception e) {
				throw new ForbiddenOperationException(e);
			}
		}
		
		starter.finalize(this);
	}
	
	@Override
	public void killSelfCreatedThreads() {
		if (selfTreadKilled)
			return;
		
		if (this.operationsControllers != null) {
			for (OperationController<? extends EtlDatabaseObject> operationController : this.operationsControllers) {
				operationController.killSelfCreatedThreads();
				
				ThreadPoolService.getInstance().terminateTread(logger, operationController.getControllerId(),
				    operationController);
			}
		}
		
		selfTreadKilled = true;
	}
	
	@Override
	public void markAsFinished() {
		logDebug("FINISHING PROCESS...");
		
		if (!this.processInfo.generateProcessStatusFile().exists()) {
			logDebug("FINISHING PROCESS... WRITING PROCESS STATUS ON FILE ["
			        + this.processInfo.generateProcessStatusFile().getAbsolutePath() + "]");
			
			this.processInfo.save();
			
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
	public boolean processIsAlreadyFinished() {
		for (OperationController<? extends EtlDatabaseObject> controller : this.operationsControllers) {
			if (!controller.operationIsAlreadyFinished()) {
				return false;
			}
		}
		
		return true;
		
	}
	
	@Override
	public int getWaitTimeToCheckStatus() {
		return this.getConfiguration().getWaitTimeToCheckStatus();
	}
	
	@JsonIgnore
	public String getControllerId() {
		return this.controllerId;
	}
	
	public void logDebug(String msg) {
		logger.debug(msg);
	}
	
	public void logInfo(String msg) {
		logger.info(msg);
	}
	
	public void logWarn(String msg) {
		logger.warn(msg);
	}
	
	public void logTrace(String msg) {
		logger.trace(msg);
	}
	
	public void logWarn(String msg, long interval) {
		logger.warn(msg, interval);
	}
	
	public void logErr(String msg) {
		logger.error(msg);
	}
	
	public boolean isProgressInfoLoaded() {
		return progressInfoLoaded;
	}
	
	public static <T extends EtlDatabaseObject> ProcessController retrieveRunningThread(EtlConfiguration configuration) {
		String controllerId = configuration.generateProcessId();
		
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
	
	public OpenConnection openConnection() throws DBException {
		OpenConnection conn = getDefaultConnInfo().openConnection();
		
		if (getConfiguration().doNotResolveRelationship()) {
			DBUtilities.disableForegnKeyChecks(conn);
		}
		
		return conn;
	}
	
	public OpenConnection tryToOpenMainConnection() throws DBException {
		OpenConnection conn = getConfiguration().openMainConn();
		
		if (getConfiguration().doNotResolveRelationship()) {
			DBUtilities.disableForegnKeyChecks(conn);
		}
		
		return conn;
	}
	
	public OpenConnection tryToOpenDstConn() throws DBException {
		OpenConnection conn = null;
		
		if (getConfiguration().hasDstConnInfo()) {
			conn = getDstConnInfo().openConnection();
			
			if (getConfiguration().doNotResolveRelationship()) {
				DBUtilities.disableForegnKeyChecks(conn);
			}
		}
		
		return conn;
	}
	
}
