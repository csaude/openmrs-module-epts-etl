package org.openmrs.module.eptssync.controller;

import java.sql.SQLException;

import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class OperationInitialization implements MonitoredOperation {
	private int operationStatus;
	private ProcessInitialization relatedProcessInitialization;
	private SyncTableConfiguration tableConfiguration;

	public OperationInitialization(ProcessInitialization relatedProcess, SyncTableConfiguration tableConfiguration) {
		this.relatedProcessInitialization = relatedProcess;
		this.tableConfiguration = tableConfiguration;
		
		this.operationStatus = MonitoredOperation.STATUS_NOT_INITIALIZED;
	}
	
	public SyncConfiguration getRelatedSyncConfiguration() {
		return this.relatedProcessInitialization.getSyncConfiguration();
	}
	
	
	@Override
	public void run() {
		onStart();
		
		OpenConnection conn = openConnection();
		
		try {
			tableConfiguration.setRelatedSyncTableInfoSource(this.getRelatedSyncConfiguration());
			tableConfiguration.tryToUpgradeDataBaseInfo(conn);
			
			if (getRelatedSyncConfiguration().isMustCreateClasses()) tableConfiguration.generateSkeletonRecordClass(conn);
				
			conn.markAsSuccessifullyTerminected();
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
		
		onFinish();
	}
	
	private OpenConnection openConnection() {
		return this.relatedProcessInitialization.openConnection();
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
