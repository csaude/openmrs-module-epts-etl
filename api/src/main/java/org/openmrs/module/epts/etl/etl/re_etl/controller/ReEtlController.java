package org.openmrs.module.epts.etl.etl.re_etl.controller;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.etl.controller.EtlController;

/**
 * This class is responsible for control the Re Etl process.
 * 
 * @author jpboane
 */
public class ReEtlController extends EtlController {
	
	public ReEtlController(ProcessController processController, EtlOperationConfig operationConfig,
	    String originLocationCode) {
		super(processController, operationConfig, originLocationCode);
	}
	
}
