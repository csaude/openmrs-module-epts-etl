package org.openmrs.module.epts.etl.merge.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.RefInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class MergingRecord {
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private DatabaseObject record;
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
		this.record = DatabaseObjectDAO.getByIdOnSpecificSchema(config.getSyncRecordClass(this.srcApp), Oid.fastCreate("", stageInfo.getRecordOriginId()),  stageInfo.getRecordOriginLocationCode(), conn);
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
			
			MergingRecord parentData = new MergingRecord(parentStageInfo, refInfo.getParentTableConf(), this.srcApp, this.destApp);
			parentData.record = DatabaseObjectDAO.getByIdOnSpecificSchema(refInfo.getParentSyncRecordClass(this.srcApp), parentStageInfo.getRecordOriginIdAsOid(),  parentStageInfo.getRecordOriginLocationCode(), conn);
			parentData.merge(conn);
			
			DatabaseObject parent = parentData.record;
			
			List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(refInfo.getParentTableConf(), parentData.record, conn);
			
			parent = recs != null && recs.size() > 0 ? recs.get(0) : null;
			
			record.changeParentValue(refInfo, parent);
		}		
	}
	
	private static void loadDestParentInfo(MergingRecord mergingRecord, Connection conn) throws ParentNotYetMigratedException, DBException {
		SyncTableConfiguration config = mergingRecord.config;
		
		if (!utilities.arrayHasElement(config.getParents())) return;
		
		DatabaseObject record = mergingRecord.record;
		SyncImportInfoVO stageInfo = record.getRelatedSyncInfo();
		
		for (RefInfo refInfo: config.getParentRefInfo()) {
			if (refInfo.getParentTableConf().isMetadata()) continue;
			
			Object parentIdInOrigin = record.getParentValue(refInfo.getChildColumnAsClassAttOnSimpleMapping());
				 
			if (parentIdInOrigin != null) {
				DatabaseObject parent = record.retrieveParentInDestination(Integer.parseInt(parentIdInOrigin.toString()), stageInfo.getRecordOriginLocationCode(), refInfo.getParentTableConf(),  true, conn);
		
				if (parent == null) {
					SyncImportInfoVO parentStageInfo = SyncImportInfoDAO.getByOriginIdAndLocation(refInfo.getParentTableConf(), Integer.parseInt(parentIdInOrigin.toString()), stageInfo.getRecordOriginLocationCode(), conn);
					
					if (parentStageInfo != null) {
						mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentStageInfo));
					}
					else throw new MissingParentException("Missing parent "+ refInfo + " with value [" + parentIdInOrigin + "] from [" + stageInfo.getRecordOriginLocationCode() + "]");
					
					parent = DatabaseObjectDAO.getDefaultRecord(refInfo.getParentTableConf(), conn);
				}
				
				record.changeParentValue(refInfo, parent);
			}
		}
	}
	
}
