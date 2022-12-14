package org.openmrs.module.eptssync.reconciliation.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class DataReconciliationRecord {
	private DatabaseObject record;
	private SyncTableConfiguration config;
	private SyncImportInfoVO stageInfo;
	private String recordUuid;
	private ConciliationReasonType reasonType;
	
	public DataReconciliationRecord(String recordUuid, SyncTableConfiguration config, ConciliationReasonType reasonType) {
		this.recordUuid = recordUuid;
		this.config = config;
		this.reasonType = reasonType;
	}
	
	public DataReconciliationRecord(DatabaseObject record, SyncTableConfiguration config, ConciliationReasonType reasonType) {
		this.record = record;
		this.recordUuid = record.getUuid();
		this.stageInfo = record.getRelatedSyncInfo();
		this.config = config;
		this.reasonType = reasonType;
	}
	
	public static void tryToReconciliate(DatabaseObject record, SyncTableConfiguration config, Connection conn) throws ParentNotYetMigratedException, DBException {
		DataReconciliationRecord dataReciliationRecord = new DataReconciliationRecord(record.getUuid(), config, ConciliationReasonType.OUTDATED);
		
		dataReciliationRecord.record = record; 
		dataReciliationRecord.config = config;
		dataReciliationRecord.stageInfo = record.getRelatedSyncInfo();
		
		DatabaseObject srcObj = DatabaseObjectDAO.getByIdOnSpecificSchema(config.getSyncRecordClass(config.getMainApp()), dataReciliationRecord.record.getRelatedSyncInfo().getRecordOriginId(),  dataReciliationRecord.stageInfo.getRecordOriginLocationCode(), conn);
		
		srcObj.setRelatedSyncInfo(record.getRelatedSyncInfo());
		
		DataReconciliationRecord.loadDestParentInfo(srcObj, dataReciliationRecord.getConfig(),  conn);
		
		srcObj.setRelatedSyncInfo(dataReciliationRecord.stageInfo);
		
		if (!dataReciliationRecord.record.hasExactilyTheSameDataWith(srcObj)) {
			srcObj.save(config, conn);
		
			dataReciliationRecord.save(conn);
		}
	}
	
	public void reloadRelatedRecordDataFromRemote(Connection conn) throws DBException, ForbiddenOperationException {
		if (this.stageInfo == null) this.stageInfo = SyncImportInfoDAO.getWinRecord(this.config, this.recordUuid, conn);
		
		if (this.stageInfo != null) {
			this.record= DatabaseObjectDAO.getByIdOnSpecificSchema(config.getSyncRecordClass(config.getMainApp()), stageInfo.getRecordOriginId(), stageInfo.getRecordOriginLocationCode(), conn);
		}
		else {
			this.record = null;
		}
		
		if (this.record != null) {
			this.record.setRelatedSyncInfo(this.stageInfo);
		}
	}
	
	public void reloadRelatedRecordDataFromDestination(Connection conn) throws DBException, ForbiddenOperationException {
		throw new ForbiddenOperationException();
		//this.record= DatabaseObjectDAO.getByUuid(this.config.getSyncRecordClass(config.getMainApp()), this.recordUuid, conn).get(0);
	}
	
	public ConciliationReasonType getReasonType() {
		return reasonType;
	}
	
	public String getRecordOriginLocationCode(){
		return stageInfo != null ? stageInfo.getRecordOriginLocationCode() : "Aknown";
	}
	
	public String getTableName() {
		return config.getTableName();
	}
	
	public String getRecordUuid() {
		return recordUuid;
	}
	
	public SyncTableConfiguration getConfig() {
		return config;
	}
	
	public void consolidateAndSaveData(Connection conn) throws DBException{
		if (!config.isFullLoaded()) config.fullLoad(); 
		
		DataReconciliationRecord.loadDestParentInfo(this.record, this.config, conn);
		
		record.save(config, conn);

		
		if (getConfig().isFromOpenMRSModel() &&  getTableName().equals("person")) {
			//Try to Restore the related patient
			
			for (RefInfo refInfo: config.getChildred()) {
				if (refInfo.getTableName().equals("patient")) {
					DataReconciliationRecord childData = new DataReconciliationRecord(this.recordUuid, refInfo.getRefTableConfiguration(), ConciliationReasonType.MISSING);
					
					childData.reloadRelatedRecordDataFromRemote(conn);
					
					if (childData.record != null) {
						childData.consolidateAndSaveData(conn);
						childData.save(conn);
					}
					
					break;
				}
			}
		}
		
	}

	private static void loadDestParentInfo(DatabaseObject record, SyncTableConfiguration config, Connection conn) throws ParentNotYetMigratedException, DBException {
		SyncImportInfoVO stageInfo = record.getRelatedSyncInfo();
		
		
		for (RefInfo refInfo: config.getParents()) {
			if (refInfo.getRefTableConfiguration().isMetadata()) continue;
			
			Integer parentIdInOrigin = record.getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			if (parentIdInOrigin != null) {
				DatabaseObject parent;
			
				parent = record.retrieveParentInDestination(parentIdInOrigin, stageInfo.getRecordOriginLocationCode(), refInfo.getRefTableConfiguration(),  true, conn);
			
		
				if (parent == null) {
					SyncImportInfoVO parentStageInfo = SyncImportInfoDAO.getByOriginIdAndLocation(refInfo.getRefTableConfiguration(), parentIdInOrigin, stageInfo.getRecordOriginLocationCode(), conn);
					
					if (parentStageInfo != null) {
						if (parentStageInfo.getConsistent() != 1) {
							parentStageInfo = SyncImportInfoDAO.getWinRecord(refInfo.getRefTableConfiguration(), parentStageInfo.getRecordUuid(), conn);
						}
						
						DataReconciliationRecord parentData = new DataReconciliationRecord(parentStageInfo.getRecordUuid(), refInfo.getRefTableConfiguration(), ConciliationReasonType.MISSING);
						
						parentData.reloadRelatedRecordDataFromRemote(conn);
						parentData.consolidateAndSaveData(conn);
						
						parentData.save(conn);
						
						parent = parentData.record;
						
						throw new ForbiddenOperationException();

						//parent = DatabaseObjectDAO.getByUuid(refInfo.getRefTableConfiguration().getSyncRecordClass(config.getMainApp()), parentStageInfo.getRecordUuid(), conn).get(0);
					}
				}
				
				record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
			}
		}
	}

	public void save(Connection conn) throws DBException {
		DataReconciliationRecordDAO.insert(this, conn);
	}

	public void removeRelatedRecord(Connection conn) throws DBException{
		if (!config.isFullLoaded()) config.fullLoad();
		
		for (RefInfo refInfo: config.getChildred()) {
			if (!refInfo.getRefTableConfiguration().isConfigured()) continue;
		
			
			List<DatabaseObject> children =  DatabaseObjectDAO.getByParentId(refInfo.getRefTableConfiguration().getSyncRecordClass(config.getMainApp()), refInfo.getRefColumnName(), this.record.getObjectId(), conn);
					
			for (DatabaseObject child : children) {
				DataReconciliationRecord childDataInfo = new DataReconciliationRecord(child.getUuid(), refInfo.getRefTableConfiguration(), ConciliationReasonType.WRONG_RELATIONSHIPS);
				
				childDataInfo.reloadRelatedRecordDataFromRemote(conn);
				
				if (childDataInfo.record != null) {
					childDataInfo.consolidateAndSaveData(conn);
				}
				else {
					childDataInfo.reloadRelatedRecordDataFromDestination(conn);
					childDataInfo.reasonType = ConciliationReasonType.PHANTOM;
					childDataInfo.removeRelatedRecord(conn);
				}
				
				childDataInfo.save(conn);
			}
		}
		
		this.record.remove(conn);
	}
}
