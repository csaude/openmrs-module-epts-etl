package org.openmrs.module.epts.etl.exceptions;

public enum ActionOnEtlException {
	ABORT_PROCESS,
	LOG,
	IGNORE;
	
	public boolean log() {
		return this.equals(LOG);
	}
	
	public boolean abort() {
		return this.equals(ABORT_PROCESS);
	}
	
	public boolean ignore() {
		return this.equals(IGNORE);
	}
	
}
