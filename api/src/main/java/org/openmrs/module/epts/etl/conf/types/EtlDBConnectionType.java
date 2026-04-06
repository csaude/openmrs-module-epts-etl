package org.openmrs.module.epts.etl.conf.types;

public enum EtlDBConnectionType {
	
	MAIN,
	SRC,
	DST;
	
	public boolean isDst() {
		return this.equals(DST);
	}
	
	public boolean isSrc() {
		return this.equals(SRC);
	}
	
	public boolean isMain() {
		return this.equals(MAIN);
	}
}
