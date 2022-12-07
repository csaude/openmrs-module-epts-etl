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
import org.openmrs.module.eptssync.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class MergingRecord {
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private DatabaseObject record;
	private SyncTableConfiguration config;
	private List<ParentInfo>  parentsWithDefaultValues;
	private AppInfo srcApp;
	private AppInfo destApp;
	
	public MergingRecord(DatabaseObject record, SyncTableConfiguration config, AppInfo srcApp, AppInfo destApp) {
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
		
		List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(this.config, this.record, destConn);
		
		DatabaseObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
		
		((AbstractDatabaseObject) record).resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
	}
	
	private void reloadParentsWithDefaultValues(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException {
		for (ParentInfo parentInfo: this.parentsWithDefaultValues) {
			
			RefInfo refInfo = parentInfo.getRefInfo();
			
			DatabaseObject parent= parentInfo.getParent();
			
			MergingRecord parentData = new MergingRecord(parent, refInfo.getRefTableConfiguration(), this.srcApp, this.destApp);
			parentData.merge(srcConn, destConn);
				
			List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(refInfo.getRefTableConfiguration(), this.record, destConn);
			
			parent = utilities.arrayHasElement(recs) ? recs.get(0) : null;
			
			record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
		}		
	}
	
	private static void loadDestParentInfo(MergingRecord mergingRecord, Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, SQLException {
		DatabaseObject record = mergingRecord.record;
		
		SyncTableConfiguration config = mergingRecord.config;
		
		if (true) {
			System.out.println();
		}
		
		for (RefInfo refInfo: config.getParents()) {
			if (refInfo.getRefTableConfiguration().isMetadata()) continue;
			
			Integer parentIdInOrigin = record.getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			if (parentIdInOrigin != null) {
				DatabaseObject parentInOrigin = DatabaseObjectDAO.getById(refInfo.getRefObjectClass(mergingRecord.srcApp), parentIdInOrigin, srcConn);
				
				if (parentInOrigin == null) throw new MissingParentException(parentIdInOrigin, refInfo.getTableName(), mergingRecord.config.getOriginAppLocationCode(), refInfo);
				
				List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(refInfo.getRefTableConfiguration(), parentInOrigin, destConn);
				
				DatabaseObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
				if (parentInDest == null) {
					mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
					
					parentInDest = DatabaseObjectDAO.getDefaultRecord(refInfo.getRefTableConfiguration(), destConn);
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
		DatabaseObject record = mergingRecord.record;
		SyncTableConfiguration config = mergingRecord.config;
		
		for (RefInfo refInfo: config.getParents()) {
			if (!refInfo.getRefTableConfiguration().isMetadata()) continue;
			
			Integer parentId = record.getParentValue(refInfo.getRefColumnAsClassAttName());
				 
			if (parentId != null) {
				DatabaseObject parent = DatabaseObjectDAO.getById(refInfo.getRefObjectClass(mergingRecord.destApp), parentId, destConn);
				
				if (parent == null) throw new MissingParentException(parentId, refInfo.getTableName(), mergingRecord.config.getOriginAppLocationCode(), refInfo);
			}
		}
	}
	
	private static void loadDestConditionalParentInfo(MergingRecord mergingRecord, Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(mergingRecord.config.getConditionalParents())) return;
			
		DatabaseObject record = mergingRecord.record;
		SyncTableConfiguration config = mergingRecord.config;
		
		for (RefInfo refInfo: config.getConditionalParents()) {
			if (refInfo.getRefTableConfiguration().isMetadata()) continue;
			
			Object conditionFieldValue = record.getFieldValues(refInfo.getRefConditionFieldAsClassAttName())[0];
			
			if (!conditionFieldValue.equals(refInfo.getConditionValue())) continue;
			
			Integer parentIdInOrigin = null;
			
			try {
				parentIdInOrigin = record.getParentValue(refInfo.getRefColumnAsClassAttName());
			}
			catch (NumberFormatException e) {
			}
				 
			if (parentIdInOrigin != null) {
				DatabaseObject parentInOrigin = DatabaseObjectDAO.getById(refInfo.getRefObjectClass(mergingRecord.srcApp), parentIdInOrigin, srcConn);
				
				if (parentInOrigin == null) throw new MissingParentException(parentIdInOrigin, refInfo.getTableName(), mergingRecord.config.getOriginAppLocationCode(), refInfo);
				
				List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(refInfo.getRefTableConfiguration(), parentInOrigin, destConn); 
			
				DatabaseObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
				if (parentInDest == null) {
					mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
					
					parentInDest = DatabaseObjectDAO.getDefaultRecord(refInfo.getRefTableConfiguration(), destConn);
				}
				
				record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parentInDest);
			}
		}
	}
	
	
	public DatabaseObject getRecord() {
		return record;
	}
	
}
