package org.openmrs.module.epts.etl.etl.model;

public enum LoadStatus {
	
	/**
	 * Undefined
	 */
	UNDEFINED,
	
	/**
	 * Load skipped
	 */
	SKIP,
	
	/**
	 * Sucessifuly loaded
	 */
	SUCCESS,
	
	/**
	 * Ready to load
	 */
	READY,
	
	/**
	 * Fail to load
	 */
	FAIL;
	
	public boolean isReady() {
		return this.equals(READY);
	}
	
	public boolean isSuccess() {
		return this.equals(SUCCESS);
	}
	
	public boolean isFail() {
		return this.equals(FAIL);
	}
	
	public boolean isSkip() {
		return this.equals(SKIP);
	}
	
	public boolean isUndefined() {
		return this.equals(UNDEFINED);
	}
	
	public boolean isLessThan(LoadStatus other) {
		return this.compareTo(other) < 0;
	}
}
