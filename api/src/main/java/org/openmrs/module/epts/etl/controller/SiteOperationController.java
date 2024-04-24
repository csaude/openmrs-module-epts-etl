package org.openmrs.module.epts.etl.controller;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;

public abstract class SiteOperationController extends OperationController{

	protected String appOriginLocationCode;
	
	public SiteOperationController(ProcessController processController, EtlOperationConfig operationConfig, String appOriginLocationCode) {
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
