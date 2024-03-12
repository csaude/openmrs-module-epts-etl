package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.EtlExtraDataSource;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class DstConf extends SyncDataConfiguration {
	
	private List<FieldsMapping> fieldsMapping;
	
	private AppInfo relatedAppInfo;
	
	private SrcConf srcConf;
	
	private SyncTableConfiguration dstTableConf;
	
	private boolean fullLoaded;
	
	public DstConf() {
	}
	
	public SyncTableConfiguration getDstTableConf() {
		return dstTableConf;
	}
	
	public void setDstTableConf(SyncTableConfiguration dstTable) {
		this.dstTableConf = dstTable;
	}
	
	public SrcConf getSrcConf() {
		return srcConf;
	}
	
	public void setSrcConf(SrcConf srcSyncConfiguration) {
		this.srcConf = srcSyncConfiguration;
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
		
		FieldsMapping fm = new FieldsMapping(srcField, this.dstTableConf.getTableName(), destField);
		
		if (this.fieldsMapping.contains(fm))
			throw new ForbiddenOperationException("The field [" + fm + "] already exists on mapping");
		
		this.fieldsMapping.add(fm);
	}
	
	public static DstConf generateFromSyncTableConfiguration(SrcConf srcSyncConfig) {
		if (!srcSyncConfig.isFullLoaded())
			throw new ForbiddenOperationException("The tableInfo is not full loaded!");
		
		DstConf dstSyncConfiguration = new DstConf();
		
		dstSyncConfiguration.dstTableConf.clone(srcSyncConfig.getMainSrcTableConf());
		
		dstSyncConfiguration.generateMappingFields(srcSyncConfig.getMainSrcTableConf());
		
		dstSyncConfiguration.loadAdditionalFieldsInfo();
		
		dstSyncConfiguration.setSrcConf(srcSyncConfig);
		
		return dstSyncConfiguration;
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
		
		return machedFields.get(0).getDstField();
	}
	
	public AppInfo getRelatedAppInfo() {
		return relatedAppInfo;
	}
	
	public void setRelatedAppInfo(AppInfo relatedAppInfo) {
		this.relatedAppInfo = relatedAppInfo;
	}
	
	public synchronized void fullLoad(Connection conn) {
		if (this.fullLoaded) {
			return;
		}
		
		this.dstTableConf.fullLoad(conn);
		this.fullLoaded = true;
	}
	
	public synchronized void fullLoad() throws DBException {
		OpenConnection conn = this.relatedAppInfo.openConnection();
		
		try {
			this.fullLoad(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	public DatabaseObject generateMappedObject(DatabaseObject srcObject, Connection srcConn, AppInfo srcAppInfo,
	        AppInfo dstAppInfo) throws DBException, ForbiddenOperationException {
		try {
			
			List<DatabaseObject> srcObjects = new ArrayList<>();
			
			srcObjects.add(srcObject);
			
			if (utilities.arrayHasElement(this.srcConf.getExtraDataSource())) {
				for (EtlExtraDataSource mappingInfo : this.srcConf.getExtraDataSource()) {
					DatabaseObject relatedSrcObject = mappingInfo.loadRelatedSrcObject(srcObject, srcConn, srcAppInfo);
					
					if (relatedSrcObject == null) {
						
						if (mappingInfo.getAvaliableSrc().isRequired()) {
							return null;
						} else {
							relatedSrcObject = mappingInfo.getSyncRecordClass(srcAppInfo).newInstance();
						}
					}
					
					srcObjects.add(relatedSrcObject);
					
				}
			}
			
			DatabaseObject mappedObject = this.dstTableConf.getSyncRecordClass(dstAppInfo).newInstance();
			
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
				field.setDataSourceName(this.srcConf.getMainSrcTableConf().getTableName());
			}
		}
	}
	
}
