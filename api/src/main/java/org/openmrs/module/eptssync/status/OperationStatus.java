package org.openmrs.module.eptssync.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.SyncProgressMeter;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class OperationStatus {
	private String operationName;
	private String operationTable;
	private int qtyRecords;
	private Date startTime;
	private Date finishTime;
	private double elapsedTime;
	
	public OperationStatus() {
		
	}
	
	public OperationStatus(OperationController controller, SyncTableConfiguration conf, Engine engine, TimeController timer) {
		operationName = controller.getControllerId();
		operationTable = conf.getTableName();
		
		qtyRecords = engine != null && engine.getProgressMeter() != null ? engine.getProgressMeter().getTotal() : 0;
		
		startTime = timer.getStartTime();
		finishTime = DateAndTimeUtilities.getCurrentDate();
		elapsedTime = timer != null ? timer.getDuration(TimeController.DURACAO_IN_MINUTES) : 0;
	}
	
	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	public String getOperationTable() {
		return operationTable;
	}

	public void setOperationTable(String operationTable) {
		this.operationTable = operationTable;
	}

	public int getQtyRecords() {
		return qtyRecords;
	}

	public void setQtyRecords(int qtyRecords) {
		this.qtyRecords = qtyRecords;
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

	@JsonIgnore
	public String parseToJSON(){
		try {
			return new ObjectMapperProvider().getContext(OperationStatus.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static OperationStatus loadFromFile(File file) {
		try {
			return OperationStatus.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static OperationStatus loadFromJSON (String json) {
		try {
			OperationStatus config = new ObjectMapperProvider().getContext(OperationStatus.class).readValue(json, OperationStatus.class);
		
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
	
	
	public SyncProgressMeter parseToProgressMeter() {
		SyncProgressMeter pm = SyncProgressMeter.defaultProgressMeter(this.operationTable);
	
		pm.refresh("FINISHED", this.qtyRecords, this.qtyRecords);
		
		return pm;
	}
}
