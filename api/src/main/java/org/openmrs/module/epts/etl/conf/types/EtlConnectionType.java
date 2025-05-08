package org.openmrs.module.epts.etl.conf.types;

public enum EtlConnectionType {
	
	mainConnInfo,
	srcConnInfo,
	dstConnInfo;
	
	public boolean isMainConnInfo() {
		return this.equals(mainConnInfo);
	}
	
	public boolean isSrcConnInfo() {
		return this.equals(srcConnInfo);
	}
	
	public boolean isDstConnInfo() {
		return this.equals(dstConnInfo);
	}
	
}
