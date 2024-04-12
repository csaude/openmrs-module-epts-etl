package org.openmrs.module.epts.etl.controller.conf;

import org.openmrs.module.epts.etl.utilities.AttDefinedElements;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RefMapping {
	
	private Key childField;
	
	private Key parentField;
	
	private Object defaultValueDueInconsistency;
	
	private RefInfo refInfo;
	
	private boolean setNullDueInconsistency;
	
	/*
	 * Indicate if this parent can be ignored if not found in referenced table or not
	 */
	private boolean ignorable;
	
	public boolean isIgnorable() {
		return ignorable;
	}
	
	public void setIgnorable(boolean ignorable) {
		this.ignorable = ignorable;
	}
	
	public boolean isSetNullDueInconsistency() {
		return setNullDueInconsistency;
	}
	
	public void setSetNullDueInconsistency(boolean setNullDueInconsistency) {
		this.setNullDueInconsistency = setNullDueInconsistency;
	}
	
	public RefInfo getRefInfo() {
		return refInfo;
	}
	
	public void setRefInfo(RefInfo refInfo) {
		this.refInfo = refInfo;
	}
	
	public Object getDefaultValueDueInconsistency() {
		return defaultValueDueInconsistency;
	}
	
	public void setDefaultValueDueInconsistency(Object defaultValueDueInconsistency) {
		this.defaultValueDueInconsistency = defaultValueDueInconsistency;
	}
	
	public Key getChildField() {
		return childField;
	}
	
	public void setChildField(Key childField) {
		this.childField = childField;
	}
	
	public Key getParentField() {
		return parentField;
	}
	
	public void setParentField(Key parentField) {
		this.parentField = parentField;
	}
	
	public static RefMapping fastCreate(String childFieldName, String parentFieldName) {
		RefMapping ref = new RefMapping();
		
		ref.childField = new Key(childFieldName);
		ref.parentField = new Key(parentFieldName);
		
		return ref;
	}
	
	@Override
	public String toString() {
		String str = "";
		
		String referencingTableName = "";
		String referencedTableName = "";
		
		if (this.refInfo != null) {
			referencingTableName = this.refInfo.getChildTableName() + ".";
			referencedTableName = this.refInfo.getParentTableName() + ".";
		}
		
		str += referencingTableName + this.childField.getName() + ">";
		str += referencedTableName + this.parentField.getName();
		
		return str;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RefMapping))
			return false;
		
		RefMapping otherObj = (RefMapping) obj;
		
		return this.childField.equals(otherObj.childField) && this.parentField.equals(otherObj.parentField);
	}
	
	@JsonIgnore
	public boolean isNumericRefColumn() {
		return AttDefinedElements.isNumeric(this.childField.getType());
	}
	
}
