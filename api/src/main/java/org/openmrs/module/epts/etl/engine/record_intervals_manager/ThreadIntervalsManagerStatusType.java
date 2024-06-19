package org.openmrs.module.epts.etl.engine.record_intervals_manager;

public enum ThreadIntervalsManagerStatusType {
	NOT_INITIALIZED,
	BETWEEN_LIMITS,
	OUT_OF_LIMITS;
	
	public boolean isBetweenLimits() {
		return this.equals(BETWEEN_LIMITS);
	}
	
	public boolean isOutOfLimits() {
		return this.equals(OUT_OF_LIMITS);
	}
	
	public boolean isNotInitialized() {
		return this.equals(NOT_INITIALIZED);
	}
	
	public boolean isInitialized() {
		return !this.equals(NOT_INITIALIZED);
	}
}
