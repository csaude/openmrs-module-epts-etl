package org.openmrs.module.epts.etl.controller.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.EtlExtraDataSource;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class DstConf extends SyncTableConfiguration {
	
	private List<FieldsMapping> allFieldsMapping;
	
	private List<FieldsMapping> manualFieldsMapping;
	
	private AppInfo relatedAppInfo;
	
	public DstConf() {
	}
	
	public List<FieldsMapping> getManualFieldsMapping() {
		return manualFieldsMapping;
	}
	
	public void setManualFieldsMapping(List<FieldsMapping> manualFieldsMapping) {
		this.manualFieldsMapping = manualFieldsMapping;
	}
	
	public List<FieldsMapping> getAllFieldsMapping() {
		return allFieldsMapping;
	}
	
	private void addMapping(String srcField, String destField) {
		addMapping(new FieldsMapping(srcField, this.getTableName(), destField));
	}
	
	private void addMapping(FieldsMapping fm) throws ForbiddenOperationException {
		if (this.allFieldsMapping == null) {
			this.allFieldsMapping = new ArrayList<FieldsMapping>();
		}
		
		if (this.allFieldsMapping.contains(fm))
			throw new ForbiddenOperationException("The field [" + fm + "] already exists on mapping");
		
		this.allFieldsMapping.add(fm);
	}
	
	public static DstConf generateDefaultDstConf(EtlConfiguration etlConf) {
		DstConf dstSyncConfiguration = new DstConf();
		
		dstSyncConfiguration.clone(etlConf.getSrcConf());
		
		dstSyncConfiguration.generateAllFieldsMapping();
		
		return dstSyncConfiguration;
	}
	
	public void generateAllFieldsMapping() {
		this.allFieldsMapping = new ArrayList<>();
		
		if (utilities.arrayHasElement(this.manualFieldsMapping)) {
			for (FieldsMapping fm : this.manualFieldsMapping) {
				if (!utilities.stringHasValue(fm.getDataSourceName())) {
					fm.setDataSourceName(getParent().getSrcConf().getTableName());
				}
				
				addMapping(fm);
			}
		}
		
		for (Field field : getParent().getSrcConf().getFields()) {
			FieldsMapping fm = new FieldsMapping(field.getName(), this.getTableName(), field.getName());
			
			if (!this.allFieldsMapping.contains(fm)) {
				this.addMapping(field.getName(), field.getName());
			}
		}
	}
	
	public String getMappedField(String srcField) {
		List<FieldsMapping> machedFields = new ArrayList<FieldsMapping>();
		
		for (FieldsMapping field : this.allFieldsMapping) {
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
	
	public synchronized void fullLoad() throws DBException {
		OpenConnection conn = this.relatedAppInfo.openConnection();
		
		try {
			this.fullLoad(conn);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private SrcConf getSrcConf() {
		return this.getParent().getSrcConf();
	}
	
	public DatabaseObject generateMappedObject(DatabaseObject srcObject, Connection srcConn, AppInfo srcAppInfo,
	        AppInfo dstAppInfo) throws DBException, ForbiddenOperationException {
		try {
			
			List<DatabaseObject> srcObjects = new ArrayList<>();
			
			srcObjects.add(srcObject);
			
			if (utilities.arrayHasElement(this.getParent().getSrcConf().getExtraDataSource())) {
				for (EtlExtraDataSource mappingInfo : this.getSrcConf().getExtraDataSource()) {
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
			
			DatabaseObject mappedObject = this.getSyncRecordClass(dstAppInfo).newInstance();
			
			for (FieldsMapping fieldsMapping : this.allFieldsMapping) {
				
				Object srcValue = fieldsMapping.retrieveValue(mappedObject, srcObjects, dstAppInfo, srcConn);
				
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
	
	@Override
	public void setParent(SyncDataConfiguration parent) {
		super.setParent((EtlConfiguration) parent);
	}
	
	@Override
	public EtlConfiguration getParent() {
		return (EtlConfiguration) super.getParent();
	}
	
}
