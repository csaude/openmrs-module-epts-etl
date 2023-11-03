package org.openmrs.module.epts.etl.problems_solver.model.mozart;


public enum MozartFieldToFillType {
	UUID;
	
	public boolean isUuid(){
		return this.equals(UUID);
	}
}
