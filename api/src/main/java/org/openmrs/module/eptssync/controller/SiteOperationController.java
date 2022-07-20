package org.openmrs.module.eptssync.controller;

import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;

public abstract class SiteOperationController extends OperationController{

	protected String appOriginLocationCode;
	
	public SiteOperationController(ProcessController processController, SyncOperationConfig operationConfig, String appOriginLocationCode) {
		super(processController, operationConfig);
		
		this.appOriginLocationCode = appOriginLocationCode;
	}
	
	
	public String getAppOriginLocationCode() {
		return appOriginLocationCode;
	}
	
	public void setAppOriginLocationCode(String appOriginLocationCode) {
		this.appOriginLocationCode = appOriginLocationCode;
	}
	
}
