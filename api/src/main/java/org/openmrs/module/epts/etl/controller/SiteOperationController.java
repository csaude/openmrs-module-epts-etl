package org.openmrs.module.epts.etl.controller;

import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public abstract class SiteOperationController<T extends EtlDatabaseObject> extends OperationController<T>{

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
