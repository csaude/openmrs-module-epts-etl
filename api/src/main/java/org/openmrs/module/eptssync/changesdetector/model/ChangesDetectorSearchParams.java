package org.openmrs.module.eptssync.changesdetector.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class ChangesDetectorSearchParams extends SyncSearchParams<OpenMRSObject>{
	private boolean selectAllRecords;
	private String appCode;
	
	public ChangesDetectorSearchParams(SyncTableConfiguration tableInfo, String appCode, RecordLimits limits, Connection conn) {
		super(tableInfo, limits);
		
		this.appCode = appCode;
		
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
		
		searchClauses.addColumnToSelect("*");
		searchClauses.addToClauseFrom(tableInfo.getTableName());
		
		if (!this.selectAllRecords) {
			
			if (tableInfo.isMetadata()) {
				searchClauses.addToClauses("date_created >= ? or  date_changed > ? or date_voided > ?");
			}
			else {
				searchClauses.addToClauses("date_created >= ? or  date_changed > ? or date_retired > ?");
			}
			
			searchClauses.addToParameters(this.getSyncStartDate());
			searchClauses.addToParameters(this.getSyncStartDate());
			searchClauses.addToParameters(this.getSyncStartDate());
			
			searchClauses.addToClauses("NOT EXISTS (SELECT 	id " +
									   "			FROM    detected_record_info " + 
									   "			WHERE   record_id = " + tableInfo.getTableName() + "." + tableInfo.getPrimaryKey() + 
									   "					AND app_cod = ? " +
									   "				    AND record_origin_location_code = ? )");
			
			
			searchClauses.addToParameters(this.appCode);
			searchClauses.addToParameters(tableInfo.getOriginAppLocationCode());
			
			if (limits != null) {
				searchClauses.addToClauses(tableInfo.getPrimaryKey() + " between ? and ?");
				searchClauses.addToParameters(this.limits.getFirstRecordId());
				searchClauses.addToParameters(this.limits.getLastRecordId());
			}
			
			if (this.tableInfo.getExtraConditionForExport() != null) {
				searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
			}
		}
		
		return searchClauses;
	}	
	
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		return this.tableInfo.getSyncRecordClass();
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		ChangesDetectorSearchParams auxSearchParams = new ChangesDetectorSearchParams(this.tableInfo, this.appCode, this.limits, conn);
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
