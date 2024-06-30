package org.openmrs.module.epts.etl.conf;

public enum EtlDstType {
	
	// @formatter:off
	db,
	json,
	dump,
	csv;
	
	// @formatter:on
	public boolean isDefault() {
		return this.isDb();
	}
	
	public boolean isDb() {
		return this.equals(db);
	}
	
	public boolean isJson() {
		return this.equals(json);
	}
	
	public boolean isDump() {
		return this.equals(dump);
	}
	
	public boolean isCsv() {
		return this.equals(csv);
	}
	
	public boolean isFile() {
		return isCsv() || isJson() || isDump();
	}
	
}
