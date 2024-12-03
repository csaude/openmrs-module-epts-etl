package org.openmrs.module.epts.etl.conf.types;

public enum AutoIncrementHandlingType {
	
	AS_SCHEMA_DEFINED,
	IGNORE_SCHEMA_DEFINITION;
	
	public boolean isAsSchemaDefined() {
		return this.equals(AS_SCHEMA_DEFINED);
	}
	
	public boolean isIgnoreSchemaDefinition() {
		return this.equals(IGNORE_SCHEMA_DEFINITION);
	}
	
}
