package org.openmrs.module.epts.etl.conf.types;

public enum EtlTotalRecordsCountStrategy {
	
	/**
	 * The count of total records will not done, instead the max recordId will be used as total
	 * records
	 */
	USE_MAX_RECORD_ID_AS_COUNT,
	
	/**
	 * The calculation will be done once and on the following will be taken from database
	 */
	COUNT_ONCE,
	/**
	 * The calculation will be done always
	 */
	COUNT_ALWAYS;
	
	public boolean isUseMaxRecordIdAsCount() {
		return this.equals(USE_MAX_RECORD_ID_AS_COUNT);
	}
	
	public boolean isCountOnce() {
		return this.equals(COUNT_ONCE);
	}
	
	public boolean isCountAlways() {
		return this.equals(COUNT_ALWAYS);
	}
}
