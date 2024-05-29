package org.openmrs.module.epts.etl.synchronization.controller;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
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
public class DatabaseMergeFromJSONController extends EtlController {
	
	public DatabaseMergeFromJSONController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DataBaseMergeFromJSONEngine(monitor, limits);
	}
	
	@Override
	public OpenConnection openConnection() throws DBException {
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
	public long getMinRecordId(EtlItemConfiguration config) {
		OpenConnection conn = null;
		
		try {
			conn = openConnection();
			
			DataBaseMergeFromJSONSearchParams searchParams = new DataBaseMergeFromJSONSearchParams(config, null, this);
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
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	@Override
	public long getMaxRecordId(EtlItemConfiguration config) {
		OpenConnection conn = null;
		
		try {
			conn = openConnection();
			
			DataBaseMergeFromJSONSearchParams searchParams = new DataBaseMergeFromJSONSearchParams(config, null, this);
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
			if (conn != null)
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
