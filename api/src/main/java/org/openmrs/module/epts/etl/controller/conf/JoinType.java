package org.openmrs.module.epts.etl.controller.conf;

public enum JoinType {
	
	INNER,
	LEFT;
	
	public boolean isLeftJoin() {
		return this.equals(LEFT);
	}
}
