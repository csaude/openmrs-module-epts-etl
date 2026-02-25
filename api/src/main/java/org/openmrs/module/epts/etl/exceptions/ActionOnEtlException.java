package org.openmrs.module.epts.etl.exceptions;

public enum ActionOnEtlException {
	ABORT,
	LOG,
	IGNORE;
	
	public boolean log() {
		return this.equals(LOG);
	}
	
	public boolean abort() {
		return this.equals(ABORT);
	}
	
	public boolean ignore() {
		return this.equals(IGNORE);
	}
	
}
