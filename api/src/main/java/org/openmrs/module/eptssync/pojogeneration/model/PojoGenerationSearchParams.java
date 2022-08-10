package org.openmrs.module.eptssync.pojogeneration.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.pojogeneration.engine.PojoGenerationEngine;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class PojoGenerationSearchParams extends SyncSearchParams<OpenMRSObject>{
	private PojoGenerationEngine engine;
	
	public PojoGenerationSearchParams(PojoGenerationEngine engine, RecordLimits limits, Connection conn) {
		super(engine.getSyncTableConfiguration(), limits);
		
		this.engine = engine;
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		return null;
	}	
	
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		return this.tableInfo.getSyncRecordClass(engine.getDefaultApp());
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return 1;
	}

	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return engine.isPojoGenerated() ? 0 : 1;
	}
}
