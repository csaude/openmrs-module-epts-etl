package org.openmrs.module.epts.etl.conf.types;

/**
 * The ETL action type
 */
public enum EtlTemplatOverrideType {
	
	ADD,
	
	DELETE,
	
	UPDATE;
	
	public boolean isAdd() {
		return this.equals(ADD);
	}
	
	public boolean isDelete() {
		return this.equals(DELETE);
	}
	
	public boolean isUpdate() {
		return this.equals(UPDATE);
	}
}
