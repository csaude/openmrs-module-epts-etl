package org.openmrs.module.epts.etl.synchronization.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoSearchParams;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DataBaseMergeFromJSONSearchParams extends SyncImportInfoSearchParams {
	
	private boolean forProgressMeter;
	
	public DataBaseMergeFromJSONSearchParams(EtlConfiguration config, RecordLimits limits) {
		super(config, limits);
		
		setOrderByFields("id");
	}
	
	public DataBaseMergeFromJSONSearchParams(EtlConfiguration config, RecordLimits limits, String appOriginLocationCode) {
		super(config, limits, appOriginLocationCode);
		setOrderByFields("id");
	}
	
	@Override
	public SearchClauses<SyncImportInfoVO> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<SyncImportInfoVO> searchClauses = new SearchClauses<SyncImportInfoVO>(this);
		
		AbstractTableConfiguration tableInfo = this.getSrcTableConf();
		
		searchClauses.addColumnToSelect(tableInfo.generateFullStageTableName() + ".*");
		
		searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName());
		
		if (!forProgressMeter) {
			searchClauses.addToClauses("last_sync_date is null or last_sync_date < ?");
			searchClauses.addToParameters(this.getSyncStartDate());
			
			if (this.getLimits() != null) {
				searchClauses.addToClauses("id between ? and ?");
				searchClauses.addToParameters(this.getLimits().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getLimits().getCurrentLastRecordId());
			}
		} else {
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
		DatabaseObjectSearchParams migratedRecordSearchParams = new DatabaseObjectSearchParams(getConfig(), null);
		
		int migrated = SearchParamsDAO.countAll(migratedRecordSearchParams, conn);
		int notMigrated = countNotProcessedRecords(conn);
		
		return migrated + notMigrated;
	}
	
	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		return SearchParamsDAO.countAll(this, conn);
	}
}
