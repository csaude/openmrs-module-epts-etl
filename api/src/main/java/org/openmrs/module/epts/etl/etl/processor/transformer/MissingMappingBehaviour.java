package org.openmrs.module.epts.etl.etl.processor.transformer;

public enum MissingMappingBehaviour {
	
	COMPLAIN,
	IGNORE,
	USE_INPUT,
	USE_DEFAULT;
	
	public boolean useInputOnMissingMapping() {
		return this.equals(USE_INPUT);
	}
	
	public boolean complainOnMissingMapping() {
		return this.equals(COMPLAIN);
	}
	
	public boolean ignoreOnMissingMapping() {
		return this.equals(IGNORE);
	}
	
	public boolean useDefaultOnMissingMapping() {
		return this.equals(USE_DEFAULT);
	}
}
