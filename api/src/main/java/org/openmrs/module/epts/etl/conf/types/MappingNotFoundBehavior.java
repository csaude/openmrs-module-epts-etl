package org.openmrs.module.epts.etl.conf.types;

public enum MappingNotFoundBehavior {
	
	/**
	 * Marks the record as failed but allows the ETL process to continue.
	 */
	MARK_RECORD_AS_FAILED,
	
	/**
	 * Sets the destination field value to null.
	 */
	SET_TO_NULL,
	
	/**
	 * Aborts the ETL process by throwing an exception.
	 */
	ABORT_PROCESS;
	
	public boolean markRecordAsFailed() {
		return this.equals(MARK_RECORD_AS_FAILED);
	}
	
	public boolean setToNull() {
		return this.equals(SET_TO_NULL);
	}
	
	public boolean abortProcess() {
		return this.equals(ABORT_PROCESS);
	}
}
