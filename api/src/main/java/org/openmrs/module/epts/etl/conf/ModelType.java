package org.openmrs.module.epts.etl.conf;


public enum ModelType {
	OPENMRS,
	OTHER;
	
	public boolean isOpenMRS() {
		return this.equals(OPENMRS);
	}
}
