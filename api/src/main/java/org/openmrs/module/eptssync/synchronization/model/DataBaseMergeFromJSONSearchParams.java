package org.openmrs.module.eptssync.synchronization.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.common.model.SyncImportInfoSearchParams;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class DataBaseMergeFromJSONSearchParams extends SyncImportInfoSearchParams{
	private boolean forProgressMeter;
	
	public DataBaseMergeFromJSONSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits) {
		super(tableInfo, limits);
		
		setOrderByFields("id");
	}
	
	public DataBaseMergeFromJSONSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits, String appOriginLocationCode) {
		super(tableInfo, limits, appOriginLocationCode);
		setOrderByFields("id");
	}
	
	@Override
	public SearchClauses<SyncImportInfoVO> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<SyncImportInfoVO> searchClauses = new SearchClauses<SyncImportInfoVO>(this);
		
		searchClauses.addColumnToSelect(tableInfo.generateFullStageTableName() + ".*");
		
		searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName());    
		
		
		if (!forProgressMeter) {
			searchClauses.addToClauses("last_sync_date is null or last_sync_date < ?");
			searchClauses.addToParameters(this.getSyncStartDate());
			
			if (limits != null) {
			  	searchClauses.addToClauses("id between ? and ?");
				searchClauses.addToParameters(this.limits.getCurrentFirstRecordId());
				searchClauses.addToParameters(this.limits.getCurrentLastRecordId());
			}
		}
		else {
			searchClauses.addToClauses("migration_status in (?, ?)");
			
			searchClauses.addToParameters(SyncImportInfoVO.MIGRATION_STATUS_FAILED);
			searchClauses.addToParameters(SyncImportInfoVO.MIGRATION_STATUS_PENDING);
		}
		
		searchClauses.addToClauses("record_origin_location_code = ?");
		searchClauses.addToParameters(this.getAppOriginLocationCode());
		
		if (utilities.stringHasValue(getExtraCondition())) {
			searchClauses.addToClauses(getExtraCondition());
		}
		
		return searchClauses;
	}	
	
	@Override
	public Class<SyncImportInfoVO> getRecordClass() {
		return SyncImportInfoVO.class;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		OpenMRSObjectSearchParams migratedRecordSearchParams = new OpenMRSObjectSearchParams(getTableInfo(), null);
		
		int migrated = SearchParamsDAO.countAll(migratedRecordSearchParams, conn);
		int notMigrated = countNotProcessedRecords(conn);
		
		return migrated + notMigrated;
	}

	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		return SearchParamsDAO.countAll(this, conn);
	}
}
