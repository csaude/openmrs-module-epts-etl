package org.openmrs.module.epts.etl.databasepreparation.controller;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.databasepreparation.engine.DatabasePreparationEngine;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;

/**
 * This class is responsible for data base preparation
 * 
 * @author jpboane
 */
public class DatabasePreparationController extends OperationController {
	
	public DatabasePreparationController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DatabasePreparationEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlConfiguration config) {
		return 1;
	}
	
	@Override
	public long getMaxRecordId(EtlConfiguration config) {
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
	
}
