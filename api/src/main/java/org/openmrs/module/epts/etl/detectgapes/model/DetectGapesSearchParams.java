package org.openmrs.module.epts.etl.detectgapes.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.types.DbmsType;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.detectgapes.controller.DetectGapesController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DetectGapesSearchParams extends EtlDatabaseObjectSearchParams {
	
	private int savedCount;
	
	private Engine<EtlDatabaseObject> engine;
	
	public DetectGapesSearchParams(Engine<EtlDatabaseObject> engine,
	    ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine.getSrcConf(), limits);
		
		this.engine = engine;
		
		setOrderByFields(getSrcConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
	public DetectGapesController getRelatedController() {
		return (DetectGapesController) engine.getController();
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord limits,
	        EtlDatabaseObject parentObject, List<EtlDatabaseObject> auxDataSourceObjects, Connection srcConn,
	        Connection dstCOnn) throws DBException {
		AbstractTableConfiguration tableInfo = getSrcConf();
		
		SearchClauses<EtlDatabaseObject> searchClauses = new SearchClauses<EtlDatabaseObject>(this);
		
		searchClauses.addToClauseFrom(tableInfo.generateFullTableNameWithAlias(srcConn));
		searchClauses.addColumnToSelect(tableInfo.generateFullAliasedSelectColumns());
		
		tryToAddLimits(limits, searchClauses);
		
		tryToAddExtraConditionForExport(searchClauses, parentObject, auxDataSourceObjects,
		    DbmsType.determineFromConnection(srcConn));
		
		if (utilities.stringHasValue(getExtraCondition())) {
			searchClauses.addToClauses(getExtraCondition());
		}
		
		return searchClauses;
		
	}
	
	@Override
	public int countAllRecords(OperationController<EtlDatabaseObject> controller, Connection conn) throws DBException {
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
	public synchronized int countNotProcessedRecords(OperationController<EtlDatabaseObject> controller, Connection conn)
	        throws DBException {
		return countAllRecords(controller, conn);
	}
}
