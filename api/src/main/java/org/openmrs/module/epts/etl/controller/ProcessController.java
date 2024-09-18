package org.openmrs.module.epts.etl.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.OperationProgressInfo;
import org.openmrs.module.epts.etl.model.ProcessProgressInfo;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
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
	
	public ProcessController() {
		this.progressInfo = new ProcessProgressInfo(this);
	}
	
	public ProcessController(ProcessStarter starter, EtlConfiguration configuration) throws DBException {
		this();
		
		this.starter = starter;
		
		this.logger = new EptsEtlLogger(ProcessController.class);
		
		init(configuration);
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
		
		this.controllerId = configuration.generateControllerId();
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
		
		this.operationsControllers = new ArrayList<>();
		
		if (!this.isImportStageSchemaExists()) {
			this.createStageSchema();
		}
		
		if (!existInconsistenceInfoTable()) {
			createInconsistenceInfoTable();
		}
		
		if (!existOperationProgressInfoTable()) {
			createTableOperationProgressInfo();
		}
		
		if (!existsDefaultGeneratedObjectKeyTable()) {
			createDefaultGeneratedObjectKeyTable();
		}
		
		if (!existEtlRecordErrorTable()) {
			createEtlRecordErrorTable();
		}
		
		if (!existsSkippedRecordsTable()) {
			createSkippedRecordsTable();
		}
		
		OpenConnection conn = getDefaultConnInfo().openConnection();
		
		if (getConfiguration().hasDstConnInfo()) {
			
			//Try to openConnection to determine if db schama exists
			boolean dstDbExists = false;
			
			try {
				OpenConnection dstConn = configuration.openDstConn();
				dstConn.finalizeConnection();
				
				dstDbExists = true;
			}
			catch (DBException e) {
				if (DBUtilities.determineDataBaseFromException(e).equals(DBUtilities.MYSQL_DATABASE)) {
					if (!DBException.checkIfExceptionContainsMessage(e, "Unknown database")) {
						throw e;
					}
				} else
					throw e;
			}
			
			if (!dstDbExists) {
				
				String databaseName = getDstConnInfo().determineSchema();
				
				if (!DBUtilities.isSameDatabaseServer(getDefaultConnInfo().getConnectionURI(),
				    getDstConnInfo().getConnectionURI())) {
					throw new ForbiddenOperationException("The database '" + databaseName
					        + "' does not exists and the application cannot connect to the related database to automcatically create it!");
				}
				
				if (getDstConnInfo().getDatabaseSchemaPath() != null) {
					DBUtilities.createDatabaseSchema(databaseName, conn);
					
					OpenConnection dstConn = null;
					
					try {
						dstConn = getDstConnInfo().openConnection();
						
						DBUtilities.executeSqlScript(dstConn, getDstConnInfo().getDatabaseSchemaPath());
						
						dstConn.markAsSuccessifullyTerminated();
					}
					finally {
						if (dstConn != null) {
							dstConn.finalizeConnection();
						}
					}
				}
				
			}
		}
		
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
	
	@SuppressWarnings("unchecked")
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
		
		if (utilities.arrayHasElement(this.operationsControllers)) {
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
		
		if (utilities.arrayHasElement(this.operationsControllers)) {
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
		} else if (utilities.arrayHasElement(this.operationsControllers)) {
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
		FileUtilities.removeFile(this.processInfo.generateProcessStatusFile());
		
		OpenConnection conn = openConnection();
		
		try {
			this.progressInfo = new ProcessProgressInfo(this);
			
			for (OperationController<? extends EtlDatabaseObject> controller : this.operationsControllers) {
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
	
	public OpenConnection openConnection() throws DBException {
		OpenConnection conn = getDefaultConnInfo().openConnection();
		
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
	
	private void createStageSchema() throws DBException {
		OpenConnection conn = getDefaultConnInfo().openConnection();
		
		try {
			if (DBUtilities.isMySQLDB(conn)) {
				DBUtilities.createDatabaseSchema(getConfiguration().getSyncStageSchema(), conn);
			} else {
				BaseDAO.executeBatch(conn, "CREATE SCHEMA " + getConfiguration().getSyncStageSchema());
			}
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private boolean isImportStageSchemaExists() throws DBException {
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
	
	public boolean existInconsistenceInfoTable() throws DBException {
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
	
	public boolean existsDefaultGeneratedObjectKeyTable() throws DBException {
		OpenConnection conn = openConnection();
		
		String schema = getConfiguration().getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = EtlConfiguration.DEFAULT_GENERATED_OBJECT_KEY_TABLE_NAME;
		
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
	
	public boolean existsSkippedRecordsTable() throws DBException {
		OpenConnection conn = openConnection();
		
		String schema = getConfiguration().getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = EtlConfiguration.SKIPPED_RECORD_TABLE_NAME;
		
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
	
	public boolean existOperationProgressInfoTable() throws DBException {
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
	
	public boolean existEtlRecordErrorTable() throws DBException {
		OpenConnection conn = openConnection();
		
		String schema = getConfiguration().getSyncStageSchema();
		String resourceType = DBUtilities.RESOURCE_TYPE_TABLE;
		String tabName = EtlConfiguration.ETL_RECORD_ERROR_TABLE_NAME;
		
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
	
	private void createTableOperationProgressInfo() throws DBException {
		
		EtlConfiguration config = getConfiguration();
		
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
			sql += DBUtilities.generateTableIntegerField("min_record_id", 11, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableIntegerField("max_record_id", 11, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableIntegerField("total_records", 11, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableIntegerField("total_processed_records", 11, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableVarcharField("status", 50, "NOT NULL", conn) + ",\n";
			sql += DBUtilities.generateTableTimeStampField("creation_date", conn) + ",\n";
			sql += DBUtilities.generateTableUniqueKeyDefinition(
			    config.getSyncStageSchema() + "_UNQ_OPERATION_ID".toLowerCase(), "operation_id", conn) + ",\n";
			sql += DBUtilities.generateTablePrimaryKeyDefinition("id", "table_operation_progress_info_pk", conn) + "\n";
			
			sql += ");\n";
			
			BaseDAO.executeBatch(conn, sql);
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void createSkippedRecordsTable() throws DBException {
		OpenConnection conn = openConnection();
		
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String schema = getConfiguration().getSyncStageSchema();
		
		String tableName = EtlConfiguration.SKIPPED_RECORD_TABLE_NAME;
		
		sql += "CREATE TABLE " + schema + "." + tableName + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 30, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("object_id", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unq_key".toLowerCase(), "table_name, object_id",
		    conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
		sql += ")";
		
		try {
			BaseDAO.executeBatch(conn, sql);
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void createDefaultGeneratedObjectKeyTable() throws DBException {
		OpenConnection conn = openConnection();
		
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String schema = getConfiguration().getSyncStageSchema();
		
		String tableName = EtlConfiguration.DEFAULT_GENERATED_OBJECT_KEY_TABLE_NAME;
		
		sql += "CREATE TABLE " + schema + "." + tableName + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 30, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("column_name", 30, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("key_value", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unq_key".toLowerCase(), "table_name, column_name",
		    conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
		sql += ")";
		
		try {
			BaseDAO.executeBatch(conn, sql);
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void createEtlRecordErrorTable() throws DBException {
		OpenConnection conn = openConnection();
		
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String schema = getConfiguration().getSyncStageSchema();
		
		String tableName = EtlConfiguration.ETL_RECORD_ERROR_TABLE_NAME;
		
		sql += "CREATE TABLE " + schema + "." + tableName + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("record_id", notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 50, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("origin_location_code", 50, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("exception", 200, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("exception_description", 1000, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
		sql += ")";
		
		String idxDefinition = DBUtilities.generateIndexDefinition(schema + "." + tableName,
		    tableName + "_idx".toLowerCase(), "table_name, origin_location_code", conn) + ";";
		
		try {
			BaseDAO.executeBatch(conn, sql, idxDefinition);
			
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private void createInconsistenceInfoTable() throws DBException {
		OpenConnection conn = openConnection();
		
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String sql = "";
		
		sql += "CREATE TABLE " + getConfiguration().getSyncStageSchema() + ".inconsistence_info (\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("table_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableIntegerField("record_id", 11, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("parent_table_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("parent_id", notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("default_parent_id", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("record_origin_location_code", 100, notNullConstraint, conn)
		        + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", "inconsistence_info_pk", conn);
		sql += ");";
		
		try {
			BaseDAO.executeBatch(conn, sql);
			conn.markAsSuccessifullyTerminated();
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
}
