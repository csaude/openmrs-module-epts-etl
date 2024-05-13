package org.openmrs.module.epts.etl.conf;

import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.RelatedTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SharedPkDstConf extends DstConf implements ParentTable {
	
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
	
	private String refCode;
	
	private List<RefMapping> refMapping;
	
	public SharedPkDstConf() {
	}
	
	public SharedPkDstConf(String tableName, String refCode) {
		setTableName(tableName);
		setRefCode(refCode);
	}
	
	@Override
	public void clone(TableConfiguration toCloneFrom) {
		super.clone(toCloneFrom);
		
		if (toCloneFrom instanceof ParentTable) {
			ParentTable toCloneFromAsParent = (ParentTable) toCloneFrom;
			
			this.setChildTableConf(toCloneFromAsParent.getChildTableConf());
			this.setConditionalFields(toCloneFromAsParent.getConditionalFields());
			this.setDefaultValueDueInconsistency(toCloneFromAsParent.getDefaultValueDueInconsistency());
			this.setSetNullDueInconsistency(toCloneFromAsParent.isSetNullDueInconsistency());
			
			this.setRefCode(toCloneFromAsParent.getRefCode());
			this.setRefMapping(toCloneFromAsParent.getRefMapping());
		}
	}
	
	@Override
	public AbstractTableConfiguration getChildTableConf() {
		return childTableConf;
	}
	
	@Override
	public void setChildTableConf(TableConfiguration childTableConf) {
		this.childTableConf = (AbstractTableConfiguration) childTableConf;
		
		this.setRelatedSyncConfiguration(childTableConf.getRelatedSyncConfiguration());
	}
	
	@Override
	public List<Field> getConditionalFields() {
		return conditionalFields;
	}
	
	@Override
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
	public AbstractTableConfiguration getRelatedTabConf() {
		return this.childTableConf;
	}
	
	@Override
	public void setRelatedTabConf(TableConfiguration relatedTabConf) {
		this.childTableConf = (AbstractTableConfiguration) relatedTabConf;
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
			
			for (RefMapping map : this.getRefMapping()) {
				if (utilities.stringHasValue(mappingStr)) {
					mappingStr += ",";
				}
				
				mappingStr += map.toString();
			}
			
			str += ": " + mappingStr;
		}
		
		return str;
	}
	
	public String getRefCode() {
		return refCode;
	}
	
	public void setRefCode(String refCode) {
		this.refCode = refCode;
	}
	
	public List<RefMapping> getRefMapping() {
		return refMapping;
	}
	
	public void setRefMapping(List<RefMapping> mapping) {
		this.refMapping = mapping;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (obj instanceof String) {
			return this.refCode.equals(obj);
		}
		
		if (!(obj instanceof RelatedTable))
			return false;
		
		RelatedTable other = (RelatedTable) obj;
		
		if (this.hasRefCode() && other.hasRefCode()) {
			return this.getRefCode().equals(other.getRefCode());
		}
		
		if (!this.getRelatedTabConf().equals(other.getRelatedTabConf())) {
			return false;
		}
		
		for (RefMapping map : this.refMapping) {
			if (!other.getRefMapping().contains(map)) {
				return false;
			}
		}
		
		for (RefMapping map : other.getRefMapping()) {
			if (!this.getRefMapping().contains(map)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static SharedPkDstConf generateFromSrcConfSharedPkParent(DstConf child) throws ForbiddenOperationException {
		
		if (!child.useSharedPKKey())
			throw new ForbiddenOperationException("The source table '" + child.getTableName() + "' does not use shared pk!");
		
		ParentTable parent = child.getSharedKeyRefInfo();
		
		SharedPkDstConf ds = new SharedPkDstConf();
		
		ds.clone(parent);
		
		return ds;
	}
}
