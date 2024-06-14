package org.openmrs.module.epts.etl.resolveconflictsinstagearea.controller;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.resolveconflictsinstagearea.engine.ResolveConflictsInStageAreaEngine;
import org.openmrs.module.epts.etl.resolveconflictsinstagearea.model.ResolveConflictsInStageAreaSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class ResolveConflictsInStageAreaController extends OperationController {
	
	public ResolveConflictsInStageAreaController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public TaskProcessor initRelatedEngine(EngineMonitor monitor, ThreadRecordIntervalsManager limits) {
		return new ResolveConflictsInStageAreaEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		OpenConnection conn = null;
		
		ResolveConflictsInStageAreaSearchParams searchParams = new ResolveConflictsInStageAreaSearchParams(config, null,
		        conn);
		
		try {
			conn = openConnection();
			
			SyncImportInfoVO rec = SyncImportInfoDAO.getFirstRecord(searchParams, conn);
			
			return rec != null ? rec.getId() : 0;
			
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
		
		ResolveConflictsInStageAreaSearchParams searchParams = new ResolveConflictsInStageAreaSearchParams(config, null,
		        conn);
		
		try {
			conn = openConnection();
			
			SyncImportInfoVO rec = SyncImportInfoDAO.getLastRecord(searchParams, conn);
			
			return rec != null ? rec.getId() : 0;
			
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
		return false;
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
	public boolean canBeRunInMultipleEngines() {
		return false;
	}
}
