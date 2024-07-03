package org.openmrs.module.epts.etl.conf.types;

/**
 * The ETL action type
 */
public enum EtlActionType {
	
	// @formatter:off
	/**
	 * This action creates new record on ETL operation
	 */
	CREATE,
	
	/**
	 * This action deletes the record on ETL operation
	 */
	DELETE,
	
	/**
	 * This action update the record on ETL operation
	 */
	UPDATE,

	/**
	 * Undefined action.
	 */
	UNDEFINED;
	
	public boolean isCreate() {
		return this.equals(CREATE);
	}
	
	public boolean isDelete() {
		return this.equals(DELETE);
	}
	
	public boolean isUpdate() {
		return this.equals(UPDATE);
	}
	
	public boolean isUndefined() {
		return this.equals(UNDEFINED);
	}
	
}
