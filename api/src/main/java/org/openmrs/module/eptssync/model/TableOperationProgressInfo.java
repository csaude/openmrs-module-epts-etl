package org.openmrs.module.eptssync.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;

import javax.ws.rs.ForbiddenException;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.SiteOperationController;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.SyncProgressMeter;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TableOperationProgressInfo extends BaseVO{
	private Engine engine;
	
	private SyncTableConfiguration tableConfiguration;
	private SyncProgressMeter progressMeter;
	private OperationController controller;
	
	
	/*
	 * Since in destination site the tableConfiguration is aplayed to all sites, then it is needed to fix it to allow manual specification
	 */
	private String originAppLocationCode;
	
	public TableOperationProgressInfo() {
	}
	
	public TableOperationProgressInfo(OperationController controller, SyncTableConfiguration tableConfiguration) {
		this.controller = controller;
		this.tableConfiguration = tableConfiguration;
		this.originAppLocationCode = determineAppLocationCode(controller);
		this.progressMeter = SyncProgressMeter.defaultProgressMeter(getOperationId());
	}
	
	private String determineAppLocationCode(OperationController controller) {
		
		if (controller.getOperationConfig().isSupposedToHaveOriginAppCode()) {
			return controller.getConfiguration().getOriginAppLocationCode();
		}
		
		if (controller instanceof SiteOperationController) {
			return ((SiteOperationController)controller).getAppOriginLocationCode();
		}
		 
		if (controller.getOperationConfig().isDatabasePreparationOperation() || 
				controller.getOperationConfig().isPojoGeneration() || 
						controller.getOperationConfig().isResolveConflictsInStageArea() ||
							controller.getOperationConfig().isMissingRecordsDetector() ||
								controller.getOperationConfig().isOutdateRecordsDetector() ||
									controller.getOperationConfig().isPhantomRecordsDetector() ||
										controller.getOperationConfig().isDBMergeFromSourceDB() ||
											controller.getOperationConfig().isDataBaseMergeFromJSONOperation()) return "central_site"; 
		
		throw new ForbiddenException("The originAppCode cannot be determined for "+controller.getOperationType().name().toLowerCase() + " operation!");
	}
	
	public void setController(OperationController controller) {
		this.controller = controller;
	}
	
	@JsonIgnore
	public SyncTableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}
	
	public void setTableConfiguration(SyncTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}

	public String getOperationId() {
		return this.controller.getControllerId() + "_" + this.tableConfiguration.getTableName();
	}
	
	public String getOperationName() {
		return this.controller.getControllerId();
	}

	public String getOperationTable() {
		return this.tableConfiguration.getTableName();
	}

	public SyncProgressMeter getProgressMeter() {
		return progressMeter;
	}
	
	public String getOriginAppLocationCode() {
		return originAppLocationCode;
	}
	
	public void setOriginAppLocationCode(String originAppLocationCode) {
		this.originAppLocationCode = originAppLocationCode;
	}
	
	/*public void init(OperationController controller, Engine engine) {
		TimeController timer = engine.getTimer();
		
		this.controller = controller;
		this.controller = controller;
		
		qtyRecords = this.engine != null && this.engine.getProgressMeter() != null ? this.engine.getProgressMeter().getTotal() : 0;
		
		startTime = timer != null ? timer.getStartTime(): DateAndTimeUtilities.getCurrentDate();
		finishTime = DateAndTimeUtilities.getCurrentDate();
		elapsedTime = timer != null ? timer.getDuration(TimeController.DURACAO_IN_MINUTES) : 0;
	}*/
	
	public void tryToReloadProgressMeter(Engine engine) {
		this.engine = engine;
		
		if (this.engine != null) {
			while(this.engine.getProgressMeter() == null) {
				try {Thread.sleep(1000*2);} catch (InterruptedException e) {}
			}
			
			SyncProgressMeter sourceProgressMeter = this.engine.getProgressMeter();
				
			this.progressMeter.refresh(sourceProgressMeter.getStatusMsg(), sourceProgressMeter.getTotal(), sourceProgressMeter.getProcessed());
			
		}
		
	}

	public void save(Connection conn) throws DBException {
		String fileName = this.controller.generateTableProcessStatusFile(this.tableConfiguration).getAbsolutePath();
		
		if (new File(fileName).exists()) {
			FileUtilities.removeFile(fileName);
		}	
		
		String desc = this.parseToJSON();
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		FileUtilities.write(fileName, desc);
		
		refreshOnDB(conn);
	}

	@JsonIgnore
	public String parseToJSON(){
		try {
			return new ObjectMapperProvider().getContext(TableOperationProgressInfo.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static TableOperationProgressInfo loadFromFile(File file) {
		try {
			TableOperationProgressInfo top = TableOperationProgressInfo.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
			top.getProgressMeter().retrieveTimer();
			
			return top;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public static TableOperationProgressInfo loadFromJSON (String json) {
		try {
			TableOperationProgressInfo config = new ObjectMapperProvider().getContext(TableOperationProgressInfo.class).readValue(json, TableOperationProgressInfo.class);
		
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

	public void refreshProgressMeter() {
		
	}

	public void refreshOnDB(Connection conn) throws DBException {
		TableOperationProgressInfo recordOnDB = TableOperationProgressInfoDAO.find(getOperationId(), getTableConfiguration(), conn);
		
		if (recordOnDB != null) {
			TableOperationProgressInfoDAO.update(this, getTableConfiguration(), conn);
		}
		else {
			TableOperationProgressInfoDAO.insert(this, getTableConfiguration(), conn);
		}
	}
	
}
