package org.openmrs.module.epts.etl.etl.model.stage;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfigurationTableConf;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.etl.model.EtlLoadStatus;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class EtlStageAreaObject extends GenericDatabaseObject {
	
	private List<EtlDatabaseObject> keyInfo;
	
	private EtlDatabaseObject relatedEtlObject;
	
	private Boolean alreadyExistOnDB;
	
	private Boolean keyInfoAlreadyExistsOnDb;
	
	private EtlStageTableType type;
	
	private EtlLoadStatus status;
	
	public EtlStageAreaObject() {
	}
	
	private EtlStageAreaObject(EtlStageAreaObject srcStageInfoObject, EtlDatabaseObject obj, EtlStageTableType type,
	    Connection srcConn, Connection dstConn) throws DBException {
		this.type = type;
		this.relatedEtlObject = obj;
		
		TableConfiguration etlTable = (TableConfiguration) obj.getRelatedConfiguration();
		
		EtlConfigurationTableConf keyInfoTabConf = null;
		
		if (type.isSrc()) {
			this.setRelatedConfiguration(etlTable.generateRelatedSrcStageTableConf(srcConn));
			
			this.setFieldValue("record_origin_location_code", etlTable.getOriginAppLocationCode());
			this.setFieldValue("compacted_object_uk", UniqueKeyInfo.compactAll(this.getAllKeys()));
			
			this.status = EtlLoadStatus.NOT_LOADED;
			
			if (obj.hasDestinationRecords()) {
				status = obj.getLoadStatus();
			}
			
			this.setFieldValue("migration_status", status.toInt());
			
			keyInfoTabConf = etlTable.generateRelatedStageSrcUniqueKeysTableConf(srcConn);
			
			EtlStageAreaObject existing = EtlStageAreaObjectDAO.getByKey(etlTable.generateRelatedSrcStageTableConf(srcConn),
			    this.getFieldValue("compacted_object_uk").toString(),
			    this.getFieldValue("record_origin_location_code").toString(), srcConn);
			
			if (existing != null) {
				this.setFieldValue("id", existing.getFieldValue("id"));
				this.loadObjectIdData();
				
				setAlreadyExistOnDB(true);
				setKeyInfoAlreadyExistsOnDb(true);
				
			} else {
				setAlreadyExistOnDB(false);
				setKeyInfoAlreadyExistsOnDb(false);
			}
			
		} else {
			if (!obj.isInEtlProcess())
				throw new EtlExceptionImpl("The destination object " + obj + ". Is not in etl process");
			
			if (srcStageInfoObject == null)
				throw new EtlExceptionImpl("The related srcStageInfoObject cannot be null");
			
			EtlConfigurationTableConf dstStageTable = etlTable.generateRelatedDstStageTableConf(srcConn);
			
			this.setRelatedConfiguration(dstStageTable);
			
			EtlConfigurationTableConf srcStageTable = ((DstConf) etlTable).getSrcConf()
			        .generateRelatedSrcStageTableConf(srcConn);
			
			keyInfoTabConf = etlTable.generateRelatedStageDstUniqueKeysTableConf(srcConn);
			
			String operation_id = obj.getEtlInfo().getRelatedItemConf().getConfigCode();
			
			EtlStageAreaObject existing = null;
			
			if (srcStageInfoObject.hasValuedObjectId()) {
				String condition = "etl_confing_id = ? and src_stage_table_name = ? and stage_record_id = ? and dst_table_name = ? ";
				Object[] params = { operation_id, srcStageTable.getTableName(),
				        srcStageInfoObject.getObjectId().asSimpleValue(), etlTable.getTableName() };
				
				existing = EtlStageAreaObjectDAO.get(etlTable.generateRelatedDstStageTableConf(srcConn), condition, params,
				    srcConn);
				
				if (existing != null) {
					this.setFieldValue("id", existing.getFieldValue("id"));
					this.setFieldValue("stage_record_id", existing.getFieldValue("id"));
					
					existing.loadObjectIdData();
					
					setAlreadyExistOnDB(true);
				} else {
					setAlreadyExistOnDB(false);
				}
			} else {
				setAlreadyExistOnDB(false);
			}
			
			this.status = obj.getEtlInfo().hasExceptionOnEtl() ? EtlLoadStatus.NOT_LOADED_DUE_ERRORS
			        : EtlLoadStatus.FULL_LOADED;
			
			this.setFieldValue("last_sync_try_err",
			    !status.isFullLoaded() ? obj.getEtlInfo().getExceptionOnEtl().getLocalizedMessage() : null);
			
			this.setFieldValue("etl_confing_id", operation_id);
			this.setFieldValue("src_stage_table_name", srcStageTable.getTableName());
			this.setFieldValue("migration_status", status.toInt());
			this.setFieldValue("last_sync_date", DateAndTimeUtilities.getCurrentSystemDate(srcConn));
			this.setFieldValue("dst_table_name", etlTable.getTableName());
			
			if (status.isFullLoaded()) {
				this.setFieldValue("dst_compacted_object_uk", UniqueKeyInfo.compactAll(this.getAllKeys()));
				this.setFieldValue("conflict_resolution_type",
				    this.getRelatedEtlObject().getEtlInfo().getConflictResolutionType().toString());
			} else {
				this.setFieldValue("conflict_resolution_type", ConflictResolutionType.NONE.toString());
			}
			
		}
		
		if (alreadyExistOnDB()) {
			String condition = "stage_record_id = ?";
			Object[] params = { this.getObjectId().asSimpleValue() };
			
			this.keyInfo = EtlStageAreaObjectDAO.getAll(keyInfoTabConf, condition, params, srcConn);
		}
		
		boolean loadKey = utilities.listHasNoElement(this.keyInfo) && (this.isSrc() || (isDst() && isLoadedSuccessifuly()));
		
		if (loadKey) {
			this.generateUniqueKeyInfoRecord(keyInfoTabConf);
		} else {
			
			//We intencionaly null the keyInfo as we do want them to be stored again
			this.keyInfo = null;
		}
	}
	
	public boolean isLoadedSuccessifuly() {
		return this.status.isFullLoaded();
	}
	
	public boolean isSrc() {
		return this.type.isSrc();
	}
	
	public boolean isDst() {
		return this.type.isDst();
	}
	
	public Boolean keyInfoAlreadyExistsOnDb() {
		return keyInfoAlreadyExistsOnDb;
	}
	
	public void setKeyInfoAlreadyExistsOnDb(Boolean keyInfoExistsOnDb) {
		this.keyInfoAlreadyExistsOnDb = keyInfoExistsOnDb;
	}
	
	public boolean alreadyExistOnDB() {
		return this.alreadyExistOnDB;
	}
	
	public void setAlreadyExistOnDB(Boolean alreadyExistOnDB) {
		this.alreadyExistOnDB = alreadyExistOnDB;
	}
	
	private static EtlStageAreaObject generate(EtlStageAreaObject srcStageInfoObject, EtlDatabaseObject relatedEtlObject,
	        EtlStageTableType type, Connection srcConn, Connection dstConn) throws DBException {
		
		return new EtlStageAreaObject(srcStageInfoObject, relatedEtlObject, type, srcConn, dstConn);
	}
	
	public static EtlStageAreaObject generateSrc(EtlDatabaseObject relatedEtlObject, Connection srcConn, Connection dstConn)
	        throws DBException {
		
		TableConfiguration tabConf = (TableConfiguration) relatedEtlObject.getRelatedConfiguration();
		
		if (tabConf.hasPK()) {
			return generate(null, relatedEtlObject, EtlStageTableType.SRC, srcConn, dstConn);
		} else {
			return null;
		}
	}
	
	public static List<EtlStageAreaObject> generateDst(EtlStageAreaObject srcStageInfoObject,
	        List<EtlDatabaseObject> relatedEtlObject, Connection srcConn, Connection dstConn) throws DBException {
		
		List<EtlStageAreaObject> recs = new ArrayList<>();
		
		if (utilities.listHasElement(relatedEtlObject)) {
			
			for (EtlDatabaseObject etlObject : relatedEtlObject) {
				TableConfiguration tabConf = (TableConfiguration) etlObject.getRelatedConfiguration();
				
				if (tabConf.hasPK()) {
					recs.add(generate(srcStageInfoObject, etlObject, EtlStageTableType.DST, srcConn, dstConn));
				}
			}
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
		EtlDatabaseObject etlObject = this.relatedEtlObject;
		
		List<UniqueKeyInfo> allKeys = new ArrayList<>();
		
		TableConfiguration etlObjectRelatedTabConf = (TableConfiguration) etlObject.getRelatedConfiguration();
		
		etlObject.loadObjectIdData(etlObjectRelatedTabConf);
		
		if (etlObjectRelatedTabConf.hasPK()) {
			//We want to preserve the UK name
			UniqueKeyInfo uk = new UniqueKeyInfo();
			uk.copy(etlObject.getObjectId());
			uk.setKeyName(etlObjectRelatedTabConf.getPrimaryKey().getKeyName());
			
			allKeys.add(uk);
		}
		
		etlObject.loadUniqueKeyValues();
		
		if (etlObject.hasUniqueKeys()) {
			//We want to preserve the key name
			allKeys.addAll(
			    UniqueKeyInfo.cloneAllWithKeyNameAndLoadValues(etlObjectRelatedTabConf.getUniqueKeys(), etlObject));
		}
		
		return allKeys;
	}
	
	public EtlDatabaseObject getRelatedEtlObject() {
		return this.relatedEtlObject;
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
			if (sti.getKeyInfo() != null) {
				collected.addAll(sti.getKeyInfo());
			}
		}
		
		return collected;
	}
	
	public static void loadParentIdToChild(List<EtlStageAreaObject> stageAreaObjects) {
		for (EtlStageAreaObject stageObject : stageAreaObjects) {
			stageObject.loadIdToChilds();
		}
	}
	
}
