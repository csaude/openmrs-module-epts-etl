package org.openmrs.module.epts.etl.data.validation.missingrecords.controller;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.data.validation.missingrecords.engine.DetectMIssingRecordsEngine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;

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
	public TaskProcessor initRelatedEngine(EngineMonitor monitor, ThreadRecordIntervalsManager limits) {
		return new DetectMIssingRecordsEngine(monitor, limits);
	}
}
