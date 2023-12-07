package org.openmrs.module.epts.etl.synchronization.controller;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.synchronization.engine.DataBaseMergeFromJSONEngine;
import org.openmrs.module.epts.etl.synchronization.model.DataBaseMergeFromJSONSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the synchronization processs between tables
 * 
 * @author jpboane
 */
public class DatabaseMergeFromJSONController extends OperationController {
	
	public DatabaseMergeFromJSONController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DataBaseMergeFromJSONEngine(monitor, limits);
	}
	
	@Override
	public OpenConnection openConnection() {
		OpenConnection conn = super.openConnection();
		
		if (getOperationConfig().isDoIntegrityCheckInTheEnd()) {
			try {
				DBUtilities.disableForegnKeyChecks(conn);
			}
			catch (DBException e) {
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
			DataBaseMergeFromJSONSearchParams searchParams = new DataBaseMergeFromJSONSearchParams(tableInfo, null);
			searchParams.setSyncStartDate(this.progressInfo.getStartTime());
			
			SyncImportInfoVO obj = SyncImportInfoDAO.getFirstRecord(searchParams, conn);
			
			if (obj != null)
				return obj.getId();
			
			return 0;
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
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			DataBaseMergeFromJSONSearchParams searchParams = new DataBaseMergeFromJSONSearchParams(tableInfo, null);
			searchParams.setSyncStartDate(this.progressInfo.getStartTime());
			
			SyncImportInfoVO obj = SyncImportInfoDAO.getLastRecord(searchParams, conn);
			
			if (obj != null)
				return obj.getId();
			
			return 0;
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
		return hasNestedController() ? false : true;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
