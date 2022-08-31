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
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class MergingRecord {
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
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
			MergingRecord.loadDestConditionalParentInfo(this, srcConn, destConn);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}

		try {
			record.save(config, destConn);
		}
		catch (DBException e) {
			if (e.isIntegrityConstraintViolationException()) {
				determineMissingMetadataParent(this, srcConn, destConn);
			}
			else throw e;
		}
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
	}
	
	public void resolveConflict(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException{
		if (!config.isFullLoaded()) config.fullLoad(); 
		
		try {
			MergingRecord.loadDestParentInfo(this,  srcConn, destConn);
			MergingRecord.loadDestConditionalParentInfo(this, srcConn, destConn);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		List<OpenMRSObject> recs = OpenMRSObjectDAO.getByUuid(this.config.getSyncRecordClass(this.destApp), this.record.getUuid(), destConn);
		
		OpenMRSObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
		
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
				
			List<OpenMRSObject> recs = OpenMRSObjectDAO.getByUuid(refInfo.getRefTableConfiguration().getSyncRecordClass(this.destApp), parent.getUuid(), destConn);
			
			parent = utilities.arrayHasElement(recs) ? recs.get(0) : null;
			
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
				
				if (parentInOrigin == null) throw new MissingParentException(parentIdInOrigin, refInfo.getTableName(), mergingRecord.config.getOriginAppLocationCode(), refInfo);
				
				List<OpenMRSObject> recs = OpenMRSObjectDAO.getByUuid(refInfo.getRefObjectClass(mergingRecord.destApp), parentInOrigin.getUuid(), destConn);
		
				OpenMRSObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
				if (parentInDest == null) {
					mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
					
					parentInDest = OpenMRSObjectDAO.getDefaultRecord(refInfo.getRefTableConfiguration(), destConn);
				}
				
				record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parentInDest);
			}
		}
	}
	
	
	/**
	 * 
	 * @param mergingRecord
	 * @param srcConn
	 * @param destConn
	 * @throws DBException 
	 * @throws ParentNotYetMigratedException
	 * @throws SQLException
	 */
	private static void determineMissingMetadataParent(MergingRecord mergingRecord, Connection srcConn, Connection destConn) throws MissingParentException, DBException{
		OpenMRSObject record = mergingRecord.record;
		SyncTableConfiguration config = mergingRecord.config;
		
		for (RefInfo refInfo: config.getParents()) {
			if (!refInfo.getRefTableConfiguration().isMetadata()) continue;
			
			Integer parentId = record.getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			if (parentId != null) {
				OpenMRSObject parent = OpenMRSObjectDAO.getById(refInfo.getRefObjectClass(mergingRecord.destApp), parentId, destConn);
				
				if (parent == null) throw new MissingParentException(parentId, refInfo.getTableName(), mergingRecord.config.getOriginAppLocationCode(), refInfo);
			}
		}
	}
	
	private static void loadDestConditionalParentInfo(MergingRecord mergingRecord, Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(mergingRecord.config.getConditionalParents())) return;
			
		OpenMRSObject record = mergingRecord.record;
		SyncTableConfiguration config = mergingRecord.config;
		
		for (RefInfo refInfo: config.getConditionalParents()) {
			if (refInfo.getRefTableConfiguration().isMetadata()) continue;
			
			Object conditionFieldValue = record.getFieldValue(refInfo.getRefConditionFieldAsClassAttName());
			
			if (!conditionFieldValue.equals(refInfo.getConditionValue())) continue;
			
			Integer parentIdInOrigin = null;
			
			try {
				parentIdInOrigin = record.getParentValue(refInfo.getRefColumnAsClassAttName());
			}
			catch (NumberFormatException e) {
			}
				 
			if (parentIdInOrigin != null) {
				OpenMRSObject parentInOrigin = OpenMRSObjectDAO.getById(refInfo.getRefObjectClass(mergingRecord.srcApp), parentIdInOrigin, srcConn);
				
				if (parentInOrigin == null) throw new MissingParentException(parentIdInOrigin, refInfo.getTableName(), mergingRecord.config.getOriginAppLocationCode(), refInfo);
				
				List<OpenMRSObject> recs = OpenMRSObjectDAO.getByUuid(refInfo.getRefObjectClass(mergingRecord.destApp), parentInOrigin.getUuid(), destConn);
		
				OpenMRSObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
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
