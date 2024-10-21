package org.openmrs.module.epts.etl.etl.model;

public enum RecusiveRelashionshipLevel {
	
	/**
	 * Table level recursive. It happens when a record has one or more parents on the very same
	 * table
	 */
	TABLE,
	
	/**
	 * Record level recursive. It happens when a record has a parent in a table which is also child
	 * of that record table
	 */
	
	RECORD;
	
	public boolean isTableLevel() {
		return this.equals(TABLE);
	}
	
	public boolean isRecordLevel() {
		return this.equals(RECORD);
	}
	
}
