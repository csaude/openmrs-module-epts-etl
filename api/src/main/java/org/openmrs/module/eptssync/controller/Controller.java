package org.openmrs.module.eptssync.controller;

import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;

public interface Controller extends MonitoredOperation{
	void logDebug(String msg);
	void logInfo(String msg);
	void logWarn(String msg);
	void logErr(String msg);
	
	String getControllerId();
	
	void markAsFinished();
}
