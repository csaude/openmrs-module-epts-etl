package org.openmrs.module.epts.etl.inconsistenceresolver.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class InconsistenceSolverSearchParams extends SyncSearchParams<DatabaseObject>{
	private boolean selectAllRecords;
	
	public InconsistenceSolverSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, Connection conn) {
		super(tableInfo, limits);
		
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<DatabaseObject> searchClauses = new SearchClauses<DatabaseObject>(this);
		
		if (tableInfo.isFromOpenMRSModel() && tableInfo.getTableName().equalsIgnoreCase("patient")) {
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
	public Class<DatabaseObject> getRecordClass() {
		return this.tableInfo.getSyncRecordClass(tableInfo.getMainApp());
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
