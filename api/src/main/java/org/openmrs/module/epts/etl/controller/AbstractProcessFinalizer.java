package org.openmrs.module.epts.etl.controller;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;

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
	
	public EtlConfiguration getConfiguration() {
		return getRelatedProcessController().getConfiguration();
	}
}
