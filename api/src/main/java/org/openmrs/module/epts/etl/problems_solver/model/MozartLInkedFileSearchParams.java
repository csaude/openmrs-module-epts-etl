package org.openmrs.module.epts.etl.problems_solver.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.problems_solver.engine.GenerateLinkedConfFiles;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class MozartLInkedFileSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> {
	
	private GenerateLinkedConfFiles engine;
	
	public MozartLInkedFileSearchParams(GenerateLinkedConfFiles engine, ThreadRecordIntervalsManager limits) {
		super(engine.getEtlConfiguration(), limits, null);
		
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
