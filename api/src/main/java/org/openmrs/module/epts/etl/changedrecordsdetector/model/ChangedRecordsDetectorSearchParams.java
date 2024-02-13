package org.openmrs.module.epts.etl.changedrecordsdetector.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.SyncOperationType;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.DatabaseEntityPOJOGenerator;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ChangedRecordsDetectorSearchParams extends SyncSearchParams<DatabaseObject> {
	
	private boolean selectAllRecords;
	
	private String appCode;
	
	private SyncOperationType type;
	
	public ChangedRecordsDetectorSearchParams(SyncTableConfiguration tableInfo, String appCode, RecordLimits limits,
	    SyncOperationType type, Connection conn) {
		super(tableInfo, limits);
		
		this.appCode = appCode;
		
		setOrderByFields(tableInfo.getPrimaryKey());
		
		this.type = type;
	}
	
	public String getAppCode() {
		return appCode;
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		searchClauses.addToClauseFrom(tableInfo.getTableName());
		
		if (tableInfo.isFromOpenMRSModel()) {
			
			if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
				searchClauses.addColumnToSelect("patient.*, person.uuid");
				searchClauses.addToClauseFrom("inner join person on person.person_id = patient_id");
			} else {
				searchClauses.addColumnToSelect("*");
			}
			
			if (this.type.isNewRecordsDetector()) {
				searchClauses.addToClauses(tableInfo.getTableName() + ".date_created >= ?");
				
				searchClauses.addToParameters(this.getSyncStartDate());
			} else if (!tableInfo.isMetadata() && !tableInfo.getTableName().equalsIgnoreCase("users")
			        && !tableInfo.getTableName().equalsIgnoreCase("obs")) {
				searchClauses.addToClauses(tableInfo.getTableName() + ".date_created < ? and (" + tableInfo.getTableName()
				        + ".date_changed >= ? or " + tableInfo.getTableName() + ".date_voided >= ?)");
				
				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
			} else if (tableInfo.getTableName().equalsIgnoreCase("obs")) {
				searchClauses.addToClauses(
				    tableInfo.getTableName() + ".date_created < ? and " + tableInfo.getTableName() + ".date_voided >= ?");
				
				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
			} else {
				searchClauses.addToClauses(tableInfo.getTableName() + ".date_created < ? and (" + tableInfo.getTableName()
				        + ".date_changed >= ? or " + tableInfo.getTableName() + ".date_retired >= ?)");
				
				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
			}
		}
		
		if (!this.selectAllRecords) {
			if (limits != null) {
				searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
				searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
				searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
			}
		}
		
		if (this.tableInfo.getExtraConditionForExport() != null) {
			searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
		}
		
		return searchClauses;
	}
	
	@Override
	public Class<DatabaseObject> getRecordClass() {
		return DatabaseEntityPOJOGenerator
		        .tryToGetExistingCLass("org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject");
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.limits;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.limits = bkpLimits;
		
		return count;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
}
