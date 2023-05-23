package org.openmrs.module.eptssync.problems_solver.model.mozart;


public enum MozartFieldToFillType {
	UUID;
	
	public boolean isUuid(){
		return this.equals(UUID);
	}
}
