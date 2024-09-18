package org.openmrs.module.epts.etl.conf.types;

public enum JoinType {
	
	INNER,
	LEFT,
	RIGHT;
	
	public boolean isLeftJoin() {
		return this.equals(LEFT);
	}
	
	public boolean isInnerJoin() {
		return this.equals(INNER);
	}
	
	public boolean isRightJoin() {
		return this.equals(RIGHT);
	}
}
