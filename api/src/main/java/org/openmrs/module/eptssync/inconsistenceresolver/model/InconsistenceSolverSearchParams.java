package org.openmrs.module.eptssync.inconsistenceresolver.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class InconsistenceSolverSearchParams extends SyncSearchParams<OpenMRSObject>{
	private boolean selectAllRecords;
	
	public InconsistenceSolverSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, Connection conn) {
		super(tableInfo, limits);
		
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
		
		
		if (tableInfo.getTableName().equalsIgnoreCase("patient")) {
			searchClauses.addColumnToSelect("patient.*, person.uuid");
			searchClauses.addToClauseFrom("patient left join person on patient_id = person_id");
		}
		else {
			searchClauses.addColumnToSelect("*");
			searchClauses.addToClauseFrom(tableInfo.getTableName());
		}
		
		if (!this.selectAllRecords) {
			searchClauses.addToClauses("NOT EXISTS (SELECT 	id " +
									   "			FROM    " + tableInfo.generateFullStageTableName() + 
									   "			WHERE   record_origin_id = " + tableInfo.getTableName() + "." + tableInfo.getPrimaryKey() + ")");
			
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
		InconsistenceSolverSearchParams auxSearchParams = new InconsistenceSolverSearchParams(this.tableInfo, this.limits, conn);
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
