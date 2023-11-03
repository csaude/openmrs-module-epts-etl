package org.openmrs.module.epts.etl.engine;


public enum MigrationFinalCheckStatus {
	NOT_INITIALIZED,
	ONGOING,
	DONE,
	IGNORED;
	
	public boolean notInitialized() {
		return this.equals(NOT_INITIALIZED);
	}
	
	public boolean onGoing() {
		return this.equals(ONGOING);
	}
	
	public boolean done() {
		return this.equals(DONE);
	}
	
	public boolean ignored() {
		return this.equals(IGNORED);
	}
	
}
