package org.openmrs.module.epts.etl.conf;

import java.util.List;

import org.openmrs.module.epts.etl.model.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a parent table.
 */
public class ParentTable extends RelatedTable {
	
	private AbstractTableConfiguration childTableConf;
	
	private List<Field> conditionalFields;
	
	/*
	 * Generic defaultValueDueInconsistency value which will be applied to all auto mapped field if the ref is not specified
	 */
	private Object defaultValueDueInconsistency;
	
	/*
	 * Generic setNullDueInconsistency value which will be applied to all auto mapped field if the ref is not specified
	 */
	private boolean setNullDueInconsistency;
	
	public ParentTable() {
	}
	
	public ParentTable(String tableName, String refCode) {
		super(tableName, refCode);
	}
	
	public static ParentTable init(String tableName, String refCode) {
		ParentTable p = new ParentTable(tableName, refCode);
		
		return p;
	}
	
	@Override
	public void clone(AbstractTableConfiguration toCloneFrom) {
		super.clone(toCloneFrom);
		
		ParentTable toCloneFromAsParent = (ParentTable)toCloneFrom;
		
		this.childTableConf = toCloneFromAsParent.childTableConf;
		this.conditionalFields = toCloneFromAsParent.conditionalFields;
		this.defaultValueDueInconsistency = toCloneFromAsParent.defaultValueDueInconsistency;
		this.setNullDueInconsistency = toCloneFromAsParent.setNullDueInconsistency;
	}
	
	public AbstractTableConfiguration getChildTableConf() {
		return childTableConf;
	}
	
	public void setChildTableConf(AbstractTableConfiguration childTableConf) {
		this.childTableConf = childTableConf;
		
		this.setRelatedSyncConfiguration(childTableConf.getRelatedSyncConfiguration());
	}
	
	public List<Field> getConditionalFields() {
		return conditionalFields;
	}
	
	public void setConditionalFields(List<Field> conditionalFields) {
		this.conditionalFields = conditionalFields;
	}
	
	@Override
	public boolean isGeneric() {
		return false;
	}
	
	@Override
	public AppInfo getRelatedAppInfo() {
		return this.childTableConf.getRelatedAppInfo();
	}
	
	public Object getDefaultValueDueInconsistency() {
		return defaultValueDueInconsistency;
	}
	
	public void setDefaultValueDueInconsistency(Object defaultValueDueInconsistency) {
		this.defaultValueDueInconsistency = defaultValueDueInconsistency;
	}
	
	public boolean isSetNullDueInconsistency() {
		return setNullDueInconsistency;
	}
	
	public void setSetNullDueInconsistency(boolean setNullDueInconsistency) {
		this.setNullDueInconsistency = setNullDueInconsistency;
	}
	
	@Override
	public UniqueKeyInfo parseRelationshipToSelfKey() {
		UniqueKeyInfo uk = new UniqueKeyInfo(this);
		
		for (RefMapping map : this.getMapping()) {
			uk.addKey(new Key(map.getParentFieldName()));
		}
		
		return uk;
	}
	
	@Override
	public String generateJoinCondition() {
		String conditionFields = "";
		
		for (int i = 0; i < this.getMapping().size(); i++) {
			if (i > 0)
				conditionFields += " AND ";
			
			RefMapping field = this.getMapping().get(i);
			
			conditionFields += getRelatedTabConf().getTableAlias() + "." + field.getChildFieldName() + " = "
			        + this.getTableAlias() + "." + field.getParentFieldName();
		}
		
		return conditionFields;
	}
	
	@Override
	public AbstractTableConfiguration getRelatedTabConf() {
		return this.childTableConf;
	}
	
	@Override
	public void setRelatedTabConf(AbstractTableConfiguration relatedTabConf) {
		this.childTableConf = relatedTabConf;
	}
	
	public boolean hasConditionalFields() {
		return utilities.arrayHasElement(this.conditionalFields);
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String str = this.getTableName();
		
		str += this.hasRelated() ? ">>" + this.getRelatedTabConf().getTableName() : "";
		
		String mappingStr = "";
		
		if (hasMapping()) {
			
			for (RefMapping map : this.getMapping()) {
				if (utilities.stringHasValue(mappingStr)) {
					mappingStr += ",";
				}
				
				mappingStr += map.toString();
			}
			
			str += ": " + mappingStr;
		}
		
		return str;
	}
}
