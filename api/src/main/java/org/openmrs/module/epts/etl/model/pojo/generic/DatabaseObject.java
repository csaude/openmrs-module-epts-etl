package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.RefInfo;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.epts.etl.exceptions.SyncExeption;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;

/**
 * This interface represent any Database Entintiy class subject of synchronization records.
 * 
 * @author jpboane
 */
public interface DatabaseObject extends SyncRecord {
	
	public static final int CONSISTENCE_STATUS = 1;
	
	public static final int INCONSISTENCE_STATUS = -1;
	
	public abstract void refreshLastSyncDateOnOrigin(AbstractTableConfiguration tableConfiguration,
	        String recordOriginLocationCode, Connection conn);
	
	public abstract void refreshLastSyncDateOnDestination(AbstractTableConfiguration tableConfiguration,
	        String recordOriginLocationCode, Connection conn);
	
	public abstract Oid getObjectId();
	
	public abstract void setObjectId(Oid objectId);
	
	public abstract List<UniqueKeyInfo> getUniqueKeysInfo();
	
	public abstract void setUniqueKeysInfo(List<UniqueKeyInfo> uniqueKeysInfo);
	
	/**
	 * Load the destination parents id to this object
	 * 
	 * @param conn
	 * @throws DBException
	 */
	public void loadDestParentInfo(AbstractTableConfiguration tableInfo, String recordOriginLocationCode, Connection conn)
	        throws ParentNotYetMigratedException, DBException;
	
	public abstract Object[] getInsertParamsWithoutObjectId();
	
	public abstract String getInsertSQLWithoutObjectId();
	
	public abstract Object[] getInsertParamsWithObjectId();
	
	public abstract String getInsertSQLWithObjectId();
	
	public abstract String getUpdateSQL();
	
	public abstract Object[] getUpdateParams();
	
	public abstract String generateInsertValuesWithoutObjectId();
	
	public abstract String generateInsertValuesWithObjectId();
	
	public abstract boolean hasIgnoredParent();
	
	public abstract void save(AbstractTableConfiguration syncTableInfo, Connection conn) throws DBException;
	
	public abstract String getUuid();
	
	public abstract void setUuid(String uuid);
	
	public abstract boolean hasParents();
	
	public abstract Object getParentValue(String parentAttName);
	
	public abstract String generateTableName();
	
	/**
	 * Load the objectId info
	 * 
	 * @param tabConf the table configuration
	 */
	public abstract void loadObjectIdData(AbstractTableConfiguration tabConf);
	
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
	public abstract void consolidateData(AbstractTableConfiguration tableInfo, Connection conn)
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
	public abstract void resolveInconsistence(AbstractTableConfiguration tableInfo, Connection conn)
	        throws InconsistentStateException, DBException;
	
	public abstract SyncImportInfoVO retrieveRelatedSyncInfo(AbstractTableConfiguration tableInfo,
	        String recordOriginLocationCode, Connection conn) throws DBException;
	
	public abstract DatabaseObject retrieveParentInDestination(Integer parentId, String recordOriginLocationCode,
	        AbstractTableConfiguration parentTableConfiguration, boolean ignorable, Connection conn)
	        throws ParentNotYetMigratedException, DBException;
	
	public abstract SyncImportInfoVO getRelatedSyncInfo();
	
	public abstract void setRelatedSyncInfo(SyncImportInfoVO relatedSyncInfo);
	
	public abstract String generateMissingInfo(Map<RefInfo, Integer> missingParents);
	
	public abstract void remove(Connection conn) throws DBException;
	
	public abstract Map<RefInfo, Integer> loadMissingParents(AbstractTableConfiguration tableInfo, Connection conn)
	        throws DBException;
	
	public abstract void removeDueInconsistency(AbstractTableConfiguration syncTableInfo,
	        Map<RefInfo, Integer> missingParents, Connection conn) throws DBException;
	
	public abstract void changeParentValue(RefInfo refInfo, DatabaseObject newParent);
	
	public abstract void setParentToNull(RefInfo refInfo);
	
	public abstract void changeObjectId(AbstractTableConfiguration abstractTableConfiguration, Connection conn)
	        throws DBException;
	
	public abstract void changeParentForAllChildren(DatabaseObject newParent, AbstractTableConfiguration syncTableInfo,
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
	public abstract Object getFieldValue(String fieldName) throws ForbiddenOperationException;
	
	public abstract void setFieldValue(String fieldName, Object value);
	
	/**
	 * Retrive values for all {@link AbstractTableConfiguration#getUniqueKeys()} fields. The values
	 * follow the very same sequence defined with {@link AbstractTableConfiguration#getUniqueKeys()}
	 * 
	 * @param tableConfiguration the {@link AbstractTableConfiguration} from where the
	 *            {@link AbstractTableConfiguration#getUniqueKeys()} will be retrieved from
	 * @return values for all {@link AbstractTableConfiguration#getUniqueKeys()} field.
	 * @throws ForbiddenOperationException if one or more fields in any key have null value
	 */
	public default Object[] getUniqueKeysFieldValues(AbstractTableConfiguration tableConfiguration)
	        throws ForbiddenOperationException {
		if (!tableConfiguration.isFullLoaded()) {
			try {
				tableConfiguration.fullLoad();
			}
			catch (DBException e) {
				throw new SyncExeption(e) {
					
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
	
	/**
	 * Retrive values for all fields in any unique key.
	 * 
	 * @param uniqueKeyFields the list of fields in a unique key
	 * @return values for all fields in a unique key.
	 * @throws ForbiddenOperationException if one or more fields in any key have null value
	 */
	public default Object[] getUniqueKeysFieldValues(UniqueKeyInfo uniqueKey) throws ForbiddenOperationException {
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
	
	public abstract void fastCreateSimpleNumericKey(long i);
	
}
