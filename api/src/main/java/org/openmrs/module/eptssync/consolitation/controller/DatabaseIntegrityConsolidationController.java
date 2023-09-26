package org.openmrs.module.eptssync.consolitation.controller;

import org.openmrs.module.eptssync.consolitation.engine.DatabaseIntegrityConsolidationEngine;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the data consolidation in the synchronization processs
 * 
 * @author jpboane
 *
 */
public class DatabaseIntegrityConsolidationController extends OperationController {
	
	public DatabaseIntegrityConsolidationController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
				
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DatabaseIntegrityConsolidationEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			return DatabaseObjectDAO.getFirstRecord(tableInfo, conn);
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			return DatabaseObjectDAO.getLastRecord(tableInfo, conn);
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

	public OpenConnection openConnection() {
		OpenConnection conn = getDefaultApp().openConnection();
		
		try {
			DBUtilities.disableForegnKeyChecks(conn);
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		
		return conn;
	}
}
