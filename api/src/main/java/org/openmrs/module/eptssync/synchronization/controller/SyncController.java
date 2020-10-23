package org.openmrs.module.eptssync.synchronization.controller;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.load.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.synchronization.engine.SyncEngine;
import org.openmrs.module.eptssync.synchronization.model.SynchronizationSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the synchronization processs between
 * tables
 * 
 * @author jpboane
 *
 */
public class SyncController extends OperationController {	
	public SyncController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}

	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new SyncEngine(monitor, limits);
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
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			SynchronizationSearchParams searchParams = new SynchronizationSearchParams(tableInfo, null);
			
			SyncImportInfoVO obj = SyncImportInfoDAO.getFirstRecord(searchParams, conn);
		
			if (obj != null) return obj.getId();
			
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
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			SynchronizationSearchParams searchParams = new SynchronizationSearchParams(tableInfo, null);
			
			SyncImportInfoVO obj = SyncImportInfoDAO.getLastRecord(searchParams, conn);
		
			if (obj != null) return obj.getId();
			
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
		return hasNestedController() ? false : true;
	}
	
	@Override
	public String getOperationType() {
		return SyncOperationConfig.SYNC_OPERATION_SYNCHRONIZATION;
	}	
}
