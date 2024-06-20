package org.openmrs.module.epts.etl.synchronization.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoSearchParams;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.synchronization.controller.DatabaseMergeFromJSONController;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DataBaseMergeFromJSONSearchParams extends SyncImportInfoSearchParams {
	
	private boolean forProgressMeter;
	
	private DatabaseMergeFromJSONController relatedController;
	
	public DataBaseMergeFromJSONSearchParams(Engine<SyncImportInfoVO> engine, ThreadRecordIntervalsManager<SyncImportInfoVO> limits) {
		super(engine, limits);
		
		setOrderByFields("id");
	}
	
	public DataBaseMergeFromJSONSearchParams(Engine<SyncImportInfoVO> config, ThreadRecordIntervalsManager<SyncImportInfoVO> limits,
	    String appOriginLocationCode) {
		super(config, limits, appOriginLocationCode);
		setOrderByFields("id");
	}
	
	public DatabaseMergeFromJSONController getRelatedController() {
		return relatedController;
	}
	
	@Override
	public SearchClauses<SyncImportInfoVO> generateSearchClauses(IntervalExtremeRecord recordLimits, Connection srcConn,
	        Connection dstConn) throws DBException {
		
		SearchClauses<SyncImportInfoVO> searchClauses = new SearchClauses<SyncImportInfoVO>(this);
		
		AbstractTableConfiguration tableInfo = this.getSrcTableConf();
		
		searchClauses.addColumnToSelect(tableInfo.generateFullStageTableName() + ".*");
		
		searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName());
		
		if (!forProgressMeter) {
			searchClauses.addToClauses("last_sync_date is null or last_sync_date < ?");
			searchClauses.addToParameters(this.getSyncStartDate());
			
			if (this.getThreadRecordIntervalsManager() != null) {
				searchClauses.addToClauses("id between ? and ?");
				searchClauses.addToParameters(this.getThreadRecordIntervalsManager().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getThreadRecordIntervalsManager().getCurrentLastRecordId());
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
		
		throw new ForbiddenOperationException("review this method!");
		
		/*
		
		EtlDatabaseObjectSearchParams migratedRecordSearchParams = new EtlDatabaseObjectSearchParams(getRelatedEngine(), null);
		
		int migrated = SearchParamsDAO.countAll(migratedRecordSearchParams, conn);
		int notMigrated = countNotProcessedRecords(conn);
		
		return migrated + notMigrated;*/
	}
	
	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		return SearchParamsDAO.countAll(this, null, conn);
	}
	
	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public AbstractEtlSearchParams<SyncImportInfoVO> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
