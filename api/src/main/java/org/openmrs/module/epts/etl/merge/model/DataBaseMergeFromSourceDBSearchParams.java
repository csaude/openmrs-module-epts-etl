package org.openmrs.module.epts.etl.merge.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.merge.controller.DataBaseMergeFromSourceDBController;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DataBaseMergeFromSourceDBSearchParams extends AbstractEtlSearchParams<SyncImportInfoVO> {
	
	private boolean selectAllRecords;
	
	public DataBaseMergeFromSourceDBSearchParams(EtlItemConfiguration config, RecordLimits limits, Connection conn,
	    DataBaseMergeFromSourceDBController relatedController) {
		super(config, limits, null);
	}
	
	public DataBaseMergeFromSourceDBController getRelatedController() {
		return (DataBaseMergeFromSourceDBController) super.getRelatedController();
	}
	
	@Override
	public SearchClauses<SyncImportInfoVO> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<SyncImportInfoVO> searchClauses = new SearchClauses<SyncImportInfoVO>(this);
		
		searchClauses.addColumnToSelect("*");
		searchClauses.addToClauseFrom(getSrcTableConf().generateFullStageTableName());
		searchClauses.addToClauses("consistent = 1");
		
		if (!this.selectAllRecords) {
			if (this.getLimits() != null) {
				searchClauses.addToClauses("id between ? and ?");
				
				searchClauses.addToParameters(this.getLimits().getCurrentFirstRecordId());
				searchClauses.addToParameters(this.getLimits().getCurrentLastRecordId());
			}
			
			if (this.getConfig().getSrcConf().getExtraConditionForExtract() != null) {
				searchClauses.addToClauses(this.getConfig().getSrcConf().getExtraConditionForExtract());
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
		DataBaseMergeFromSourceDBSearchParams auxSearchParams = new DataBaseMergeFromSourceDBSearchParams(this.getConfig(),
		        this.getLimits(), conn, getRelatedController());
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, conn);
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		EtlDatabaseObjectSearchParams migratedRecordsSearchParams = new EtlDatabaseObjectSearchParams(getConfig(), null, getRelatedController());
		
		int processed = SearchParamsDAO.countAll(migratedRecordsSearchParams, conn);
		int allRecords = countAllRecords(conn);
		
		return allRecords - processed;
	}

	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AbstractEtlSearchParams<SyncImportInfoVO> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
}
