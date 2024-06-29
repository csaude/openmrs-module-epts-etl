package org.openmrs.module.epts.etl.inconsistenceresolver.controller;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.inconsistenceresolver.engine.InconsistenceSolverEngine;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceSolverSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the data inconsistence resolving in the synchronization
 * processs
 * 
 * @author jpboane
 */
public class InconsistenceSolverController extends OperationController<EtlDatabaseObject> {
	
	public InconsistenceSolverController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		return new InconsistenceSolverEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(
	        ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt, Engine<EtlDatabaseObject> engine) {
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new InconsistenceSolverSearchParams(engine, intervalsMgt);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getProgressInfo().getStartTime());
		
		return searchParams;
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			EtlDatabaseObject obj = DatabaseObjectDAO.getFirstNeverProcessedRecordOnOrigin(engine.getSrcConf(), conn);
			
			if (obj != null)
				return obj.getObjectId().getSimpleValueAsInt();
			
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
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			EtlDatabaseObject obj = DatabaseObjectDAO.getLastNeverProcessedRecordOnOrigin(engine.getSrcConf(), conn);
			
			if (obj != null)
				return obj.getObjectId().getSimpleValueAsInt();
			
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
		return false;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
	
	@Override
	public void afterEtl(List<EtlDatabaseObject> objs, Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		
	}
}
