package org.openmrs.module.eptssync.consolitation.controller;

import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.consolitation.engine.DatabaseIntegrityConsolidationEngine;
import org.openmrs.module.eptssync.controller.DestinationOperationController;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
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
public class DatabaseIntegrityConsolidationController extends OperationController implements DestinationOperationController{
	private String appOriginLocationCode;

	public DatabaseIntegrityConsolidationController(ProcessController processController, SyncOperationConfig operationConfig, String appOriginLocationCode) {
		super(processController, operationConfig);
		
		this.appOriginLocationCode = appOriginLocationCode;
		
		this.controllerId = processController.getControllerId() + "_" + getOperationType() + "_from_" + appOriginLocationCode;	
		
		this.progressInfo = this.processController.initOperationProgressMeter(this);
	}
	
	public String getAppOriginLocationCode() {
		return appOriginLocationCode;
	}
				
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DatabaseIntegrityConsolidationEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			SyncImportInfoVO importInfo = SyncImportInfoDAO.getFirstRecordInDestination(tableInfo, getAppOriginLocationCode(), conn);
		
			OpenMRSObject obj = importInfo != null ? OpenMRSObjectDAO.getByUuid(tableInfo.getSyncRecordClass(), importInfo.getRecordUuid(), conn) : null;
			
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
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			SyncImportInfoVO importInfo = SyncImportInfoDAO.getLastRecordInDestination(tableInfo, getAppOriginLocationCode(), conn);
			
			OpenMRSObject obj = importInfo != null ? OpenMRSObjectDAO.getByUuid(tableInfo.getSyncRecordClass(), importInfo.getRecordUuid(), conn) : null;
		
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
	public OpenConnection openConnection() {
		OpenConnection conn = super.openConnection();
	
		//if (getOperationConfig().isDoIntegrityCheckInTheEnd()) {
		
		try {
			DBUtilities.disableForegnKeyChecks(conn);
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		
		//}
		
		return conn;
	}
}
