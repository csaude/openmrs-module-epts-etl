package org.openmrs.module.epts.etl.etl.model.stage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class EtlStageAreaObject extends GenericDatabaseObject {
	
	private List<EtlDatabaseObject> keyInfo;
	
	public EtlStageAreaObject() {
	}
	
	private EtlStageAreaObject(EtlStageTableType type, EtlDatabaseObject relatedEtlObject, Connection conn)
	        throws DBException {
		TableConfiguration etlTable = (TableConfiguration) relatedEtlObject.getRelatedConfiguration();
		
		EtlConfigurationTableConf keyInfoTabConf = null;
		
		setSrcRelatedObject(relatedEtlObject);
		
		if (type.isSrc()) {
			this.setRelatedConfiguration(etlTable.generateRelatedSrcStageTableConf(conn));
			
			this.setFieldValue("record_origin_location_code", etlTable.getOriginAppLocationCode());
			this.setFieldValue("compacted_object_uk", UniqueKeyInfo.compactAll(this.getAllKeys()));
			
			keyInfoTabConf = etlTable.generateRelatedStageSrcUniqueKeysTableConf(conn);
			
		} else {
			this.setRelatedConfiguration(((DstConf) etlTable).getSrcConf().generateRelatedDstStageTableConf(conn));
			
			this.setFieldValue("dst_table_name", etlTable.getTableName());
			this.setFieldValue("dst_compacted_object_uk", UniqueKeyInfo.compactAll(this.getAllKeys()));
			this.setFieldValue("conflict_resolution_type",
			    this.getRelatedEtlObject().getConflictResolutionType().toString());
			
			keyInfoTabConf = ((DstConf) etlTable).getSrcConf().generateRelatedStageDstUniqueKeysTableConf(conn);
		}
		
		this.generateUniqueKeyInfoRecord(keyInfoTabConf);
	}
	
	private static EtlStageAreaObject generate(EtlStageTableType type, EtlDatabaseObject relatedEtlObject, Connection conn)
	        throws DBException {
		return new EtlStageAreaObject(type, relatedEtlObject, conn);
	}
	
	public static EtlStageAreaObject generateSrc(EtlDatabaseObject relatedEtlObject, Connection conn) throws DBException {
		return generate(EtlStageTableType.SRC, relatedEtlObject, conn);
	}
	
	public static List<EtlStageAreaObject> generateDst(List<EtlDatabaseObject> relatedEtlObject, Connection conn)
	        throws DBException {
		List<EtlStageAreaObject> recs = new ArrayList<>();
		
		for (EtlDatabaseObject etlObject : relatedEtlObject) {
			recs.add(generate(EtlStageTableType.DST, etlObject, conn));
		}
		
		return recs;
	}
	
	@Override
	public EtlConfigurationTableConf getRelatedConfiguration() {
		return (EtlConfigurationTableConf) super.getRelatedConfiguration();
	}
	
	public List<EtlDatabaseObject> getKeyInfo() {
		return keyInfo;
	}
	
	public void setKeyInfo(List<EtlDatabaseObject> keyInfo) {
		this.keyInfo = keyInfo;
	}
	
	private void addKeyInfo(EtlDatabaseObject uniqueKeyInfoRecord) {
		if (getKeyInfo() == null) {
			setKeyInfo(new ArrayList<>());
		}
		
		getKeyInfo().add(uniqueKeyInfoRecord);
	}
	
	private List<UniqueKeyInfo> getAllKeys() {
		EtlDatabaseObject etlObject = getSrcRelatedObject();
		
		List<UniqueKeyInfo> allKeys = new ArrayList<>();
		
		TableConfiguration etlObjectRelatedTabConf = (TableConfiguration) etlObject.getRelatedConfiguration();
		
		etlObject.loadObjectIdData(etlObjectRelatedTabConf);
		
		//We want to preserve the UK name
		UniqueKeyInfo uk = new UniqueKeyInfo();
		uk.copy(etlObject.getObjectId());
		uk.setKeyName(etlObjectRelatedTabConf.getPrimaryKey().getKeyName());
		
		allKeys.add(uk);
		
		etlObject.loadUniqueKeyValues();
		
		if (etlObject.hasUniqueKeys()) {
			//We want to preserve the key name
			allKeys.addAll(
			    UniqueKeyInfo.cloneAllWithKeyNameAndLoadValues(etlObjectRelatedTabConf.getUniqueKeys(), etlObject));
		}
		
		return allKeys;
	}
	
	public EtlDatabaseObject getRelatedEtlObject() {
		return super.getSrcRelatedObject();
	}
	
	public TableConfiguration getRelatedEtlTableConf() {
		return (TableConfiguration) this.getRelatedEtlObject().getRelatedConfiguration();
	}
	
	private void generateUniqueKeyInfoRecord(EtlConfigurationTableConf tabConf) {
		
		List<UniqueKeyInfo> allKeys = new ArrayList<>();
		
		UniqueKeyInfo pk = this.getRelatedEtlTableConf().getPrimaryKey();
		
		pk.setKeyName("pk");
		
		allKeys.add(pk);
		
		if (this.getRelatedEtlObject().hasUniqueKeys()) {
			allKeys.addAll(this.getRelatedEtlTableConf().getUniqueKeys());
		}
		
		for (UniqueKeyInfo uKey : UniqueKeyInfo.cloneAllWithKeyNameAndLoadValues(allKeys, this.getRelatedEtlObject())) {
			
			for (Key key : uKey.getFields()) {
				
				if (key.hasValue()) {
					GenericDatabaseObject obj = new GenericDatabaseObject(tabConf);
					
					obj.setFieldValue("key_name", uKey.getKeyName());
					obj.setFieldValue("column_name", key.getName());
					obj.setFieldValue("key_value", key.getValue());
					
					obj.setSrcRelatedObject(this);
					
					this.addKeyInfo(obj);
				}
			}
			
		}
	}
	
	public void loadIdToChilds() {
		for (EtlDatabaseObject obj : this.getKeyInfo()) {
			obj.setFieldValue("stage_record_id", this.getFieldValue("id"));
		}
	}
	
	public static List<EtlDatabaseObject> collectAllKeyInfo(List<EtlStageAreaObject> stageInfo) {
		List<EtlDatabaseObject> collected = new ArrayList<>(stageInfo.size());
		
		for (EtlStageAreaObject sti : stageInfo) {
			collected.addAll(sti.getKeyInfo());
		}
		
		return collected;
	}
	
	public static void loadParentIdToChild(List<EtlStageAreaObject> stageAreaObjects) {
		for (EtlStageAreaObject stageObject : stageAreaObjects) {
			stageObject.loadIdToChilds();
		}
	}
	
}
