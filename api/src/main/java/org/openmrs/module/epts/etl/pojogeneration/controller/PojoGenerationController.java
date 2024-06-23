package org.openmrs.module.epts.etl.pojogeneration.controller;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.pojogeneration.engine.PojoGenerationEngine;
import org.openmrs.module.epts.etl.pojogeneration.model.PojoGenerationRecord;
import org.openmrs.module.epts.etl.pojogeneration.model.PojoGenerationSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * This class is responsible for data base preparation
 * 
 * @author jpboane
 */
public class PojoGenerationController extends OperationController<PojoGenerationRecord> {
	
	public PojoGenerationController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public TaskProcessor<PojoGenerationRecord> initRelatedTaskProcessor(Engine<PojoGenerationRecord> monitor,
	        IntervalExtremeRecord limits,  boolean runningInConcurrency) {
		return new PojoGenerationEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<PojoGenerationRecord> initMainSearchParams(ThreadRecordIntervalsManager<PojoGenerationRecord> intervalsMgt,
	        Engine<PojoGenerationRecord> engine) {
			
		return new PojoGenerationSearchParams(engine, intervalsMgt);
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
	
	public EtlConfiguration getEtlConfiguration() {
		return getProcessController().getConfiguration();
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return false;
	}

	@Override
	public void afterEtl(List<PojoGenerationRecord> objs, Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		
	}
}
