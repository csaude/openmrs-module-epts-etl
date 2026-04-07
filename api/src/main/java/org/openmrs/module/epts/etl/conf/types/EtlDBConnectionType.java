package org.openmrs.module.epts.etl.conf.types;

public enum EtlDBConnectionType {
	
	mainConnInfo,
	srcConnInfo,
	dstConnInfo;
	
	public boolean isDst() {
		return this.equals(dstConnInfo);
	}
	
	public boolean isSrc() {
		return this.equals(srcConnInfo);
	}
	
	public boolean isMain() {
		return this.equals(mainConnInfo);
	}
}
