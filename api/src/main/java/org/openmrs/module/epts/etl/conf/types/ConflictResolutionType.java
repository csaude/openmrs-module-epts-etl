package org.openmrs.module.epts.etl.conf.types;

public enum ConflictResolutionType {
	// @formatter:off
	
	
	NONE,
	
	/**
	 * Represents the applied resolution
	 */
	KEPT_EXISTING,
	
	
	/**
	 * Represents the applied resolution
	 */
	UPDATED_EXISTING,
	
	/**
	 * Represents the resolution to apply on conflict
	 */
	KEEP_EXISTING,
	
	/**
	 * Represents the resolution to apply on conflict
	 */
	UPDATE_EXISTING,
	
	/**
	 * Represents the resolution to apply on conflict
	 */
	MAKE_YOUR_DECISION;
	
	// @formatter:on
	public boolean keepExisting() {
		return this.equals(KEEP_EXISTING);
	}
	
	public boolean updateExisting() {
		return this.equals(UPDATE_EXISTING);
	}
	
	public boolean makeYourDecision() {
		return this.equals(MAKE_YOUR_DECISION);
	}
	
	public boolean keptExisting() {
		return this.equals(KEPT_EXISTING);
	}
	
	public boolean none() {
		return this.equals(NONE);
	}
}
