package org.openmrs.module.epts.etl.data.validation.missingrecords.controller;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.data.validation.missingrecords.model.DetectMissingRecordsSearchParams;
import org.openmrs.module.epts.etl.data.validation.missingrecords.processor.DetectMIssingRecordsProcessor;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public class DetectMissingRecordsController extends EtlController {
	
	public DetectMissingRecordsController(ProcessController processController, EtlOperationConfig operationConfig,
	    String originLocationCode) {
		super(processController, operationConfig, originLocationCode);
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits,  boolean runningInConcurrency) {
		return new DetectMIssingRecordsProcessor(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt,
	        Engine<EtlDatabaseObject> engine) {
		
		return new DetectMissingRecordsSearchParams(engine, intervalsMgt);
	}
	
}
