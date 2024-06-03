package org.openmrs.module.epts.etl.dbextract.controller;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.dbextract.engine.DbExtractEngine;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;

/**
 * This class is responsible for control the Db Extract process.
 * 
 * @author jpboane
 */
public class DbExtractController extends EtlController {
	
	public DbExtractController(ProcessController processController, EtlOperationConfig operationConfig,
	    String originLocationCode) {
		super(processController, operationConfig, originLocationCode);
		
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, ThreadRecordIntervalsManager limits) {
		return new DbExtractEngine(monitor, limits);
	}
	
}
