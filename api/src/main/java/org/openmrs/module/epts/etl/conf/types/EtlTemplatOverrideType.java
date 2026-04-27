package org.openmrs.module.epts.etl.conf.types;

/**
 * The ETL action type
 */
public enum EtlTemplatOverrideType {
	
	ADD_TO_LIST,
	
	DELETE_ON_LIST,
	
	UPDATE_ON_LIST,
	
	OVERRIDE,
	
	EMPTY;
	
	public boolean isAddToList() {
		return this.equals(ADD_TO_LIST);
	}
	
	public boolean isDeleteOnList() {
		return this.equals(DELETE_ON_LIST);
	}
	
	public boolean isUpdateOnList() {
		return this.equals(UPDATE_ON_LIST);
	}
	
	public boolean isOverride() {
		return this.equals(OVERRIDE);
	}
	
	public boolean isEmpty() {
		return this.equals(EMPTY);
	}
}
