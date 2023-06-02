package org.openmrs.module.eptssync.problems_solver.model.mozart;


public enum MozartTaskType {
	QUERY, BATCH;
	
	public boolean isQuery() {
		return this.equals(QUERY);
	}
	
	public boolean isBatch() {
		return this.equals(BATCH);
	}
}
