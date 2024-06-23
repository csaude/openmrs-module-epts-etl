package org.openmrs.module.epts.etl.etl.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.model.ParentInfo;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class LoadRecord {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected EtlDatabaseObject record;
	
	protected DstConf dstConf;
	
	protected List<ParentInfo> parentsWithDefaultValues;
	
	protected boolean writeOperationHistory;
	
	protected long destinationRecordId;
	
	protected EtlEngine engine;
	
	private SrcConf srcConf;
	
	public LoadRecord(EtlDatabaseObject record, SrcConf srcConf, DstConf dstConf, EtlEngine engine,
	    boolean writeOperationHistory) {
		this.record = record;
		this.srcConf = srcConf;
		this.dstConf = dstConf;
		
		this.engine = engine;
		
		this.writeOperationHistory = writeOperationHistory;
		
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
		
		this.record.setUniqueKeysInfo(UniqueKeyInfo.cloneAllAndLoadValues(this.dstConf.getUniqueKeys(), this.record));
	}
	
	public boolean isWriteOperationHistory() {
		return writeOperationHistory;
	}
	
	public EtlEngine getEngine() {
		return engine;
	}
	
	public AppInfo getSrcApp() {
		return getEngine().getSrcApp();
	}
	
	public AppInfo getDstApp() {
		return getEngine().getDstApp();
	}
	
	public SrcConf getSrcConf() {
		return srcConf;
	}
	
	public EtlConfiguration getEtlConfiguration() {
		return getDstConf().getRelatedSyncConfiguration();
	}
	
	public EtlDatabaseObject getRecord() {
		return record;
	}
	
	public DstConf getDstConf() {
		return dstConf;
	}
	
	public void setDestinationRecordId(long destinationRecordId) {
		this.destinationRecordId = destinationRecordId;
	}
	
	public long getDestinationRecordId() {
		return destinationRecordId;
	}
	
	public List<ParentInfo> getParentsWithDefaultValues() {
		return parentsWithDefaultValues;
	}
	
	public boolean hasParentsWithDefaultValues() {
		return utilities.arrayHasElement(getParentsWithDefaultValues());
	}
	
	public void consolidateAndSaveData(boolean create, Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		if (!getDstConf().isFullLoaded()) {
			getDstConf().fullLoad();
		}
		
		loadDstParentInfo(srcConn, dstConn);
		
		try {
			
			if (create) {
				getRecord().save(getDstConf(), dstConn);
			} else {
				getRecord().update(getDstConf(), dstConn);
			}
			
			if (getDstConf().useSimpleNumericPk()) {
				this.setDestinationRecordId(getRecord().getObjectId().getSimpleValueAsInt());
			}
		}
		catch (DBException e) {
			if (e.isDuplicatePrimaryOrUniqueKeyException()) {
				
				boolean existWinningRecInfo = utilities.arrayHasElement(getDstConf().getWinningRecordFieldsInfo());
				boolean existObservationDateFields = utilities.arrayHasElement(getDstConf().getObservationDateFields());
				
				if (existObservationDateFields || existWinningRecInfo) {
					EtlDatabaseObject recordOnDB = DatabaseObjectDAO.getByUniqueKeys(this.getRecord(), dstConn);
					
					if (recordOnDB != null) {
						((AbstractDatabaseObject) getRecord()).resolveConflictWithExistingRecord(recordOnDB,
						    this.getDstConf(), dstConn);
					} else {
						getEngine().logWarn(
						    "Conflict with non avaliable record found. This will be igored and the issue will be documented");
					}
					
					if (getDstConf().useSimpleNumericPk()) {
						this.setDestinationRecordId(getRecord().getObjectId().getSimpleValueAsInt());
					}
				}
				
			} else if (e.isIntegrityConstraintViolationException()) {
				determineMissingMetadataParent(this, srcConn, dstConn);
				
				//If there is no missing metadata parent, throw exception
				throw e;
			} else
				throw e;
		}
		
		if (this.hasParentsWithDefaultValues()) {
			reloadParentsWithDefaultValues(srcConn, dstConn);
		}
	}
	
	public void resolveConflict(Connection srcConn, Connection dstConn) throws ParentNotYetMigratedException, DBException {
		if (!getDstConf().isFullLoaded())
			dstConf.fullLoad();
		
		loadDstParentInfo(srcConn, dstConn);
		
		EtlDatabaseObject recordOnDB = DatabaseObjectDAO.getByUniqueKeys(this.getRecord(), dstConn);
		
		((AbstractDatabaseObject) record).resolveConflictWithExistingRecord(recordOnDB, this.getDstConf(), dstConn);
		
		if (!this.getParentsWithDefaultValues().isEmpty()) {
			reloadParentsWithDefaultValues(srcConn, dstConn);
		}
	}
	
	public void reloadParentsWithDefaultValues(Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		for (ParentInfo parentInfo : this.getParentsWithDefaultValues()) {
			
			List<SrcConf> avaliableSrcForCurrParent = parentInfo.getParentTableConfInDst()
			        .findRelatedSrcConfWhichAsAtLeastOnematchingDst();
			
			if (utilities.arrayHasNoElement(avaliableSrcForCurrParent)) {
				throw new ForbiddenOperationException(
				        "There are relashioship which cannot auto resolved as there is no configured etl for "
				                + parentInfo.getParentTableConfInDst().getTableName() + " as source and destination!");
			}
			
			EtlDatabaseObject dstParent = null;
			
			for (SrcConf src : avaliableSrcForCurrParent) {
				DstConf dst = src.getParentConf().findDstTable(parentInfo.getParentTableConfInDst().getTableName());
				
				EtlDatabaseObject recordAsSrc = src.createRecordInstance();
				recordAsSrc.setRelatedConfiguration(src);
				
				recordAsSrc.copyFrom(parentInfo.getParentRecordInOrigin());
				
				dstParent = dst.transform(recordAsSrc, srcConn, getSrcApp(), getDstApp());
				
				if (dstParent != null) {
					LoadRecord parentData = new LoadRecord(dstParent,
					        parentInfo.getParentTableConfInDst().findRelatedSrcConf(), dst, getEngine(),
					        this.isWriteOperationHistory());
					
					parentData.load(srcConn, dstConn);
					
					dstConf = dst;
					
					break;
				}
			}
			
			EtlDatabaseObject parent = DatabaseObjectDAO.getByUniqueKeys(dstParent, dstConn);
			
			if (parent != null) {
				getRecord().changeParentValue(parentInfo.getParentTableConfInDst(), parent);
			} else {
				getEngine().logWarn(
				    "The parent " + parentInfo.getParentRecordInOrigin() + " exists on db but not avaliable yet. record "
				            + this.getRecord() + ". The task will keep trying...");
				
				TimeCountDown.sleep(10);
			}
		}
	}
	
	protected EtlOperationItemResult<EtlDatabaseObject> loadDstParentInfo(Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, MissingParentException, DBException {
		
		EtlOperationItemResult<EtlDatabaseObject> itemResult = new EtlOperationItemResult<>(getRecord());
		
		if (!utilities.arrayHasElement(getDstConf().getParentRefInfo())) {
			return itemResult;
		}
		
		for (ParentTable refInfo : getDstConf().getParentRefInfo()) {
			if (refInfo.isMetadata()) {
				continue;
			}
			
			if (!refInfo.isFullLoaded()) {
				refInfo.tryToGenerateTableAlias(this.getEtlConfiguration());
				
				refInfo.fullLoad(dstConn);
			}
			
			if (!getRecord().hasAllPerentFieldsFilled(refInfo)) {
				continue;
			}
			
			if (refInfo.useSharedPKKey()) {
				if (!refInfo.getSharedKeyRefInfo().isFullLoaded()) {
					refInfo.getSharedKeyRefInfo().tryToGenerateTableAlias(this.getEtlConfiguration());
					
					refInfo.getSharedKeyRefInfo().fullLoad(dstConn);
				}
			}
			
			if (refInfo.hasConditionalFields()) {
				if (refInfo.hasMoreThanOneConditionalFields()) {
					throw new ForbiddenOperationException("Currently not supported multiple conditional fields");
				}
				
				String conditionalFieldName = refInfo.getConditionalFields().get(0).getName();
				Object conditionalvalue = refInfo.getConditionalFields().get(0).getValue();
				
				if (!conditionalvalue.equals(getRecord().getFieldValue(conditionalFieldName).toString()))
					continue;
			}
			
			Oid key = refInfo.generateParentOidFromChild(getRecord());
			
			TableConfiguration tabConfInSrc = this.getSrcConf().findFullConfiguredConfInAllRelatedTable(
			    refInfo.generateFullTableNameOnSchema(getSrcConf().getSchema()));
			
			if (tabConfInSrc == null) {
				tabConfInSrc = new GenericTableConfiguration(this.getSrcConf());
				tabConfInSrc.setTableName(refInfo.getTableName());
				tabConfInSrc.setRelatedSyncConfiguration(this.getEtlConfiguration());
				tabConfInSrc.fullLoad(srcConn);
			}
			
			EtlDatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(tabConfInSrc, key, srcConn);
			
			if (parentInOrigin == null) {
				
				if (refInfo.hasDefaultValueDueInconsistency()) {
					
					if (refInfo.useSimplePk()) {
						key.asSimpleKey().setValue(refInfo.getDefaultValueDueInconsistency());
					} else
						throw new ForbiddenOperationException(
						        "There is a defaultValueDueInconsistency but the key is not simple on table "
						                + refInfo.getTableName());
					
					parentInOrigin = DatabaseObjectDAO.getByOid(tabConfInSrc, key, srcConn);
				}
				
				itemResult.addInconsistence(
				    InconsistenceInfo.generate(getRecord().generateTableName(), getRecord().getObjectId(),
				        refInfo.getTableName(), refInfo.generateParentOidFromChild(getRecord()).getSimpleValue(),
				        refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
				
				if (parentInOrigin == null) {
					continue;
				}
			}
			
			EtlDatabaseObject parent;
			
			if (getEngine().getRelatedEtlConfiguration().isDoNotTransformsPrimaryKeys()) {
				parent = retrieveParentByOid(refInfo, parentInOrigin, dstConn);
			} else {
				EtlDatabaseObject recInDst = refInfo.createRecordInstance();
				recInDst.setRelatedConfiguration(refInfo);
				recInDst.copyFrom(parentInOrigin);
				recInDst.loadUniqueKeyValues(refInfo);
				recInDst.loadObjectIdData(refInfo);
				
				parent = retrieveParentByUnikeKeys(recInDst, dstConn);
			}
			
			if (parent == null) {
				parent = refInfo.getDefaultObject(dstConn);
				
				if (parent == null) {
					parent = refInfo.generateAndSaveDefaultObject(dstConn);
				}
				this.getParentsWithDefaultValues().add(new ParentInfo(refInfo, parentInOrigin));
			}
			
			getRecord().changeParentValue(refInfo, parent);
			
		}
		
		return itemResult;
	}
	
	private EtlDatabaseObject retrieveParentByOid(ParentTable refInfo, EtlDatabaseObject parentInOrigin, Connection dstConn)
	        throws DBException {
		
		return DatabaseObjectDAO.getByOid(refInfo, parentInOrigin.getObjectId(), dstConn);
	}
	
	private EtlDatabaseObject retrieveParentByUnikeKeys(EtlDatabaseObject parentInOrigin, Connection dstConn)
	        throws DBException {
		
		return DatabaseObjectDAO.getByUniqueKeys(parentInOrigin, dstConn);
	}
	
	/**
	 * @param loadRecord
	 * @param srcConn
	 * @param destConn
	 * @throws DBException
	 * @throws ParentNotYetMigratedException
	 * @throws SQLException
	 */
	public static void determineMissingMetadataParent(LoadRecord loadRecord, Connection srcConn, Connection destConn)
	        throws MissingParentException, DBException {
		TableConfiguration dstConf = loadRecord.getDstConf();
		
		EtlDatabaseObject record = loadRecord.getRecord();
		
		for (ParentTable refInfo : dstConf.getParentRefInfo()) {
			if (!refInfo.isMetadata())
				continue;
			
			Object oParentId = record.getParentValue(refInfo);
			
			if (oParentId != null) {
				Integer parentId = (Integer) oParentId;
				
				EtlDatabaseObject parent = DatabaseObjectDAO.getByOid(refInfo,
				    Oid.fastCreate(refInfo.getParentColumnOnSimpleMapping(), parentId), destConn);
				
				if (parent == null)
					throw new MissingParentException(record, parentId, refInfo.getTableName(),
					        loadRecord.getDstConf().getOriginAppLocationCode(), refInfo, null);
			}
		}
	}
	
	public void save(Connection conn) throws DBException {
		SyncImportInfoVO syncInfo = SyncImportInfoVO.generateFromSyncRecord(getRecord(),
		    getDstConf().getOriginAppLocationCode(), false);
		
		syncInfo.setDestinationId((int) this.destinationRecordId);
		
		syncInfo.save(getDstConf(), conn);
	}
	
	public void load(Connection srcConn, Connection destConn) throws DBException {
		consolidateAndSaveData(true, srcConn, destConn);
		
		if (writeOperationHistory) {
			save(srcConn);
		}
	}
	
	public void reLoad(Connection srcConn, Connection destConn) throws ParentNotYetMigratedException, DBException {
		consolidateAndSaveData(false, srcConn, destConn);
	}
	
	public static EtlOperationResultHeader<EtlDatabaseObject> loadAll_(List<LoadRecord> mergingRecs, Connection srcConn,
	        Connection dstConn) throws ParentNotYetMigratedException, DBException {
		
		AbstractTableConfiguration config = mergingRecs.get(0).dstConf;
		
		if (!config.isFullLoaded()) {
			config.fullLoad();
		}
		
		EtlEngine engine = mergingRecs.get(0).getEngine();
		
		List<EtlDatabaseObject> objects = new ArrayList<EtlDatabaseObject>(mergingRecs.size());
		List<LoadRecord> processedRecors = new ArrayList<>();
		
		EtlOperationResultHeader<EtlDatabaseObject> currResult = new EtlOperationResultHeader<>(new IntervalExtremeRecord());
		
		for (LoadRecord loadRecord : mergingRecs) {
			
			boolean recursiveKeys = engine.isRunningInConcurrency()
			        ? loadRecord.hasUnresolvedRecursiveRelationship(srcConn, dstConn)
			        : false;
			
			if (recursiveKeys) {
				engine.logDebug("Record " + loadRecord.getRecord()
				        + " has recursive relationship and will be skipped to avoid dedlocks!");
				
				currResult.addAllToRecordsWithRecursiveRelashionship(loadRecord.getRecord());
				
				loadRecord.getDstConf().saveSkippedRecord(loadRecord.getRecord(), srcConn);
			} else {
				EtlOperationItemResult<EtlDatabaseObject> r = loadRecord.loadDstParentInfo(srcConn, dstConn);
				
				if (!r.hasUnresolvedInconsistences()) {
					objects.add(loadRecord.getRecord());
				}
				
				if (r.hasInconsistences()) {
					currResult.addToRecordsWithUnresolvedErrors(r);
				}
				
				processedRecors.add(loadRecord);
			}
		}
		
		currResult.addAllFromOtherResult(
		    DatabaseObjectDAO.insertAll(objects, config, config.getOriginAppLocationCode(), dstConn));
		
		if (config.hasParentRefInfo()) {
			
			for (LoadRecord r : processedRecors) {
				if (r.hasParentsWithDefaultValues()) {
					
					engine.logTrace("Reloading parents for record " + r.getRecord());
					
					r.reloadParentsWithDefaultValues(srcConn, dstConn);
					
					Oid originalOid = r.getRecord().getObjectId();
					
					EtlDatabaseObject recByUniqueKeys = null;
					
					if (!r.getEtlConfiguration().isDoNotTransformsPrimaryKeys()) {
						recByUniqueKeys = DatabaseObjectDAO.getByUniqueKeys(r.getRecord(), dstConn);
						
						if (recByUniqueKeys != null) {
							r.getRecord().setObjectId(recByUniqueKeys.getObjectId());
						} else {
							throw new ForbiddenOperationException("The record " + r.getRecord() + " where not found!");
						}
					}
					
					r.getRecord().update(r.getDstConf(), dstConn);
					
					r.getRecord().setObjectId(originalOid);
				}
			}
		}
		
		return currResult;
	}
	
	private boolean hasUnresolvedRecursiveRelationship(Connection srcConn, Connection dstConn) throws DBException {
		if (!utilities.arrayHasElement(getDstConf().getParentRefInfo())) {
			return false;
		}
		
		for (ParentTable refInfo : getDstConf().getParentRefInfo()) {
			
			if (refInfo.isMetadata()) {
				continue;
			}
			
			if (!refInfo.getTableName().equals(getDstConf().getTableName())) {
				continue;
			}
			
			if (!refInfo.isFullLoaded()) {
				refInfo.tryToGenerateTableAlias(this.getEtlConfiguration());
				
				refInfo.fullLoad(dstConn);
			}
			
			if (!getRecord().hasAllPerentFieldsFilled(refInfo)) {
				continue;
			}
			
			Oid key = refInfo.generateParentOidFromChild(getRecord());
			
			TableConfiguration tabConfInSrc = this.getSrcConf().findFullConfiguredConfInAllRelatedTable(
			    refInfo.generateFullTableNameOnSchema(getSrcConf().getSchema()));
			
			if (tabConfInSrc == null) {
				tabConfInSrc = new GenericTableConfiguration(this.getSrcConf());
				tabConfInSrc.setTableName(refInfo.getTableName());
				tabConfInSrc.setRelatedSyncConfiguration(this.getEtlConfiguration());
				tabConfInSrc.fullLoad(srcConn);
			}
			
			EtlDatabaseObject parentInOrigin = DatabaseObjectDAO.getByOid(tabConfInSrc, key, srcConn);
			
			if (parentInOrigin == null) {
				continue;
			}
			
			EtlDatabaseObject parent;
			
			if (getEngine().getRelatedEtlConfiguration().isDoNotTransformsPrimaryKeys()) {
				parent = retrieveParentByOid(refInfo, parentInOrigin, dstConn);
			} else {
				EtlDatabaseObject recInDst = refInfo.createRecordInstance();
				recInDst.setRelatedConfiguration(refInfo);
				recInDst.copyFrom(parentInOrigin);
				recInDst.loadUniqueKeyValues(refInfo);
				recInDst.loadObjectIdData(refInfo);
				
				parent = retrieveParentByUnikeKeys(recInDst, dstConn);
			}
			
			if (parent == null) {
				return true;
			}
			
		}
		
		return false;
		
	}
	
	public static EtlOperationResultHeader<EtlDatabaseObject> loadAll(Map<String, List<LoadRecord>> mergingRecs,
	        Connection srcConn, Connection dstConn) throws ParentNotYetMigratedException, DBException {
		
		EtlOperationResultHeader<EtlDatabaseObject> result = new EtlOperationResultHeader<>(new IntervalExtremeRecord());
		
		for (String key : mergingRecs.keySet()) {
			EtlOperationResultHeader<EtlDatabaseObject> currresult = loadAll_(mergingRecs.get(key), srcConn, dstConn);
			
			result.addAllFromOtherResult(currresult);
		}
		
		return result;
	}
}
