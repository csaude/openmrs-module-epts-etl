package org.openmrs.module.epts.etl.model;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;
import org.openmrs.module.epts.etl.conf.interfaces.MainJoiningEntity;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.ConflictResolutionType;
import org.openmrs.module.epts.etl.exceptions.ConflictWithRecordNotYetAvaliableException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;

/**
 * This interface represent any Database Entity class subject of synchronization records.
 * 
 * @author jpboane
 */
public interface EtlDatabaseObject extends EtlObject {
	
	public static final int CONSISTENCE_STATUS = 1;
	
	public static final int INCONSISTENCE_STATUS = -1;
	
	ConflictResolutionType getConflictResolutionType();
	
	void setConflictResolutionType(ConflictResolutionType conflictResolutionType);
	
	void refreshLastSyncDateOnOrigin(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn);
	
	void refreshLastSyncDateOnDestination(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn);
	
	EtlDatabaseObject getSrcRelatedObject();
	
	void setSrcRelatedObject(EtlDatabaseObject srcRelatedObject);
	
	Oid getObjectId();
	
	void setObjectId(Oid objectId);
	
	List<EtlDatabaseObjectUniqueKeyInfo> getUniqueKeysInfo();
	
	void setUniqueKeysInfo(List<EtlDatabaseObjectUniqueKeyInfo> uniqueKeysInfo);
	
	/**
	 * If the {@link #getRelatedConfiguration()} is instance of {@link MainJoiningEntity} then the
	 * objects related to tables presents on {@link MainJoiningEntity#getJoiningTable()} will be
	 * placed on this field.
	 */
	List<? extends EtlDatabaseObject> getAuxLoadObject();
	
	/**
	 * Load the destination parents id to this object
	 * 
	 * @param conn
	 * @throws DBException
	 */
	void loadDestParentInfo(TableConfiguration tableInfo, String recordOriginLocationCode, Connection conn)
	        throws ParentNotYetMigratedException, DBException;
	
	Object[] getInsertParamsWithoutObjectId();
	
	String getInsertSQLWithoutObjectId();
	
	Object[] getInsertParamsWithObjectId();
	
	String getInsertSQLWithObjectId();
	
	String getUpdateSQL();
	
	Object[] getUpdateParams();
	
	String generateInsertValuesWithoutObjectId();
	
	String generateInsertValuesWithObjectId();
	
	void setInsertSQLQuestionMarksWithObjectId(String insertQuestionMarks);
	
	String getInsertSQLQuestionMarksWithObjectId();
	
	void setInsertSQLQuestionMarksWithoutObjectId(String insertQuestionMarks);
	
	String getInsertSQLQuestionMarksWithoutObjectId();
	
	boolean hasIgnoredParent();
	
	void save(TableConfiguration syncTableInfo, ConflictResolutionType onConflict, Connection conn) throws DBException;
	
	void save(TableConfiguration syncTableInfo, Connection conn) throws DBException;
	
	void update(TableConfiguration syncTableInfo, Connection conn) throws DBException;
	
	String getUuid();
	
	void setUuid(String uuid);
	
	boolean hasParents();
	
	Object getParentValue(ParentTable refInfo);
	
	String generateTableName();
	
	String generateFullFilledUpdateSql();
	
	EtlDatabaseObject getSharedPkObj();
	
	void setSharedPkObj(EtlDatabaseObject sharedPkObj);
	
	/**
	 * Consolidate data for database consistency
	 * <p>
	 * The consolidation consist on re-arranging foreign keys between records from different tables
	 * <p>
	 * Because the consolidation process would be in cascade mode, each consolidation is imediatily
	 * commited to t@Override he dadabase
	 * 
	 * @param tableInfo
	 * @param conn
	 * @throws InconsistentStateException
	 * @throws DBException
	 */
	void consolidateData(TableConfiguration tableInfo, Connection conn) throws InconsistentStateException, DBException;
	
	/**
	 * Resolve the inconsistency of a dstRecord
	 * <p>
	 * The resolution consists on checking if all parent of this records are present in data base.
	 * If not then this dstRecord will be moved to staging area and all its children will be moved
	 * in cascade
	 * 
	 * @param tableInfo
	 * @param conn
	 * @throws InconsistentStateException
	 * @throws DBException
	 */
	void resolveInconsistence(TableConfiguration tableInfo, Connection conn) throws InconsistentStateException, DBException;
	
	EtlStageRecordVO retrieveRelatedSyncInfo(TableConfiguration tableInfo, String recordOriginLocationCode, Connection conn)
	        throws DBException;
	
	EtlDatabaseObject retrieveParentInDestination(Integer parentId, String recordOriginLocationCode,
	        TableConfiguration parentTableConfiguration, boolean ignorable, Connection conn)
	        throws ParentNotYetMigratedException, DBException;
	
	EtlStageRecordVO getRelatedSyncInfo();
	
	void setRelatedSyncInfo(EtlStageRecordVO relatedSyncInfo);
	
	String generateMissingInfo(Map<ParentTableImpl, Integer> missingParents);
	
	void remove(Connection conn) throws DBException;
	
	Map<ParentTableImpl, Integer> loadMissingParents(TableConfiguration tableInfo, Connection conn) throws DBException;
	
	void removeDueInconsistency(TableConfiguration syncTableInfo, Map<ParentTableImpl, Integer> missingParents,
	        Connection conn) throws DBException;
	
	void changeParentValue(ParentTable refInfo, EtlDatabaseObject newParent);
	
	void setParentToNull(ParentTableImpl refInfo);
	
	void changeObjectId(TableConfiguration abstractTableConfiguration, Connection conn) throws DBException;
	
	void changeParentForAllChildren(EtlDatabaseObject newParent, TableConfiguration syncTableInfo, Connection conn)
	        throws DBException;
	
	Date getDateChanged();
	
	Date getDateVoided();
	
	Date getDateCreated();
	
	/**
	 * Check if this dstRecord has exactily the same values in all fields with a given object
	 * 
	 * @param srcObj
	 * @return true if this dstRecord has exactily the same values in all fields with the given
	 *         object
	 */
	boolean hasExactilyTheSameDataWith(EtlDatabaseObject srcObj);
	
	void fastCreateSimpleNumericKey(long i);
	
	void loadWithDefaultValues(Connection conn) throws DBException;
	
	/**
	 * Checks if there are recursive relashioship between the {@link #getRelatedConfiguration()} and
	 * the one passed by parameter
	 * 
	 * @param otherTabConf the related configuration against what the recursive relationship will be
	 *            checked
	 * @param conn
	 * @return true if the recursive relationship can be resolved
	 * @throws ForbiddenOperationException
	 * @throws DBException
	 */
	default boolean checkIfAllRelationshipCanBeresolved(TableConfiguration otherTabConf, Connection conn)
	        throws DBException, ForbiddenOperationException {
		
		if (otherTabConf.getDefaultObject(conn) != null) {
			return true;
		}
		
		if (getRelatedConfiguration() == null) {
			throw new ForbiddenOperationException("The related table configuration is not set");
		}
		
		if (!(getRelatedConfiguration() instanceof TableConfiguration)) {
			throw new ForbiddenOperationException("The related configuration should be type of TableConfiguration");
		}
		
		TableConfiguration thisTabConf = (TableConfiguration) getRelatedConfiguration();
		
		if (!thisTabConf.isFullLoaded()) {
			thisTabConf.fullLoad(conn);
		}
		
		if (utils.arrayHasElement(thisTabConf.getParentRefInfo())) {
			for (ParentTable ref : thisTabConf.getParentRefInfo()) {
				//Recursive relashionship
				if (ref.getTableName().equals(otherTabConf.getTableName())) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	default EtlDatabaseObject findOnDB(TableConfiguration tabConf, Connection conn)
	        throws DBException, ForbiddenOperationException {
		Oid pk = this.getObjectId();
		
		pk.setTabConf(tabConf);
		
		String sql = tabConf.generateSelectFromQuery();
		
		sql += " WHERE " + pk.parseToParametrizedStringConditionWithAlias();
		
		return DatabaseObjectDAO.find(tabConf.getLoadHealper(), tabConf.getSyncRecordClass(), sql, pk.parseValuesToArray(),
		    conn);
	}
	
	/**
	 * Load the objectId info
	 * 
	 * @param tabConf the table configuration
	 */
	default void loadObjectIdData(TableConfiguration tabConf) {
		if (tabConf.getPrimaryKey() != null) {
			
			tabConf.getPrimaryKey().setTabConf(tabConf);
			
			this.setObjectId(tabConf.getPrimaryKey().generateOid(this));
			
			this.getObjectId().setFullLoaded(true);
		}
	}
	
	default void loadObjectIdData() throws ForbiddenOperationException {
		TableConfiguration tabConf = (TableConfiguration) getRelatedConfiguration();
		
		if (tabConf == null)
			throw new ForbiddenOperationException("The related tabConf is not specified!");
		
		loadObjectIdData(tabConf);
	}
	
	/**
	 * Check if all fields from a given parent has value or not
	 * 
	 * @param refInfo the parent info to check
	 * @return true if all the field from a given parent are not empty or false in there is at least
	 *         one empty field
	 */
	default boolean hasAllPerentFieldsFilled(ParentTable refInfo) {
		
		for (RefMapping map : refInfo.getRefMapping()) {
			if (this.getFieldValue(map.getChildFieldName()) == null) {
				return false;
			}
		}
		
		return true;
	}
	
	default boolean hasAuxLoadObject() {
		return utils.arrayHasElement(this.getAuxLoadObject());
	}
	
	/**
	 * Check this dstRecord has at least one uniqueKey with all fields filled
	 * 
	 * @param refInfo the parent info to check
	 * @return true if has at least one uniqueKey with all fields filled or false in all the
	 *         unikeKeys has at least one empty field
	 */
	default boolean hasAtLeastOnUniqueKeyWIthAllFieldsFilled() {
		if (!hasRelatedConfiguration())
			throw new ForbiddenOperationException("The related configuration is not defined!");
		
		TableConfiguration tabConf = (TableConfiguration) getRelatedConfiguration();
		
		if (!tabConf.hasUniqueKeys())
			throw new ForbiddenOperationException(
			        "The related configuration " + tabConf.getTableName() + " has no uniqueKey");
		
		for (UniqueKeyInfo uk : tabConf.getUniqueKeys()) {
			boolean allFiled = true;
			
			for (Key key : uk.getFields()) {
				if (this.getFieldValue(key.getName()) == null) {
					allFiled = false;
					
					break;
				}
			}
			
			if (allFiled)
				return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	static <T extends EtlDatabaseObject> List<T> collectAllSrcRelatedOBjects(List<EtlDatabaseObject> objs) {
		List<T> list = new ArrayList<>(objs.size());
		
		for (EtlDatabaseObject o : objs) {
			
			if (!o.hasSrcRelatedObject())
				throw new ForbiddenOperationException("The object " + o + " has no srcRelatedObject");
			
			list.add((T) o.getSrcRelatedObject());
		}
		
		return list;
	}
	
	default boolean hasSrcRelatedObject() {
		return this.getSrcRelatedObject() != null;
	}
	
	void copyFrom(EtlDatabaseObject parentRecordInOrigin);
	
	/**
	 * Retrive values for all {@link TableConfiguration#getUniqueKeys()} fields. The values follow
	 * the very same sequence defined with {@link TableConfiguration#getUniqueKeys()}
	 * 
	 * @param tableConfiguration the {@link TableConfiguration} from where the
	 *            {@link TableConfiguration#getUniqueKeys()} will be retrieved from
	 * @return values for all {@link TableConfiguration#getUniqueKeys()} field.
	 * @throws ForbiddenOperationException if one or more fields in any key have null value
	 */
	default Object[] getUniqueKeysFieldValues(TableConfiguration tableConfiguration) throws ForbiddenOperationException {
		if (!tableConfiguration.isFullLoaded()) {
			try {
				tableConfiguration.fullLoad();
			}
			catch (DBException e) {
				throw new EtlExceptionImpl(e) {
					
					private static final long serialVersionUID = 6237531946353999983L;
				};
			}
		}
		
		List<Object> values = new ArrayList<Object>();
		
		for (UniqueKeyInfo uniqueKey : tableConfiguration.getUniqueKeys()) {
			Object[] fieldValues = new Object[uniqueKey.getFields().size()];
			
			for (int i = 0; i < uniqueKey.getFields().size(); i++) {
				Field f = uniqueKey.getFields().get(i);
				
				Object fv = null;
				
				try {
					fv = this.getFieldValue(f.getName());
				}
				catch (ForbiddenOperationException e) {
					fv = this.getFieldValue(f.getNameAsClassAtt());
				}
				
				if (fv == null) {
					throw new ForbiddenOperationException("On or more fields of key [" + uniqueKey + "] has no value.");
				}
				
				fieldValues[i] = fv;
			}
			
			values.addAll(utils.parseArrayToList(fieldValues));
		}
		
		return utils.parseListToArray(values);
	}
	
	default boolean shasSharedPkObj() {
		return getSharedPkObj() != null;
	}
	
	default void generateFields() {
		List<Field> fields = new ArrayList<Field>();
		Class<?> cl = getClass();
		
		while (cl != null) {
			java.lang.reflect.Field[] in = cl.getDeclaredFields();
			for (int i = 0; i < in.length; i++) {
				java.lang.reflect.Field field = in[i];
				if (Modifier.isStatic(field.getModifiers()))
					continue;
				
				field.setAccessible(true);
				fields.add(Field.fastCreateWithType(field.getName(), field.getType().getTypeName()));
			}
			cl = cl.getSuperclass();
		}
		
		setFields(fields);
	}
	
	default UniqueKeyInfo getUniqueKeyInfo(UniqueKeyInfo keyToFind) {
		if (hasUniqueKeys()) {
			for (UniqueKeyInfo key : this.getUniqueKeysInfo()) {
				if (keyToFind.equals(key)) {
					return key;
				}
			}
		}
		
		return null;
	}
	
	default boolean hasUniqueKeys() {
		return utils.arrayHasElement(getUniqueKeysInfo());
	}
	
	default void setRelatedConfiguration(DatabaseObjectConfiguration config) {
	}
	
	default DatabaseObjectConfiguration getRelatedConfiguration() {
		return null;
	}
	
	default boolean hasRelatedConfiguration() {
		return getRelatedConfiguration() != null;
	}
	
	/**
	 * Retrive values for all fields in any unique key.
	 * 
	 * @param uniqueKeyFields the list of fields in a unique key
	 * @return values for all fields in a unique key.
	 * @throws ForbiddenOperationException if one or more fields in any key have null value
	 */
	default Object[] getUniqueKeysFieldValues(UniqueKeyInfo uniqueKey) throws ForbiddenOperationException {
		Object[] fieldValues = new Object[uniqueKey.getFields().size()];
		
		for (int i = 0; i < uniqueKey.getFields().size(); i++) {
			Field f = uniqueKey.getFields().get(i);
			
			Object fv = null;
			
			try {
				fv = this.getFieldValue(f.getName());
			}
			catch (ForbiddenOperationException e) {
				fv = this.getFieldValue(f.getNameAsClassAtt());
			}
			
			if (fv == null) {
				throw new ForbiddenOperationException("On or more fields of key [" + uniqueKey + "] has no value.");
			}
			
			fieldValues[i] = fv;
		}
		
		return fieldValues;
	}
	
	default void loadUniqueKeyValues(TableConfiguration tabConf) {
		this.setUniqueKeysInfo(EtlDatabaseObjectUniqueKeyInfo.generate(tabConf, this));
	}
	
	default void loadUniqueKeyValues() {
		this.loadUniqueKeyValues((TableConfiguration) getRelatedConfiguration());
	}
	
	default void loadSharedKeyParentRelatedObject(Connection conn) throws DBException {
		TableConfiguration tabConf = (TableConfiguration) this.getRelatedConfiguration();
		
		if (!tabConf.useSharedPKKey())
			throw new ForbiddenOperationException(
			        "The table '" + tabConf.getFullTableDescription() + " does not use shared key");
		
		ParentTable sharedKeyConf = tabConf.getSharedKeyRefInfo();
		
		Oid key = sharedKeyConf.generateParentOidFromChild(this);
		
		EtlDatabaseObject obj = DatabaseObjectDAO.getByOid(sharedKeyConf, key, conn);
		
		this.setSharedPkObj(obj);
	}
	
	default EtlDatabaseObject getSharedKeyChildRelatedObject(TableConfiguration childTabConf, Connection conn)
	        throws DBException {
		TableConfiguration tabConf = (TableConfiguration) this.getRelatedConfiguration();
		
		if (!childTabConf.useSharedPKKey())
			throw new ForbiddenOperationException(
			        "The table '" + childTabConf.getFullTableDescription() + " does not use shared key");
		
		if (!childTabConf.getSharedKeyRefInfo().equals(tabConf)) {
			throw new ForbiddenOperationException("The table '" + childTabConf.getFullTableDescription()
			        + " does not share primary key with " + tabConf.getFullTableDescription());
		}
		
		Oid key = childTabConf.getSharedKeyRefInfo().generateChildOidFromParent(this);
		
		EtlDatabaseObject child = DatabaseObjectDAO.getByOid(childTabConf, key, conn);
		
		if (child != null) {
			child.setSharedPkObj(this);
		}
		
		return child;
		
		/*
		if (sharedKeyChildInOrigin != null) {
			EtlDatabaseObject sharedKeyChild = childTabConf.createRecordInstance();
			sharedKeyChild.setRelatedConfiguration(childTabConf);
			sharedKeyChild.copyFrom(sharedKeyChildInOrigin);
			sharedKeyChild.loadUniqueKeyValues(childTabConf);
			sharedKeyChild.loadObjectIdData(childTabConf);
			
			return DatabaseObjectDAO.getByOid(childTabConf, sharedKeyChild.getObjectId(), conn);
		}
		
		return null;*/
		
	}
	
	default EtlDatabaseObject retrieveParentInSrcUsingDstParentInfo(ParentTable refInfo, SrcConf src, Connection srcConn)
	        throws DBException {
		
		Oid prentOid = refInfo.generateParentOidFromChild(this);
		
		TableConfiguration tabConfInSrc = src.findFullConfiguredConfInAllRelatedTable(
		    refInfo.generateFullTableNameOnSchema(src.getSchema()), new ArrayList<>());
		
		if (tabConfInSrc == null) {
			tabConfInSrc = new GenericTableConfiguration(refInfo.getTableName(), src);
			tabConfInSrc.setRelatedEtlConfig(src.getRelatedEtlConf());
			
			tabConfInSrc.fullLoad(srcConn);
		}
		
		return DatabaseObjectDAO.getByOid(tabConfInSrc, prentOid, srcConn);
	}
	
	default void delete(Connection conn) throws DBException {
		DatabaseObjectDAO.remove(this, conn);
	}
	
	default EtlDatabaseObject findDstDb(Connection dstConn) throws DBException {
		return DatabaseObjectDAO.getByUniqueKeys(this, dstConn);
	}
	
	default boolean checkIfExistsOnDstDb(Connection dstConn) throws DBException {
		return findDstDb(dstConn) != null;
	}
	
	default boolean checkIfExistsOnSrc(Connection srcConn) throws DBException {
		EtlDatabaseObject recOnDb = findOnDB((TableConfiguration) this.getRelatedConfiguration(), srcConn);
		
		return recOnDb != null;
	}
	
	default EtlDatabaseObject retrieveParentByOid(ParentTable refInfo, Connection conn) throws DBException {
		return retrieveParentByOid(refInfo, refInfo.generateParentOidFromChild(this), conn);
	}
	
	default EtlDatabaseObject retrieveParentByOid(ParentTable refInfo, Oid parentId, Connection conn) throws DBException {
		return DatabaseObjectDAO.getByOid(refInfo, parentId, conn);
	}
	
	default EtlDatabaseObject retrieveParentInDestination(ParentTable refInfo, EtlDatabaseObject parentInOrigin,
	        Connection dstConn) throws DBException {
		
		EtlDatabaseObject recInDst = refInfo.createRecordInstance();
		recInDst.setRelatedConfiguration(refInfo);
		recInDst.copyFrom(parentInOrigin);
		recInDst.loadUniqueKeyValues(refInfo);
		recInDst.loadObjectIdData(refInfo);
		
		return DatabaseObjectDAO.getByUniqueKeys(recInDst, dstConn);
	}
	
	default boolean hasResolvedConflict() {
		return this.getConflictResolutionType() != null;
	}
	
	default void resolveConflictWithExistingRecord(TableConfiguration tableConfiguration, DBException exception,
	        Connection conn) throws DBException, ForbiddenOperationException {
		
		EtlDatabaseObject recordOnDB = null;
		
		if (tableConfiguration.getRelatedEtlConf().isDoNotTransformsPrimaryKeys()) {
			recordOnDB = DatabaseObjectDAO.getByOid(tableConfiguration, this.getObjectId(), conn);
		}
		
		if (recordOnDB == null) {
			TableConfiguration refInfo = (TableConfiguration) getRelatedConfiguration();
			
			if (refInfo.useSharedPKKey() && !refInfo.hasItsOwnKeys()) {
				//Ignore duplication if it uses shared key
				//TODO resolve conflict
				return;
			}
			
			recordOnDB = DatabaseObjectDAO.getByUniqueKeys(this, conn);
		}
		
		boolean existingRecordIsOutdated = false;
		
		//Quickly abort the conflict resolution if the resolution type is ConflictResolutionType.KEEP_EXISTING
		if (tableConfiguration.onConflict().keepExisting()) {
			
			if (recordOnDB != null) {
				this.setObjectId(recordOnDB.getObjectId());
			}
			
			//Nothing to do
		} else {
			if (recordOnDB == null) {
				throw new ConflictWithRecordNotYetAvaliableException(this, exception);
			} else if (tableConfiguration.onConflict().updateExisting()) {
				existingRecordIsOutdated = true;
			} else if (utils.arrayHasElement(tableConfiguration.getWinningRecordFieldsInfo())) {
				for (List<org.openmrs.module.epts.etl.model.Field> fields : tableConfiguration
				        .getWinningRecordFieldsInfo()) {
					
					//Start assuming that this dstRecord is updated
					boolean thisRecordIsUpdated = true;
					
					for (org.openmrs.module.epts.etl.model.Field field : fields) {
						Object thisRecordFieldValue;
						
						try {
							thisRecordFieldValue = this.getFieldValue(field.getName());
						}
						catch (ForbiddenOperationException e) {
							thisRecordFieldValue = this.getFieldValue(field.getNameAsClassAtt());
						}
						
						//If at least one of field value is different from the winning value, assume that this dstRecord is not updated
						if (!thisRecordFieldValue.toString().equals(field.getValue().toString())) {
							thisRecordIsUpdated = false;
							
							//Check the next list of fields
							break;
						}
					}
					
					if (thisRecordIsUpdated) {
						existingRecordIsOutdated = true;
						
						break;
					}
				}
			} else if (tableConfiguration.hasObservationDateFields()) {
				for (String dateField : tableConfiguration.getObservationDateFields()) {
					
					Date thisRecordDate;
					Date recordOnDBDate;
					
					try {
						thisRecordDate = (Date) this.getFieldValue(dateField);
					}
					catch (ForbiddenOperationException e) {
						thisRecordDate = (Date) this
						        .getFieldValue(AttDefinedElements.convertTableAttNameToClassAttName(dateField));
					}
					
					try {
						recordOnDBDate = (Date) recordOnDB.getFieldValue(dateField);
					}
					catch (NullPointerException e) {
						recordOnDBDate = null;
					}
					catch (ForbiddenOperationException e) {
						recordOnDBDate = (Date) recordOnDB
						        .getFieldValue(AttDefinedElements.convertTableAttNameToClassAttName(dateField));
					}
					
					if (thisRecordDate != null) {
						if (recordOnDBDate == null) {
							existingRecordIsOutdated = true;
							
							break;
						} else if (DateAndTimeUtilities.dateDiff(thisRecordDate, recordOnDBDate) > 0) {
							existingRecordIsOutdated = true;
							
							break;
						}
					}
				}
			}
			
			if (existingRecordIsOutdated) {
				this.setConflictResolutionType(ConflictResolutionType.UPDATED_EXISTING);
				
				this.setObjectId(recordOnDB.getObjectId());
				this.update(tableConfiguration, conn);
			} else {
				this.setConflictResolutionType(ConflictResolutionType.KEPT_EXISTING);
				
				this.setObjectId(recordOnDB.getObjectId());
			}
		}
	}
	
	default Field getField(String fieldName) {
		for (Field field : this.getFields()) {
			if (field.getName().equals(fieldName)) {
				return field;
			}
		}
		
		throw new ForbiddenOperationException("The field '" + fieldName + "' cannot be found on object " + this);
	}
	
	default Class<?> getFieldType(String fieldName) {
		
		if (this.hasFields()) {
			Field field = this.getField(fieldName);
			
			return field.getTypeClass();
		}
		
		return utils.getFieldType(this, fieldName);
	}
	
	default boolean hasFields() {
		return utils.arrayHasElement(this.getFields());
	}
	
	/**
	 * If this {@link EtlDatabaseObject} uses the dynamic fields based on map or list of
	 * {@link Field}, then this method should replace the correspondent field with the given key
	 * 
	 * @param key the correspondent key to replace the field
	 */
	void tryToReplaceFieldWithKey(Key k);
}
