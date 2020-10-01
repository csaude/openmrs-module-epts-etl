package org.openmrs.module.eptssync.status;

import org.openmrs.module.eptssync.controller.AbstractSyncController;

/**
 * This class represent the current status of hole synchronization process. It can pass the status or receive inputs to the synchronization process
 * 
 * @author jpboane
 *
 */
public class SyncOperationStatus {
	public static final String STATUS_NOT_INITIALIZED="NOT INITIALIZED";
	public static final String STATUS_RUNNING="RUNNING";
	public static final String STATUS_PAUSED = "PAUSED";
	public static final String STATUS_STOPPED="STOPPED";
	public static final String STATUS_SLEEPENG="SLEEPING";
	public static final String STATUS_FINISHED="FINISHED";

	private String id;
	private String status;
	private String lastRefresh;
	private boolean requestedStopAllEngines;
	private String lastMsgFromController;
	
	private AbstractSyncController syncController;
	
	public SyncOperationStatus(AbstractSyncController syncController){
		this.syncController = syncController;
		
		this.status = STATUS_NOT_INITIALIZED;
		
		this.id = this.syncController.getOperationType() + "_" + this.syncController.getSyncTableInfoSource().getDesignation();
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLastRefresh() {
		return lastRefresh;
	}

	public void setLastRefresh(String lastRefresh) {
		this.lastRefresh = lastRefresh;
	}

	public boolean isRequestedStopAllEngines() {
		return requestedStopAllEngines;
	}


	public void setRequestedStopAllEngines(boolean requestedStopAllEngines) {
		this.requestedStopAllEngines = requestedStopAllEngines;
	}


	public String getLastMsgFromController() {
		return lastMsgFromController;
	}


	public void setLastMsgFromController(String lastMsgFromController) {
		this.lastMsgFromController = lastMsgFromController;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void refresh() {
		this.status = determineStatus();
	}
	
	private String determineStatus() {
		
		
		return "";
	}
	
}
