package org.openmrs.module.eptssync.changedrecordsdetector.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncOperationType;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.OpenMRSPOJOGenerator;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class ChangedRecordsDetectorSearchParams extends SyncSearchParams<OpenMRSObject>{
	private boolean selectAllRecords;
	private String appCode;
	private SyncOperationType type;
	
	public ChangedRecordsDetectorSearchParams(SyncTableConfiguration tableInfo, String appCode, RecordLimits limits, SyncOperationType type, Connection conn) {
		super(tableInfo, limits);
		
		this.appCode = appCode;
		
		setOrderByFields(tableInfo.getPrimaryKey());
		
		this.type = type;
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
		
		searchClauses.addToClauseFrom(tableInfo.getTableName());
	
		if (tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addColumnToSelect("patient.*, person.uuid");
			searchClauses.addToClauseFrom("inner join person on person.person_id = patient_id");
		}
		else {
			searchClauses.addColumnToSelect("*");
		}
			
		if (!this.selectAllRecords) {
			if (this.type.isNewRecordsDetector()){
				searchClauses.addToClauses(tableInfo.getTableName() +".date_created >= ?");
				
				searchClauses.addToParameters(this.getSyncStartDate());
			}
			else
			if (!tableInfo.isMetadata() && !tableInfo.getTableName().equalsIgnoreCase("users") && !tableInfo.getTableName().equalsIgnoreCase("obs")) {
				searchClauses.addToClauses(tableInfo.getTableName() +".date_created < ? and ("+tableInfo.getTableName() + ".date_changed >= ? or " + tableInfo.getTableName() + ".date_voided >= ?)");
				
				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
			}
			else
			if(tableInfo.getTableName().equalsIgnoreCase("obs")) {
				searchClauses.addToClauses(tableInfo.getTableName() +".date_created < ? and " + tableInfo.getTableName() + ".date_voided >= ?");
				
				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
			}
			else {
				searchClauses.addToClauses(tableInfo.getTableName() +".date_created < ? and ("+tableInfo.getTableName() + ".date_changed >= ? or " + tableInfo.getTableName() + ".date_retired >= ?)");

				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
				searchClauses.addToParameters(this.getSyncStartDate());
			}
			
			if (limits != null) {
				searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
				searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
				searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
			}
			
			if (this.tableInfo.getExtraConditionForExport() != null) {
				searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
			}
		}
		
		return searchClauses;
	}	
	
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		 return OpenMRSPOJOGenerator.tryToGetExistingCLass("org.openmrs.module.eptssync.model.pojo.generic.GenericOpenMRSObject");
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		ChangedRecordsDetectorSearchParams auxSearchParams = new ChangedRecordsDetectorSearchParams(this.tableInfo, this.appCode, this.limits, this.type, conn);
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, conn);
	}

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.limits;
		
		this.limits = null;
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.limits = bkpLimits;
		
		return count;
	}
}
