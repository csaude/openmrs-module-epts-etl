package org.openmrs.module.eptssync.controller.conf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.Field;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;

public class MappedTableInfo extends SyncTableConfiguration {
	
	private List<FieldsMapping> fieldsMapping;
	
	private SyncTableConfiguration relatedTableConfiguration;
	
	public MappedTableInfo() {
	}
	
	public void setRelatedTableConfiguration(SyncTableConfiguration relatedTableConfiguration) {
		this.relatedTableConfiguration = relatedTableConfiguration;
	}
	
	public SyncTableConfiguration getRelatedTableConfiguration() {
		return relatedTableConfiguration;
	}
	
	public List<FieldsMapping> getFieldsMapping() {
		return fieldsMapping;
	}
	
	public void setFieldsMappings(List<FieldsMapping> fieldsMapping) {
		this.fieldsMapping = fieldsMapping;
	}
	
	private void addMapping(String srcField, String destField) {
		if (this.fieldsMapping == null) {
			this.fieldsMapping = new ArrayList<FieldsMapping>();
		}
		
		FieldsMapping fm = new FieldsMapping(srcField, destField);
		
		if (this.fieldsMapping.contains(fm))
			throw new ForbiddenOperationException("The field [" + fm + "] already exists on mapping");
		
		this.fieldsMapping.add(fm);
	}
	
	public static MappedTableInfo generateFromSyncTableConfiguration(SyncTableConfiguration tableConfiguration) {
		if (!tableConfiguration.isFullLoaded())
			throw new ForbiddenOperationException("The tableInfo is not full loaded!");
		
		MappedTableInfo mappedTableInfo = new MappedTableInfo();
		
		mappedTableInfo.clone(tableConfiguration);
		
		mappedTableInfo.generateMappingFields(tableConfiguration);
		
		return mappedTableInfo;
	}
	
	public void generateMappingFields(SyncTableConfiguration tableConfiguration) {
		for (Field field : tableConfiguration.getFields()) {
			this.addMapping(field.getName(), field.getName());
		}		
	}
	
	public String getMappedField(String srcField) {
		List<FieldsMapping> machedFields = new ArrayList<FieldsMapping>();
		
		for (FieldsMapping field : this.fieldsMapping) {
			if (field.getSrcField().equals(srcField)) {
				machedFields.add(field);
				
				if (machedFields.size() > 1) {
					throw new ForbiddenOperationException("Cannot determine the mapping field for '" + srcField
					        + "' since it has multiple matching fields");
				}
			}
		}
		
		if (machedFields.isEmpty()) {
			throw new ForbiddenOperationException("Cannot determine the mapping field for '" + srcField + "'");
		}
		
		return machedFields.get(0).getDestField();
	}
	
	@Override
	protected synchronized void fullLoad(Connection conn) {
		try {
			getPrimaryKey(conn);
			loadUniqueKeys(conn);
			
			loadParents(conn);
			loadChildren(conn);
			
			loadConditionalParents(conn);
			
			setFields(DBUtilities.getTableFields(getTableName(), DBUtilities.determineSchemaName(conn), conn));
			org.postgresql.Driver d;
			
			this.fullLoaded = true;
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public DatabaseObject generateMappedObject(DatabaseObject srcObject, AppInfo application) {
		try {
			DatabaseObject mappedObject = getSyncRecordClass(application).newInstance();
			
			for (FieldsMapping fieldsMapping : this.getFieldsMapping()) {
				
				Object srcValue = srcObject.getFieldValue(fieldsMapping.getSrcFieldAsClassField());
				
				mappedObject.setFieldValue(fieldsMapping.getDestFieldAsClassField(), srcValue);
			}
			
			return mappedObject;
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
	}
}
