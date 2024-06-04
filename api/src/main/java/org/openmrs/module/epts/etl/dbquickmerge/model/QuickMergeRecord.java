package org.openmrs.module.epts.etl.dbquickmerge.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

public class QuickMergeRecord {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected EtlDatabaseObject record;
	
	protected DstConf config;
	
	protected List<ParentInfo> parentsWithDefaultValues;
	
	protected AppInfo srcApp;
	
	protected AppInfo destApp;
	
	protected boolean writeOperationHistory;
	
	protected long destinationRecordId;
	
	protected SrcConf srcConf;
	
	public QuickMergeRecord(EtlDatabaseObject record, SrcConf srcConf, DstConf config, AppInfo srcApp, AppInfo destApp,
	    boolean writeOperationHistory) {
		this.record = record;
		this.config = config;
		this.srcApp = srcApp;
		this.destApp = destApp;
		this.writeOperationHistory = writeOperationHistory;
		
		this.srcConf = srcConf;
		
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
		
		this.record.setUniqueKeysInfo(UniqueKeyInfo.cloneAllAndLoadValues(this.config.getUniqueKeys(), this.record));
	}
	
	public AppInfo getSrcApp() {
		return srcApp;
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
	
	public DstConf getConfig() {
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
					List<EtlDatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(this.config, this.record, destConn);
					
					EtlDatabaseObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
					
					((AbstractDatabaseObject) record).resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
					
					if (config.useSimpleNumericPk()) {
						this.destinationRecordId = record.getObjectId().getSimpleValueAsInt();
					}
				}
				
			} else if (e.isIntegrityConstraintViolationException()) {
				determineMissingMetadataParent(this, srcConn, destConn);
				
				//If there is no missing metadata parent, throw exception
				throw e;
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
		
		List<EtlDatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(this.config, this.record, destConn);
		
		EtlDatabaseObject recordOnDB = utilities.arrayHasElement(recs) ? recs.get(0) : null;
		
		((AbstractDatabaseObject) record).resolveConflictWithExistingRecord(recordOnDB, this.config, destConn);
		
		if (!this.parentsWithDefaultValues.isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, destConn);
		}
	}
	
	public void reloadParentsWithDefaultValues(Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		for (ParentInfo parentInfo : this.parentsWithDefaultValues) {
			
			List<DstConf> dstSharedConf = parentInfo.getParentTableConfInDst().findRelatedDstConf();
			
			if (utilities.arrayHasNoElement(dstSharedConf)) {
				throw new ForbiddenOperationException(
				        "There are relashioship which cannot auto resolved as there is no configured etl for "
				                + parentInfo.getParentTableConfInDst().getTableName() + " as destination!");
			}
			
			EtlDatabaseObject dstParent = null;
			DstConf dstConf = null;
			
			for (DstConf dst : dstSharedConf) {
				dstParent = dst.transform(parentInfo.getParentRecordInOrigin(), srcConn, srcApp, destApp);
				
				if (dstParent != null) {
					QuickMergeRecord parentData = new QuickMergeRecord(dstParent,
					        parentInfo.getParentTableConfInDst().findRelatedSrcConf(), dst, srcApp, destApp,
					        this.writeOperationHistory);
					
					parentData.merge(srcConn, destConn);
					
					dstConf = dst;
					
					break;
				}
			}
			
			List<EtlDatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(dstConf, dstParent, destConn);
			
			EtlDatabaseObject parent = utilities.arrayHasElement(recs) ? recs.get(0) : null;
			
			record.changeParentValue(parentInfo.getParentTableConfInDst(), parent);
		}
	}
	
	protected static void loadDestParentInfo(QuickMergeRecord quickMergeRecord, Connection srcConn, Connection destConn)
	        throws ParentNotYetMigratedException, DBException {
		
		TableConfiguration config = quickMergeRecord.config;
		
		if (!utilities.arrayHasElement(config.getParentRefInfo()))
			return;
		
		EtlDatabaseObject record = quickMergeRecord.record;
		
		for (ParentTable refInfo : config.getParentRefInfo()) {
			if (refInfo.isMetadata())
				continue;
			
			String fieldNameOnParentTable = refInfo.getSimpleRefMapping().getParentField().getNameAsClassAtt();
			
			Object oParentIdInOrigin = record.getParentValue(refInfo);
			
			if (oParentIdInOrigin != null) {
				Integer parentIdInOrigin = (Integer) oParentIdInOrigin;
				
				EtlDatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(refInfo,
				    Oid.fastCreate(fieldNameOnParentTable, parentIdInOrigin), srcConn);
				
				if (parentInOrigin == null) {
					
					if (refInfo.getSimpleRefMapping().getDefaultValueDueInconsistency() == null) {
						throw new MissingParentException(record, parentIdInOrigin, refInfo.getTableName(),
						        quickMergeRecord.config.getOriginAppLocationCode(), refInfo);
					} else {
						
						parentIdInOrigin = refInfo.getSimpleRefMapping().getDefaultValueDueInconsistencyAsInt();
						
						parentInOrigin = DatabaseObjectDAO.getByOid(refInfo,
						    Oid.fastCreate(fieldNameOnParentTable, parentIdInOrigin), srcConn);
						
						if (parentInOrigin == null) {
							throw new MissingParentException(record, parentIdInOrigin, refInfo.getTableName(),
							        quickMergeRecord.config.getOriginAppLocationCode(), refInfo);
						}
					}
				}
				
				TableConfiguration parentTabConf = refInfo;
				
				List<EtlDatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(parentTabConf, parentInOrigin, destConn);
				
				EtlDatabaseObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
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
		TableConfiguration config = quickMergeRecord.config;
		
		EtlDatabaseObject record = quickMergeRecord.record;
		
		for (ParentTable refInfo : config.getParentRefInfo()) {
			if (!refInfo.isMetadata())
				continue;
			
			Object oParentId = record.getParentValue(refInfo);
			
			if (oParentId != null) {
				Integer parentId = (Integer) oParentId;
				
				EtlDatabaseObject parent = DatabaseObjectDAO.getByOid(refInfo,
				    Oid.fastCreate(refInfo.getParentColumnOnSimpleMapping(), parentId), destConn);
				
				if (parent == null)
					throw new MissingParentException(record, parentId, refInfo.getTableName(),
					        quickMergeRecord.config.getOriginAppLocationCode(), refInfo);
			}
		}
	}
	
	protected static void loadDestConditionalParentInfo(QuickMergeRecord quickMergeRecord, Connection srcConn,
	        Connection destConn) throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(quickMergeRecord.config.getConditionalParents()))
			return;
		
		EtlDatabaseObject record = quickMergeRecord.record;
		TableConfiguration config = quickMergeRecord.config;
		
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
				parentIdInOrigin = (Integer) record.getParentValue(parent);
			}
			catch (NullPointerException | NumberFormatException e) {}
			
			if (parentIdInOrigin != null) {
				Oid objectId = Oid.fastCreate(parent.getParentColumnOnSimpleMapping(), parentIdInOrigin);
				
				EtlDatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(parent, objectId, srcConn);
				
				if (parentInOrigin == null)
					throw new MissingParentException(record, parentIdInOrigin, parent.getTableName(),
					        quickMergeRecord.config.getOriginAppLocationCode(), parent);
				
				List<EtlDatabaseObject> recs = DatabaseObjectDAO.getByUniqueKeys(parent, parentInOrigin, destConn);
				
				EtlDatabaseObject parentInDest = utilities.arrayHasElement(recs) ? recs.get(0) : null;
				
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
	
	public EtlDatabaseObject getRecord() {
		return record;
	}
	
	public static void mergeAll(List<QuickMergeRecord> mergingRecs, Connection srcConn, OpenConnection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		if (!utilities.arrayHasElement(mergingRecs)) {
			return;
		}
		
		TableConfiguration config = mergingRecs.get(0).config;
		
		if (!config.isFullLoaded()) {
			config.fullLoad();
		}
		
		List<EtlDatabaseObject> objects = new ArrayList<EtlDatabaseObject>(mergingRecs.size());
		
		for (QuickMergeRecord quickMergeRecord : mergingRecs) {
			if (config.hasParentRefInfo()) {
				QuickMergeRecord.loadDestParentInfo(quickMergeRecord, srcConn, dstConn);
				QuickMergeRecord.loadDestConditionalParentInfo(quickMergeRecord, srcConn, dstConn);
			}
			
			objects.add(quickMergeRecord.record);
		}
		
		DatabaseObjectDAO.insertAll(objects, config, config.getOriginAppLocationCode(), dstConn);
		
		if (config.hasParentRefInfo())
		
		{
			for (QuickMergeRecord quickMergeRecord : mergingRecs) {
				if (!quickMergeRecord.parentsWithDefaultValues.isEmpty()) {
					quickMergeRecord.reloadParentsWithDefaultValues(srcConn, dstConn);
				}
			}
		}
	}
	
	public static void mergeAll(List<String> mapOrder, Map<String, List<QuickMergeRecord>> mergingRecs, Connection srcConn,
	        OpenConnection dstConn) throws ParentNotYetMigratedException, DBException {
		for (String key : mapOrder) {
			mergeAll(mergingRecs.get(key), srcConn, dstConn);
		}
	}
	
	public EtlConfiguration getEtlConfiguration() {
		return getConfig().getRelatedSyncConfiguration();
	}
}
