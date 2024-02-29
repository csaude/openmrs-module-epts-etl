package org.openmrs.module.epts.etl.dbquickmerge.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.RefInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.controller.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class MergingRecord {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private DatabaseObject record;
	
	private SyncTableConfiguration config;
	
	private List<ParentInfo> parentsWithDefaultValues;
	
	private AppInfo srcApp;
	
	private AppInfo destApp;
	
	private boolean writeOperationHistory;
	
	private long destinationRecordId;
	
	public MergingRecord(DatabaseObject record, SyncTableConfiguration config, AppInfo srcApp, AppInfo destApp,
	    boolean writeOperationHistory) {
		this.record = record;
		this.config = config;
		this.srcApp = srcApp;
		this.destApp = destApp;
		this.writeOperationHistory = writeOperationHistory;
		
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
		
	}
	
	public void merge(Connection srcConn, Connection destConn) throws DBException {
		this.record.setUniqueKeysInfo(UniqueKeyInfo.cloneAll(this.config.getUniqueKeys()));
		
		consolidateAndSaveData(srcConn, destConn);
		
		if (writeOperationHistory) {
			save(srcConn);
		}
	}
	
	public SyncTableConfiguration getConfig() {
		return config;
	}
	
	private void consolidateAndSaveData(Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		if (!config.isFullLoaded())
			config.fullLoad();
		
		MergingRecord.loadDestParentInfo(this, srcConn, destConn);
		MergingRecord.loadDestConditionalParentInfo(this, srcConn, destConn);
		
		try {
			this.destinationRecordId = record.save(config, destConn);
		}
		catch (DBException e) {
			if (e.isDuplicatePrimaryOrUniqueKeyException()) {
				
				boolean existWinningRecInfo = utilities.arrayHasElement(config.getWinningRecordFieldsInfo());
				boolean existObservationDateFields = utilities.arrayHasElement(config.getObservationDateFields());
				
				if (existObservationDateFields || existWinningRecInfo) {
					List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(this.config, this.record, destConn);
					
					DatabaseObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
					
					this.destinationRecordId = ((AbstractDatabaseObject) record)
					        .resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
				}
				
			} else if (e.isIntegrityConstraintViolationException()) {
				determineMissingMetadataParent(this, srcConn, destConn);
			} else
				throw e;
		}
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
	}
	
	public void resolveConflict(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException {
		if (!config.isFullLoaded())
			config.fullLoad();
		
		MergingRecord.loadDestParentInfo(this, srcConn, destConn);
		MergingRecord.loadDestConditionalParentInfo(this, srcConn, destConn);
		
		List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(this.config, this.record, destConn);
		
		DatabaseObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
		
		((AbstractDatabaseObject) record).resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
	}
	
	private void reloadParentsWithDefaultValues(Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		for (ParentInfo parentInfo : this.parentsWithDefaultValues) {
			
			RefInfo refInfo = parentInfo.getRefInfo();
			
			DatabaseObject parent = parentInfo.getParent();
			
			MergingRecord parentData = new MergingRecord(parent, refInfo.getRefTableConfiguration(), this.srcApp,
			        this.destApp, this.writeOperationHistory);
			parentData.merge(srcConn, destConn);
			
			List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(refInfo.getRefTableConfiguration(), this.record,
			    destConn);
			
			parent = utilities.arrayHasElement(recs) ? recs.get(0) : null;
			
			record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
		}
	}
	
	private static void loadDestParentInfo(MergingRecord mergingRecord, Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		SyncTableConfiguration config = mergingRecord.config;
		
		if (!utilities.arrayHasElement(config.getParents()))
			return;
		
		DatabaseObject record = mergingRecord.record;
		
		for (RefInfo refInfo : config.getParents()) {
			if (refInfo.getRefTableConfiguration().isMetadata())
				continue;
			
			Integer parentIdInOrigin = record.getParentValue(refInfo.getRefColumnAsClassAttName());
			
			if (parentIdInOrigin != null) {
				DatabaseObject parentInOrigin = DatabaseObjectDAO.getById(refInfo.getRefObjectClass(mergingRecord.srcApp),
				    parentIdInOrigin, srcConn);
				
				if (parentInOrigin == null) {
					
					if (refInfo.getDefaultValueDueInconsistency() == null) {
						throw new MissingParentException(parentIdInOrigin, refInfo.getTableName(),
						        mergingRecord.config.getOriginAppLocationCode(), refInfo);
					}
				}
				
				SyncTableConfiguration refTab = refInfo.getRefTableConfiguration();
				
				List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(refTab, parentInOrigin, destConn);
				
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
	 * @param mergingRecord
	 * @param srcConn
	 * @param destConn
	 * @throws DBException
	 * @throws ParentNotYetMigratedException
	 * @throws SQLException
	 */
	private static void determineMissingMetadataParent(MergingRecord mergingRecord, Connection srcConn, Connection destConn)
	        throws MissingParentException, DBException {
		SyncTableConfiguration config = mergingRecord.config;
		
		if (!utilities.arrayHasElement(config.getParents()))
			return;
		
		DatabaseObject record = mergingRecord.record;
		
		for (RefInfo refInfo : config.getParents()) {
			if (!refInfo.getRefTableConfiguration().isMetadata())
				continue;
			
			Integer parentId = record.getParentValue(refInfo.getRefColumnAsClassAttName());
			
			if (parentId != null) {
				DatabaseObject parent = DatabaseObjectDAO.getById(refInfo.getRefObjectClass(mergingRecord.destApp), parentId,
				    destConn);
				
				if (parent == null)
					throw new MissingParentException(parentId, refInfo.getTableName(),
					        mergingRecord.config.getOriginAppLocationCode(), refInfo);
			}
		}
	}
	
	private static void loadDestConditionalParentInfo(MergingRecord mergingRecord, Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(mergingRecord.config.getConditionalParents()))
			return;
		
		DatabaseObject record = mergingRecord.record;
		SyncTableConfiguration config = mergingRecord.config;
		
		for (RefInfo refInfo : config.getConditionalParents()) {
			if (refInfo.getRefTableConfiguration().isMetadata())
				continue;
			
			Object conditionFieldValue = record.getFieldValues(refInfo.getRefConditionFieldAsClassAttName())[0];
			
			if (!conditionFieldValue.equals(refInfo.getConditionValue()))
				continue;
			
			Integer parentIdInOrigin = null;
			
			try {
				parentIdInOrigin = record.getParentValue(refInfo.getRefColumnAsClassAttName());
			}
			catch (NumberFormatException e) {}
			
			if (parentIdInOrigin != null) {
				DatabaseObject parentInOrigin = DatabaseObjectDAO.getById(refInfo.getRefObjectClass(mergingRecord.srcApp),
				    parentIdInOrigin, srcConn);
				
				if (parentInOrigin == null)
					throw new MissingParentException(parentIdInOrigin, refInfo.getTableName(),
					        mergingRecord.config.getOriginAppLocationCode(), refInfo);
				
				List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(refInfo.getRefTableConfiguration(),
				    parentInOrigin, destConn);
				
				DatabaseObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
				if (parentInDest == null) {
					mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
					
					parentInDest = DatabaseObjectDAO.getDefaultRecord(refInfo.getRefTableConfiguration(), destConn);
				}
				
				record.changeParentValue(refInfo.getRefColumnAsClassAttName(), parentInDest);
			}
		}
	}
	
	public void save(Connection conn) throws DBException {
		SyncImportInfoVO syncInfo = SyncImportInfoVO.generateFromSyncRecord(getRecord(),
		    getConfig().getOriginAppLocationCode(), false);
		
		syncInfo.setDestinationId((int) this.destinationRecordId);
		
		syncInfo.save(getConfig(), conn);
	}
	
	public DatabaseObject getRecord() {
		return record;
	}
	
	public static void mergeAll(List<MergingRecord> mergingRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(mergingRecs)) {
			return;
		}
		
		SyncTableConfiguration config = mergingRecs.get(0).config;
		
		if (!config.isFullLoaded()) {
			config.fullLoad();
		}
		
		List<DatabaseObject> objects = new ArrayList<DatabaseObject>(mergingRecs.size());
		
		for (MergingRecord mergingRecord : mergingRecs) {
			mergingRecord.record.setUniqueKeysInfo(UniqueKeyInfo.cloneAll(mergingRecord.config.getUniqueKeys()));
			
			MergingRecord.loadDestParentInfo(mergingRecord, srcConn, dstConn);
			MergingRecord.loadDestConditionalParentInfo(mergingRecord, srcConn, dstConn);
			
			objects.add(mergingRecord.record);
		}
		
		DatabaseObjectDAO.insertAll(objects, config, config.getOriginAppLocationCode(), dstConn);
		
		for (MergingRecord mergingRecord : mergingRecs) {
			if (!mergingRecord.parentsWithDefaultValues.isEmpty()) {
				mergingRecord.reloadParentsWithDefaultValues(srcConn, dstConn);
			}
		}
	}
	
	public static void mergeAll(Map<String, List<MergingRecord>> mergingRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		for (String key : mergingRecs.keySet()) {
			mergeAll(mergingRecs.get(key), srcConn, dstConn);
		}
	}
}
