package org.openmrs.module.epts.etl.databasepreparation.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.databasepreparation.engine.DatabasePreparationEngine;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabasePreparationSearchParams extends AbstractEtlSearchParams<DatabasePreparationRecord> {
	
	DatabasePreparationEngine processor;
	
	public DatabasePreparationSearchParams(DatabasePreparationEngine processor,
	    ThreadRecordIntervalsManager<DatabasePreparationRecord> limits) {
		super(processor.getMonitor(), limits);
		
		this.processor = processor;
	}
	
	@Override
	public List<DatabasePreparationRecord> search(IntervalExtremeRecord intervalExtremeRecord, Connection srcConn,
	        Connection dstCOnn) throws DBException {
		if (processor.isUpdateDone())
			return null;
		
		List<DatabasePreparationRecord> records = new ArrayList<>();
		
		records.add(new DatabasePreparationRecord(getSrcConf()));
		
		return records;
	}
	
	@Override
	public SearchClauses<DatabasePreparationRecord> generateSearchClauses(IntervalExtremeRecord recordLimits,
	        Connection srcConn, Connection dstConn) throws DBException {
		
		return null;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return 1;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return processor.isUpdateDone() ? 0 : 1;
	}
	
	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public AbstractEtlSearchParams<DatabasePreparationRecord> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) {
		return null;
	}
}
