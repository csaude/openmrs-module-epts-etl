package org.openmrs.module.epts.etl.controller;

import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;

public abstract class AbstractProcessFinalizer implements ProcessFinalizer{
	protected ProcessController relatedProcessController;
	
	public AbstractProcessFinalizer(ProcessController relatedProcessController) {
		this.relatedProcessController = relatedProcessController;
	}
	
	public ProcessController getRelatedProcessController() {
		return relatedProcessController;
	}
	
	public void setRelatedProcessController(ProcessController relatedProcessController) {
		this.relatedProcessController = relatedProcessController;
	}
	
	public SyncConfiguration getConfiguration() {
		return getRelatedProcessController().getConfiguration();
	}
}
