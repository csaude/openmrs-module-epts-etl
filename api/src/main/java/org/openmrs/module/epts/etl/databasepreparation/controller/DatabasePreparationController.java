package org.openmrs.module.epts.etl.databasepreparation.controller;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.databasepreparation.engine.DatabasePreparationEngine;
import org.openmrs.module.epts.etl.databasepreparation.model.DatabasePreparationRecord;
import org.openmrs.module.epts.etl.databasepreparation.model.DatabasePreparationSearchParams;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * This class is responsible for data base preparation
 * 
 * @author jpboane
 */
public class DatabasePreparationController extends OperationController<DatabasePreparationRecord> {
	
	public DatabasePreparationController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public TaskProcessor<DatabasePreparationRecord> initRelatedTaskProcessor(Engine<DatabasePreparationRecord> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		
		return new DatabasePreparationEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		return 1;
	}
	
	@Override
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		return 1;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return false;
	}
	
	@Override
	public AbstractEtlSearchParams<DatabasePreparationRecord> initMainSearchParams(
	        ThreadRecordIntervalsManager<DatabasePreparationRecord> intervalsMgt, Engine<DatabasePreparationRecord> engine) {
		
		DatabasePreparationEngine processor = (DatabasePreparationEngine) initRelatedTaskProcessor(engine, null, false);
		
		return new DatabasePreparationSearchParams(processor, intervalsMgt);
	}
	
	@Override
	public void afterEtl(List<DatabasePreparationRecord> objs, Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
}
