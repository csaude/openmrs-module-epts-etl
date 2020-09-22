package org.openmrs.module.eptssync.consolitation.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class DatabaseIntegrityConsolidationSearchParams extends SyncSearchParams<OpenMRSObject>{
	private SyncTableInfo tableInfo;
	
	private boolean selectAllRecords;
	private RecordLimits limits;
	
	public DatabaseIntegrityConsolidationSearchParams(SyncTableInfo tableInfo, RecordLimits limits) {
		this.tableInfo = tableInfo;
		this.limits = limits;
		
		setOrderByFields(tableInfo.getPrimaryKey());
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
		
		searchClauses.addColumnToSelect("*");
		searchClauses.addToClauseFrom(tableInfo.getTableName());
		
		if (!this.selectAllRecords) {
			searchClauses.addToClauses("consistent = -1");
		
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
		return this.tableInfo.getRecordClass();
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		DatabaseIntegrityConsolidationSearchParams auxSearchParams = new DatabaseIntegrityConsolidationSearchParams(this.tableInfo, this.limits);
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
