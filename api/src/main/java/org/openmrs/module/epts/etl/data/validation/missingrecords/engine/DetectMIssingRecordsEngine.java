package org.openmrs.module.epts.etl.data.validation.missingrecords.engine;

import org.openmrs.module.epts.etl.data.validation.missingrecords.controller.DetectMissingRecordsController;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;

public class DetectMIssingRecordsEngine extends EtlEngine {
	
	public DetectMIssingRecordsEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public DetectMissingRecordsController getRelatedOperationController() {
		return (DetectMissingRecordsController) super.getRelatedOperationController();
	}
	
}
