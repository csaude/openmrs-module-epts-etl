package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.model.base.EtlObject;

public interface EtlException {
	
	String getLocalizedMessage();
	
	void printStackTrace();
	
	Throwable getException();
	
	ActionOnEtlException getAction();
	
	EtlObject getEtlObject();
	
}
