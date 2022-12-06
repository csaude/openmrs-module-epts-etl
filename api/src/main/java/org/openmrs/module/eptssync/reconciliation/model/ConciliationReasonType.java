package org.openmrs.module.eptssync.reconciliation.model;


public enum ConciliationReasonType {
	MISSING, 
	OUTDATED, 
	PHANTOM,
	WRONG_RELATIONSHIPS; 
	
	public boolean isMissing() {
		return this.equals(MISSING);
	}
	
	public boolean isOutdate() {
		return this.equals(OUTDATED);
	}
	
	public boolean isPhantom() {
		return this.equals(PHANTOM);
	}
	
	public boolean isWrongRelationships() {
		return this.equals(WRONG_RELATIONSHIPS);
	}

}
