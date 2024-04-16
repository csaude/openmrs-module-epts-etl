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
import org.openmrs.module.epts.etl.controller.conf.TableParent;
import org.openmrs.module.epts.etl.controller.conf.UniqueKeyInfo;
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
		
		this.record.setUniqueKeysInfo(UniqueKeyInfo.cloneAll(this.config.getUniqueKeys()));
	}
	
	public void merge(Connection srcConn, Connection destConn) throws DBException {
		
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
			record.save(config, destConn);
			
			if (config.getPrimaryKey().isSimpleNumericKey()) {
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
					
					if (config.getPrimaryKey().isSimpleNumericKey()) {
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
			
			MergingRecord parentData = new MergingRecord(parent, refInfo.getParentTableConf(), this.srcApp, this.destApp,
			        this.writeOperationHistory);
			
			parentData.merge(srcConn, destConn);
			
			List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(refInfo.getChildTableConf(), this.record,
			    destConn);
			
			parent = utilities.arrayHasElement(recs) ? recs.get(0) : null;
			
			record.changeParentValue(refInfo, parent);
		}
	}
	
	private static void loadDestParentInfo(MergingRecord mergingRecord, Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		SyncTableConfiguration config = mergingRecord.config;
		
		if (!utilities.arrayHasElement(config.getParents()))
			return;
		
		DatabaseObject record = mergingRecord.record;
		
		for (RefInfo refInfo : config.getParentRefInfo()) {
			if (refInfo.getParentTableConf().isMetadata())
				continue;
			
			String fieldNameOnParentTable = refInfo.getSimpleRefMapping().getParentField().getNameAsClassAtt();
			String filedNameOnChildTable = refInfo.getSimpleRefMapping().getChildField().getNameAsClassAtt();
			
			Object oParentIdInOrigin = record.getParentValue(filedNameOnChildTable);
			
			if (oParentIdInOrigin != null) {
				Integer parentIdInOrigin = (Integer) oParentIdInOrigin;
				
				DatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(
				    refInfo.getParentSyncRecordClass(mergingRecord.srcApp),
				    Oid.fastCreate(fieldNameOnParentTable, parentIdInOrigin), srcConn);
				
				if (parentInOrigin == null) {
					
					if (refInfo.getSimpleRefMapping().getDefaultValueDueInconsistency() == null) {
						throw new MissingParentException(parentIdInOrigin, refInfo.getParentTableName(),
						        mergingRecord.config.getOriginAppLocationCode(), refInfo);
					} else {
						
						parentIdInOrigin = refInfo.getSimpleRefMapping().getDefaultValueDueInconsistencyAsInt();
						
						parentInOrigin = DatabaseObjectDAO.getByOid(refInfo.getParentSyncRecordClass(mergingRecord.srcApp),
						    Oid.fastCreate(fieldNameOnParentTable, parentIdInOrigin), srcConn);
						
						if (parentInOrigin == null) {
							throw new MissingParentException(parentIdInOrigin, refInfo.getParentTableName(),
							        mergingRecord.config.getOriginAppLocationCode(), refInfo);
						}
					}
				}
				
				SyncTableConfiguration parentTabConf = refInfo.getParentTableConf();
				
				List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(parentTabConf, parentInOrigin, destConn);
				
				DatabaseObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
				if (parentInDest == null) {
					mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
					
					parentInDest = DatabaseObjectDAO.getDefaultRecord(parentTabConf, destConn);
				}
				
				record.changeParentValue(refInfo, parentInDest);
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
		
		for (RefInfo refInfo : config.getParentRefInfo()) {
			if (!refInfo.getParentTableConf().isMetadata())
				continue;
			
			Object oParentId = record.getParentValue(refInfo.getChildColumnAsClassAttOnSimpleMapping());
			
			if (oParentId != null) {
				Integer parentId = (Integer) oParentId;
				
				DatabaseObject parent = DatabaseObjectDAO.getByOid(refInfo.getParentSyncRecordClass(mergingRecord.destApp),
				    Oid.fastCreate(refInfo.getParentColumnOnSimpleMapping(), parentId), destConn);
				
				if (parent == null)
					throw new MissingParentException(parentId, refInfo.getParentTableName(),
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
		
		for (TableParent parent : config.getConditionalParents()) {
			if (parent.isMetadata())
				continue;
			
			RefInfo refInfo = parent.getRef();
			
			if (utilities.arrayHasMoreThanOneElements(refInfo.getConditionalFields())) {
				throw new ForbiddenOperationException("Currently not supported multiple conditional fields");
			}
			
			String conditionalFieldName = refInfo.getConditionalFields().get(0).getNameAsClassAtt();
			Object conditionalvalue = refInfo.getConditionalFields().get(0).getValue();
			
			if (!conditionalvalue.equals(record.getFieldValue(conditionalFieldName)))
				continue;
			
			Integer parentIdInOrigin = null;
			
			try {
				parentIdInOrigin = (Integer) record.getParentValue(refInfo.getChildColumnAsClassAttOnSimpleMapping());
			}
			catch (NullPointerException | NumberFormatException e) {}
			
			if (parentIdInOrigin != null) {
				Oid objectId = Oid.fastCreate(refInfo.getParentColumnOnSimpleMapping(), parentIdInOrigin);
				
				DatabaseObject parentInOrigin = DatabaseObjectDAO
				        .getByOid(refInfo.getParentSyncRecordClass(mergingRecord.srcApp), objectId, srcConn);
				
				if (parentInOrigin == null)
					throw new MissingParentException(parentIdInOrigin, parent.getTableName(),
					        mergingRecord.config.getOriginAppLocationCode(), refInfo);
				
				List<DatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(refInfo.getParentTableConf(), parentInOrigin,
				    destConn);
				
				DatabaseObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
				if (parentInDest == null) {
					mergingRecord.parentsWithDefaultValues.add(new ParentInfo(refInfo, parentInOrigin));
					
					parentInDest = DatabaseObjectDAO.getDefaultRecord(refInfo.getParentTableConf(), destConn);
				}
				
				record.changeParentValue(refInfo, parentInDest);
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
