package org.openmrs.module.epts.etl.dbquickmerge.controller;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.dbquickmerge.engine.DBQuickMergeEngine;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;

/**
 * This class is responsible for control the quick merge process. The quick merge process
 * immediately merge records from the source to the destination db This process assume that the
 * source and destination are located in the same network
 * 
 * @author jpboane
 */
public class DBQuickMergeController extends EtlController {
	
	public DBQuickMergeController(ProcessController processController, EtlOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		return new DBQuickMergeEngine(monitor, limits);
	}
}
