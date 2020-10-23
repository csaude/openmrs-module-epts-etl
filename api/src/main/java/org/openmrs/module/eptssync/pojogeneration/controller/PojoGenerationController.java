package org.openmrs.module.eptssync.pojogeneration.controller;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.monitor.EngineActivityMonitor;
import org.openmrs.module.eptssync.pojogeneration.engine.PojoGenerationEngine;

/**
 * This class is responsible for data base preparation
 * 
 * @author jpboane
 *
 */
public class PojoGenerationController extends OperationController {
	
	public PojoGenerationController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public Engine initRelatedEngine(EngineActivityMonitor monitor, RecordLimits limits) {
		return new PojoGenerationEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		return 1;
	}

	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		return 1;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}

	@Override
	public String getOperationType() {
		return SyncOperationConfig.SYNC_OPERATION_POJO_GENERATION;
	}	
}
