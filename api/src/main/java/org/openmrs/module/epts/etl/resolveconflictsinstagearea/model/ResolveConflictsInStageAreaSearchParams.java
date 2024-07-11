package org.openmrs.module.epts.etl.resolveconflictsinstagearea.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoSearchParams;
import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ResolveConflictsInStageAreaSearchParams extends SyncImportInfoSearchParams {
	
	private boolean selectAllRecords;
	
	public ResolveConflictsInStageAreaSearchParams(Engine<EtlStageRecordVO> engine, ThreadRecordIntervalsManager<EtlStageRecordVO> limits) {
		super(engine, limits);
	}
	
	@Override
	public SearchClauses<EtlStageRecordVO> generateSearchClauses(IntervalExtremeRecord recordLimits, Connection srcConn,
	        Connection dstConn) throws DBException {
	
		SearchClauses<EtlStageRecordVO> searchClauses = new SearchClauses<EtlStageRecordVO>(this);
		
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
			
			if (this.getThreadRecordIntervalsManager() != null) {
				searchClauses.addToClauses("id between ? and ?");
				
				searchClauses.addToParameters(this.getThreadRecordIntervalsManager().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getThreadRecordIntervalsManager().getCurrentLastRecordId());
			}
			
			if (this.getConfig().getSrcConf().getExtraConditionForExtract() != null) {
				searchClauses.addToClauses(this.getConfig().getSrcConf().getExtraConditionForExtract());
			}
			
			searchClauses.addToClauses("consistent = 1");
		}
		
		return searchClauses;
	}
	
	@Override
	public Class<EtlStageRecordVO> getRecordClass() {
		return EtlStageRecordVO.class;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		ResolveConflictsInStageAreaSearchParams auxSearchParams = new ResolveConflictsInStageAreaSearchParams(
		        this.getRelatedEngine(), this.getThreadRecordIntervalsManager());
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, null, conn);
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		ThreadRecordIntervalsManager<EtlStageRecordVO> bkpLimits = this.getThreadRecordIntervalsManager();
		
		this.removeLimits();
		
		int count = SearchParamsDAO.countAll(this, null, conn);
		
		this.setThreadRecordIntervalsManager(bkpLimits);
		
		return count;
	}

	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractEtlSearchParams<EtlStageRecordVO> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
