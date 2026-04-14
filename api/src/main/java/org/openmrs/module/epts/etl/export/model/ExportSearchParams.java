package org.openmrs.module.epts.etl.export.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.types.DbmsType;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ExportSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> {
	
	private boolean selectAllRecords;
	
	private Engine<EtlDatabaseObject> relatedEngine;
	
	public ExportSearchParams(Engine<EtlDatabaseObject> engine, ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine.getSrcConf(), limits);
		
		this.relatedEngine = engine;
	}
	
	public Engine<EtlDatabaseObject> getRelatedEngine() {
		return relatedEngine;
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord limits, Connection srcConn,
	        Connection dstConn) throws DBException {
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		AbstractTableConfiguration tableInfo = getSrcConf();
		
		searchClauses.addColumnToSelect(tableInfo.generateFullAliasedSelectColumns());
		searchClauses.addToClauseFrom(tableInfo.generateSelectFromClauseContent());
		
		searchClauses.addToClauseFrom(
		    "inner join " + tableInfo.generateFullStageTableName() + " on record_origin_id  = " + tableInfo.getPrimaryKey());
		
		if (!this.selectAllRecords) {
			tryToAddLimits(limits, searchClauses);
			tryToAddExtraConditionForExport(searchClauses, DbmsType.determineFromConnection(srcConn));
		}
		
		searchClauses.addToClauses("consistent = 1");
		
		return searchClauses;
	}
	
	@Override
	public int countAllRecords(OperationController<EtlDatabaseObject> controller, Connection conn) throws DBException {
		ExportSearchParams auxSearchParams = new ExportSearchParams(getRelatedEngine(),
		        this.getThreadRecordIntervalsManager());
		
		auxSearchParams.selectAllRecords = true;
		
		return SearchParamsDAO.countAll(auxSearchParams, null, conn);
	}
	
	@Override
	public synchronized int countNotProcessedRecords(OperationController<EtlDatabaseObject> controller, Connection conn)
	        throws DBException {
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
	public AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
