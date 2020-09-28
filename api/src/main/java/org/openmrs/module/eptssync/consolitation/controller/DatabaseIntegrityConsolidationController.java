package org.openmrs.module.eptssync.consolitation.controller;

import org.openmrs.module.eptssync.consolitation.engine.DatabaseIntegrityConsolidationEngine;
import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the data export in the synchronization processs
 * 
 * @author jpboane
 *
 */
public class DatabaseIntegrityConsolidationController extends AbstractSyncController {
	
	public DatabaseIntegrityConsolidationController() {
		super();
	}

	@Override
	public SyncEngine initRelatedEngine(SyncTableInfo syncInfo, RecordLimits limits) {
		return new DatabaseIntegrityConsolidationEngine(syncInfo, limits, this);
	}

	@Override
	protected long getMinRecordId(SyncTableInfo tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			OpenMRSObject obj = OpenMRSObjectDAO.getFirstRecord(tableInfo, conn);
		
			if (obj != null) return obj.getObjectId();
			
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
	protected long getMaxRecordId(SyncTableInfo tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			OpenMRSObject obj = OpenMRSObjectDAO.getLastRecord(tableInfo, conn);
		
			if (obj != null) return obj.getObjectId();
			
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
	public String getOperationName() {
		return AbstractSyncController.SYNC_OPERATION_CONSOLIDATION;
	}
	
	@Override
	public OpenConnection openConnection() {
		OpenConnection conn = super.openConnection();
	
		if (getSyncTableInfoSource().isDoIntegrityCheckInTheEnd()) {
			try {
				DBUtilities.disableForegnKeyChecks(conn);
			} catch (DBException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
		
		return conn;
	}
}
