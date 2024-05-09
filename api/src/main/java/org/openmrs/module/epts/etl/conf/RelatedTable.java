package org.openmrs.module.epts.etl.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.DuplicateMappingException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents an related table, to an {@link AbstractTableConfiguration}
 */
public abstract class RelatedTable extends AbstractTableConfiguration {
	
	public static final String PARENT_REF_TYPE = "PARENT";
	
	public static final String CHILD_REF_TYPE = "CHILD";
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String refCode;
	
	private List<RefMapping> mapping;
	
	public RelatedTable() {
	}
	
	public RelatedTable(String tableName, String refCode) {
		super(tableName);
		
		this.refCode = refCode;
	}
	
	public RefMapping getSimpleRefMapping() {
		if (!isSimpleMapping()) {
			throw new ForbiddenOperationException("The ref is not simple!");
		}
		
		return this.mapping.get(0);
	}
	
	
	@Override
	public void clone(AbstractTableConfiguration toCloneFrom) {
		super.clone(toCloneFrom);
		
		RelatedTable toCloneFromAsRelated = (RelatedTable)toCloneFrom;
		
		this.refCode = toCloneFromAsRelated.refCode;
		this.mapping = toCloneFromAsRelated.mapping;
		}
	
	public void addMapping(RefMapping mapping) {
		if (this.mapping == null) {
			this.mapping = new ArrayList<>();
		}
		
		if (this.mapping.contains(mapping))
			throw new DuplicateMappingException("The maaping you tried to add alredy exists on mapping field on table ["
			        + this.getRelatedTabConf().getTableName() + "]");
		
		this.mapping.add(mapping);
	}
	
	public String getRefCode() {
		return refCode;
	}
	
	public void setRefCode(String refCode) {
		this.refCode = refCode;
	}
	
	public List<RefMapping> getMapping() {
		return mapping;
	}
	
	public void setMapping(List<RefMapping> mapping) {
		this.mapping = mapping;
	}
	
	public boolean hasRelated() {
		return this.getRelatedTabConf() != null;
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
		
		if (utilities.stringHasValue(this.refCode) && utilities.stringHasValue(other.refCode)) {
			return this.refCode.equals(other.refCode);
		}
		
		if (!this.getRelatedTabConf().equals(other.getRelatedTabConf())) {
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
	
	public boolean isCompositeMapping() {
		return utilities.arraySize(this.mapping) > 1;
	}
	
	@JsonIgnore
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
	
	/**
	 * Generates the parent oid based on data within a child record
	 * 
	 * @param obj
	 * @return
	 */
	public Oid generateParentOidFromChild(DatabaseObject obj) {
		
		Oid oid = new Oid();
		
		for (RefMapping map : this.mapping) {
			oid.addKey(new Key(map.getParentFieldName(), obj.getFieldValue(map.getChildFieldName())));
		}
		
		oid.setFieldValuesLoaded(true);
		
		return oid;
	}
	
	public boolean hasMapping() {
		return utilities.arrayHasElement(this.mapping);
	}
	
	public List<RefMapping> cloneAllMapping() {
		if (!hasMapping())
			return null;
		
		List<RefMapping> cloned = new ArrayList<>(this.mapping.size());
		
		for (RefMapping map : this.mapping) {
			cloned.add(map.clone());
		}
		
		return cloned;
	}
	
	public abstract AbstractTableConfiguration getRelatedTabConf();
	
	public abstract void setRelatedTabConf(AbstractTableConfiguration relatedTabConf);
	
	public abstract UniqueKeyInfo parseRelationshipToSelfKey();
	
	public abstract String generateJoinCondition();
	
}
