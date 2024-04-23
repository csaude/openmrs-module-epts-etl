package org.openmrs.module.epts.etl.inconsistenceresolver.controller;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.inconsistenceresolver.engine.InconsistenceSolverEngine;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the data inconsistence resolving in the synchronization processs
 * 
 * @author jpboane
 *
 */
public class InconsistenceSolverController extends OperationController {
	public InconsistenceSolverController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new InconsistenceSolverEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		OpenConnection conn = openConnection();
		
		try {
			DatabaseObject obj = DatabaseObjectDAO.getFirstNeverProcessedRecordOnOrigin(config.getSrcConf(), conn);
		
			if (obj != null) return obj.getObjectId().getSimpleValueAsInt();
			
			return 0;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	public long getMaxRecordId(EtlItemConfiguration config) {
		OpenConnection conn = openConnection();
		
		try {
			DatabaseObject obj = DatabaseObjectDAO.getLastNeverProcessedRecordOnOrigin(config.getSrcConf(), conn);
		
			if (obj != null) return obj.getObjectId().getSimpleValueAsInt();
			
			return 0;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	@Override
	public OpenConnection openConnection() {
		OpenConnection conn = super.openConnection();
	
		if (getOperationConfig().isDoIntegrityCheckInTheEnd()) {
			try {
				DBUtilities.disableForegnKeyChecks(conn);
			} catch (DBException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
		
		return conn;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}