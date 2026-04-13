package org.openmrs.module.epts.etl.conf.interfaces;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.exceptions.DuplicateMappingException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface RelatedTable extends TableConfiguration {
	
	Boolean isManualyConfigured();
	
	void setManualyConfigured(Boolean manualyConfigured);
	
	default Boolean hasRefCode() {
		return utilities.stringHasValue(getRefCode());
	}
	
	default RefMapping getSimpleRefMapping() throws ForbiddenOperationException {
		if (!isSimpleMapping()) {
			throw new ForbiddenOperationException("The ref is not simple!");
		}
		
		return this.getRefMapping().get(0);
	}
	
	default void addMapping(RefMapping mapping) {
		if (this.getRefMapping() == null) {
			this.setRefMapping(new ArrayList<>());
		}
		
		if (this.getRefMapping().contains(mapping))
			throw new DuplicateMappingException("The maaping you tried to add alredy exists on mapping field on table ["
			        + this.getRelatedTabConf().getTableName() + "]");
		
		this.getRefMapping().add(mapping);
	}
	
	String getRefCode();
	
	void setRefCode(String refCode);
	
	List<RefMapping> getRefMapping();
	
	void setRefMapping(List<RefMapping> mapping);
	
	default Boolean hasRelated() {
		return this.getRelatedTabConf() != null;
	}
	
	default String getParentColumnOnSimpleMapping() {
		return getSimpleRefMapping().getParentField().getName();
	}
	
	default String getChildColumnOnSimpleMapping() {
		return getSimpleRefMapping().getChildField().getName();
	}
	
	default String getParentColumnAsClassAttOnSimpleMapping() {
		return getSimpleRefMapping().getParentField().getNameAsClassAtt();
	}
	
	default String getChildColumnAsClassAttOnSimpleMapping() {
		return getSimpleRefMapping().getChildField().getNameAsClassAtt();
	}
	
	default Boolean isSimpleMapping() {
		return utilities.arraySize(this.getRefMapping()) <= 1;
	}
	
	default Boolean isCompositeMapping() {
		return utilities.arraySize(this.getRefMapping()) > 1;
	}
	
	@JsonIgnore
	default RefMapping getRefMappingByChildClassAttName(String attName) {
		
		for (RefMapping map : this.getRefMapping()) {
			if (map.getChildField().getNameAsClassAtt().equals(attName)) {
				return map;
			}
		}
		
		throw new ForbiddenOperationException("No mapping defined for att '" + attName + "'");
	}
	
	@JsonIgnore
	default Boolean checkIfContainsRefMappingByChildName(String attName) {
		try {
			return getRefMappingByChildName(attName) != null;
		}
		catch (ForbiddenOperationException e) {
			return false;
		}
	}
	
	@JsonIgnore
	default RefMapping getRefMappingByChildName(String attName) throws ForbiddenOperationException {
		
		for (RefMapping map : this.getRefMapping()) {
			if (map.getChildField().getName().equals(attName)) {
				return map;
			}
		}
		
		throw new ForbiddenOperationException("No mapping defined for att '" + attName + "'");
	}
	
	default List<RefMapping> getRefMappingByParentTableAtt(String attName) {
		List<RefMapping> referenced = new ArrayList<>();
		
		for (RefMapping map : this.getRefMapping()) {
			if (map.getParentField().getName().equals(attName)) {
				referenced.add(map);
			}
		}
		
		if (utilities.listHasNoElement(referenced)) {
			throw new ForbiddenOperationException("No mapping defined for att '" + attName + "'");
		}
		
		return referenced;
	}
	
	default RefMapping findRefMapping(String childField, String parentField) {
		RefMapping toFind = RefMapping.fastCreate(childField, parentField);
		
		for (RefMapping map : this.getRefMapping()) {
			if (map.equals(toFind))
				return map;
		}
		
		return null;
	}
	
	default List<Key> extractParentFieldsFromRefMapping() {
		List<Key> keys = new ArrayList<>();
		
		for (RefMapping f : this.getRefMapping()) {
			keys.add(f.getParentField());
		}
		
		return keys;
	}
	
	default List<Key> extractChildFieldsFromRefMapping() {
		List<Key> keys = new ArrayList<>();
		
		for (RefMapping f : this.getRefMapping()) {
			keys.add(f.getChildField());
		}
		
		return keys;
	}
	
	/**
	 * Generates the parent oid based on data within a child dstRecord
	 * 
	 * @param obj
	 * @return
	 */
	default Oid generateParentOidFromChild(EtlDatabaseObject obj) {
		
		Oid oid = new Oid();
		
		for (RefMapping map : this.getRefMapping()) {
			
			Key k = new Key(map.getParentFieldName());
			
			k.setValue(obj.getFieldValue(map.getChildFieldName()));
			k.setDataType(map.getChildField().getDataType());
			k.setTypeClass(map.getChildField().getTypeClass());
			oid.addKey(k);
		}
		
		oid.setFieldValuesLoaded(true);
		
		oid.setTabConf((TableConfiguration) obj.getRelatedConfiguration());
		
		return oid;
	}
	
	/**
	 * Retrieves child field instance
	 * 
	 * @param obj the object from which the child field instance is to be retrieved from
	 * @return the child field from the given object
	 */
	default org.openmrs.module.epts.etl.model.Field getChildInstanceField(EtlDatabaseObject obj) {
		if (isCompositeMapping())
			throw new ForbiddenOperationException("Not supported for composite mapping!");
		
		return obj.getField(getSimpleRefMapping().getChildFieldName());
	}
	
	/**
	 * Generates the child oid based on data within a parent dstRecord
	 * 
	 * @param obj
	 * @return
	 */
	default Oid generateChildOidFromParent(EtlDatabaseObject obj) {
		
		Oid oid = new Oid();
		
		for (RefMapping map : this.getRefMapping()) {
			
			Key k = new Key(map.getChildFieldName());
			
			k.setValue(obj.getFieldValue(map.getParentFieldName()));
			
			oid.addKey(k);
		}
		
		oid.setFieldValuesLoaded(true);
		
		return oid;
	}
	
	default Boolean hasMapping() {
		return utilities.listHasElement(this.getRefMapping());
	}
	
	default List<RefMapping> cloneAllMapping() {
		if (!hasMapping())
			return null;
		
		List<RefMapping> cloned = new ArrayList<>(this.getRefMapping().size());
		
		for (RefMapping map : this.getRefMapping()) {
			cloned.add(map.clone());
		}
		
		return cloned;
	}
	
	TableConfiguration getRelatedTabConf();
	
	void setRelatedTabConf(TableConfiguration relatedTabConf);
	
	UniqueKeyInfo parseRelationshipToSelfKey();
	
	String generateJoinCondition();
	
}
