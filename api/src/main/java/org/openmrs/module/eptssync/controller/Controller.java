package org.openmrs.module.eptssync.controller;

import org.openmrs.module.eptssync.utilities.concurrent.MonitoredOperation;

public interface Controller extends MonitoredOperation{
	void logInfo(String msg);

	String getControllerId();
	
	void markAsFinished();
}
