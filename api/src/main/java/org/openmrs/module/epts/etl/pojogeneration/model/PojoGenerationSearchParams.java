package org.openmrs.module.epts.etl.pojogeneration.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.pojogeneration.engine.PojoGenerationEngine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class PojoGenerationSearchParams extends AbstractEtlSearchParams<EtlObject> {
	
	private PojoGenerationEngine engine;
	
	public PojoGenerationSearchParams(PojoGenerationEngine engine, ThreadLimitsManager limits, Connection conn) {
		super(engine.getEtlConfiguration(), limits, null);
		
		this.engine = engine;
	}
	
	
	public PojoGenerationEngine getEngine() {
		return engine;
	}
	
	@Override
	public List<EtlObject> searchNextRecords(Connection conn) throws DBException {
		if (getEngine().isPojoGenerated())
			return null;
		
		List<EtlObject> records = new ArrayList<>();
		
		records.add(new PojoGenerationRecord(getSrcConf()));
		
		return records;
	}
	
	@Override
	public SearchClauses<EtlObject> generateSearchClauses(Connection conn) throws DBException {
		return null;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return 1;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return engine.isPojoGenerated() ? 0 : 1;
	}


	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected AbstractEtlSearchParams<EtlObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		return null;
	}
}
