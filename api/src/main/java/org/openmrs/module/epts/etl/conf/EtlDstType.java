package org.openmrs.module.epts.etl.conf;

public enum AppInfoType {
	
	// @formatter:off
	db,
	json,
	dump,
	csv;
	
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

	boolean isFile() {
		return isCsv() || isJson() || isDump();
	}
	
}
