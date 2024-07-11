package org.openmrs.module.epts.etl.data.validation.missingrecords.processor;

import org.openmrs.module.epts.etl.data.validation.missingrecords.controller.DetectMissingRecordsController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public class DetectMIssingRecordsProcessor extends EtlProcessor {
	
	public DetectMIssingRecordsProcessor(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public DetectMissingRecordsController getRelatedOperationController() {
		return (DetectMissingRecordsController) super.getRelatedOperationController();
	}
	
}
