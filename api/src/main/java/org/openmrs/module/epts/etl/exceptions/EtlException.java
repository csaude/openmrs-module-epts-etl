package org.openmrs.module.epts.etl.exceptions;

public interface EtlException {
	
	String getLocalizedMessage();
	
	void printStackTrace();
	
	Throwable getException();
}
