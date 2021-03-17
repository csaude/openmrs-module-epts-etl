package org.openmrs.module.eptssync.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
	public static final String STATUS_SLEEPING="SLEEPING";
	public static final String STATUS_FINISHED="FINISHED";

	private String operationName;
	private Date startTime;
	private Date finishTime;
	private double elapsedTime;
	
	private String status;
	private String lastRefresh;
	private boolean requestedStopAllEngines;
	private String lastMsgFromController;
	
	private OperationController controller;
	
	public SyncOperationStatus() {
	}
	
	public SyncOperationStatus(OperationController syncController){
		this.controller = syncController;
		this.status = STATUS_NOT_INITIALIZED;
		this.operationName = syncController.getControllerId();
	}
	
	public void setController(OperationController controller) {
		this.controller = controller;
	}
	
	@JsonIgnore
	public OperationController getController() {
		return controller;
	}
	
	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(Date finishTime) {
		this.finishTime = finishTime;
	}

	public double getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(double elapsedTime) {
		this.elapsedTime = elapsedTime;
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

	public void refresh() {
		this.status = determineStatus();
	}

	public boolean isRunning() {
		return this.status.equals(SyncOperationStatus.STATUS_RUNNING);
	}
	
	public boolean isPaused() {
		return this.status.equals(SyncOperationStatus.STATUS_PAUSED);
	}
	
	public boolean isStopped() {
		return this.status.equals(SyncOperationStatus.STATUS_STOPPED);
	}
	
	public boolean isSleeping() {
		return this.status.equals(SyncOperationStatus.STATUS_SLEEPING);
	}
	
	public boolean isFinished() {
		return this.status.equals(SyncOperationStatus.STATUS_FINISHED);
	}

	public void changeStatusToSleeping() {
		this.status = SyncOperationStatus.STATUS_SLEEPING;
		
		save();
	}
	
	public void changeStatusToRunning() {
		this.status = SyncOperationStatus.STATUS_RUNNING;
		
		save();
	}
	
	public void changeStatusToStopped() {
		this.status = SyncOperationStatus.STATUS_STOPPED;	
		
		save();
	}
	
	public void changeStatusToFinished() {
		this.status = SyncOperationStatus.STATUS_FINISHED;	
		this.finishTime = DateAndTimeUtilities.getCurrentDate();
		
		save();
	}
	
	
	private String determineStatus() {
		return "";
	}
	
	@JsonIgnore
	public String parseToJSON(){
		try {
			return new ObjectMapperProvider().getContext(SyncOperationStatus.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static SyncOperationStatus loadFromFile(File file) {
		try {
			return SyncOperationStatus.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static SyncOperationStatus loadFromJSON (String json) {
		try {
			SyncOperationStatus config = new ObjectMapperProvider().getContext(SyncOperationStatus.class).readValue(json, SyncOperationStatus.class);
		
			return config;
		} catch (JsonParseException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} 
	}	
	
	public void save() {
		String fileName = this.controller.generateProcessStatusFile().getAbsolutePath();
		
		if (new File(fileName).exists()) {
			FileUtilities.removeFile(fileName);
		}	
		
		this.elapsedTime = this.elapsedTime + DateAndTimeUtilities.dateDiff(DateAndTimeUtilities.getCurrentDate(), this.startTime, DateAndTimeUtilities.MINUTE_FORMAT);
		
		String desc = this.parseToJSON();
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		FileUtilities.write(fileName, desc);
	}
	
}
