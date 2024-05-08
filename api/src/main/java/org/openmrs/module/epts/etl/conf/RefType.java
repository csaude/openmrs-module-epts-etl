package org.openmrs.module.epts.etl.conf;

public enum RefType {
	
	IMPORTED,
	EXPORTED;
	
	public boolean isImported() {
		return this.equals(IMPORTED);
	}
	
	public boolean isExported() {
		return this.equals(EXPORTED);
	}
	
	
	public boolean isChild() {
		return isImported();
	}
	
	public boolean isParent() {
		return isExported();
	}
	
}
