package org.openmrs.module.eptssync.model.pojo.generic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.controller.conf.UniqueKeyInfo;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.AttDefinedElements;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.InconsistentStateException;

/**
 * This interface represent any Database Entintiy class subject of synchronization records.
 * 
 * @author jpboane
 */
public interface DatabaseObject extends SyncRecord {
	
	public static final int CONSISTENCE_STATUS = 1;
	
	public static final int INCONSISTENCE_STATUS = -1;
	
	public abstract void refreshLastSyncDateOnOrigin(SyncTableConfiguration tableConfiguration,
	        String recordOriginLocationCode, Connection conn);
	
	public abstract void refreshLastSyncDateOnDestination(SyncTableConfiguration tableConfiguration,
	        String recordOriginLocationCode, Connection conn);
	
	public abstract String generateDBPrimaryKeyAtt();
	
	public abstract Integer getObjectId();
	
	public abstract void setObjectId(Integer objectId);
	
	public abstract List<UniqueKeyInfo> getUniqueKeysInfo();
	
	public abstract void setUniqueKeysInfo(List<UniqueKeyInfo> uniqueKeysInfo);
	
	/**
	 * Load the destination parents id to this object
	 * 
	 * @param conn
	 * @throws DBException
	 */
	public void loadDestParentInfo(SyncTableConfiguration tableInfo, String recordOriginLocationCode, Connection conn)
	        throws ParentNotYetMigratedException, DBException;
	
	public abstract Object[] getInsertParamsWithoutObjectId();
	
	public abstract String getInsertSQLWithoutObjectId();
	
	public abstract Object[] getInsertParamsWithObjectId();
	
	public abstract String getInsertSQLWithObjectId();
	
	public abstract String getUpdateSQL();
	
	public abstract Object[] getUpdateParams();
	
	public abstract String generateInsertValues();
	
	public abstract boolean hasIgnoredParent();
	
	public abstract void save(SyncTableConfiguration syncTableInfo, Connection conn) throws DBException;
	
	public abstract String getUuid();
	
	public abstract void setUuid(String uuid);
	
	public abstract boolean hasParents();
	
	public abstract Integer getParentValue(String parentAttName);
	
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
	public abstract void consolidateData(SyncTableConfiguration tableInfo, Connection conn)
	        throws InconsistentStateException, DBException;
	
	/**
	 * Resolve the inconsistency of a record
	 * <p>
	 * The resolution consists on checking if all parent of this records are present in data base.
	 * If not then this record will be moved to staging area and all its children will be moved in
	 * cascade
	 * 
	 * @param tableInfo
	 * @param conn
	 * @throws InconsistentStateException
	 * @throws DBException
	 */
	public abstract void resolveInconsistence(SyncTableConfiguration tableInfo, Connection conn)
	        throws InconsistentStateException, DBException;
	
	public abstract SyncImportInfoVO retrieveRelatedSyncInfo(SyncTableConfiguration tableInfo,
	        String recordOriginLocationCode, Connection conn) throws DBException;
	
	public abstract DatabaseObject retrieveParentInDestination(Integer parentId, String recordOriginLocationCode,
	        SyncTableConfiguration parentTableConfiguration, boolean ignorable, Connection conn)
	        throws ParentNotYetMigratedException, DBException;
	
	public abstract SyncImportInfoVO getRelatedSyncInfo();
	
	public abstract void setRelatedSyncInfo(SyncImportInfoVO relatedSyncInfo);
	
	public abstract String generateMissingInfo(Map<RefInfo, Integer> missingParents);
	
	public abstract void remove(Connection conn) throws DBException;
	
	public abstract Map<RefInfo, Integer> loadMissingParents(SyncTableConfiguration tableInfo, Connection conn)
	        throws DBException;
	
	public abstract void removeDueInconsistency(SyncTableConfiguration syncTableInfo, Map<RefInfo, Integer> missingParents,
	        Connection conn) throws DBException;
	
	public abstract void changeParentValue(String parentAttName, DatabaseObject newParent);
	
	public abstract void setParentToNull(String parentAttName);
	
	public abstract void changeObjectId(SyncTableConfiguration syncTableConfiguration, Connection conn) throws DBException;
	
	public abstract void changeParentForAllChildren(DatabaseObject newParent, SyncTableConfiguration syncTableInfo,
	        Connection conn) throws DBException;
	
	public abstract Date getDateChanged();
	
	public abstract Date getDateVoided();
	
	public abstract Date getDateCreated();
	
	/**
	 * Check if this record has exactily the same values in all fields with a given object
	 * 
	 * @param srcObj
	 * @return true if this record has exactily the same values in all fields with the given object
	 */
	public abstract boolean hasExactilyTheSameDataWith(DatabaseObject srcObj);
	
	/**
	 * Return a value of given field
	 * 
	 * @param fieldName of field to retrieve
	 * @return Return a value of given field
	 */
	public abstract Object[] getFieldValues(String... fieldName);
	
	/**
	 * Retrive values for all {@link SyncTableConfiguration#getUniqueKeys()} fields. The values
	 * follow the very same sequence defined with {@link SyncTableConfiguration#getUniqueKeys()}
	 * 
	 * @param tableConfiguration the {@link SyncTableConfiguration} from where the
	 *            {@link SyncTableConfiguration#getUniqueKeys()} will be retrieved from
	 * @return values for all {@link SyncTableConfiguration#getUniqueKeys()} field.
	 * @throws ForbiddenOperationException if one or more fields in any key have null value
	 */
	public default Object[] getUniqueKeysFieldValues(SyncTableConfiguration tableConfiguration)
	        throws ForbiddenOperationException {
		if (!tableConfiguration.isFullLoaded())
			tableConfiguration.fullLoad();
		
		List<Object> values = new ArrayList<Object>();
		
		for (UniqueKeyInfo uniqueKey : tableConfiguration.getUniqueKeys()) {
			Object[] fieldValues = this.getFieldValues(
			    AttDefinedElements.convertTableAttNameToClassAttName(utils.parseListToArray(uniqueKey.generateListFromFieldsNames())));
			
			if (fieldValues != null && fieldValues.length == uniqueKey.getFields().size()) {
				values.addAll(utils.parseArrayToList(fieldValues));
			} else
				throw new ForbiddenOperationException("On or more fields of key [" + uniqueKey + "] has no value.");
		}
		
		return utils.parseListToArray(values);
	}
	
	/**
	 * Retrive values for all fields in any unique key.
	 * 
	 * @param uniqueKeyFields the list of fields in a unique key
	 * @return values for all fields in a unique key.
	 * @throws ForbiddenOperationException if one or more fields in any key have null value
	 */
	public default Object[] getUniqueKeysFieldValues(UniqueKeyInfo uniqueKey) throws ForbiddenOperationException {
		Object[] fieldValues = this.getFieldValues(
		    AttDefinedElements.convertTableAttNameToClassAttName(utils.parseListToArray(uniqueKey.generateListFromFieldsNames())));
		
		if (fieldValues != null && fieldValues.length == uniqueKey.getFields().size()) {
			return fieldValues;
		} else
			throw new ForbiddenOperationException("On or more fields of key [" + uniqueKey.toString() + "] has no value.");
	}
}
