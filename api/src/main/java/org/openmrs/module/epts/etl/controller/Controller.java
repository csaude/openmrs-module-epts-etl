package org.openmrs.module.epts.etl.controller;

import org.openmrs.module.epts.etl.utilities.concurrent.MonitoredOperation;

public interface Controller extends MonitoredOperation{
	void logDebug(String msg);
	void logInfo(String msg);
	void logWarn(String msg);
	void logErr(String msg);
	
	String getControllerId();
	
	void markAsFinished();
	
	void killSelfCreatedThreads();
}
