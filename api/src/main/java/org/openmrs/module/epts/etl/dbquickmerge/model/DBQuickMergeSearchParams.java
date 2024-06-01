package org.openmrs.module.epts.etl.dbquickmerge.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.dbquickmerge.engine.DBQuickMergeEngine;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DBQuickMergeSearchParams extends EtlDatabaseObjectSearchParams {
		
	public DBQuickMergeSearchParams(EtlItemConfiguration config, ThreadLimitsManager limits, DBQuickMergeEngine engine) {
		super(config, limits, engine);
		
	}
	
	public DBQuickMergeEngine getEngine() {
		return (DBQuickMergeEngine) super.getRelatedEngine();
	}
	
	@Override
	public DBQuickMergeController getRelatedController() {
		return (DBQuickMergeController) super.getRelatedController();
	}
	
	@Override
	protected AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		DBQuickMergeSearchParams cloned = new DBQuickMergeSearchParams(getConfig(), null, getEngine());
		
		return cloned;
	}
	
	@Override
	public synchronized int countNotProcessedRecords(Connection conn) throws DBException {
		return countAllRecords(conn);
	}
}
