package org.openmrs.module.epts.etl.pojogeneration.controller;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.pojogeneration.engine.PojoGenerationEngine;

/**
 * This class is responsible for data base preparation
 * 
 * @author jpboane
 */
public class PojoGenerationController extends OperationController {
	
	public PojoGenerationController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public TaskProcessor initRelatedEngine(EngineMonitor monitor, ThreadRecordIntervalsManager limits) {
		return new PojoGenerationEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		return 1;
	}
	
	@Override
	public long getMaxRecordId(EtlItemConfiguration config) {
		return 1;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	public EtlConfiguration getConfiguration() {
		return getProcessController().getConfiguration();
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return false;
	}
}
