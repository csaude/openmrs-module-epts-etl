package org.openmrs.module.epts.etl.conf.types;

public enum JoinType {
	
	INNER,
	LEFT;
	
	public boolean isLeftJoin() {
		return this.equals(LEFT);
	}
	
	public boolean isInnerJoin() {
		return this.equals(INNER);
	}
}
