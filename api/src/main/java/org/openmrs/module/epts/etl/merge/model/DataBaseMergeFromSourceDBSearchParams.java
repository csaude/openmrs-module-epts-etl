package org.openmrs.module.epts.etl.merge.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DataBaseMergeFromSourceDBSearchParams extends AbstractEtlSearchParams<EtlStageRecordVO> {
	
	private boolean selectAllRecords;
	
	public DataBaseMergeFromSourceDBSearchParams(Engine<EtlStageRecordVO> engine,
	    ThreadRecordIntervalsManager<EtlStageRecordVO> limits) {
		super(engine, limits);
	}
	
	@Override
	public SearchClauses<EtlStageRecordVO> generateSearchClauses(IntervalExtremeRecord limits, Connection srcConn,
	        Connection dstConn) throws DBException {
		SearchClauses<EtlStageRecordVO> searchClauses = new SearchClauses<EtlStageRecordVO>(this);
		
		searchClauses.addColumnToSelect("*");
		searchClauses.addToClauseFrom(getSrcConf().generateFullStageTableName());
		searchClauses.addToClauses("consistent = 1");
		
		if (!this.selectAllRecords) {
			if (this.getThreadRecordIntervalsManager() != null) {
				searchClauses.addToClauses("id between ? and ?");
				
				searchClauses.addToParameters(this.getThreadRecordIntervalsManager().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getThreadRecordIntervalsManager().getCurrentLastRecordId());
			}
			
			if (this.getConfig().getSrcConf().getExtraConditionForExtract() != null) {
				searchClauses.addToClauses(this.getConfig().getSrcConf().getExtraConditionForExtract());
			}
		}
		
		return searchClauses;
	}
	
	@Override
	public Class<EtlStageRecordVO> getRecordClass() {
		return EtlStageRecordVO.class;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		throw new RuntimeException("Rever esta mensagem");
		
		/*
		DataBaseMergeFromSourceDBSearchParams auxSearchParams = new DataBaseMergeFromSourceDBSearchParams(this.getConfig(),
		        this.getThreadRecordIntervalsManager(), conn, getRelatedController());
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, conn);*/
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		throw new RuntimeException("Rever esta mensagem");
		
		/*
		EtlDatabaseObjectSearchParams migratedRecordsSearchParams = new EtlDatabaseObjectSearchParams(getConfig(), null,
		        getRelatedEngine());
		
		int processed = SearchParamsDAO.countAll(migratedRecordsSearchParams, conn);
		int allRecords = countAllRecords(conn);
		
		return allRecords - processed;
		*/
	}
	
	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public AbstractEtlSearchParams<EtlStageRecordVO> cloneMe() {
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
