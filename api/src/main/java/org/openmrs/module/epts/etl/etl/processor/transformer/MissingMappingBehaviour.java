package org.openmrs.module.epts.etl.etl.processor.transformer;

public enum MissingMappingBehaviour {
	
	COMPLAIN,
	IGNORE,
	USE_DEFAULT;
	
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
