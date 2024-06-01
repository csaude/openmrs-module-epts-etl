package org.openmrs.module.epts.etl.data.validation.missingrecords.engine;

import java.sql.Connection;

import org.openmrs.module.epts.etl.data.validation.missingrecords.controller.DetectMissingRecordsController;
import org.openmrs.module.epts.etl.data.validation.missingrecords.model.DetectMissingRecordsSearchParams;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;

public class DetectMIssingRecordsEngine extends EtlEngine {
	
	public DetectMIssingRecordsEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		super(monitor, limits);
	}
	
	@Override
	public DetectMissingRecordsController getRelatedOperationController() {
		return (DetectMissingRecordsController) super.getRelatedOperationController();
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadLimitsManager limits, Connection conn) {
		return new DetectMissingRecordsSearchParams(getEtlConfiguration(), limits, this);
	}
}
