package org.openmrs.module.epts.etl.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.OperationProgressInfo;
import org.openmrs.module.epts.etl.model.ProcessProgressInfo;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.epts.etl.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeController;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
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
	
	private SyncConfiguration configuration;
	
	private int operationStatus;
	
	private List<OperationController> operationsControllers;
	
	private String controllerId;
	
	private ProcessProgressInfo progressInfo;
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private TimeController timer;
	
	private boolean progressInfoLoaded;
	
	protected List<AppInfo> appsInfo;
	
	private ProcessStarter starter;
	
	private boolean finalized;
	
	protected boolean selfTreadKilled;
	
	private ProcessInfo processInfo;
	
	private EptsEtlLogger logger;
	
	public ProcessController() {
		this.progressInfo = new ProcessProgressInfo(this);
	}
	
	public ProcessController(ProcessStarter starter, SyncConfiguration configuration) throws DBException {
		this();
		
		this.starter = starter;
		
		this.logger = new EptsEtlLogger(ProcessController.class);
		
		init(configuration);
	}
	
	@JsonIgnore
	public List<OperationController> getOperationsControllers() {
		return operationsControllers;
	}
	
	public ProcessProgressInfo getProgressInfo() {
		return progressInfo;
	}
	
	public ProcessInfo getProcessInfo() {
		return processInfo;
	}
	
	public OperationProgressInfo initOperationProgressMeter(OperationController operationController, Connection conn)
	        throws DBException {
		return this.progressInfo.initAndAddProgressMeterToList(operationController, conn);
	}
	
	public void init(File syncCongigurationFile) throws DBException {
		try {
			init(SyncConfiguration.loadFromFile(syncCongigurationFile));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void init(SyncConfiguration configuration) throws DBException {
		this.configuration = configuration;
		this.configuration.setRelatedController(this);
		this.appsInfo = configuration.getAppsInfo();
		this.processInfo = new ProcessInfo(getConfiguration());
		
		this.controllerId = configuration.generateControllerId();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		
		this.operationsControllers = new ArrayList<OperationController>();
		
		if (!this.isImportStageSchemaExists()) {
			this.createStageSchema();
		}
		
		if (!existInconsistenceInfoTable()) {
			generateInconsistenceInfoTable();
		}
		
		if (!existOperationProgressInfoTable()) {
			generateTableOperationProgressInfo();
		}
		
		OpenConnection conn = getDefaultApp().openConnection();
		
		try {
			for (SyncOperationConfig operation : configuration.getOperations()) {
				List<OperationController> controller = operation.generateRelatedController(this,
				    operation.getRelatedSyncConfig().getOriginAppLocationCode(), conn);
				
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
	}
	
	@Override
	public void finalize(Controller c) {
		c.killSelfCreatedThreads();
		
		List<OperationController> nextOperation = ((OperationController) c).getChildren();
		
		logDebug("TRY TO INIT NEXT OPERATION");
		
		//Remember, if one of multiple child is disabled, then all other children are disabled
		while (nextOperation != null && !nextOperation.isEmpty() && nextOperation.get(0).getOperationConfig().isDisabled()) {
			nextOperation = nextOperation.get(0).getChildren();
		}
		
		if (nextOperation != null) {
			if (!stopRequested()) {
				for (OperationController controller : nextOperation) {
					logDebug("STARTING NEXT OPERATION " + controller.getControllerId());
					
					ExecutorService executor = ThreadPoolService.getInstance()
					        .createNewThreadPoolExecutor(controller.getControllerId());
					executor.execute(controller);
				}
			} else {
				String nextOperations = "[";
				for (OperationController controller : nextOperation) {
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
	
	/*@JsonIgnore
	public ProcessController getChildController() {
		return childController;
	}*/
	
	@JsonIgnore
	public AppInfo getDefaultApp() {
		return getConfiguration().getMainApp();
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
		        getConfiguration().getSyncRootDirectory() + "/process_status/stop_requested_" + getControllerId() + ".info");
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
		
		if (utilities.arrayHasElement(this.operationsControllers)) {
			for (OperationController controller : this.operationsControllers) {
				if (controller.getOperationConfig().isDisabled()) {
					continue;
				} else if (!controller.isStopped() && !controller.isFinished()) {
					return false;
				} else {
					List<OperationController> children = controller.getChildren();
					
					while (children != null) {
						List<OperationController> grandChildren = null;
						
						for (OperationController child : children) {
							if (!child.isStopped() && !child.isFinished()) {
								return false;
							}
							
							if (child.getChildren() != null) {
								if (grandChildren == null)
									grandChildren = new ArrayList<OperationController>();
								
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
		if (this.operationStatus == STATUS_FINISHED)
			return true;
		
		if (utilities.arrayHasElement(this.operationsControllers)) {
			for (OperationController controller : this.operationsControllers) {
				if (controller.getOperationConfig().isDisabled()) {
					continue;
				} else if (!controller.isFinished()) {
					return false;
				} else {
					List<OperationController> children = controller.getChildren();
					
					while (children != null) {
						List<OperationController> grandChildren = null;
						
						for (OperationController child : children) {
							
							if (!child.isFinished() && !child.getOperationConfig().isDisabled()) {
								return false;
							}
							
							if (child.getChildren() != null) {
								if (grandChildren == null)
									grandChildren = new ArrayList<OperationController>();
								
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
		
		FileUtilities.write(fileName, "{\"stopRequestedAt\":"
		        + DateAndTimeUtilities.formatToMilissegundos(DateAndTimeUtilities.getCurrentDate()) + "\"}");
		
		if (isNotInitialized()) {
			changeStatusToStopped();
		} else if (utilities.arrayHasElement(this.operationsControllers)) {
			for (OperationController controller : this.operationsControllers) {
				controller.requestStop();
			}
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
			
			return;
		}
		
		boolean wasPreviouslyFinished = processIsAlreadyFinished();
		
		if (wasPreviouslyFinished && (!canBeReRun() || !reRunConditionsAreSatisfied())) {
			logWarn("THE PROCESS " + getControllerId().toUpperCase() + " WAS ALREADY FINISHED!!!");
			onFinish();
		} else {
			
			if (wasPreviouslyFinished) {
				performePreReRunActions();
			}
			
			OpenConnection conn = getDefaultApp().openConnection();
			
			try {
				initOperationsControllers(conn);
				conn.markAsSuccessifullyTerminated();
			}
			finally {
				conn.finalizeConnection();
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
	
	private void performePreReRunActions() {
		FileUtilities.removeFile(this.processInfo.generateProcessStatusFile());
		
		OpenConnection conn = openConnection();
		
		try {
			this.progressInfo = new ProcessProgressInfo(this);
			
			for (OperationController controller : this.operationsControllers) {
				controller.resetProgressInfo(conn);
			}
			
			FileUtilities.removeFile(this.getProcessInfo().generateProcessStatusFile());
			
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
		
		if (isDBReSyncProcess() || isDBQuickExportProcess()) {
			ProcessInfo processInfoOnDB = this.processInfo.tryToLoadFromFile();
			
			return !this.processInfo.equals(processInfoOnDB);
		}
		
		return false;
		
	}
	
	private boolean canBeReRun() {
		return isDBReSyncProcess() || isDBQuickExportProcess();
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
	
	public void initOperationsControllers(Connection conn) {
		for (OperationController controller : this.operationsControllers) {
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
		
		if (getConfiguration().getFinalizerClazz() != null) {
			Class[] parameterTypes = { ProcessController.class };
			
			try {
				Constructor<? extends ProcessFinalizer> a = getConfiguration().getFinalizerClazz()
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
			for (OperationController operationController : this.operationsControllers) {
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
		return this.processInfo.generateProcessStatusFile().exists();
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
		logger.debug(msg);
	}
	
	public void logInfo(String msg) {
		logger.info(msg);
	}
	
	public void logWarn(String msg) {
		logger.warn(msg);
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
	
	private void createStageSchema() {
		OpenConnection conn = getDefaultApp().openConnection();
		
		try {
			Statement st = conn.createStatement();
			
			if (DBUtilities.isMySQLDB(conn)) {
				st.addBatch("CREATE DATABASE " + getConfiguration().getSyncStageSchema());
			} else {
				st.addBatch("CREATE SCHEMA " + getConfiguration().getSyncStageSchema());
			}
			
			st.executeBatch();
			
			st.close();
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private boolean isImportStageSchemaExists() {
		OpenConnection conn = openConnection();
		
		try {
			return DBUtilities.isResourceExist(null, null, DBUtilities.RESOURCE_TYPE_SCHEMA,
			    getConfiguration().getSyncStageSchema(), conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	public boolean existInconsistenceInfoTable() {
		OpenConnection conn = openConnection();
		
		String schema = getConfiguration().getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = "inconsistence_info";
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminated();
			conn.finalizeConnection();
		}
	}
	
	public boolean existOperationProgressInfoTable() {
		OpenConnection conn = openConnection();
		
		String schema = getConfiguration().getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = "table_operation_progress_info";
		
		try {
			return DBUtilities.isResourceExist(schema, null, resourceType, tabName, conn);
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminated();
			conn.finalizeConnection();
		}
	}
	
	private void generateTableOperationProgressInfo() throws DBException {
		
		SyncConfiguration config = getConfiguration();
		
		OpenConnection conn = openConnection();
		
		try {
			String sql = "";
			
			sql += "CREATE TABLE " + config.getSyncStageSchema() + ".table_operation_progress_info (\n";
			sql += DBUtilities.generateTableAutoIncrementField("id", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("operation_id", 250, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("operation_name", 250, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("table_name", 100, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("record_origin_location_code", 100, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableDateTimeField("started_at", "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableDateTimeField("last_refresh_at", "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableIntegerField("total_records", "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableIntegerField("total_processed_records", "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("status", 50, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableTimeStampField("creation_date", conn) + ",\n";
			sql += DBUtilities.generateTableUniqueKeyDefinition(
			    config.getSyncStageSchema() + "_UNQ_OPERATION_ID".toLowerCase(), "operation_id", conn) + ",\n";
			sql += DBUtilities.generateTablePrimaryKeyDefinition("id", "table_operation_progress_info_pk", conn) + "\n";
			
			sql += ");\n";
			
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();
			
			st.close();
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void generateInconsistenceInfoTable() throws DBException {
		OpenConnection conn = openConnection();
		
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String sql = "";
		
		sql += "CREATE TABLE " + getConfiguration().getSyncStageSchema() + ".inconsistence_info (\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableIntegerField("record_id", notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("parent_table_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("parent_id", notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("default_parent_id", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("record_origin_location_code", 100, notNullConstraint, conn)
		        + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", "inconsistence_info_pk", conn);
		sql += ");";
		
		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();
			
			st.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.markAsSuccessifullyTerminated();
			conn.finalizeConnection();
		}
	}
}
