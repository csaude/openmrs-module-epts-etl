package org.openmrs.module.eptssync.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.ParentRefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.monitor.ControllerStatusMonitor;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
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
	
	public void init() {
		OpenConnection conn = openConnection();
		
		try {
			if (configuration.mustCreateStageSchemaElements() && !this.isImportStageSchemaExists()) {
				this.createStageSchema();
			}
			
			for (SyncTableConfiguration info : this.configuration.getTablesConfigurations()) {
				info.setRelatedSyncTableInfoSource(this.getConfiguration());
				info.tryToUpgradeDataBaseInfo(conn);
				if (configuration.isMustCreateClasses()) info.generateRecordClass(conn);
			}
			
			if (configuration.isMustCreateClasses()) {
				recompileAllAvaliableClasses(conn);
			}
			
			conn.markAsSuccessifullyTerminected();
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
		
	}
	
	private void createStageSchema() {
		OpenConnection conn = openConnection();
		
		try {
			Statement st = conn.createStatement();

			st.addBatch("CREATE DATABASE " + configuration.getSyncStageSchema());

			st.executeBatch();

			st.close();
			
			conn.markAsSuccessifullyTerminected();
		} catch (SQLException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	private void recompileAllAvaliableClasses(Connection conn) {
		for (SyncTableConfiguration info : this.configuration.getTablesConfigurations()) {
			if (utilities.createInstance(info.getSyncRecordClass(conn)).isGeneratedFromSkeletonClass()) {
				info.generateRecordClass(conn);
			}
		
			for (ParentRefInfo i: info.getChildRefInfo(conn)) {
				
				if (i.getReferenceTableInfo().getTableName().equals("person")) {
					System.out.println("Stop");
				}
				
				if (!i.getReferenceTableInfo().isFullLoaded()) {
					SyncTableConfiguration existingTableInfo = configuration.retrieveTableInfoByTableName(i.getReferenceTableInfo().getTableName(), conn);
					
					if (existingTableInfo != null) {
						i.setReferenceTableInfo(existingTableInfo);
					}
					else {
						i.getReferenceTableInfo().fullLoad(conn);
					}
				}
				
				if (!i.existsRelatedReferenceClass() || utilities.createInstance(i.determineRelatedReferenceClass()).isGeneratedFromSkeletonClass()) {
					i.generateRelatedReferenceClass(conn);
				}
			}
			
			for (ParentRefInfo i: info.getParentRefInfo(conn)) {
				if (!i.getReferencedTableInfo().isFullLoaded()) {
					SyncTableConfiguration existingTableInfo = configuration.retrieveTableInfoByTableName(i.getReferencedTableInfo().getTableName(), conn);
					
					if (existingTableInfo != null) {
						i.setReferencedTableInfo(existingTableInfo);
					}
					else {
						i.getReferencedTableInfo().fullLoad(conn);
					}
				}
				
				if (!i.existsRelatedReferencedClass() || utilities.createInstance(i.determineRelatedReferencedClass()).isGeneratedFromSkeletonClass()) {
					i.generateRelatedReferencedClass(conn);
				}
			}
		}
	}

	private boolean isImportStageSchemaExists() {
		OpenConnection conn = openConnection();
		
		try {
			return DBUtilities.isResourceExist(null, DBUtilities.RESOURCE_TYPE_SCHEMA, configuration.getSyncStageSchema(), conn);
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
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
			for (OperationController controller : this.operationsControllers) {
				if (!controller.isFinished()) {
					return false;
				}
				else
				if (controller.getChild() != null && !controller.getChild().isFinished()) {
					return false;
				}
			}
			
			if (this.childController != null) {
				return this.childController.isFinished();
			}
			else {
				return true;
			}
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
		
		init();
		
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
			if (!operation.isDisabled()) {
				List<OperationController> controllers = operation.generateRelatedController(this, conn);

				for (OperationController controller : controllers) {
					this.operationsControllers.add(controller);
					
					ExecutorService executor = ThreadPoolService.getInstance().createNewThreadPoolExecutor(controller.getControllerId());
					executor.execute(controller);
				}
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
