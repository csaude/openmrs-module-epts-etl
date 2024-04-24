package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.utilities.AttDefinedElements;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RefMapping {
	
	private Key childField;
	
	private Key parentField;
	
	private String childFieldName;
	
	private String parentFieldName;
	
	private Object defaultValueDueInconsistency;
	
	private RefInfo refInfo;
	
	private boolean setNullDueInconsistency;
	
	/*
	 * Indicate if this parent can be ignored if not found in referenced table or not
	 */
	private boolean ignorable;
	
	public String getChildFieldName() {
		return childFieldName;
	}
	
	public void setChildFieldName(String childFieldName) {
		this.childFieldName = childFieldName;
		
		if (this.childField == null) {
			this.childField = new Key(this.childFieldName);
		}
		
	}
	
	public String getParentFieldName() {
		return parentFieldName;
	}
	
	public void setParentFieldName(String parentFieldName) {
		this.parentFieldName = parentFieldName;
		
		if (this.parentField == null) {
			this.parentField = new Key(this.parentFieldName);
		}
	}
	
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
	
	public Integer getDefaultValueDueInconsistencyAsInt() {
		return Integer.parseInt(this.defaultValueDueInconsistency.toString());
	}
	
	public void setDefaultValueDueInconsistency(Object defaultValueDueInconsistency) {
		this.defaultValueDueInconsistency = defaultValueDueInconsistency;
	}
	
	public String getChildFieldNameAsAttClass() {
		return this.childField.getNameAsClassAtt();
	}
	
	public String getParentFieldNameAsAttClass() {
		return this.parentField.getNameAsClassAtt();
	}
	
	public Key getChildField() {
		return childField;
	}
	
	public void setChildField(Key childField) {
		this.childField = childField;
		
		if (childField != null) {
			
			this.childFieldName = childField.getName();
		} else {
			this.childFieldName = null;
		}
	}
	
	public Key getParentField() {
		return parentField;
	}
	
	public void setParentField(Key parentField) {
		this.parentField = parentField;
		
		if (parentField != null) {
			this.parentFieldName = parentField.getName();
		} else {
			this.parentFieldName = null;
		}
	}
	
	public static RefMapping fastCreate(String childFieldName, String parentFieldName) {
		RefMapping ref = new RefMapping();
		
		ref.childField = new Key(childFieldName);
		ref.parentField = new Key(parentFieldName);
		
		ref.childFieldName = childFieldName;
		ref.parentFieldName = parentFieldName;
		
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
	
	@JsonIgnore
	public boolean isPrimitieveRefColumn() {
		return AttDefinedElements.isPrimitive(this.childField.getType());
	}
	
}
