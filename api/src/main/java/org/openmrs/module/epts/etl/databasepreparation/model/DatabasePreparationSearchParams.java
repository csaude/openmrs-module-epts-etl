package org.openmrs.module.epts.etl.databasepreparation.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.databasepreparation.engine.DatabasePreparationEngine;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabasePreparationSearchParams extends AbstractEtlSearchParams<DatabasePreparationRecord> {
	
	private DatabasePreparationEngine engine;
	
	public DatabasePreparationSearchParams(DatabasePreparationEngine engine, ThreadRecordIntervalsManager limits,
	    Connection conn) {
		super(engine.getEtlConfiguration(), limits, null);
		
		this.engine = engine;
	}
	
	public DatabasePreparationEngine getEngine() {
		return engine;
	}
	
	@Override
	public List<DatabasePreparationRecord> searchNextRecords(Engine monitor, Connection conn) {
		if (getEngine().isUpdateDone())
			return null;
		
		List<DatabasePreparationRecord> records = new ArrayList<>();
		
		records.add(new DatabasePreparationRecord(getEngine().getEtlConfiguration().getSrcConf()));
		
		return records;
	}
	
	@Override
	public SearchClauses<DatabasePreparationRecord> generateSearchClauses(Connection conn) throws DBException {
		return null;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return 1;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return engine.isUpdateDone() ? 0 : 1;
	}
	
	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected AbstractEtlSearchParams<DatabasePreparationRecord> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) {
		return null;
	}
}
