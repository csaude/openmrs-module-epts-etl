package org.openmrs.module.eptssync.controller;

public interface DestinationOperationController extends Controller{
	public OperationController cloneForOrigin(String appOriginCode);
	public String getAppOriginLocationCode();
}
