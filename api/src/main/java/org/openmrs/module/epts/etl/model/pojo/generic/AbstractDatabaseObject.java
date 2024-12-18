package org.openmrs.module.epts.etl.model.pojo.generic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.conf.ChildTable;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.dbquickmerge.model.ParentInfo;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.EtlDatabaseObjectUniqueKeyInfo;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.base.BaseVO;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractDatabaseObject extends BaseVO implements EtlDatabaseObject {
	
	protected boolean metadata;
	
	protected Oid objectId;
	
	/*
	 * Indicate if there where parents which have been ingored
	 */
	protected boolean hasIgnoredParent;
	
	protected String uuid;
	
	protected EtlStageRecordVO relatedSyncInfo;
	
	protected List<EtlDatabaseObjectUniqueKeyInfo> uniqueKeysInfo;
	
	protected List<ParentInfo> parentsWithDefaultValues;
	
	protected EtlDatabaseObject srcRelatedObject;
	
	protected ConflictResolutionType conflictResolutionType;
	
	public AbstractDatabaseObject() {
		this.objectId = new Oid();
		
		this.conflictResolutionType = ConflictResolutionType.NONE;
	}
	
	@Override
	public ConflictResolutionType getConflictResolutionType() {
		return conflictResolutionType;
	}
	
	@Override
	public void setConflictResolutionType(ConflictResolutionType conflictResolutionType) {
		this.conflictResolutionType = conflictResolutionType;
	}
	
	@Override
	@JsonIgnore
	public EtlDatabaseObject getSrcRelatedObject() {
		return srcRelatedObject;
	}
	
	@Override
	public void setSrcRelatedObject(EtlDatabaseObject srcRelatedObject) {
		this.srcRelatedObject = srcRelatedObject;
	}
	
	public void load(ResultSet rs) throws SQLException {
		super.load(rs);
		
		try {
			this.uuid = rs.getString("uuid");
		}
		catch (SQLException e) {}
		
		try {
			this.relatedSyncInfo = new EtlStageRecordVO();
			this.relatedSyncInfo.load(rs);
			
		}
		catch (SQLException e) {}
	}
	
	@Override
	public Oid getObjectId() {
		return this.objectId;
	}
	
	public void setObjectId(Oid objectId) {
		this.objectId = objectId;
		
		if (objectId != null) {
			for (Key key : objectId.getFields()) {
				setFieldValue(key.getName(), key.getValue());
			}
		}
		
	}
	
	/**
	 * Retrieve a specific parent of this dstRecord. The parent is loaded using the origin (source)
	 * identification key
	 * 
	 * @param <T>
	 * @param parentClass parent class
	 * @param parentId in origin (source database)
	 * @param ignorable
	 * @param conn
	 * @return
	 * @throws ParentNotYetMigratedException if the parent is not ignorable and is not found on
	 *             database
	 * @throws DBException
	 */
	@Override
	public EtlDatabaseObject retrieveParentInDestination(Integer parentId, String recordOriginLocationCode,
	        TableConfiguration parentTableConfiguration, boolean ignorable, Connection conn)
	        throws ParentNotYetMigratedException, DBException {
		if (parentId == null)
			return null;
		
		EtlDatabaseObject parentOnDestination;
		
		try {
			parentOnDestination = DatabaseObjectDAO.thinGetByRecordOrigin(parentId, recordOriginLocationCode,
			    parentTableConfiguration, conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			TimeCountDown.sleep(2000);
			
			throw new RuntimeException(e);
		}
		
		if (parentOnDestination != null) {
			return parentOnDestination;
		}
		
		if (ignorable) {
			this.hasIgnoredParent = true;
			return null;
		}
		
		throw new ParentNotYetMigratedException(parentId, parentTableConfiguration.getTableName(),
		        this.relatedSyncInfo.getRecordOriginLocationCode(), null);
	}
	
	@Override
	public boolean hasExactilyTheSameDataWith(EtlDatabaseObject srcObj) {
		for (Field field : getFields()) {
			Object thisValue = this.getFieldValue(field.getNameAsClassAtt());
			Object otherValue = srcObj.getFieldValue(field.getNameAsClassAtt());
			
			if (thisValue == null && otherValue != null || otherValue == null && thisValue != null) {
				return false;
			}
			
			if (thisValue != null && !thisValue.equals(otherValue)) {
				return false;
			}
			
		}
		
		return true;
	}
	
	@Override
	public void setFieldValue(String fieldName, Object value) {
		try {
			
			for (java.lang.reflect.Field field : getInstanceFields()) {
				
				if (field.getName().equals(fieldName)) {
					if (value == null) {
						field.set(this, null);
					} else if (field.getType().equals(String.class)) {
						field.set(this, value.toString());
					} else if (field.getType().equals(Integer.class) && value instanceof Double) {
						/*
						 * Cast value to int if the field type is Integer.
						 * 
						 * This was added to resolve some issues when using generic etl where some field from query come
						 * with double value to be inserted in int fields
						 */
						String str = utilities.displayDoubleOnIntegerFormat((Double) value);
						
						field.set(this, Integer.parseInt(str));
					} else if (field.getType().isEnum()) {
						
						@SuppressWarnings("rawtypes")
						Class enumClazz = field.getType();
						
						@SuppressWarnings("unchecked")
						Enum<?> eValue = Enum.valueOf(enumClazz, value.toString());
						
						field.set(this, eValue);
					} else {
						field.set(this, value);
					}
					
					return;
				}
			}
			
			throw new ForbiddenOperationException(
			        "The field " + fieldName + " was not found on entity " + this.getClass().getName());
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public Object getFieldValue(String fieldsName) throws ForbiddenOperationException {
		return utilities.getFieldValue(this, fieldsName);
	}
	
	@Override
	@JsonIgnore
	public List<EtlDatabaseObjectUniqueKeyInfo> getUniqueKeysInfo() {
		return this.uniqueKeysInfo;
	}
	
	@Override
	public void setUniqueKeysInfo(List<EtlDatabaseObjectUniqueKeyInfo> uniqueKeysInfo) {
		this.uniqueKeysInfo = uniqueKeysInfo;
		
		if (utilities.arrayHasElement(this.uniqueKeysInfo)) {
			for (UniqueKeyInfo uk : this.uniqueKeysInfo) {
				uk.loadValuesToFields(this);
			}
		}
	}
	
	@Override
	@JsonIgnore
	public EtlStageRecordVO getRelatedSyncInfo() {
		return relatedSyncInfo;
	}
	
	@Override
	public void setRelatedSyncInfo(EtlStageRecordVO relatedSyncInfo) {
		this.relatedSyncInfo = relatedSyncInfo;
	}
	
	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public String getUuid() {
		return this.uuid;
	}
	
	@JsonIgnore
	public boolean hasIgnoredParent() {
		return hasIgnoredParent;
	}
	
	public void setHasIgnoredParent(boolean hasIgnoredParent) {
		this.hasIgnoredParent = hasIgnoredParent;
	}
	
	@Override
	public void save(TableConfiguration tableConfiguration, ConflictResolutionType onConflict, Connection conn)
	        throws DBException {
		try {
			DatabaseObjectDAO.insert(this, tableConfiguration, conn);
		}
		catch (DBException e) {
			
			if (e.isDuplicatePrimaryOrUniqueKeyException()) {
				DBUtilities.handlePostgresExceptionIssue(conn);
				
				if (onConflict.makeYourDecision()) {
					try {
						resolveConflictWithExistingRecord(tableConfiguration, e, conn);
					}
					catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			} else
				throw e;
		}
		
	}
	
	@Override
	public void save(TableConfiguration tableConfiguration, Connection conn) throws DBException {
		save(tableConfiguration, tableConfiguration.onConflict(), conn);
	}
	
	@Override
	public void update(TableConfiguration syncTableInfo, Connection conn) throws DBException {
		try {
			DatabaseObjectDAO.update(this, conn);
		}
		catch (DBException e) {
			throw e;
		}
	}
	
	/**
	 * Resolve collision between existing metadata (in destination) and newly coming metadata (from
	 * any source). The collision resolution consist on changind existing children to point the
	 * newly coming metadata
	 * 
	 * @param tableConfig
	 * @param recordInConflict
	 * @param conn
	 * @throws DBException
	 */
	@SuppressWarnings("unused")
	private void resolveMetadataCollision(EtlDatabaseObject recordInConflict, TableConfiguration tableConfig,
	        Connection conn) throws DBException {
		//Object Id Collision
		if (this.getObjectId() == recordInConflict.getObjectId()) {
			recordInConflict.changeObjectId(tableConfig, conn);
			
			DatabaseObjectDAO.insert(this, tableConfig, conn);
		} else if (this.getUuid() != null && this.getUuid().equals(recordInConflict.getUuid())) {
			//In case of uuid collision it is assumed that the records are same then the old dstRecord must be changed to the new one
			
			//1. Change existing dstRecord Uuid
			recordInConflict.setUuid(recordInConflict.getUuid() + "_");
			
			DatabaseObjectDAO.update(recordInConflict, conn);
			
			//2. Check if the new object id is avaliable
			EtlDatabaseObject recOnDBById = DatabaseObjectDAO.getByOid(tableConfig, this.getObjectId(), conn);
			
			if (recOnDBById == null) {
				//3. Save the new dstRecord
				DatabaseObjectDAO.insert(this, tableConfig, conn);
			} else {
				recOnDBById.changeObjectId(tableConfig, conn);
				
				DatabaseObjectDAO.insert(this, tableConfig, conn);
			}
			
			recordInConflict.changeParentForAllChildren(this, tableConfig, conn);
			
			recordInConflict.remove(conn);
		}
	}
	
	@Override
	public void changeObjectId(TableConfiguration syncTableInfo, Connection conn) throws DBException {
		if (syncTableInfo.getPrimaryKey().isCompositeKey()) {
			throw new ForbiddenOperationException("The related table (" + syncTableInfo.getTableName()
			        + ") has composite pk. YOu cannot change the object Id!");
		}
		
		//1. backup the old dstRecord
		GenericDatabaseObject oldRecod = GenericDatabaseObject.fastCreate(getRelatedSyncInfo(), syncTableInfo);
		
		//2. Retrieve any avaliable id for old dstRecord
		Integer avaliableId = DatabaseObjectDAO.getAvaliableObjectId(syncTableInfo, 999999999, conn);
		
		this.getObjectId().retrieveSimpleKey().setValue(avaliableId);
		this.setUuid("tmp" + avaliableId);
		this.setRelatedSyncInfo(null);
		
		//3. Save the new recod
		DatabaseObjectDAO.insert(this, syncTableInfo, conn);
		
		//4. Change existing dstRecord's children to point to new parent
		oldRecod.changeParentForAllChildren(this, syncTableInfo, conn);
		
		//5. Remove old dstRecord
		oldRecod.remove(conn);
		
		//6. Reset dstRecord info
		this.setUuid(oldRecod.getUuid());
		this.setRelatedSyncInfo(oldRecod.getRelatedSyncInfo());
		
		DatabaseObjectDAO.update(this, conn);
	}
	
	@Override
	public void changeParentForAllChildren(EtlDatabaseObject newParent, TableConfiguration syncTableInfo, Connection conn)
	        throws DBException {
		
		if (syncTableInfo.getPrimaryKey().isCompositeKey()) {
			throw new ForbiddenOperationException("The related table (" + syncTableInfo.getTableName()
			        + ") has composite pk. YOu cannot change the parent for children!");
		}
		
		this.loadObjectIdData(syncTableInfo);
		
		for (ChildTable refInfo : syncTableInfo.getChildRefInfo()) {
			
			List<EtlDatabaseObject> children = DatabaseObjectDAO.getByParentId(refInfo.getParentTableConf(),
			    refInfo.getSimpleRefMapping().getParentField().getName(), this.getObjectId().getSimpleValueAsInt(), conn);
			
			for (EtlDatabaseObject child : children) {
				child.changeParentValue((ParentTableImpl) refInfo.getParentTableConf(), newParent);
				DatabaseObjectDAO.update(child, conn);
			}
		}
	}
	
	@Override
	public void refreshLastSyncDateOnOrigin(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn) {
		try {
			DatabaseObjectDAO.refreshLastSyncDateOnOrigin(this, tableConfiguration, recordOriginLocationCode, conn);
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void refreshLastSyncDateOnDestination(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn) {
		try {
			DatabaseObjectDAO.refreshLastSyncDateOnDestination(this, tableConfiguration, recordOriginLocationCode, conn);
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void resolveInconsistence(TableConfiguration tableConfiguration, Connection conn)
	        throws InconsistentStateException, DBException {
		if (!tableConfiguration.isFullLoaded())
			tableConfiguration.fullLoad();
		
		//this.generateRelatedSyncInfo(tableConfiguration, tableConfiguration.getOriginAppLocationCode(), conn);
		
		Map<ParentTableImpl, Integer> missingParents = loadMissingParents(tableConfiguration, conn);
		
		int qtyInconsistence = missingParents.size();
		
		if (qtyInconsistence == 0) {
			getRelatedSyncInfo().setConsistent(EtlDatabaseObject.CONSISTENCE_STATUS);
		} else {
			boolean solvedCurrentInconsistency = true;
			
			for (Entry<ParentTableImpl, Integer> entry : missingParents.entrySet()) {
				//try to load the default parent
				
				if (entry.getKey().getSimpleRefMapping().isSetNullDueInconsistency()) {
					this.setParentToNull(entry.getKey());
					this.save(tableConfiguration, conn);
					
					qtyInconsistence--;
				} else if (entry.getKey().getSimpleRefMapping().getDefaultValueDueInconsistency() != null) {
					Oid oid = Oid.fastCreate(tableConfiguration.getPrimaryKey().retrieveSimpleKey().getNameAsClassAtt(),
					    entry.getKey().getSimpleRefMapping().getDefaultValueDueInconsistency());
					
					EtlDatabaseObject parent = DatabaseObjectDAO.getByOid(entry.getKey(), oid, conn);
					
					if (parent == null) {
						solvedCurrentInconsistency = false;
					} else {
						this.changeParentValue(entry.getKey(), parent);
						this.save(tableConfiguration, conn);
						
						qtyInconsistence--;
					}
				} else {
					solvedCurrentInconsistency = false;
				}
				
				saveInconsistence(tableConfiguration, entry, solvedCurrentInconsistency,
				    getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
			}
			
			if (qtyInconsistence == 0) {
				getRelatedSyncInfo().setConsistent(EtlDatabaseObject.CONSISTENCE_STATUS);
			} else {
				getRelatedSyncInfo().setLastSyncTryErr(generateMissingInfo(missingParents));
				this.remove(conn);
				resolveChildrenInconsistences(tableConfiguration, missingParents, conn);
			}
		}
		
		getRelatedSyncInfo().save(tableConfiguration, conn);
	}
	
	private void saveInconsistence(TableConfiguration tableConfiguration,
	        Entry<ParentTableImpl, Integer> inconsistenceInfoSource, boolean inconsistenceResoloved,
	        String recordOriginLocationCode, Connection conn) throws DBException {
		
		if (tableConfiguration.getPrimaryKey().isCompositeKey()) {
			throw new ForbiddenOperationException("The related table (" + tableConfiguration.getTableName()
			        + ") has composite pk. You cannot performe the request action!");
		}
		
		Integer defaultParent = (Integer) inconsistenceInfoSource.getKey().getSimpleRefMapping()
		        .getDefaultValueDueInconsistency();
		
		InconsistenceInfo info = InconsistenceInfo.generate(tableConfiguration.getTableName(), this.getObjectId(),
		    inconsistenceInfoSource.getKey().getChildTableConf().getTableName(), inconsistenceInfoSource.getValue(),
		    defaultParent, recordOriginLocationCode);
		info.save(tableConfiguration, conn);
	}
	
	public void resolveChildrenInconsistences(TableConfiguration syncTableInfo, Map<ParentTableImpl, Integer> missingParents,
	        Connection conn) throws DBException {
		
		if (syncTableInfo.getPrimaryKey().isCompositeKey()) {
			throw new ForbiddenOperationException("The related table (" + syncTableInfo.getTableName()
			        + ") has composite pk. You cannot performe the request action!");
		}
		
		if (!syncTableInfo.getRelatedEtlConf().isSourceSyncProcess())
			throw new EtlExceptionImpl("You cannot move dstRecord to stage area in a installation different to source") {
				
				private static final long serialVersionUID = 1L;
				
			};
		
		if ((syncTableInfo.isMetadata() || syncTableInfo.isRemoveForbidden()) && !syncTableInfo.isRemovableMetadata())
			throw new EtlExceptionImpl("This metadata metadata [" + syncTableInfo.getTableName() + " = " + this.getObjectId()
			        + ". is missing its some parents [" + generateMissingInfo(missingParents)
			        + "] You must resolve this inconsistence manual") {
				
				private static final long serialVersionUID = 1L;
			};
		
		for (ChildTable refInfo : syncTableInfo.getChildRefInfo()) {
			if (!refInfo.isConfigured())
				continue;
			
			Integer qtyChildren = DatabaseObjectDAO.countAllOfParentId(
			    refInfo.getSyncRecordClass(syncTableInfo.getSrcConnInfo()),
			    refInfo.getSimpleRefMapping().getChildField().getName(), this.getObjectId().getSimpleValueAsInt(), conn);
			
			if (qtyChildren == 0) {
				continue;
			}
			
			List<EtlDatabaseObject> children = DatabaseObjectDAO.getByParentId(refInfo,
			    refInfo.getSimpleRefMapping().getChildField().getName(), this.getObjectId().getSimpleValueAsInt(), conn);
			
			for (EtlDatabaseObject child : children) {
				child.resolveInconsistence(refInfo, conn);
			}
		}
	}
	
	@Override
	public void consolidateData(TableConfiguration tableConfiguration, Connection conn) throws DBException {
		utilities.throwReviewMethodException();
		/*
		if (!tableConfiguration.isFullLoaded())
			tableConfiguration.fullLoad();
		
		if (tableConfiguration.getPrimaryKey().isCompositeKey()) {
			throw new ForbiddenOperationException("The related table (" + tableConfiguration.getTableName()
			        + ") has composite pk. You cannot performe the request action!");
		}
		
		Map<RefInfo, Integer> missingParents = loadMissingParents(tableConfiguration, conn);
		
		int qtyInconsistence = missingParents.size();
		
		if (!missingParents.isEmpty()) {
			for (Entry<RefInfo, Integer> entry : missingParents.entrySet()) {
				boolean solvedCurrentInconsistency = true;
				
				//try to load the default parent
				if (entry.getKey().getDefaultValueDueInconsistency() != null) {
					
					Oid oid = Oid.fastCreate(tableConfiguration.getPrimaryKey().retrieveSimpleKey().getName(),
					    entry.getKey().getDefaultValueDueInconsistency());
					
					EtlDatabaseObject parent = DatabaseObjectDAO
					        .getByOid(entry.getKey().getRefObjectClass(tableConfiguration.getMainApp()), oid, conn);
					
					if (parent == null) {
						solvedCurrentInconsistency = false;
					} else {
						this.changeParentValue(entry.getKey().getRefColumnAsClassAttName(), parent);
						qtyInconsistence--;
					}
				} else {
					solvedCurrentInconsistency = false;
				}
				
				saveInconsistence(tableConfiguration, entry, solvedCurrentInconsistency,
				    getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
			}
		}
		
		if (qtyInconsistence == 0) {
			loadDestParentInfo(tableConfiguration, getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
			
			save(tableConfiguration, conn);
			
			this.getRelatedSyncInfo().markAsConsistent(tableConfiguration, conn);
		} else {
			removeDueInconsistency(tableConfiguration, missingParents, conn);
			getRelatedSyncInfo().markAsFailedToMigrate(tableConfiguration, generateMissingInfo(missingParents), conn);
		}*/
	}
	
	@Override
	public void loadDestParentInfo(TableConfiguration tableInfo, String recordOriginLocationCode, Connection conn)
	        throws ParentNotYetMigratedException, DBException {
		
		utilities.throwReviewMethodException();
		
		/*
		if (tableInfo.getPrimaryKey().isCompositeKey()) {
			throw new ForbiddenOperationException("The related table (" + tableInfo.getTableName()
			        + ") has composite pk. You cannot performe the request action!");
		}
		
		if (!tableInfo.getRelatedSyncConfiguration().isDataBaseMergeFromJSONProcess()
		        && !tableInfo.getRelatedSyncConfiguration().isDataReconciliationProcess())
			throw new ForbiddenOperationException("You can only load destination parent in a destination installation");
		
		if (!utilities.arrayHasElement(tableInfo.getParents()))
			return;
		
		for (RefInfo refInfo : tableInfo.getParentRefInfo()) {
			if (tableInfo.getSharePkWith() != null && tableInfo.getSharePkWith().equals(refInfo.getParentTableName())) {
				continue;
			}
			
			Integer parentId = getParentValue(refInfo.getSimpleRefMapping().getChildField().getNameAsClassAtt());
			
			if (parentId != null) {
				EtlDatabaseObject parent;
				
				if (refInfo.getParentTableCof().isMetadata()) {
					Oid oid = Oid.fastCreate(refInfo.getParentTableCof().getPrimaryKey().retrieveSimpleKey().getName(),
					    parentId);
					
					parent = DatabaseObjectDAO.getByOid(refInfo.getSimpleRefMapping().getParentField().getName(), oid, conn);
				} else {
					boolean ignorable = refInfo.getSimpleRefMapping().isIgnorable()
					        || refInfo.getSimpleRefMapping().getDefaultValueDueInconsistencyAsInt() > 0;
					
					parent = retrieveParentInDestination(parentId, recordOriginLocationCode,
					    refInfo.getSimpleRefMapping().getParentField().getName(), ignorable, conn);
				}
				
				if (parent == null) {
					//Try to recover the parent from stage_area and check if this dstRecord doesnt exist on destination with same uuid
					
					Oid oid = Oid.fastCreate(
					    refInfo.getRefTableConfiguration().getPrimaryKey().retrieveSimpleKey().getName(), parentId);
					
					EtlDatabaseObject parentFromSource = new GenericDatabaseObject(refInfo.getRefTableConfiguration());
					parentFromSource.setObjectId(oid);
					
					parentFromSource.setRelatedSyncInfo(
					    EtlStageRecordVO.generateFromSyncRecord(parentFromSource, recordOriginLocationCode, true));
					
					EtlStageRecordVO sourceInfo = SyncImportInfoDAO.retrieveFromOpenMRSObject(
					    refInfo.getRefTableConfiguration(), parentFromSource, recordOriginLocationCode, conn);
					
					parentFromSource = sourceInfo.convertToOpenMRSObject(refInfo.getRefTableConfiguration(), conn);
					
					EtlDatabaseObject parentFromDestionationSharingSameObjectId = DatabaseObjectDAO
					        .getByOid(refInfo.getRefObjectClass(tableInfo.getMainApp()), oid, conn);
					
					boolean sameUuid = true;
					
					sameUuid = sameUuid && parentFromDestionationSharingSameObjectId != null;
					sameUuid = sameUuid && parentFromDestionationSharingSameObjectId.getUuid() != null
					        && parentFromSource.getUuid() != null;
					sameUuid = sameUuid
					        && parentFromSource.getUuid().equals(parentFromDestionationSharingSameObjectId.getUuid());
					
					if (sameUuid) {
						parent = parentFromDestionationSharingSameObjectId;
					}
				}
				
				if (parent == null && refInfo.getDefaultValueDueInconsistency() > 0) {
					Oid oid = Oid.fastCreate(
					    refInfo.getRefTableConfiguration().getPrimaryKey().retrieveSimpleKey().getName(),
					    refInfo.getDefaultValueDueInconsistency());
					
					parent = DatabaseObjectDAO.getByOid(refInfo.getRefObjectClass(tableInfo.getMainApp()), oid, conn);
				}
				
				changeParentValue(refInfo.getRefColumnAsClassAttName(), parent);
			}
		}*/
	}
	
	@Override
	public EtlStageRecordVO retrieveRelatedSyncInfo(TableConfiguration tableInfo, String recordOriginLocationCode,
	        Connection conn) throws DBException {
		return SyncImportInfoDAO.retrieveFromOpenMRSObject(tableInfo, this, recordOriginLocationCode, conn);
	}
	
	public void removeDueInconsistency(TableConfiguration syncTableInfo, Map<ParentTableImpl, Integer> missingParents,
	        Connection conn) throws DBException {
		
		utilities.throwReviewMethodException();
		
		/*
		if (syncTableInfo.isMetadata() || syncTableInfo.isRemoveForbidden())
			throw new EtlExceptionImpl("This metadata [" + syncTableInfo.getTableName() + " = " + this.getObjectId()
			        + ". is missing its some parents [" + generateMissingInfo(missingParents)
			        + "] You must resolve this inconsistence manual") {
				
				private static final long serialVersionUID = 1L;
			};
		
		this.remove(conn);
		
		for (RefInfo refInfo : syncTableInfo.getChildred()) {
			if (!refInfo.getRefTableConfiguration().isConfigured())
				continue;
			
			int qtyChildren = DatabaseObjectDAO.countAllOfOriginParentId(refInfo.getRefColumnName(),
			    getRelatedSyncInfo().getRecordOriginId(), getRelatedSyncInfo().getRecordOriginLocationCode(),
			    refInfo.getRefTableConfiguration(), conn);
			
			if (qtyChildren == 0) {
				continue;
			} else {
				List<EtlDatabaseObject> children = DatabaseObjectDAO.getByOriginParentId(refInfo.getRefColumnName(),
				    getRelatedSyncInfo().getRecordOriginId(), getRelatedSyncInfo().getRecordOriginLocationCode(),
				    refInfo.getRefTableConfiguration(), conn);
				
				for (EtlDatabaseObject child : children) {
					child.consolidateData(refInfo.getRefTableConfiguration(), conn);
				}
			}
		}*/
	}
	
	public void remove(Connection conn) throws DBException {
		DatabaseObjectDAO.remove(this, conn);
	}
	
	public Map<ParentTableImpl, Integer> loadMissingParents(TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		
		utilities.throwReviewMethodException();
		
		return null;
		
		/*
		Map<RefInfo, Integer> missingParents = new HashMap<RefInfo, Integer>();
		 
		if (!utilities.arrayHasElement(tableInfo.getParents()))
			return missingParents;
		
		for (RefInfo refInfo : tableInfo.getParents()) {
			Integer parentId = null;
			
			try {
				parentId = getParentValue(refInfo.getRefColumnAsClassAttName());
			}
			catch (Exception e2) {
				e2.printStackTrace();
			}
			
			try {
				if (parentId != null) {
					EtlDatabaseObject parent;
					Oid oid = Oid.fastCreate(
					    refInfo.getRefTableConfiguration().getPrimaryKey().retrieveSimpleKey().getName(), parentId);
					
					if (refInfo.getRefTableConfiguration().isMetadata()) {
						parent = DatabaseObjectDAO.getByOid(refInfo.getRefTableConfiguration().getTableName(), oid, conn);
					} else {
						
						if (tableInfo.getRelatedSyncConfiguration().isDataBaseMergeFromJSONProcess()) {
							parent = retrieveParentInDestination(parentId,
							    this.getRelatedSyncInfo().getRecordOriginLocationCode(), refInfo.getRefTableConfiguration(),
							    refInfo.isIgnorable() || refInfo.getDefaultValueDueInconsistency() > 0, conn);
							
							if (parent == null) {
								//Try to recover the parent from stage_area and check if this dstRecord doesnt exist on destination with same uuid
								
								EtlDatabaseObject parentFromSource = new GenericDatabaseObject(
								        refInfo.getRefTableConfiguration());
								parentFromSource.setObjectId(oid);
								
								parentFromSource.setRelatedSyncInfo(EtlStageRecordVO.generateFromSyncRecord(parentFromSource,
								    getRelatedSyncInfo().getRecordOriginLocationCode(), true));
								
								EtlStageRecordVO sourceInfo = SyncImportInfoDAO.retrieveFromOpenMRSObject(
								    refInfo.getRefTableConfiguration(), parentFromSource,
								    getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
								
								parentFromSource = sourceInfo.convertToOpenMRSObject(refInfo.getRefTableConfiguration(),
								    conn);
								
								EtlDatabaseObject parentFromDestionationSharingSameObjectId = DatabaseObjectDAO
								        .getByOid(refInfo.getRefObjectClass(tableInfo.getMainApp()), oid, conn);
								
								boolean sameUuid = true;
								
								sameUuid = sameUuid && parentFromDestionationSharingSameObjectId != null;
								sameUuid = sameUuid && parentFromDestionationSharingSameObjectId.getUuid() != null
								        && parentFromSource.getUuid() != null;
								sameUuid = sameUuid && parentFromSource.getUuid()
								        .equals(parentFromDestionationSharingSameObjectId.getUuid());
								
								if (sameUuid) {
									parent = parentFromDestionationSharingSameObjectId;
								}
							}
						} else {
							parent = DatabaseObjectDAO.getByOid(refInfo.getRefObjectClass(tableInfo.getMainApp()), oid,
							    conn);
						}
					}
					
					if (parent == null) {
						missingParents.put(refInfo, parentId);
					}
				}
				
			}
			catch (ParentNotYetMigratedException e) {
				EtlDatabaseObject parent = utilities.createInstance(refInfo.getRefObjectClass(tableInfo.getMainApp()));
				parent.setRelatedSyncInfo(EtlStageRecordVO.generateFromSyncRecord(parent,
				    getRelatedSyncInfo().getRecordOriginLocationCode(), true));
				
				try {
					SyncImportInfoDAO.retrieveFromOpenMRSObject(refInfo.getRefTableConfiguration(), parent,
					    getRelatedSyncInfo().getRecordOriginLocationCode(), conn);
				}
				catch (DBException e1) {
					e1.printStackTrace();
				}
				catch (ForbiddenOperationException e1) {
					throw new ForbiddenOperationException("The parent '" + refInfo.getRefTableConfiguration().getTableName()
					        + " = " + parentId + "' from '" + this.getRelatedSyncInfo().getRecordOriginLocationCode()
					        + "' was not found in the main database nor in the stagging area. You must resolve this inconsistence manual!!!!!!");
				}
				
				missingParents.put(refInfo, parentId);
			}
		}
		
		return missingParents;*/
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (!obj.getClass().equals(this.getClass()))
			return false;
		
		AbstractDatabaseObject objAsOpenMrs = (AbstractDatabaseObject) obj;
		
		if (this.getObjectId().equals(objAsOpenMrs.getObjectId()))
			return this.getObjectId() == objAsOpenMrs.getObjectId();
		
		if (utilities.stringHasValue(this.getUuid()) && utilities.stringHasValue(objAsOpenMrs.getUuid())) {
			return this.getUuid().equals(objAsOpenMrs.getUuid());
		}
		
		if (this.hasUniqueKeys()) {
			for (UniqueKeyInfo key : this.getUniqueKeysInfo()) {
				
				UniqueKeyInfo otherKey = objAsOpenMrs.getUniqueKeyInfo(key);
				
				if (otherKey != null && key.hasSameValues(otherKey)) {
					return true;
				}
				
			}
		}
		
		return super.equals(obj);
	}
	
	public String generateMissingInfo(Map<ParentTableImpl, Integer> missingParents) {
		String missingInfo = "";
		
		for (Entry<ParentTableImpl, Integer> missing : missingParents.entrySet()) {
			missingInfo = utilities.concatStringsWithSeparator(missingInfo,
			    "[" + missing.getKey().getTableName() + ": " + missing.getValue() + "]", ";");
		}
		
		return "The dstRecord [" + this.generateTableName() + " = " + this.getObjectId()
		        + "] is in inconsistent state. There are missing these parents: " + missingInfo;
	}
	
	public String generateMissingInfoForSolvedInconsistency(Map<ParentTableImpl, Integer> missingParents) {
		String missingInfo = "";
		
		for (Entry<ParentTableImpl, Integer> missing : missingParents.entrySet()) {
			missingInfo = utilities.concatStringsWithSeparator(missingInfo,
			    "[" + missing.getKey().getTableName() + ": " + missing.getValue() + "]", ";");
		}
		
		return "The dstRecord [" + this.generateTableName() + " = " + this.getObjectId()
		        + "] is was in inconsistent state solved using some default parents.  These are missing parents: "
		        + missingInfo;
	}
	
	@SuppressWarnings("unchecked")
	public Class<EtlDatabaseObject> tryToGetExistingCLass(File targetDirectory, String fullClassName) {
		try {
			URLClassLoader loader = URLClassLoader.newInstance(new URL[] { targetDirectory.toURI().toURL() });
			
			Class<EtlDatabaseObject> c = (Class<EtlDatabaseObject>) loader.loadClass(fullClassName);
			
			loader.close();
			
			return c;
		}
		catch (ClassNotFoundException e) {
			return null;
		}
		catch (IOException e) {
			e.printStackTrace();
			
			return null;
		}
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		
		String objectId = "objectId = " + (this.getObjectId() != null ? this.getObjectId() : "");
		
		String ukeys = "";
		
		if (utilities.arrayHasElement(getUniqueKeysInfo())) {
			int i = 0;
			
			for (UniqueKeyInfo uk : getUniqueKeysInfo()) {
				uk.loadValuesToFields(this);
				
				if (i > 0)
					ukeys += ", ";
				
				ukeys += uk.toString();
				
				i++;
			}
		}
		
		return "[" + utilities.concatStringsWithSeparator(objectId, ukeys, ",") + "]";
	}
	
	@Override
	public void fastCreateSimpleNumericKey(long i) {
		Oid oid = new Oid();
		
		Key k = new Key();
		k.setValue(i);
		
		oid.addKey(k);
	}
	
	@Override
	public void setParentToNull(ParentTableImpl refInfo) {
		for (RefMapping map : refInfo.getRefMapping()) {
			setFieldValue(map.getChildFieldNameAsAttClass(), null);
		}
	}
	
	@Override
	public void changeParentValue(ParentTable refInfo, EtlDatabaseObject newParent) {
		for (RefMapping map : refInfo.getRefMapping()) {
			Object parentValue = newParent.getFieldValue(map.getChildFieldNameAsAttClass());
			this.setFieldValue(map.getChildFieldNameAsAttClass(), parentValue);
		}
	}
}
