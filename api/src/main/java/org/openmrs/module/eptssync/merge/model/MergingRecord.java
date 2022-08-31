package org.openmrs.module.eptssync.merge.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.MissingParentException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class MergingRecord {
	private OpenMRSObject record;
	private SyncTableConfiguration config;
	private SyncImportInfoVO stageInfo;
	private List<ParentInfo>  parentsWithDefaultValues;

	private AppInfo srcApp;
	private AppInfo destApp;
	
	public MergingRecord(SyncImportInfoVO stageInfo, SyncTableConfiguration config, AppInfo srcApp, AppInfo destApp) {
		this.srcApp = srcApp;
		this.destApp = destApp;
		this.stageInfo = stageInfo;
		this.config = config;
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
	}
	
	public void merge(Connection conn) throws DBException {
		this.record = OpenMRSObjectDAO.getByIdOnSpecificSchema(config.getSyncRecordClass(this.srcApp), stageInfo.getRecordOriginId(),  stageInfo.getRecordOriginLocationCode(), conn);
		this.record.setRelatedSyncInfo(stageInfo);
		
		consolidateAndSaveData(conn);
	}
	
	private void consolidateAndSaveData(Connection conn) throws DBException{
		if (!config.isFullLoaded()) config.fullLoad(); 
		
		MergingRecord.loadDestParentInfo(this, conn);
		
		record.save(config, conn);
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(conn);
		}
	}
	
	private void reloadParentsWithDefaultValues(Connection conn) throws ParentNotYetMigratedException, DBException {
		for (ParentInfo parentInfo: this.parentsWithDefaultValues) {
			
			RefInfo refInfo = parentInfo.getRefInfo();
			
			SyncImportInfoVO parentStageInfo = parentInfo.getParentStageInfo();
			
			MergingRecord parentData = new MergingRecord(parentStageInfo, refInfo.getRefTableConfiguration(), this.srcApp, this.destApp);
			parentData.record = OpenMRSObjectDAO.getByIdOnSpecificSchema(refInfo.getRefTableConfiguration().getSyncRecordClass(this.srcApp), parentStageInfo.getRecordOriginId(),  parentStageInfo.getRecordOriginLocationCode(), conn);
			parentData.merge(conn);
			
			OpenMRSObject parent = parentData.record;
			
			List<OpenMRSObject> recs = OpenMRSObjectDAO.getByUuid(refInfo.getRefTableConfiguration().getSyncRecordClass(this.destApp), parentStageInfo.getRecordUuid(), conn);
			
			parent = recs != null && recs.size() > 0 ? recs.get(0) : null;
			
			record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
		}		
	}
	
	private static void loadDestParentInfo(MergingRecord mergingRecord, Connection conn) throws ParentNotYetMigratedException, DBException {
		OpenMRSObject record = mergingRecord.record;
		SyncImportInfoVO stageInfo = record.getRelatedSyncInfo();
		SyncTableConfiguration config = mergingRecord.config;
		
		for (RefInfo refInfo: config.getParents()) {
			if (refInfo.getRefTableConfiguration().isMetadata()) continue;
			
			Integer parentIdInOrigin = record.getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			if (parentIdInOrigin != null) {
				OpenMRSObject parent = record.retrieveParentInDestination(parentIdInOrigin, stageInfo.getRecordOriginLocationCode(), refInfo.getRefTableConfiguration(),  true, conn);
		
				if (parent == null) {
					SyncImportInfoVO parentStageInfo = SyncImportInfoDAO.getByOriginIdAndLocation(refInfo.getRefTableConfiguration(), parentIdInOrigin, stageInfo.getRecordOriginLocationCode(), conn);
					
					if (parentStageInfo != null) {
						mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentStageInfo));
					}
					else throw new MissingParentException("Missing parent "+ refInfo + " with value [" + parentIdInOrigin + "] from [" + stageInfo.getRecordOriginLocationCode() + "]");
					
					parent = OpenMRSObjectDAO.getDefaultRecord(refInfo.getRefTableConfiguration(), conn);
				}
				
				record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
			}
		}
	}
	
}
