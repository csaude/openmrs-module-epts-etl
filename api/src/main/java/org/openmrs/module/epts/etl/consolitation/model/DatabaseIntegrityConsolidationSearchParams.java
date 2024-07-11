package org.openmrs.module.epts.etl.consolitation.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseIntegrityConsolidationSearchParams extends EtlDatabaseObjectSearchParams {
	
	private boolean selectAllRecords;
	
	public DatabaseIntegrityConsolidationSearchParams(Engine<EtlDatabaseObject> engine,
	    ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		
		super(engine, limits);
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord limits, Connection srcConn,
	        Connection dstConn) throws DBException {
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		searchClauses.addColumnToSelect(getSrcTableConf().generateFullAliasedSelectColumns());
		searchClauses.addToClauseFrom(getSrcTableConf().generateSelectFromClauseContent());
		
		searchClauses
		        .addToClauseFrom("INNER JOIN " + getSrcTableConf().generateFullStageTableName() + " ON record_uuid = uuid");
		
		if (!this.selectAllRecords) {
			searchClauses.addToClauses("consistent = -1");
			searchClauses.addToClauses("last_sync_date is null or last_sync_date < ?");
			searchClauses.addToParameters(this.getSyncStartDate());
			
			tryToAddLimits(limits, searchClauses);
			
			tryToAddExtraConditionForExport(searchClauses);
		}
		
		return searchClauses;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		DatabaseIntegrityConsolidationSearchParams auxSearchParams = new DatabaseIntegrityConsolidationSearchParams(
		        this.getRelatedEngine(), this.getThreadRecordIntervalsManager());
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, null, conn);
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		ThreadRecordIntervalsManager<EtlDatabaseObject> bkpLimits = this.getThreadRecordIntervalsManager();
		
		this.setThreadRecordIntervalsManager(null);
		
		int count = SearchParamsDAO.countAll(this, null, conn);
		
		this.setThreadRecordIntervalsManager(bkpLimits);
		
		return count;
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) {
		return null;
	}
}
