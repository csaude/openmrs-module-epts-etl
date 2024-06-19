package org.openmrs.module.epts.etl.inconsistenceresolver.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class InconsistenceSolverSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> {
	
	private boolean selectAllRecords;
	
	public InconsistenceSolverSearchParams(Engine<EtlDatabaseObject> engine, ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine, limits);
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord recordLimits, Connection srcConn,
	        Connection dstConn) throws DBException {
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		AbstractTableConfiguration tableInfo = getSrcTableConf();
		
		searchClauses.addColumnToSelect(tableInfo.generateFullAliasedSelectColumns());
		searchClauses.addToClauseFrom(tableInfo.generateSelectFromClauseContent());
		
		if (!this.selectAllRecords) {
			searchClauses.addToClauses("NOT EXISTS (SELECT 	id " + "			FROM    "
			        + tableInfo.generateFullStageTableName() + "			WHERE   record_origin_id = "
			        + tableInfo.getTableName() + "." + tableInfo.getPrimaryKey() + ")");
			tryToAddLimits(recordLimits, searchClauses);
			
			tryToAddExtraConditionForExport(searchClauses);
		}
		
		return searchClauses;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		InconsistenceSolverSearchParams auxSearchParams = new InconsistenceSolverSearchParams(this.getRelatedEngine(),
		        this.getThreadRecordIntervalsManager());
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, null, conn);
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		
		ThreadRecordIntervalsManager<EtlDatabaseObject> bkpLimits = this.getThreadRecordIntervalsManager();
		
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
	protected AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
