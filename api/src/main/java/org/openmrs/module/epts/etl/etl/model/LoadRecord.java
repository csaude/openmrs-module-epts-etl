package org.openmrs.module.epts.etl.etl.model;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.EtlDstType;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.model.ParentInfo;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class LoadRecord {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected EtlDatabaseObject record;
	
	protected DstConf dstConf;
	
	protected List<ParentInfo> parentsWithDefaultValues;
	
	protected boolean writeOperationHistory;
	
	protected long destinationRecordId;
	
	protected EtlEngine engine;
	
	private SrcConf srcConf;
	
	private EtlOperationItemResult<EtlDatabaseObject> resultItem;
	
	public LoadRecord(EtlDatabaseObject record, SrcConf srcConf, DstConf dstConf, EtlEngine engine,
	    boolean writeOperationHistory) {
		this.record = record;
		this.srcConf = srcConf;
		this.dstConf = dstConf;
		
		this.engine = engine;
		
		this.writeOperationHistory = writeOperationHistory;
		
		this.parentsWithDefaultValues = new ArrayList<ParentInfo>();
		
		this.record.setUniqueKeysInfo(UniqueKeyInfo.cloneAllAndLoadValues(this.dstConf.getUniqueKeys(), this.record));
		
		this.resultItem = new EtlOperationItemResult<EtlDatabaseObject>(record);
	}
	
	public EtlOperationItemResult<EtlDatabaseObject> getResultItem() {
		return resultItem;
	}
	
	public boolean isWriteOperationHistory() {
		return writeOperationHistory;
	}
	
	public EtlEngine getTaskProcessor() {
		return engine;
	}
	
	public DBConnectionInfo getSrcConnInfo() {
		return getTaskProcessor().getSrcConnInfo();
	}
	
	public DBConnectionInfo getDstConnInfo() {
		return getTaskProcessor().getDstConnInfo();
	}
	
	public SrcConf getSrcConf() {
		return srcConf;
	}
	
	public EtlConfiguration getEtlConfiguration() {
		return getDstConf().getRelatedEtlConf();
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
		
		EtlDstType dstType = getTaskProcessor().determineDstType(this.getDstConf());
		
		if (dstType.isDb()) {
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
							getTaskProcessor().logWarn(
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
		} else if (dstType.isFile()) {
			loadAllToFile(utilities.parseObjectToList_(this, LoadRecord.class));
		} else {
			throw new ForbiddenOperationException("Unsupported dstType '" + dstType + "'");
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
				
				dstParent = dst.transform(recordAsSrc, srcConn, getSrcConnInfo(), getDstConnInfo());
				
				if (dstParent != null) {
					LoadRecord parentData = new LoadRecord(dstParent,
					        parentInfo.getParentTableConfInDst().findRelatedSrcConf(), dst, getTaskProcessor(),
					        this.isWriteOperationHistory());
					
					try {
						parentData.load(srcConn, dstConn);
					}
					catch (DBException e) {
						if (e.isIntegrityConstraintViolationException()) {
							engine.logDebug("The parent for default for parent [" + parentInfo.getParentRecordInOrigin()
							        + "] could not be loaded. The record [");
							
							this.getResultItem()
							        .addInconsistence(InconsistenceInfo.generate(getRecord().generateTableName(),
							            getRecord().getObjectId(), parentInfo.getParentTableConfInDst().getTableName(),
							            parentInfo.getParentRecordInOrigin().getObjectId().getSimpleValueAsInt(), null,
							            this.getDstConf().getOriginAppLocationCode()));
							
						} else {
							this.getResultItem().setException(e);
						}
					}
					
					dstConf = dst;
					
					break;
				}
			}
			
			EtlDatabaseObject parent = DatabaseObjectDAO.getByUniqueKeys(dstParent, dstConn);
			
			if (parent != null) {
				getRecord().changeParentValue(parentInfo.getParentTableConfInDst(), parent);
			} else {
				getTaskProcessor().logWarn(
				    "The parent " + parentInfo.getParentRecordInOrigin() + " exists on db but not avaliable yet. record "
				            + this.getRecord() + ". The task will keep trying...");
				
				TimeCountDown.sleep(10);
			}
		}
	}
	
	protected void loadDstParentInfo(Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, MissingParentException, DBException {
		
		if (!utilities.arrayHasElement(getDstConf().getParentRefInfo())) {
			return;
		}
		
		for (ParentTable refInfo : getDstConf().getParentRefInfo()) {
			if (!refInfo.isFullLoaded()) {
				refInfo.tryToGenerateTableAlias(this.getEtlConfiguration());
				
				refInfo.fullLoad(dstConn);
			}
			
			if (!getRecord().hasAllPerentFieldsFilled(refInfo)) {
				continue;
			}
			
			if (refInfo.isMetadata()) {
				
				EtlDatabaseObject parentInOrigin = getRecord().getRelatedParentObject(refInfo, getSrcConf(), srcConn);
				EtlDatabaseObject parent = null;
				
				if (parentInOrigin != null) {
					parent = retrieveParentByOid(refInfo, parentInOrigin, dstConn);
					
					if (parent == null) {
						getTaskProcessor().logWarn("Missing metadata " + parentInOrigin
						        + ". This issue will be documented on inconsitence_info table");
						
						this.getResultItem().addInconsistence(
						    InconsistenceInfo.generate(getRecord().generateTableName(), getRecord().getObjectId(),
						        refInfo.getTableName(), refInfo.generateParentOidFromChild(getRecord()).getSimpleValue(),
						        refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
					}
				} else {
					getTaskProcessor().logWarn("Missing metadata " + refInfo.generateParentOidFromChild(getRecord())
					        + ". This issue will be documented on inconsitence_info table");
					
					this.getResultItem()
					        .addInconsistence(InconsistenceInfo.generate(getRecord().generateTableName(),
					            getRecord().getObjectId(), refInfo.getTableName(),
					            refInfo.generateParentOidFromChild(getRecord()).getSimpleValue(),
					            refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
				}
				
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
			
			EtlDatabaseObject parentInOrigin = getRecord().getRelatedParentObject(refInfo, getSrcConf(), srcConn);
			
			EtlDatabaseObject sharedkeyParentInOrigin = null;
			
			if (parentInOrigin == null) {
				
				if (refInfo.hasDefaultValueDueInconsistency()) {
					
					Oid key = refInfo.generateParentOidFromChild(getRecord());
					
					if (refInfo.useSimplePk()) {
						key.asSimpleKey().setValue(refInfo.getDefaultValueDueInconsistency());
					} else
						throw new ForbiddenOperationException(
						        "There is a defaultValueDueInconsistency but the key is not simple on table "
						                + refInfo.getTableName());
					
					parentInOrigin = getRecord().getRelatedParentObjectOnSrc(refInfo, key, getSrcConf(), srcConn);
				}
				
				this.getResultItem().addInconsistence(
				    InconsistenceInfo.generate(getRecord().generateTableName(), getRecord().getObjectId(),
				        refInfo.getTableName(), refInfo.generateParentOidFromChild(getRecord()).getSimpleValue(),
				        refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
				
				if (parentInOrigin == null) {
					continue;
				}
			}
			
			//Try to load the shared key parent and generate inconsistence if is not exists
			if (refInfo.useSharedPKKey() && !refInfo.hasItsOwnKeys()) {
				sharedkeyParentInOrigin = parentInOrigin.getSharedKeyParentRelatedObject(srcConn);
				
				if (sharedkeyParentInOrigin == null && !refInfo.hasDefaultValueDueInconsistency()) {
					this.getResultItem()
					        .addInconsistence(InconsistenceInfo.generate(getRecord().generateTableName(),
					            getRecord().getObjectId(), refInfo.getTableName(),
					            refInfo.generateParentOidFromChild(getRecord()).getSimpleValue(),
					            refInfo.getDefaultValueDueInconsistency(), this.getDstConf().getOriginAppLocationCode()));
					
					continue;
				}
			}
			
			EtlDatabaseObject parent = null;
			
			if (getTaskProcessor().getRelatedEtlConfiguration().isDoNotTransformsPrimaryKeys()) {
				parent = retrieveParentByOid(refInfo, parentInOrigin, dstConn);
			} else {
				
				//Retrieve parent using shared key parent unique key
				if (sharedkeyParentInOrigin != null) {
					EtlDatabaseObject recInDst = refInfo.getSharedKeyRefInfo().createRecordInstance();
					recInDst.setRelatedConfiguration(refInfo.getSharedKeyRefInfo());
					recInDst.copyFrom(sharedkeyParentInOrigin);
					recInDst.loadUniqueKeyValues(refInfo.getSharedKeyRefInfo());
					recInDst.loadObjectIdData(refInfo.getSharedKeyRefInfo());
					
					EtlDatabaseObject sharedPkParent = retrieveParentByUnikeKeys(recInDst, dstConn);
					
					if (sharedPkParent != null) {
						parent = sharedPkParent.getSharedKeyChildRelatedObject(refInfo, dstConn);
					}
					
				} else {
					EtlDatabaseObject recInDst = refInfo.createRecordInstance();
					recInDst.setRelatedConfiguration(refInfo);
					recInDst.copyFrom(parentInOrigin);
					recInDst.loadUniqueKeyValues(refInfo);
					recInDst.loadObjectIdData(refInfo);
					
					parent = retrieveParentByUnikeKeys(recInDst, dstConn);
				}
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
		
	}
	
	private EtlDatabaseObject retrieveParentByOid(ParentTable refInfo, EtlDatabaseObject parentInOrigin, Connection dstConn)
	        throws DBException {
		
		return DatabaseObjectDAO.getByOid(refInfo, parentInOrigin.getObjectId(), dstConn);
	}
	
	private EtlDatabaseObject retrieveParentByUnikeKeys(EtlDatabaseObject parent, Connection dstConn) throws DBException {
		return DatabaseObjectDAO.getByUniqueKeys(parent, dstConn);
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
	
	public static void loadAllToDb(List<LoadRecord> mergingRecs, Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		AbstractTableConfiguration config = mergingRecs.get(0).dstConf;
		
		if (!config.isFullLoaded()) {
			config.fullLoad();
		}
		
		EtlEngine processor = mergingRecs.get(0).getTaskProcessor();
		
		List<EtlDatabaseObject> objects = new ArrayList<EtlDatabaseObject>(mergingRecs.size());
		List<LoadRecord> processedRecors = new ArrayList<>();
		
		processor.logDebug("Preparing the load of " + mergingRecs.size());
		
		for (LoadRecord loadRecord : mergingRecs) {
			processor.logTrace("Preparing the load of record " + loadRecord.getRecord());
			
			boolean recursiveKeys = processor.isRunningInConcurrency()
			        ? loadRecord.hasUnresolvedRecursiveRelationship(srcConn, dstConn)
			        : false;
			
			if (recursiveKeys) {
				processor.logDebug("Record " + loadRecord.getRecord()
				        + " has recursive relationship and will be skipped to avoid dedlocks!");
				
				processor.getTaskResultInfo().addToRecordsWithRecursiveRelashionship(loadRecord.getRecord());
				
				loadRecord.getDstConf().saveSkippedRecord(loadRecord.getRecord(), srcConn);
			} else {
				loadRecord.loadDstParentInfo(srcConn, dstConn);
				
				if (!loadRecord.getResultItem().hasUnresolvedInconsistences()) {
					objects.add(loadRecord.getRecord());
					processedRecors.add(loadRecord);
					
					if (loadRecord.getResultItem().hasInconsistences()) {
						processor.logTrace(
						    "Found inconsistences on record " + loadRecord.getRecord() + " but all were resolved!");
					}
				}
				
				processor.getTaskResultInfo().add(loadRecord.getResultItem());
			}
		}
		
		processor.logDebug("Starting the insertion of " + objects.size() + " on db...");
		processor.getTaskResultInfo().addAllFromOtherResult(
		    DatabaseObjectDAO.insertAll(objects, config, config.getOriginAppLocationCode(), dstConn));
		
		processor.logDebug(objects.size() + " records inserted on db!");
		
		if (config.hasParentRefInfo()) {
			
			for (LoadRecord r : processedRecors) {
				if (r.hasParentsWithDefaultValues()) {
					
					processor.logTrace("Reloading parents for record " + r.getRecord());
					
					r.reloadParentsWithDefaultValues(srcConn, dstConn);
					
					if (r.getResultItem().hasUnresolvedInconsistences()) {
						processor.logDebug(
						    "The record has inconsistence after reloading of default parent.  Removing it " + r.getRecord());
						r.getRecord().remove(dstConn);
						
						processor.getTaskResultInfo().remove(r.getResultItem());
						
						processor.getTaskResultInfo().add(r.getResultItem());
					} else {
						
						Oid originalOid = r.getRecord().getObjectId();
						
						EtlDatabaseObject recByUniqueKeys = null;
						
						if (!r.getEtlConfiguration().isDoNotTransformsPrimaryKeys()) {
							recByUniqueKeys = DatabaseObjectDAO.getByUniqueKeys(r.getRecord(), dstConn);
							
							if (recByUniqueKeys != null) {
								r.getRecord().setObjectId(recByUniqueKeys.getObjectId());
								
								r.getRecord().update(r.getDstConf(), dstConn);
								
								r.getRecord().setObjectId(originalOid);
							} else {
								r.getResultItem().setException(new ForbiddenOperationException("The record " + r.getRecord()
								        + " where not found after the it has been loaded to db!"));
							}
						} else {
							r.getRecord().update(r.getDstConf(), dstConn);
							
							r.getRecord().setObjectId(originalOid);
						}
					}
				}
			}
		}
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
			
			if (getTaskProcessor().getRelatedEtlConfiguration().isDoNotTransformsPrimaryKeys()) {
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
	
	public static void loadAll(Map<String, List<LoadRecord>> mergingRecs, Connection srcConn, Connection dstConn)
	        throws ParentNotYetMigratedException, DBException {
		
		for (String key : mergingRecs.keySet()) {
			LoadRecord defaultRec = mergingRecs.get(key).get(0);
			
			EtlDstType dstType = defaultRec.getTaskProcessor().determineDstType(defaultRec.getDstConf());
			
			if (dstType.isDb()) {
				loadAllToDb(mergingRecs.get(key), srcConn, dstConn);
			} else if (dstType.isFile()) {
				loadAllToFile(mergingRecs.get(key));
			} else {
				throw new ForbiddenOperationException("Unsupported dstType '" + dstType + "'");
			}
		}
	}
	
	public EtlDatabaseObject parseToEtlObject() {
		return this.getRecord();
	}
	
	public static List<EtlDatabaseObject> parseAllToEtlObject(List<LoadRecord> mergingRecs) {
		List<EtlDatabaseObject> objs = new ArrayList<>(mergingRecs.size());
		
		for (LoadRecord l : mergingRecs) {
			objs.add(l.parseToEtlObject());
		}
		
		return objs;
	}
	
	public static void loadAllToFile(List<LoadRecord> mergingRecs) throws ParentNotYetMigratedException, DBException {
		
		EtlEngine taskProcessor = mergingRecs.get(0).getTaskProcessor();
		
		Engine<EtlDatabaseObject> engine = taskProcessor.getEngine();
		
		List<EtlDatabaseObject> objs = parseAllToEtlObject(mergingRecs);
		
		String dataFile = engine.getDataDir().getAbsolutePath() + File.separator + objs.get(0).generateTableName();
		
		String data = null;
		
		if (engine.isJsonDst()) {
			data = utilities.parseToJSON(objs);
			
			dataFile += ".json";
		} else if (engine.isCsvDst()) {
			
			boolean includeHeader = FileUtilities.isEmpty(new File(dataFile));
			
			data = utilities.parseToCSV(objs, includeHeader);
			
			dataFile += ".csv";
		} else if (engine.isDumpDst()) {
			data = TableConfiguration.generateInsertDump(objs);
			
			dataFile += ".sql";
		}
		
		synchronized (engine) {
			FileUtilities.write(dataFile, data);
		}
		
		taskProcessor.getTaskResultInfo()
		        .addAllToRecordsWithNoError(EtlOperationItemResult.parseFromEtlDatabaseObject(objs));
	}
	
	public static String parseToJson(List<EtlDatabaseObject> objs) {
		return utilities.parseToJSON(objs);
	}
	
}
