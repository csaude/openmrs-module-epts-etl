package org.openmrs.module.eptssync.resolveduplications.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class ResolveConflictsInStageAreaSearchParams extends SyncSearchParams<SyncImportInfoVO>{
	private boolean selectAllRecords;
	private String appCode;
	private String type;
	
	public ResolveConflictsInStageAreaSearchParams(SyncTableConfiguration tableInfo, String appCode, RecordLimits limits, String type, Connection conn) {
		super(tableInfo, limits);
		
		this.appCode = appCode;
		
		setOrderByFields(tableInfo.getPrimaryKey());
		
		this.type = type;
	}
	
	@Override
	public SearchClauses<SyncImportInfoVO> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<SyncImportInfoVO> searchClauses = new SearchClauses<SyncImportInfoVO>(this);
			
		searchClauses.addColumnToSelect("src_.record_uuid uuid");
		
		searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName() + " src_ ");
		
		searchClauses.addToGroupingFields("record_uuid");
		
		searchClauses.addToHavingClauses("count(*) > 1");
			
		if (!this.selectAllRecords) {
			
			if (limits != null) {
				searchClauses.addToClauses("id between ? and ?");
				
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
	public Class<SyncImportInfoVO> getRecordClass() {
		return SyncImportInfoVO.class;
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		ResolveConflictsInStageAreaSearchParams auxSearchParams = new ResolveConflictsInStageAreaSearchParams(this.tableInfo, this.appCode, this.limits, this.type, conn);
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
