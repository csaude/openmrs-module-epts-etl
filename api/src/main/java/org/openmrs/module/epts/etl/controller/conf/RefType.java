package org.openmrs.module.epts.etl.controller.conf;

public enum RefType {
	
	IMPORTED,
	EXPORTED;
	
	public boolean isImported() {
		return this.equals(IMPORTED);
	}
	
	public boolean isExported() {
		return this.equals(EXPORTED);
	}
	
}
