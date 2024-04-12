package org.openmrs.module.epts.etl.controller.conf;

public enum RefType {
	
	PARENT,
	CHILD;
	
	public boolean isParent() {
		return this.equals(PARENT);
	}
	
	public boolean isChild() {
		return this.equals(CHILD);
	}
	
}
