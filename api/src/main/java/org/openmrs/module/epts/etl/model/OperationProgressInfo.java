package org.openmrs.module.epts.etl.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.EtlProgressMeter;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.ObjectMapperProvider;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class OperationProgressInfo {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String operationName;
	
	private Date startTime;
	
	private Date finishTime;
	
	private double elapsedTime;
	
	private String status;
	
	private OperationController<? extends EtlDatabaseObject> controller;
	
	private List<TableOperationProgressInfo> itemsProgressInfo;
	
	public OperationProgressInfo() {
	}
	
	public OperationProgressInfo(OperationController<? extends EtlDatabaseObject> controller) {
		this.controller = controller;
		this.status = EtlProgressMeter.STATUS_NOT_INITIALIZED;
		this.operationName = controller.getControllerId();
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
	
	@JsonIgnore
	public OperationController<? extends EtlDatabaseObject> getController() {
		return controller;
	}
	
	public void setController(OperationController<? extends EtlDatabaseObject> controller) {
		this.controller = controller;
	}
	
	public void setItemsProgressInfo(List<TableOperationProgressInfo> itemsProgressInfo) {
		this.itemsProgressInfo = itemsProgressInfo;
	}
	
	public void initProgressMeter(Connection conn) throws DBException {
		this.itemsProgressInfo = new ArrayList<TableOperationProgressInfo>();
		
		List<EtlItemConfiguration> allItem = null;
		
		if (this.getConfiguration().hasTestingItem()) {
			allItem = utilities.parseToList(this.getConfiguration().getTestingEtlItemConfiguration());
		} else {
			allItem = this.getConfiguration().getEtlItemConfiguration();
		}
		
		for (EtlItemConfiguration tabConf : allItem) {
			TableOperationProgressInfo pm = null;
			
			try {
				pm = TableOperationProgressInfoDAO.find(getController(), tabConf, conn);
			}
			catch (DBException e) {
				if (!e.isTableOrViewDoesNotExistException()) {
					throw e;
				}
			}
			
			if (pm == null) {
				pm = new TableOperationProgressInfo(this.controller, tabConf);
			}
			
			this.itemsProgressInfo.add(pm);
		}
	}
	
	@JsonIgnore
	private EtlConfiguration getConfiguration() {
		return this.controller.getEtlConfiguration();
	}
	
	public void refreshProgressInfo() {
		for (TableOperationProgressInfo tabConf : this.itemsProgressInfo) {
			tabConf.refreshProgressMeter();
		}
	}
	
	public TableOperationProgressInfo retrieveProgressInfo(EtlItemConfiguration config) {
		for (TableOperationProgressInfo progressInfo : this.itemsProgressInfo) {
			if (progressInfo.getOperationConfigCode().equals(config.getConfigCode())) {
				return progressInfo;
			}
		}
		
		return null;
	}
	
	public List<TableOperationProgressInfo> getItemsProgressInfo() {
		return itemsProgressInfo;
	}
	
	public void refresh() {
		this.status = determineStatus();
	}
	
	@JsonIgnore
	public boolean isRunning() {
		return this.status.equals(EtlProgressMeter.STATUS_RUNNING);
	}
	
	@JsonIgnore
	public boolean isPaused() {
		return this.status.equals(EtlProgressMeter.STATUS_PAUSED);
	}
	
	@JsonIgnore
	public boolean isStopped() {
		return this.status.equals(EtlProgressMeter.STATUS_STOPPED);
	}
	
	@JsonIgnore
	public boolean isSleeping() {
		return this.status.equals(EtlProgressMeter.STATUS_SLEEPING);
	}
	
	@JsonIgnore
	public boolean isFinished() {
		return this.status.equals(EtlProgressMeter.STATUS_FINISHED);
	}
	
	public void changeStatusToSleeping() {
		this.status = EtlProgressMeter.STATUS_SLEEPING;
		
		save();
	}
	
	public void changeStatusToRunning() {
		this.status = EtlProgressMeter.STATUS_RUNNING;
		
		save();
	}
	
	public void changeStatusToStopped() {
		this.status = EtlProgressMeter.STATUS_STOPPED;
		
		save();
	}
	
	public void changeStatusToFinished() {
		this.status = EtlProgressMeter.STATUS_FINISHED;
		this.finishTime = DateAndTimeUtilities.getCurrentDate();
		
		save();
	}
	
	@JsonIgnore
	private String determineStatus() {
		return "";
	}
	
	@JsonIgnore
	public String parseToJSON() {
		try {
			return new ObjectMapperProvider().getContext(OperationProgressInfo.class).writeValueAsString(this);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static OperationProgressInfo loadFromFile(File file) {
		try {
			return OperationProgressInfo.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static OperationProgressInfo loadFromJSON(String json) {
		try {
			OperationProgressInfo config = new ObjectMapperProvider().getContext(OperationProgressInfo.class).readValue(json,
			    OperationProgressInfo.class);
			
			return config;
		}
		catch (JsonParseException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (JsonMappingException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public void save() {
		String fileName = this.controller.generateOperationStatusFile().getAbsolutePath();
		
		if (new File(fileName).exists()) {
			FileUtilities.removeFile(fileName);
		}
		
		this.elapsedTime = this.elapsedTime + DateAndTimeUtilities.dateDiff(DateAndTimeUtilities.getCurrentDate(),
		    this.startTime, DateAndTimeUtilities.MINUTE_FORMAT);
		
		List<TableOperationProgressInfo> bkpItems = this.itemsProgressInfo;
		
		//To avoid the items to saved. NOTE that intentionally the JsonIgron is not used on getItemsProgressInfo()
		//Because its needed on the UI
		this.itemsProgressInfo = null;
		
		String desc = this.parseToJSON();
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		FileUtilities.write(fileName, desc);
		
		this.itemsProgressInfo = bkpItems;
	}
	
	public void reset(Connection conn) throws DBException {
		if (utilities.arrayHasElement(this.itemsProgressInfo)) {
			for (TableOperationProgressInfo progress : this.itemsProgressInfo) {
				progress.clear(conn);
			}
		}
		
		FileUtilities.removeFile(this.controller.generateOperationStatusFile());
	}
}
