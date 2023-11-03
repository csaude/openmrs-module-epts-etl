package org.openmrs.module.epts.etl.merge.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DataBaseMergeFromSourceDBSearchParams extends SyncSearchParams<SyncImportInfoVO>{
	private boolean selectAllRecords;
	
	public DataBaseMergeFromSourceDBSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, Connection conn) {
		super(tableInfo, limits);				
	}
	
	@Override
	public SearchClauses<SyncImportInfoVO> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<SyncImportInfoVO> searchClauses = new SearchClauses<SyncImportInfoVO>(this);
	
		searchClauses.addColumnToSelect("*");
		searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName());
		searchClauses.addToClauses("consistent = 1");
		
			
		if (!this.selectAllRecords) {
			if (limits != null) {
				searchClauses.addToClauses("id between ? and ?");
				
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
	public Class<SyncImportInfoVO> getRecordClass() {
		return SyncImportInfoVO.class;
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		DataBaseMergeFromSourceDBSearchParams auxSearchParams = new DataBaseMergeFromSourceDBSearchParams(this.tableInfo, this.limits, conn);
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, conn);
	}

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		DatabaseObjectSearchParams migratedRecordsSearchParams = new DatabaseObjectSearchParams(getTableInfo(), null);
		
		int processed = SearchParamsDAO.countAll(migratedRecordsSearchParams, conn);
		int allRecords= countAllRecords(conn);
		
		return allRecords - processed; 
	}
}
