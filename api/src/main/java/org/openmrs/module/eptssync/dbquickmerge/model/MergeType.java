package org.openmrs.module.eptssync.dbquickmerge.model;


public enum MergeType {
	MISSING,
	EXISTING;
	
	public boolean isMissing() {
		return this.equals(MISSING);
	}
	
	public boolean isExisting() {
		return this.equals(EXISTING);
	}
}
