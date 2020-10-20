package org.openmrs.module.eptssync.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.ParentRefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.ThreadPoolService;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class ProcessInitialization implements MonitoredOperation {
	private int operationStatus;
	private ProcessController controller;
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public ProcessInitialization(ProcessController controller) {
		this.controller = controller;
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	public SyncConfiguration getSyncConfiguration() {
		return controller.getConfiguration();
	}
	
	@Override
	public void run() {
		onStart();
		
		OpenConnection conn = openConnection();
		
		SyncConfiguration configuration = controller.getConfiguration();
		
		
		List<OperationInitialization> allOperationInitialization = new ArrayList<OperationInitialization>();
		
		try {
			if (configuration.mustCreateStageSchemaElements() && !this.isImportStageSchemaExists()) {
				this.createStageSchema();
			}
			
			for (SyncTableConfiguration info : this.getSyncConfiguration().getTablesConfigurations()) {
				OperationInitialization operationInitialization = new OperationInitialization(this, info);
				allOperationInitialization.add(operationInitialization);
				
				ThreadPoolService.getInstance().createNewThreadPoolExecutor(this.controller.getControllerId().toUpperCase() + "_INITIALIZER["+info.getTableName() + "]").execute(operationInitialization);
			}
			
			int qtyRunningOperations = allOperationInitialization.size();
			
			while(qtyRunningOperations > 0) {
				this.controller.logInfo("THE PROCESS INITIALIZER IS STILL WORKING ON PROJECT AND DATABASE CONFIGURATION");
				
				qtyRunningOperations = 0;
				
				for(OperationInitialization operationInitialization: allOperationInitialization) {
					if (!operationInitialization.isFinished()) {
						qtyRunningOperations++;
					}
				}
				
				TimeCountDown.sleep(15);
			}
			
			if (configuration.isMustCreateClasses()) {
				recompileAllAvaliableClasses(conn);
			}
			
			conn.markAsSuccessifullyTerminected();
		}
		
		finally {
			conn.finalizeConnection();
		}
		
		onFinish();
	}
	

	private void createStageSchema() {
		OpenConnection conn = openConnection();
		
		try {
			Statement st = conn.createStatement();

			st.addBatch("CREATE DATABASE " + getSyncConfiguration().getSyncStageSchema());

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
		for (SyncTableConfiguration info : this.getSyncConfiguration().getTablesConfigurations()) {
			if (utilities.createInstance(info.getSyncRecordClass(conn)).isGeneratedFromSkeletonClass()) {
				info.generateRecordClass(true, conn);
			}
		
			for (ParentRefInfo i: info.getChildRefInfo(conn)) {
				
				if (!i.getReferenceTableInfo().isFullLoaded()) {
					SyncTableConfiguration existingTableInfo = getSyncConfiguration().retrieveTableInfoByTableName(i.getReferenceTableInfo().getTableName(), conn);
					
					if (existingTableInfo != null) {
						i.setReferenceTableInfo(existingTableInfo);
					}
					else {
						i.getReferenceTableInfo().fullLoad(conn);
					}
				}
				
				if (!i.existsRelatedReferenceClass(conn) || utilities.createInstance(i.determineRelatedReferenceClass(conn)).isGeneratedFromSkeletonClass()) {
					i.generateRelatedReferenceClass(true, conn);
				}
			}
			
			for (ParentRefInfo i: info.getParentRefInfo(conn)) {
				if (!i.getReferencedTableInfo().isFullLoaded()) {
					SyncTableConfiguration existingTableInfo = getSyncConfiguration().retrieveTableInfoByTableName(i.getReferencedTableInfo().getTableName(), conn);
					
					if (existingTableInfo != null) {
						i.setReferencedTableInfo(existingTableInfo);
					}
					else {
						i.getReferencedTableInfo().fullLoad(conn);
					}
				}
				
				if (!i.existsRelatedReferencedClass(conn) || utilities.createInstance(i.determineRelatedReferencedClass(conn)).isGeneratedFromSkeletonClass()) {
					i.generateRelatedReferencedClass(true, conn);
				}
			}
		}
	}
	
	private boolean isImportStageSchemaExists() {
		OpenConnection conn = openConnection();
		
		try {
			return DBUtilities.isResourceExist(null, DBUtilities.RESOURCE_TYPE_SCHEMA, getSyncConfiguration().getSyncStageSchema(), conn);
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	public OpenConnection openConnection() {
		return this.controller.openConnection();
	}

	@Override
	public TimeController getTimer() {
		return null;
	}

	@Override
	public void requestStop() {
	}

	@Override
	public boolean stopRequested() {
		return false;
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
	public void onStart() {
		changeStatusToRunning();
	}

	@Override
	public void onSleep() {
	}

	@Override
	public void onStop() {
		changeStatusToStopped();
	}

	@Override
	public void onFinish() {
		changeStatusToFinished();
	}

	@Override
	public int getWaitTimeToCheckStatus() {
		return 15;
	}
}
