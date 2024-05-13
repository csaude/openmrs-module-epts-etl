package org.openmrs.module.epts.etl.problems_solver.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.problems_solver.engine.GenerateLinkedConfFiles;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class MozartLInkedFileSearchParams extends SyncSearchParams<EtlDatabaseObject> {
	
	private GenerateLinkedConfFiles engine;
	
	public MozartLInkedFileSearchParams(GenerateLinkedConfFiles engine, RecordLimits limits) {
		super(engine.getEtlConfiguration(), limits);
		
		this.engine = engine;
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		return null;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return 1;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return engine.done() ? 0 : 1;
	}
}
