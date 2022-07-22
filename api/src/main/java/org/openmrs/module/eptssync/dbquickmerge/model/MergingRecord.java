package org.openmrs.module.eptssync.dbquickmerge.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.MissingParentException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.model.pojo.generic.AbstractOpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class MergingRecord {
	private OpenMRSObject record;
	private SyncTableConfiguration config;
	private List<ParentInfo>  parentsWithDefaultValues;
	private AppInfo srcApp;
	private AppInfo destApp;
	
	public MergingRecord(OpenMRSObject record, SyncTableConfiguration config, AppInfo srcApp, AppInfo destApp) {
		this.record = record;
		this.config = config;	
		this.srcApp = srcApp;
		this.destApp = destApp;
		
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
	}
	
	public void merge(Connection srcConn, Connection destConn) throws DBException {
		consolidateAndSaveData(srcConn, destConn);
	}
	
	private void consolidateAndSaveData(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException{
		if (!config.isFullLoaded()) config.fullLoad(); 
		
		try {
			MergingRecord.loadDestParentInfo(this,  srcConn, destConn);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		if (this.record.getObjectId() == 19420) {
			System.out.println("Stop");
		}
		
		record.save(config, destConn);
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
	}
	
	public void resolveConflict(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException{
		if (!config.isFullLoaded()) config.fullLoad(); 
		
		try {
			MergingRecord.loadDestParentInfo(this,  srcConn, destConn);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		OpenMRSObject recordOnDB = OpenMRSObjectDAO.getByUuid(this.config.getSyncRecordClass(this.destApp), this.record.getUuid(), destConn);
		
		((AbstractOpenMRSObject) record).resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
	}
	
	private void reloadParentsWithDefaultValues(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException {
		for (ParentInfo parentInfo: this.parentsWithDefaultValues) {
			
			RefInfo refInfo = parentInfo.getRefInfo();
			
			OpenMRSObject parent= parentInfo.getParent();
			
			MergingRecord parentData = new MergingRecord(parent, refInfo.getRefTableConfiguration(), this.srcApp, this.destApp);
			parentData.merge(srcConn, destConn);
				
			parent = OpenMRSObjectDAO.getByUuid(refInfo.getRefTableConfiguration().getSyncRecordClass(this.destApp), parent.getUuid(), destConn);
			
			record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
		}		
	}
	
	private static void loadDestParentInfo(MergingRecord mergingRecord, Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, SQLException {
		OpenMRSObject record = mergingRecord.record;
		SyncTableConfiguration config = mergingRecord.config;
		
		for (RefInfo refInfo: config.getParents()) {
			if (refInfo.getRefTableConfiguration().isMetadata()) continue;
			
			Integer parentIdInOrigin = record.getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			if (parentIdInOrigin != null) {
				OpenMRSObject parentInOrigin = OpenMRSObjectDAO.getById(refInfo.getRefObjectClass(mergingRecord.srcApp), parentIdInOrigin, srcConn);
				
				if (parentInOrigin == null) throw new MissingParentException(parentIdInOrigin, refInfo.getTableName(), mergingRecord.srcApp.getPojoPackageName());
				
				OpenMRSObject parentInDest = OpenMRSObjectDAO.getByUuid(refInfo.getRefObjectClass(mergingRecord.destApp), parentInOrigin.getUuid(), destConn);
		
				if (parentInDest == null) {
					mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
					
					parentInDest = OpenMRSObjectDAO.getDefaultRecord(refInfo.getRefTableConfiguration(), destConn);
				}
				
				record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parentInDest);
			}
		}
	}
	
	public OpenMRSObject getRecord() {
		return record;
	}
	
}
