package org.openmrs.module.epts.etl.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.DuplicateMappingException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Define the specific refencial information between one table with onother;
 * 
 * @author jpboane
 */
public class RefInfo {
	
	public static final String PARENT_REF_TYPE = "PARENT";
	
	public static final String CHILD_REF_TYPE = "CHILD";
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String refCode;
	
	private List<RefMapping> mapping;
	
	private AbstractTableConfiguration childTableConf;
	
	private AbstractTableConfiguration parentTableConf;
	
	private List<Field> conditionalFields;
	
	public RefInfo() {
	}
	
	public static RefInfo init(String refCode) {
		RefInfo ref = new RefInfo();
		
		ref.refCode = refCode;
		
		return ref;
	}
	
	public List<Field> getConditionalFields() {
		return conditionalFields;
	}
	
	public void setConditionalFields(List<Field> conditionalFields) {
		this.conditionalFields = conditionalFields;
	}
	
	public String getParentColumnOnSimpleMapping() {
		return getSimpleRefMapping().getParentField().getName();
	}
	
	public String getChildColumnOnSimpleMapping() {
		return getSimpleRefMapping().getChildField().getName();
	}
	
	public String getParentColumnAsClassAttOnSimpleMapping() {
		return getSimpleRefMapping().getParentField().getNameAsClassAtt();
	}
	
	public String getChildColumnAsClassAttOnSimpleMapping() {
		return getSimpleRefMapping().getChildField().getNameAsClassAtt();
	}
	
	public boolean isSimpleMapping() {
		return utilities.arraySize(this.mapping) <= 1;
	}
	
	public RefMapping getSimpleRefMapping() {
		if (!isSimpleMapping()) {
			throw new ForbiddenOperationException("The ref is not simple!");
		}
		
		return this.mapping.get(0);
	}
	
	public void addMapping(RefMapping mapping) {
		if (this.mapping == null) {
			this.mapping = new ArrayList<>();
		}
		
		if (this.mapping.contains(mapping))
			throw new DuplicateMappingException("The maaping you tried to add alredy exists on mapping field on table ["
			        + this.getChildTableName() + "]");
		
		this.mapping.add(mapping);
	}
	
	public String getRefCode() {
		return refCode;
	}
	
	public void setRefCode(String refCode) {
		this.refCode = refCode;
	}
	
	public String getChildTableName() {
		return childTableConf.getTableName();
	}
	
	public String getParentTableName() {
		return getParentTableConf().getTableName();
	}
	
	public AbstractTableConfiguration getChildTableConf() {
		return childTableConf;
	}
	
	public Class<? extends DatabaseObject> getParentSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		return this.parentTableConf.getSyncRecordClass(application);
	}
	
	public Class<? extends DatabaseObject> getChildSyncRecordClass(AppInfo application) throws ForbiddenOperationException {
		return this.childTableConf.getSyncRecordClass(application);
	}
	
	public void setChildTableConf(AbstractTableConfiguration childTableConf) {
		this.childTableConf = childTableConf;
	}
	
	public AbstractTableConfiguration getParentTableConf() {
		return parentTableConf;
	}
	
	public void setParentTableConf(AbstractTableConfiguration parentTableConf) {
		this.parentTableConf = parentTableConf;
	}
	
	@JsonIgnore
	public boolean isSharedPk() {
		if (this.childTableConf.getSharePkWith() == null) {
			return false;
		} else if (utilities.arrayHasElement(childTableConf.getParentRefInfo())) {
			
			for (RefInfo refInfo : this.childTableConf.getParentRefInfo()) {
				if (refInfo.parentTableConf.getTableName().equalsIgnoreCase(this.parentTableConf.getSharePkWith())) {
					return true;
				}
			}
		}
		
		throw new ForbiddenOperationException("The related table of shared pk " + this.parentTableConf.getSharePkWith()
		        + " of table " + this.parentTableConf.getTableName() + " is not listed inparents!");
	}
	
	public List<RefMapping> getMapping() {
		return mapping;
	}
	
	public void setMapping(List<RefMapping> mapping) {
		this.mapping = mapping;
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String str = "";
		
		for (RefMapping map : this.mapping) {
			if (utilities.stringHasValue(str)) {
				str += ",";
			}
			
			str += map.toString();
		}
		
		return str;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof RefInfo))
			return false;
		
		RefInfo other = (RefInfo) obj;
		
		if (utilities.stringHasValue(this.refCode) && utilities.stringHasValue(other.refCode)) {
			return this.refCode.equals(other.refCode);
		}
		
		if (!this.childTableConf.equals(other.childTableConf)) {
			return false;
		}
		
		if (!this.parentTableConf.equals(other.parentTableConf)) {
			return false;
		}
		
		for (RefMapping map : this.mapping) {
			if (!other.mapping.contains(map)) {
				return false;
			}
		}
		
		for (RefMapping map : other.mapping) {
			if (!this.mapping.contains(map)) {
				return false;
			}
		}
		
		return true;
	}
	
	public RefMapping getRefMappingByChildClassAttName(String attName) {
		
		for (RefMapping map : this.mapping) {
			if (map.getChildField().getNameAsClassAtt().equals(attName)) {
				return map;
			}
		}
		
		throw new ForbiddenOperationException("No mapping defined for att '" + attName + "'");
	}
	
	public List<RefMapping> getRefMappingByParentTableAtt(String attName) {
		List<RefMapping> referenced = new ArrayList<>();
		
		for (RefMapping map : this.mapping) {
			if (map.getParentField().getName().equals(attName)) {
				referenced.add(map);
			}
		}
		
		if (utilities.arrayHasNoElement(referenced)) {
			throw new ForbiddenOperationException("No mapping defined for att '" + attName + "'");
		}
		
		return referenced;
	}
	
	public RefMapping findRefMapping(String childField, String parentField) {
		RefMapping toFind = RefMapping.fastCreate(childField, parentField);
		
		for (RefMapping map : this.mapping) {
			if (map.equals(toFind))
				return map;
		}
		
		return null;
	}
	
	public List<Key> extractParentFieldsFromRefMapping() {
		List<Key> keys = new ArrayList<>();
		
		for (RefMapping f : this.mapping) {
			keys.add(f.getParentField());
		}
		
		return keys;
	}
	
	public List<Key> extractChildFieldsFromRefMapping() {
		List<Key> keys = new ArrayList<>();
		
		for (RefMapping f : this.mapping) {
			keys.add(f.getChildField());
		}
		
		return keys;
	}
	
}
