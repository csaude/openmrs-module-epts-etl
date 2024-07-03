package org.openmrs.module.epts.etl.conf.types;

public enum ConflictResolutionType {
	// @formatter:off
	
	KEEP_EXISTING,
	UPDATE_EXISTING,
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
}
