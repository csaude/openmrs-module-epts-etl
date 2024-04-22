package org.openmrs.module.epts.etl.consolitation.controller;

import org.openmrs.module.epts.etl.consolitation.engine.DatabaseIntegrityConsolidationEngine;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the data consolidation in the synchronization processs
 * 
 * @author jpboane
 */
public class DatabaseIntegrityConsolidationController extends OperationController {
	
	public DatabaseIntegrityConsolidationController(ProcessController processController,
	    SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DatabaseIntegrityConsolidationEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlConfiguration config) {
		OpenConnection conn = openConnection();
		
		try {
			return DatabaseObjectDAO.getFirstRecord(config.getSrcConf(), conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public long getMaxRecordId(EtlConfiguration config) {
		OpenConnection conn = openConnection();
		
		try {
			return DatabaseObjectDAO.getLastRecord(config.getSrcConf(), conn);
		}
		catch (DBException e) {
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
	
	public OpenConnection openConnection() {
		OpenConnection conn = getDefaultApp().openConnection();
		
		try {
			DBUtilities.disableForegnKeyChecks(conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		
		return conn;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return false;
	}
}
