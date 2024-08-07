package org.openmrs.module.epts.etl.consolitation.controller;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.consolitation.model.DatabaseIntegrityConsolidationSearchParams;
import org.openmrs.module.epts.etl.consolitation.processor.DatabaseIntegrityConsolidationProcessor;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the data consolidation in the synchronization processs
 * 
 * @author jpboane
 */
public class DatabaseIntegrityConsolidationController extends OperationController<EtlDatabaseObject> {
	
	public DatabaseIntegrityConsolidationController(ProcessController processController,
	    EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		return new DatabaseIntegrityConsolidationProcessor(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			return DatabaseObjectDAO.getFirstRecord(engine.getSrcConf(), conn);
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
			
			return DatabaseObjectDAO.getLastRecord(engine.getSrcConf(), conn);
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
	
	public OpenConnection openSrcConnection() throws DBException {
		OpenConnection conn = getDefaultConnInfo().openConnection();
		
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
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(
	        ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt, Engine<EtlDatabaseObject> engine) {
		
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new DatabaseIntegrityConsolidationSearchParams(engine,
		        intervalsMgt);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getProgressInfo().getStartTime());
		
		return searchParams;
	}
	
	@Override
	public void afterEtl(List<EtlDatabaseObject> objs, Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
}
