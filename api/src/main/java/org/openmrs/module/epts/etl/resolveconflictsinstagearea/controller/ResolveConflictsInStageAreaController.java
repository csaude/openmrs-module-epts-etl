package org.openmrs.module.epts.etl.resolveconflictsinstagearea.controller;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
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
public class ResolveConflictsInStageAreaController extends OperationController<SyncImportInfoVO> {
	
	public ResolveConflictsInStageAreaController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public TaskProcessor<SyncImportInfoVO> initRelatedTaskProcessor(Engine<SyncImportInfoVO> monitor,
	        IntervalExtremeRecord limits,  boolean runningInConcurrency) {
		return new ResolveConflictsInStageAreaEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<SyncImportInfoVO> initMainSearchParams(ThreadRecordIntervalsManager<SyncImportInfoVO> intervalsMgt,
	        Engine<SyncImportInfoVO> engine) {
		
		AbstractEtlSearchParams<SyncImportInfoVO> searchParams = new ResolveConflictsInStageAreaSearchParams(engine,
		        intervalsMgt);
		
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getStartDate());
		
		return searchParams;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		ResolveConflictsInStageAreaSearchParams searchParams = new ResolveConflictsInStageAreaSearchParams((Engine<SyncImportInfoVO>) engine, null);
		
		try {
			conn = openSrcConnection();
			
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
	
	@SuppressWarnings("unchecked")
	@Override
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		ResolveConflictsInStageAreaSearchParams searchParams = new ResolveConflictsInStageAreaSearchParams(
		        (Engine<SyncImportInfoVO>) engine, null);
		
		try {
			conn = openSrcConnection();
			
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
	public OpenConnection openSrcConnection() throws DBException {
		OpenConnection conn = super.openSrcConnection();
		
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
	
	@Override
	public void afterEtl(List<SyncImportInfoVO> objs, Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		
	}
}
