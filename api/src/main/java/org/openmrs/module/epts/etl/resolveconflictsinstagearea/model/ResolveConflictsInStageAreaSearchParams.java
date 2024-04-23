package org.openmrs.module.epts.etl.resolveconflictsinstagearea.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoSearchParams;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ResolveConflictsInStageAreaSearchParams extends SyncImportInfoSearchParams {
	
	private boolean selectAllRecords;
	
	public ResolveConflictsInStageAreaSearchParams(EtlItemConfiguration config, RecordLimits limits, Connection conn) {
		super(config, limits, null);
	}
	
	@Override
	public SearchClauses<SyncImportInfoVO> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<SyncImportInfoVO> searchClauses = new SearchClauses<SyncImportInfoVO>(this);
		
		searchClauses.addColumnToSelect("distinct (src_.record_uuid) record_uuid");
		
		AbstractTableConfiguration tableInfo = this.getSrcTableConf();
		
		searchClauses.addToClauseFrom(tableInfo.generateFullStageTableName() + " src_ ");
		
		if (this.selectAllRecords) {
			searchClauses.addToClauses("exists ( 	 select * " + " from " + tableInfo.generateFullStageTableName()
			        + " inner_ " + " where inner_.record_uuid = src_.record_uuid "
			        + " 	   and inner_.record_origin_id != src_.record_origin_id) ");
		} else {
			searchClauses.addToClauses("exists ( 	 select * " + " from " + tableInfo.generateFullStageTableName()
			        + " inner_ " + " where inner_.record_uuid = src_.record_uuid "
			        + " 	   and inner_.record_origin_id != src_.record_origin_id " + "       and consistent = 1) ");
			
			if (this.getLimits() != null) {
				searchClauses.addToClauses("id between ? and ?");
				
				searchClauses.addToParameters(this.getLimits().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getLimits().getCurrentLastRecordId());
			}
			
			if (this.getConfig().getSrcConf().getExtraConditionForExtract() != null) {
				searchClauses.addToClauses(this.getConfig().getSrcConf().getExtraConditionForExtract());
			}
			
			searchClauses.addToClauses("consistent = 1");
		}
		
		return searchClauses;
	}
	
	@Override
	public Class<SyncImportInfoVO> getRecordClass() {
		return SyncImportInfoVO.class;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		ResolveConflictsInStageAreaSearchParams auxSearchParams = new ResolveConflictsInStageAreaSearchParams(
		        this.getConfig(), this.getLimits(), conn);
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, conn);
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		RecordLimits bkpLimits = this.getLimits();
		
		this.removeLimits();
		
		int count = SearchParamsDAO.countAll(this, conn);
		
		this.setLimits(bkpLimits);
		
		return count;
	}
}
