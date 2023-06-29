package org.openmrs.module.eptssync.controller.conf;

import org.openmrs.module.eptssync.utilities.AttDefinedElements;

/**
 * This class is used to map fields between an source table and destination table
 * 
 * @author jpboane
 */
public class FieldsMapping {
	
	private String srcField;
	
	private String destField;
	
	public FieldsMapping() {
	}
	
	public FieldsMapping(String srcField, String destField) {
		this.srcField = srcField;
		this.destField = destField;
	}
	
	public String getSrcField() {
		return srcField;
	}
	
	public void setSrcField(String srcField) {
		this.srcField = srcField;
	}
	
	public String getDestField() {
		return destField;
	}
	
	public void setDestField(String destField) {
		this.destField = destField;
	}
	
	public String getSrcFieldAsClassField(){
		return AttDefinedElements.convertTableAttNameToClassAttName(this.srcField);
	}
	
	public String getDestFieldAsClassField(){
		return AttDefinedElements.convertTableAttNameToClassAttName(this.destField);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof FieldsMapping))
			return false;
		
		FieldsMapping fm = (FieldsMapping) obj;
		
		return this.destField.equals(fm.destField);
	}
	
	@Override
	public String toString() {
		return "[srcField: " + srcField + ", destField: " + destField + "]";
	}
	
}
