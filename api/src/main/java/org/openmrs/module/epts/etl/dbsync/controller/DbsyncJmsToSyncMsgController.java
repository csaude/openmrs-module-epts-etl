package org.openmrs.module.epts.etl.dbsync.controller;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.etl.controller.EtlController;

public class DbsyncJmsToSyncMsgController extends EtlController{

	public DbsyncJmsToSyncMsgController(ProcessController processController, EtlOperationConfig operationConfig,
	    String originLocationCode) {
		super(processController, operationConfig, originLocationCode);
	}
	
}
