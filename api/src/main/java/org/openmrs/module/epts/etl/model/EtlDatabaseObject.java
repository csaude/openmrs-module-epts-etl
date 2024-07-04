package org.openmrs.module.epts.etl.model;

import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.RefMapping;
import org.openmrs.module.epts.etl.conf.SrcConf;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;

/**
 * This interface represent any Database Entintiy class subject of synchronization records.
 * 
 * @author jpboane
 */
public interface EtlDatabaseObject extends EtlObject {
	
	public static final int CONSISTENCE_STATUS = 1;
	
	public static final int INCONSISTENCE_STATUS = -1;
	
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
	
	void refreshLastSyncDateOnOrigin(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn);
	
	void refreshLastSyncDateOnDestination(TableConfiguration tableConfiguration, String recordOriginLocationCode,
	        Connection conn);
	
	EtlDatabaseObject getSrcRelatedObject();
	
	void setSrcRelatedObject(EtlDatabaseObject srcRelatedObject);
	
	Oid getObjectId();
	
	void setObjectId(Oid objectId);
	
	List<UniqueKeyInfo> getUniqueKeysInfo();
	
	void setUniqueKeysInfo(List<UniqueKeyInfo> uniqueKeysInfo);
	
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
	
	void save(TableConfiguration syncTableInfo, Connection conn) throws DBException;
	
	void update(TableConfiguration syncTableInfo, Connection conn) throws DBException;
	
	String getUuid();
	
	void setUuid(String uuid);
	
	boolean hasParents();
	
	Object getParentValue(ParentTable refInfo);
	
	String generateTableName();
	
	String generateFullFilledUpdateSql();
	
	/**
	 * Load the objectId info
	 * 
	 * @param tabConf the table configuration
	 */
	void loadObjectIdData(TableConfiguration tabConf);
	
	EtlDatabaseObject getSharedPkObj();
	
	default boolean shasSharedPkObj() {
		return getSharedPkObj() != null;
	}
	
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
	
	SyncImportInfoVO retrieveRelatedSyncInfo(TableConfiguration tableInfo, String recordOriginLocationCode, Connection conn)
	        throws DBException;
	
	EtlDatabaseObject retrieveParentInDestination(Integer parentId, String recordOriginLocationCode,
	        TableConfiguration parentTableConfiguration, boolean ignorable, Connection conn)
	        throws ParentNotYetMigratedException, DBException;
	
	SyncImportInfoVO getRelatedSyncInfo();
	
	void setRelatedSyncInfo(SyncImportInfoVO relatedSyncInfo);
	
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
	
	default void loadUniqueKeyValues(TableConfiguration tabConf) {
		this.setUniqueKeysInfo(UniqueKeyInfo.cloneAllAndLoadValues(tabConf.getUniqueKeys(), this));
	}
	
	default EtlDatabaseObject getSharedKeyParentRelatedObject(Connection conn) throws DBException {
		TableConfiguration tabConf = (TableConfiguration) this.getRelatedConfiguration();
		
		if (!tabConf.useSharedPKKey())
			throw new ForbiddenOperationException(
			        "The table '" + tabConf.getFullTableDescription() + " does not use shared key");
		
		ParentTable sharedKeyConf = tabConf.getSharedKeyRefInfo();
		
		Oid key = sharedKeyConf.generateParentOidFromChild(this);
		
		return DatabaseObjectDAO.getByOid(sharedKeyConf, key, conn);
		
		/*
		if (sharedKeyParentInOrigin != null) {
			EtlDatabaseObject sharedKeyParent = sharedKeyConf.createRecordInstance();
			sharedKeyParent.setRelatedConfiguration(sharedKeyConf);
			sharedKeyParent.copyFrom(sharedKeyParentInOrigin);
			sharedKeyParent.loadUniqueKeyValues(sharedKeyConf);
			sharedKeyParent.loadObjectIdData(sharedKeyConf);
			
			return DatabaseObjectDAO.getByOid(sharedKeyConf, sharedKeyParent.getObjectId(), conn);
		}
		
		return null;
		*/
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
		
		return DatabaseObjectDAO.getByOid(childTabConf, key, conn);
		
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
	
	default EtlDatabaseObject getRelatedParentObject(ParentTable refInfo, SrcConf src, Connection conn) throws DBException {
		return getRelatedParentObjectOnSrc(refInfo, refInfo.generateParentOidFromChild(this), src, conn);
	}
	
	default EtlDatabaseObject getRelatedParentObjectOnSrc(ParentTable refInfo, Oid prentOid, SrcConf src, Connection conn)
	        throws DBException {
		
		TableConfiguration tabConfInSrc = src
		        .findFullConfiguredConfInAllRelatedTable(refInfo.generateFullTableNameOnSchema(src.getSchema()));
		
		if (tabConfInSrc == null) {
			tabConfInSrc = new GenericTableConfiguration(src);
			tabConfInSrc.setTableName(refInfo.getTableName());
			
			tabConfInSrc.setRelatedSyncConfiguration(src.getRelatedEtlConf());
			
			tabConfInSrc.fullLoad(conn);
		}
		
		return DatabaseObjectDAO.getByOid(tabConfInSrc, prentOid, conn);
	}
	
	default void delete(Connection conn) throws DBException {
		DatabaseObjectDAO.remove(this, conn);
	}
	
}
