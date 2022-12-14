package org.openmrs.module.eptssync.controller.conf;


public enum ModelType {
	OPENMRS,
	OTHER;
	
	public boolean isOpenMRS() {
		return this.equals(OPENMRS);
	}
}
