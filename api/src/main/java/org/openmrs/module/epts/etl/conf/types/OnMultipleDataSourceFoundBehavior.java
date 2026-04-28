package org.openmrs.module.epts.etl.conf.types;

public enum OnMultipleDataSourceFoundBehavior {
	
	USE_LAST,
	
	USE_FIRST,
	
	ABORT_PROCESS;
	
	public boolean useLast() {
		return this.equals(USE_LAST);
	}
	
	public boolean abortProcess() {
		return this.equals(ABORT_PROCESS);
	}
	
	public boolean useFirst() {
		return this.equals(USE_FIRST);
	}
}
