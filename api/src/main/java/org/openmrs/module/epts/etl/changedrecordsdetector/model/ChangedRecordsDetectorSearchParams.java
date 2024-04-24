package org.openmrs.module.epts.etl.changedrecordsdetector.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationType;
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
	
	private EtlOperationType type;
	
	public ChangedRecordsDetectorSearchParams(EtlItemConfiguration config, String appCode, RecordLimits limits,
	    EtlOperationType type, Connection conn) {
		super(config, limits);
		
		this.appCode = appCode;
		
		setOrderByFields(config.getSrcConf().getPrimaryKey().parseFieldNamesToArray());
		
		this.type = type;
	}
	
	public String getAppCode() {
		return appCode;
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		AbstractTableConfiguration tableInfo = getConfig().getSrcConf();
		
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
			if (getLimits() != null) {
				searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
				searchClauses.addToParameters(this.getLimits().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getLimits().getCurrentLastRecordId());
			}
		}
		
		tryToAddExtraConditionForExport(searchClauses);
		
		return searchClauses;
	}
	
	@Override
	public Class<DatabaseObject> getRecordClass() {
		return DatabaseEntityPOJOGenerator
		        .tryToGetExistingCLass("org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject");
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.getLimits();
		
		this.setLimits(null);
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.setLimits(bkpLimits);
		
		return count;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
}
