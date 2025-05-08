package org.openmrs.module.epts.etl.controller;

import org.openmrs.module.epts.etl.conf.ProcessFinalizerConf;

/**
 * Represents a process finalizer
 */
public interface ProcessFinalizer {
	
	void performeFinalizationTasks();
	
	ProcessFinalizerConf getRelatedFinalizerConf();
}
