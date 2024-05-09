package org.openmrs.module.epts.etl.dbquickmerge.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.ParentTable;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class QuickMergeRecord {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected DatabaseObject record;
	
	protected AbstractTableConfiguration config;
	
	protected List<ParentInfo> parentsWithDefaultValues;
	
	protected AppInfo srcApp;
	
	protected AppInfo destApp;
	
	protected boolean writeOperationHistory;
	
	protected long destinationRecordId;
	
	public QuickMergeRecord(DatabaseObject record, AbstractTableConfiguration config, AppInfo srcApp, AppInfo destApp,
	    boolean writeOperationHistory) {
		this.record = record;
		this.config = config;
		this.srcApp = srcApp;
		this.destApp = destApp;
		this.writeOperationHistory = writeOperationHistory;
		
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
		
		this.record.setUniqueKeysInfo(UniqueKeyInfo.cloneAllAndLoadValues(this.config.getUniqueKeys(), this.record));
	}
	
	public List<ParentInfo> getParentsWithDefaultValues() {
		return parentsWithDefaultValues;
	}
	
	public void merge(Connection srcConn, Connection destConn) throws DBException {
		
		consolidateAndSaveData(srcConn, destConn);
		
		if (writeOperationHistory) {
			save(srcConn);
		}
	}
	
	public AbstractTableConfiguration getConfig() {
		return config;
	}
	
	private void consolidateAndSaveData(Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		if (!config.isFullLoaded())
			config.fullLoad();
		
		QuickMergeRecord.loadDestParentInfo(this, srcConn, destConn);
		QuickMergeRecord.loadDestConditionalParentInfo(this, srcConn, destConn);
		
		try {
			record.save(config, destConn);
			
			if (config.useSimpleNumericPk()) {
				this.destinationRecordId = record.getObjectId().getSimpleValueAsInt();
			}
			
		}
		catch (DBException e) {
			if (e.isDuplicatePrimaryOrUniqueKeyException()) {
				
				boolean existWinningRecInfo = utilities.arrayHasElement(config.getWinningRecordFieldsInfo());
				boolean existObservationDateFields = utilities.arrayHasElement(config.getObservationDateFields());
				
				if (existObservationDateFields || existWinningRecInfo) {
					List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(this.config, this.record, destConn);
					
					DatabaseObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
					
					((AbstractDatabaseObject) record).resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
					
					if (config.useSimpleNumericPk()) {
						this.destinationRecordId = record.getObjectId().getSimpleValueAsInt();
					}
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
		
		QuickMergeRecord.loadDestParentInfo(this, srcConn, destConn);
		QuickMergeRecord.loadDestConditionalParentInfo(this, srcConn, destConn);
		
		List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(this.config, this.record, destConn);
		
		DatabaseObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
		
		((AbstractDatabaseObject) record).resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
	}
	
	public void reloadParentsWithDefaultValues(Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		for (ParentInfo parentInfo : this.parentsWithDefaultValues) {
			
			QuickMergeRecord parentData = new QuickMergeRecord(parentInfo.getParentRecord(), parentInfo.getParentTableConf(),
			        srcApp, destApp, this.writeOperationHistory);
			
			parentData.merge(srcConn, destConn);
			
			List<DatabaseObject> recs = DatabaseObjectDAO
			        .getByUniqueKeys(parentInfo.getParentTableConf().getChildTableConf(), this.record, destConn);
			
			DatabaseObject parent = utilities.arrayHasElement(recs) ? recs.get(0) : null;
			
			record.changeParentValue(parentInfo.getParentTableConf(), parent);
		}
	}
	
	protected static void loadDestParentInfo(QuickMergeRecord quickMergeRecord, Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		AbstractTableConfiguration config = quickMergeRecord.config;
		
		if (!utilities.arrayHasElement(config.getParents()))
			return;
		
		DatabaseObject record = quickMergeRecord.record;
		
		for (ParentTable refInfo : config.getParentRefInfo()) {
			if (refInfo.isMetadata())
				continue;
			
			String fieldNameOnParentTable = refInfo.getSimpleRefMapping().getParentField().getNameAsClassAtt();
			String filedNameOnChildTable = refInfo.getSimpleRefMapping().getChildField().getNameAsClassAtt();
			
			Object oParentIdInOrigin = record.getParentValue(filedNameOnChildTable);
			
			if (oParentIdInOrigin != null) {
				Integer parentIdInOrigin = (Integer) oParentIdInOrigin;
				
				DatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(refInfo,
				    Oid.fastCreate(fieldNameOnParentTable, parentIdInOrigin), srcConn);
				
				if (parentInOrigin == null) {
					
					if (refInfo.getSimpleRefMapping().getDefaultValueDueInconsistency() == null) {
						throw new MissingParentException(parentIdInOrigin, refInfo.getTableName(),
						        quickMergeRecord.config.getOriginAppLocationCode(), refInfo);
					} else {
						
						parentIdInOrigin = refInfo.getSimpleRefMapping().getDefaultValueDueInconsistencyAsInt();
						
						parentInOrigin = DatabaseObjectDAO.getByOid(refInfo,
						    Oid.fastCreate(fieldNameOnParentTable, parentIdInOrigin), srcConn);
						
						if (parentInOrigin == null) {
							throw new MissingParentException(parentIdInOrigin, refInfo.getTableName(),
							        quickMergeRecord.config.getOriginAppLocationCode(), refInfo);
						}
					}
				}
				
				AbstractTableConfiguration parentTabConf = refInfo;
				
				List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(parentTabConf, parentInOrigin, destConn);
				
				DatabaseObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
				if (parentInDest == null) {
					quickMergeRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
					
					parentInDest = DatabaseObjectDAO.getDefaultRecord(parentTabConf, destConn);
				}
				
				record.changeParentValue(refInfo, parentInDest);
			}
		}
	}
	
	/**
	 * @param quickMergeRecord
	 * @param srcConn
	 * @param destConn
	 * @throws DBException
	 * @throws ParentNotYetMigratedException
	 * @throws SQLException
	 */
	public static void determineMissingMetadataParent(QuickMergeRecord quickMergeRecord, Connection srcConn,
	        Connection destConn) throws MissingParentException, DBException {
		AbstractTableConfiguration config = quickMergeRecord.config;
		
		if (!utilities.arrayHasElement(config.getParents()))
			return;
		
		DatabaseObject record = quickMergeRecord.record;
		
		for (ParentTable refInfo : config.getParentRefInfo()) {
			if (!refInfo.isMetadata())
				continue;
			
			Object oParentId = record.getParentValue(refInfo.getChildColumnAsClassAttOnSimpleMapping());
			
			if (oParentId != null) {
				Integer parentId = (Integer) oParentId;
				
				DatabaseObject parent = DatabaseObjectDAO.getByOid(refInfo,
				    Oid.fastCreate(refInfo.getParentColumnOnSimpleMapping(), parentId), destConn);
				
				if (parent == null)
					throw new MissingParentException(parentId, refInfo.getTableName(),
					        quickMergeRecord.config.getOriginAppLocationCode(), refInfo);
			}
		}
	}
	
	protected static void loadDestConditionalParentInfo(QuickMergeRecord quickMergeRecord, Connection srcConn,
	        Connection destConn) throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(quickMergeRecord.config.getConditionalParents()))
			return;
		
		DatabaseObject record = quickMergeRecord.record;
		AbstractTableConfiguration config = quickMergeRecord.config;
		
		for (ParentTable parent : config.getConditionalParents()) {
			if (parent.isMetadata())
				continue;
			
			if (utilities.arrayHasMoreThanOneElements(parent.getConditionalFields())) {
				throw new ForbiddenOperationException("Currently not supported multiple conditional fields");
			}
			
			String conditionalFieldName = parent.getConditionalFields().get(0).getNameAsClassAtt();
			Object conditionalvalue = parent.getConditionalFields().get(0).getValue();
			
			if (!conditionalvalue.equals(record.getFieldValue(conditionalFieldName)))
				continue;
			
			Integer parentIdInOrigin = null;
			
			try {
				parentIdInOrigin = (Integer) record.getParentValue(parent.getChildColumnAsClassAttOnSimpleMapping());
			}
			catch (NullPointerException | NumberFormatException e) {}
			
			if (parentIdInOrigin != null) {
				Oid objectId = Oid.fastCreate(parent.getParentColumnOnSimpleMapping(), parentIdInOrigin);
				
				DatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(parent, objectId, srcConn);
				
				if (parentInOrigin == null)
					throw new MissingParentException(parentIdInOrigin, parent.getTableName(),
					        quickMergeRecord.config.getOriginAppLocationCode(), parent);
				
				List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(parent, parentInOrigin, destConn);
				
				DatabaseObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
				if (parentInDest == null) {
					quickMergeRecord.parentsWithDefaultValues.add(new ParentInfo(parent, parentInOrigin));
					
					parentInDest = DatabaseObjectDAO.getDefaultRecord(parent, destConn);
				}
				
				record.changeParentValue(parent, parentInDest);
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
	
	public static void mergeAll(List<QuickMergeRecord> mergingRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(mergingRecs)) {
			return;
		}
		
		AbstractTableConfiguration config = mergingRecs.get(0).config;
		
		if (!config.isFullLoaded()) {
			config.fullLoad();
		}
		
		List<DatabaseObject> objects = new ArrayList<DatabaseObject>(mergingRecs.size());
		
		for (QuickMergeRecord quickMergeRecord : mergingRecs) {
			QuickMergeRecord.loadDestParentInfo(quickMergeRecord, srcConn, dstConn);
			QuickMergeRecord.loadDestConditionalParentInfo(quickMergeRecord, srcConn, dstConn);
			
			objects.add(quickMergeRecord.record);
		}
		
		DatabaseObjectDAO.insertAll(objects, config, config.getOriginAppLocationCode(), dstConn);
		
		for (QuickMergeRecord quickMergeRecord : mergingRecs) {
			if (!quickMergeRecord.parentsWithDefaultValues.isEmpty()) {
				quickMergeRecord.reloadParentsWithDefaultValues(srcConn, dstConn);
			}
		}
	}
	
	public static void mergeAll(Map<String, List<QuickMergeRecord>> mergingRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		for (String key : mergingRecs.keySet()) {
			mergeAll(mergingRecs.get(key), srcConn, dstConn);
		}
	}
}
