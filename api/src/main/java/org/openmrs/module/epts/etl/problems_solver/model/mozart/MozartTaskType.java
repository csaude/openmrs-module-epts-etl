package org.openmrs.module.epts.etl.problems_solver.model.mozart;


public enum MozartTaskType {
	QUERY, BATCH;
	
	public boolean isQuery() {
		return this.equals(QUERY);
	}
	
	public boolean isBatch() {
		return this.equals(BATCH);
	}
}
