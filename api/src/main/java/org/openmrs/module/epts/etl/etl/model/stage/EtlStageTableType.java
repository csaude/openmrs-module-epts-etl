package org.openmrs.module.epts.etl.etl.model.stage;

public enum EtlStageTableType {
	
	SRC,
	DST;
	
	public boolean isSrc() {
		return this.equals(SRC);
	}
	
	public boolean isDst() {
		return this.equals(DST);
	}
	
}
