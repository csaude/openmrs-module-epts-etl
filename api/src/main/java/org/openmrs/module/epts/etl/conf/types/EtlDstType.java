package org.openmrs.module.epts.etl.conf.types;

public enum EtlDstType {
	
	// @formatter:off
	db,
	json,
	dump,
	csv,
	console,
	popup;
	
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
	
	public boolean isConsole() {
		return this.equals(console);
	}
	
	public boolean isPopUp() {
		return this.equals(popup);
	}
	
	public boolean isInstantaneo() {
		return this.isPopUp() || this.isConsole();
	}
}
