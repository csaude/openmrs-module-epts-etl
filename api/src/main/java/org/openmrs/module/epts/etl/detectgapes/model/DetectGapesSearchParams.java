package org.openmrs.module.epts.etl.detectgapes.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.detectgapes.controller.DetectGapesController;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DetectGapesSearchParams extends EtlDatabaseObjectSearchParams {
	
	private int savedCount;
	
	public DetectGapesSearchParams(Engine<EtlDatabaseObject> engine, ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine, limits);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
	public DetectGapesController getRelatedController() {
		return (DetectGapesController) super.getRelatedController();
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord limits, Connection srcConn,
	        Connection dstCOnn) throws DBException {
		AbstractTableConfiguration tableInfo = getSrcTableConf();
		
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		searchClauses.addToClauseFrom(tableInfo.generateFullTableNameWithAlias(srcConn));
		searchClauses.addColumnToSelect(tableInfo.generateFullAliasedSelectColumns());
		
		tryToAddLimits(limits, searchClauses);
		
		tryToAddExtraConditionForExport(searchClauses);
		
		if (utilities.stringHasValue(getExtraCondition())) {
			searchClauses.addToClauses(getExtraCondition());
		}
		
		return searchClauses;
		
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		if (this.savedCount > 0)
			return this.savedCount;
		
		ThreadRecordIntervalsManager<EtlDatabaseObject> bkpLimits = this.getThreadRecordIntervalsManager();
		
		this.removeLimits();
		
		int count = SearchParamsDAO.countAll(this, null, conn);
		
		this.setThreadRecordIntervalsManager(bkpLimits);
		
		this.savedCount = count;
		
		return count;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
}
