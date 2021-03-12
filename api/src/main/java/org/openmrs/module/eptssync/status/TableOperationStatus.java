package org.openmrs.module.eptssync.status;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.SyncProgressMeter;
import org.openmrs.module.eptssync.model.ItemProgressInfo;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.concurrent.TimeController;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TableOperationStatus {
	private String operationName;
	private String operationTable;
	private int qtyRecords;
	private Date startTime;
	private Date finishTime;
	private double elapsedTime;
	
	private SyncTableConfiguration conf;
	private OperationController controller;
	private Engine engine;
	private TimeController timer;
	
	public TableOperationStatus() {
	}
	
	public TableOperationStatus(OperationController controller, SyncTableConfiguration conf, Engine engine, TimeController timer) {
		this.conf = conf;
		this.controller = controller;
		this.engine = engine;
		this.timer = timer ;
		
		operationName = this.controller.getControllerId();
		operationTable = this.conf.getTableName();
		
		qtyRecords = this.engine != null && this.engine.getProgressMeter() != null ? this.engine.getProgressMeter().getTotal() : 0;
		
		startTime = this.timer != null ? this.timer.getStartTime(): DateAndTimeUtilities.getCurrentDate();
		finishTime = DateAndTimeUtilities.getCurrentDate();
		elapsedTime = this.timer != null ? this.timer.getDuration(TimeController.DURACAO_IN_MINUTES) : 0;
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
			return new ObjectMapperProvider().getContext(TableOperationStatus.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static TableOperationStatus loadFromFile(File file) {
		try {
			return TableOperationStatus.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static TableOperationStatus loadFromJSON (String json) {
		try {
			TableOperationStatus config = new ObjectMapperProvider().getContext(TableOperationStatus.class).readValue(json, TableOperationStatus.class);
		
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

	public void save() {
		String fileName = this.controller.generateTableProcessStatusFile(conf).getAbsolutePath();
		
		if (new File(fileName).exists()) {
			FileUtilities.removeFile(fileName);
		}	
		
		String desc = this.parseToJSON();
		
		retrieveProgressInfo(this.conf).doLastProgressMeterRefresh(this.getQtyRecords());
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		FileUtilities.write(fileName, desc);
	}
	
	private ItemProgressInfo retrieveProgressInfo(SyncTableConfiguration tableConfiguration) {
		return this.controller.getProgressInfo().retrieveProgressInfo(tableConfiguration);
	}
}
