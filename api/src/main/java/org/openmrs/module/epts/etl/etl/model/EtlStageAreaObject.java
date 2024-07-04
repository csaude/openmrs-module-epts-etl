package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class EtlStageAreaObject extends GenericDatabaseObject {
	
	private List<EtlDatabaseObject> srcUniqueKeyInfo;
	
	private List<List<EtlDatabaseObject>> dstUniqueKeyInfo;
	
	public EtlStageAreaObject() {
		
	}
	
	public EtlStageAreaObject(EtlConfigurationTableConf tabConf) {
		super(tabConf);
		
	}
	
	@Override
	public EtlConfigurationTableConf getRelatedConfiguration() {
		return (EtlConfigurationTableConf) super.getRelatedConfiguration();
	}
	
	public List<EtlDatabaseObject> getSrcUniqueKeyInfo() {
		return srcUniqueKeyInfo;
	}
	
	public void setSrcUniqueKeyInfo(List<EtlDatabaseObject> srcUniqueKeyInfo) {
		this.srcUniqueKeyInfo = srcUniqueKeyInfo;
	}
	
	public List<List<EtlDatabaseObject>> getDstUniqueKeyInfo() {
		return dstUniqueKeyInfo;
	}
	
	public void setDstUniqueKeyInfo(List<List<EtlDatabaseObject>> dstUniqueKeyInfo) {
		this.dstUniqueKeyInfo = dstUniqueKeyInfo;
	}
	
	public static EtlStageAreaObject generate(EtlDatabaseObject srcObject, List<EtlDatabaseObject> dstObject,
	        Connection srcConn) throws DBException {
		
		TableConfiguration srcTabConf = (TableConfiguration) srcObject.getRelatedConfiguration();
		
		EtlConfigurationTableConf stageAreatabConf = srcTabConf.generateRelatedSyncStageTableConf(srcConn);
		
		EtlStageAreaObject eo = new EtlStageAreaObject(stageAreatabConf);
		eo.setFieldValue("record_origin_location_code", srcTabConf.getOriginAppLocationCode());
		
		EtlConfigurationTableConf srcKeyInfoTabConf = srcTabConf.generateRelatedStageSrcUniqueKeysTableConf(srcConn);
		
		eo.setSrcUniqueKeyInfo(eo.generateUniqueKeyInfoRecord(srcKeyInfoTabConf, srcObject));
		
		EtlConfigurationTableConf dstKeyInfoTabConf = srcTabConf.generateRelatedStageDstUniqueKeysTableConf(srcConn);
		
		if (utilities.arrayHasElement(dstObject)) {
			for (EtlDatabaseObject obj : dstObject) {
				eo.addDstKeyInfo(eo.generateUniqueKeyInfoRecord(dstKeyInfoTabConf, obj));
			}
		}
		
		return eo;
	}
	
	private void addDstKeyInfo(List<EtlDatabaseObject> uniqueKeyInfoRecord) {
		if (getDstUniqueKeyInfo() == null) {
			setDstUniqueKeyInfo(new ArrayList<>());
		}
	}
	
	private List<EtlDatabaseObject> generateUniqueKeyInfoRecord(EtlConfigurationTableConf tabConf,
	        EtlDatabaseObject etlObject) {
		
		List<EtlDatabaseObject> ukInfo = new ArrayList<>();
		List<UniqueKeyInfo> allKeys = new ArrayList<>();
		
		allKeys.add(etlObject.getRelatedConfiguration().getPrimaryKey());
		
		if (etlObject.hasUniqueKeys()) {
			allKeys.addAll(etlObject.getUniqueKeysInfo());
		}
		
		for (UniqueKeyInfo uKey : UniqueKeyInfo.cloneAllAndLoadValues(allKeys, etlObject)) {
			
			for (Key key : uKey.getFields()) {
				
				GenericDatabaseObject obj = new GenericDatabaseObject(tabConf);
				
				obj.setFieldValue("stage_record_id", this.getFieldValue("id"));
				obj.setFieldValue("table_name", etlObject.getObjectName());
				obj.setFieldValue("key_name", uKey.getKeyName());
				obj.setFieldValue("column_name", key.getName());
				obj.setFieldValue("key_value", key.getValue());
				
				ukInfo.add(obj);
			}
			
		}
		
		return ukInfo;
	}
	
	public boolean hasDstKeys() {
		return utilities.arrayHasElement(getDstUniqueKeyInfo());
	}
	
	public void loadIdToChilds() {
		for (EtlDatabaseObject obj : this.getSrcUniqueKeyInfo()) {
			obj.setFieldValue("stage_record_id", this.getFieldValue("id"));
		}
		
		if (hasDstKeys()) {
			for (List<EtlDatabaseObject> objs : this.getDstUniqueKeyInfo()) {
				for (EtlDatabaseObject obj : objs) {
					obj.setFieldValue("stage_record_id", this.getFieldValue("id"));
				}
			}
		}
	}
}
