package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.SyncExtraDataSource;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class SyncDestinationTableConfiguration extends SyncTableConfiguration {
	
	private List<FieldsMapping> fieldsMapping;
	
	private List<SyncExtraDataSource> extraDataSource;
	
	private AppInfo relatedAppInfo;
	
	private SyncTableConfiguration sourceTableConfiguration;
	
	public SyncDestinationTableConfiguration() {
	}
	
	public SyncTableConfiguration getSourceTableConfiguration() {
		return sourceTableConfiguration;
	}
	
	public void setSourceTableConfiguration(SyncTableConfiguration sourceTableConfiguration) {
		this.sourceTableConfiguration = sourceTableConfiguration;
	}
	
	public List<SyncExtraDataSource> getExtraDataSource() {
		return extraDataSource;
	}
	
	public void setExtraDataSource(List<SyncExtraDataSource> extraDataSource) {
		this.extraDataSource = extraDataSource;
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
		
		FieldsMapping fm = new FieldsMapping(srcField, this.getTableName(), destField);
		
		if (this.fieldsMapping.contains(fm))
			throw new ForbiddenOperationException("The field [" + fm + "] already exists on mapping");
		
		this.fieldsMapping.add(fm);
	}
	
	public static List<SyncDestinationTableConfiguration> generateFromSyncTableConfiguration(
	        SyncTableConfiguration tableConfiguration) {
		if (!tableConfiguration.isFullLoaded())
			throw new ForbiddenOperationException("The tableInfo is not full loaded!");
		
		SyncDestinationTableConfiguration mappedTableInfo = new SyncDestinationTableConfiguration();
		
		mappedTableInfo.clone(tableConfiguration);
		
		mappedTableInfo.generateMappingFields(tableConfiguration);
		
		mappedTableInfo.loadAdditionalFieldsInfo();
		
		mappedTableInfo.setSourceTableConfiguration(tableConfiguration);
		
		return utilities.parseObjectToList(mappedTableInfo, SyncDestinationTableConfiguration.class);
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
	
	public AppInfo getRelatedAppInfo() {
		return relatedAppInfo;
	}
	
	public void setRelatedAppInfo(AppInfo relatedAppInfo) {
		this.relatedAppInfo = relatedAppInfo;
	}
	
	@Override
	public synchronized void fullLoad(Connection conn) {
		try {
			getPrimaryKey(conn);
			
			loadUniqueKeys(conn);
			
			loadParents(conn);
			loadChildren(conn);
			
			loadConditionalParents(conn);
			
			setFields(DBUtilities.getTableFields(getTableName(), DBUtilities.determineSchemaName(conn), conn));
			
			this.fullLoaded = true;
			
			OpenConnection srcConn = getSourceTableConfiguration().getMainApp().openConnection();
			
			try {
				for (SyncExtraDataSource src : this.getExtraDataSource()) {
					src.setRelatedMappedTable(this);
					
					src.fullLoad(srcConn);
				}
			}
			catch (Exception e) {
				srcConn.finalizeConnection();
			}
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public synchronized void fullLoad() throws DBException {
		OpenConnection conn = this.relatedAppInfo.openConnection();
		
		try {
			this.fullLoad(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	public SyncExtraDataSource findAdditionalDataSrc(String tableName) {
		if (!utilities.arrayHasElement(this.extraDataSource)) {
			return null;
		}
		
		for (SyncExtraDataSource src : this.extraDataSource) {
			if (src.getName().equals(tableName)) {
				return src;
			}
		}
		
		throw new ForbiddenOperationException("The table '" + tableName + "'cannot be foud on the mapping src tables");
	}
	
	public DatabaseObject generateMappedObject(DatabaseObject srcObject, Connection srcConn, AppInfo srcAppInfo,
	        AppInfo dstAppInfo) throws DBException, ForbiddenOperationException {
		try {
			
			List<DatabaseObject> srcObjects = new ArrayList<>();
			
			srcObjects.add(srcObject);
			
			if (utilities.arrayHasElement(this.extraDataSource)) {
				for (SyncExtraDataSource mappingInfo : this.extraDataSource) {
					DatabaseObject relatedSrcObject = mappingInfo.loadRelatedSrcObject(srcObject, srcConn, srcAppInfo);
					
					if (relatedSrcObject == null) {
						relatedSrcObject = mappingInfo.getSyncRecordClass(srcAppInfo).newInstance();
					}
					
					srcObjects.add(relatedSrcObject);
					
				}
			}
			
			DatabaseObject mappedObject = getSyncRecordClass(dstAppInfo).newInstance();
			
			for (FieldsMapping fieldsMapping : this.getFieldsMapping()) {
				
				Object srcValue = fieldsMapping.retrieveValue(this, srcObjects, dstAppInfo, srcConn);
				
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
	
	public void loadAdditionalFieldsInfo() {
		if (!utilities.arrayHasElement(this.fieldsMapping)) {
			throw new ForbiddenOperationException("The mapping fields was not loaded yet");
		}
		
		for (FieldsMapping field : this.fieldsMapping) {
			if (!utilities.stringHasValue(field.getDataSourceName())) {
				field.setDataSourceName(this.getSourceTableConfiguration().getTableName());
			}
		}
	}
}
