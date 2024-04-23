package org.openmrs.module.epts.etl.pojogeneration.controller;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
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
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
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
