package org.openmrs.module.epts.etl.controller;

import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;

public abstract class SiteOperationController extends OperationController{

	protected String appOriginLocationCode;
	
	public SiteOperationController(ProcessController processController, SyncOperationConfig operationConfig, String appOriginLocationCode) {
		super(processController, operationConfig);
		
		this.appOriginLocationCode = appOriginLocationCode;
		
		this.controllerId = this.controllerId + "_from_" + this.appOriginLocationCode;
	}
	
	
	public String getAppOriginLocationCode() {
		return appOriginLocationCode;
	}
	
	public void setAppOriginLocationCode(String appOriginLocationCode) {
		this.appOriginLocationCode = appOriginLocationCode;
	}
	
}
