package org.openmrs.module.epts.etl.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.engine.SyncProgressMeter;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.BaseVO;
import org.openmrs.module.epts.etl.utilities.ObjectMapperProvider;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class TableOperationProgressInfo extends BaseVO {
	
	private EtlConfiguration etlConfiguration;
	
	private SyncProgressMeter progressMeter;
	
	private OperationController controller;
	
	/*
	 * Since in destination site the etlConfiguration is aplayed to all sites, then it is needed to fix it to allow manual specification
	 */
	private String originAppLocationCode;
	
	public TableOperationProgressInfo() {
		this.originAppLocationCode = "";
	}
	
	@Override
	public void load(ResultSet resultSet) throws SQLException {
		super.load(resultSet);
		
		int total = resultSet.getInt("total_records");
		String status = resultSet.getString("status");
		int processed = resultSet.getInt("total_processed_records");
		Date startTime = resultSet.getTimestamp("started_at");
		Date lastRefreshAt = resultSet.getTimestamp("last_refresh_at");
		
		this.progressMeter = SyncProgressMeter.fullInit(status, startTime, lastRefreshAt, total, processed);
	}
	
	public TableOperationProgressInfo(OperationController controller, EtlConfiguration etlConfiguration) {
		this.controller = controller;
		this.etlConfiguration = etlConfiguration;
		this.originAppLocationCode = determineAppLocationCode(controller);
		this.progressMeter = SyncProgressMeter.defaultProgressMeter(getOperationId());
	}
	
	private String determineAppLocationCode(OperationController controller) {
		
		if (controller.getOperationConfig().isSupposedToHaveOriginAppCode()) {
			return controller.getConfiguration().getOriginAppLocationCode();
		}
		
		if (controller instanceof SiteOperationController) {
			return ((SiteOperationController) controller).getAppOriginLocationCode();
		}
		
		if (controller.getOperationConfig().isDatabasePreparationOperation()
		        || controller.getOperationConfig().isPojoGeneration()
		        || controller.getOperationConfig().isResolveConflictsInStageArea()
		        || controller.getOperationConfig().isMissingRecordsDetector()
		        || controller.getOperationConfig().isOutdateRecordsDetector()
		        || controller.getOperationConfig().isPhantomRecordsDetector()
		        || controller.getOperationConfig().isDBMergeFromSourceDB()
		        || controller.getOperationConfig().isDataBaseMergeFromJSONOperation()
		        || controller.getConfiguration().isResolveProblems() || controller.getConfiguration().isDbCopy()
		        || controller.getConfiguration().isDetectGapesOnDbTables() || controller.getConfiguration().isEtl())
			return "central_site";
		
		throw new ForbiddenOperationException("The originAppCode cannot be determined for "
		        + controller.getOperationType().name().toLowerCase() + " operation!");
	}
	
	public void setController(OperationController controller) {
		this.controller = controller;
	}
	
	@JsonIgnore
	public EtlConfiguration getEtlConfiguration() {
		return etlConfiguration;
	}
	
	public void setEtlConfiguration(EtlConfiguration etlConfiguration) {
		this.etlConfiguration = etlConfiguration;
	}
	
	public String getOperationId() {
		return generateOperationId(controller, etlConfiguration);
	}
	
	public String getOperationName() {
		return this.controller.getControllerId();
	}
	
	public String getOperationConfigCode() {
		return this.etlConfiguration.getConfigCode();
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
	
	public static String generateOperationId(OperationController operationController, EtlConfiguration config) {
		return operationController.getControllerId() + "_" + config.getConfigCode();
	}
	
	public synchronized void save(Connection conn) throws DBException {
		TableOperationProgressInfo recordOnDB = TableOperationProgressInfoDAO.find(this.controller, getEtlConfiguration(),
		    conn);
		
		if (recordOnDB != null) {
			TableOperationProgressInfoDAO.update(this, getEtlConfiguration(), conn);
		} else {
			TableOperationProgressInfoDAO.insert(this, getEtlConfiguration(), conn);
		}
	}
	
	@JsonIgnore
	public String parseToJSON() {
		try {
			return new ObjectMapperProvider().getContext(TableOperationProgressInfo.class).writeValueAsString(this);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static TableOperationProgressInfo loadFromFile(File file) {
		try {
			TableOperationProgressInfo top = TableOperationProgressInfo
			        .loadFromJSON(new String(Files.readAllBytes(file.toPath())));
			top.getProgressMeter().retrieveTimer();
			
			return top;
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	
	public static TableOperationProgressInfo loadFromJSON(String json) {
		try {
			TableOperationProgressInfo config = new ObjectMapperProvider().getContext(TableOperationProgressInfo.class)
			        .readValue(json, TableOperationProgressInfo.class);
			
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
	
	public void refreshProgressMeter() {
		
	}
	
	public void clear(Connection conn) throws DBException {
		TableOperationProgressInfoDAO.delete(this, this.etlConfiguration, conn);
	}
	
}
