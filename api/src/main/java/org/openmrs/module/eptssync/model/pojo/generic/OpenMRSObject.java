package org.openmrs.module.eptssync.model.pojo.generic;

import java.sql.Connection;
import java.util.Date;
import java.util.Map;

import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.InconsistentStateException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This interface represent any openMRS class subject of synchronization records. Ex. "Patient", "Enconter", "Obs"
 * 
 * @author jpboane
 *
 */

//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface OpenMRSObject extends SyncRecord{
	public static final int CONSISTENCE_STATUS = 1;
	public static final int INCONSISTENCE_STATUS = -1;
	
	public abstract void refreshLastSyncDate(OpenConnection conn);
	public abstract String generateDBPrimaryKeyAtt();
	
	//public abstract void setOriginRecordId(int originRecordId);
	//public abstract int getOriginRecordId();	
	
	/**
	 * Load the destination parents id to this object
	 * 
	 * @param conn
	 * @throws DBException
	 */
	public void loadDestParentInfo(SyncTableConfiguration tableInfo, String recordOriginLocationCode, Connection conn) throws ParentNotYetMigratedException, DBException;
	public abstract Object[] getInsertParamsWithoutObjectId();
	public abstract String getInsertSQLWithoutObjectId();

	public abstract Object[] getInsertParamsWithObjectId();
	public abstract String getInsertSQLWithObjectId();

	public abstract String getUpdateSQL();
	public abstract Object[] getUpdateParams();
	
	public abstract String generateInsertValues();
	
	//public abstract String getOriginAppLocationCode();
	//public abstract void setOriginAppLocationCode(String originAppLocationCode);
	
	public abstract boolean hasIgnoredParent();
	public abstract void save(SyncTableConfiguration syncTableInfo, Connection conn) throws DBException;
	
	//public abstract boolean isMetadata();
		
	/**
	 * Consolidate this object if it is an metadata object
	 * 
	 * @param conn
	 * @throws DBException
	 */
	//public abstract void consolidateMetadata(SyncTableConfiguration tableInfo, Connection conn) throws DBException;	
	
	public abstract String getUuid();
	public abstract void setUuid(String uuid);
	//public abstract void markAsInconsistent();
	//public abstract void markAsConsistent();
	//public abstract void setConsistent(int consistent);
	//public abstract boolean isConsistent();
	//public abstract int getConsistent();
	public abstract boolean hasParents();
	//public abstract int retrieveSharedPKKey(Connection conn)  throws ParentNotYetMigratedException, DBException;
	
	public abstract int getParentValue(String parentAttName);
	
	/**
	 * Indicate if this object was generated or not using an eskeleton class
	 * @return
	 */
	//public abstract boolean isGeneratedFromSkeletonClass();
	
	/**
	 * Consolidate data for database consistency
	 * <p> The consolidation consist on re-arranging foreign keys between records from different tables
	 * <p> Because the consolidation process would be in cascade mode, each consolidation is imediatily commited to the dadabase
	 * 
	 * @param tableInfo
	 * @param conn
	 * @throws InconsistentStateException
	 * @throws DBException
	 */
	public abstract void consolidateData(SyncTableConfiguration tableInfo, Connection conn) throws InconsistentStateException, DBException;

	/**
	 * Resolve the inconsistency of a record
	 * <p> The resolution consists on checking if all parent of this records are present in data base. If not then this record will be moved
	 * to staging area and all its children will be moved in cascade
	 * 
	 * @param tableInfo
	 * @param conn
	 * @throws InconsistentStateException
	 * @throws DBException
	 */
	public abstract void resolveInconsistence(SyncTableConfiguration tableInfo, Connection conn) throws InconsistentStateException, DBException;

	public abstract SyncImportInfoVO retrieveRelatedSyncInfo(SyncTableConfiguration tableInfo, Connection conn) throws DBException;
	
	public abstract SyncImportInfoVO getRelatedSyncInfo();
	public abstract void setRelatedSyncInfo(SyncImportInfoVO relatedSyncInfo);
	
	public abstract String generateMissingInfo(Map<RefInfo, Integer> missingParents);
	
	public abstract void  remove(Connection conn) throws DBException;
	
	public abstract Map<RefInfo, Integer>  loadMissingParents(SyncTableConfiguration tableInfo, Connection conn) throws DBException;
	
	public abstract void removeDueInconsistency(SyncTableConfiguration syncTableInfo, Map<RefInfo, Integer> missingParents, Connection conn) throws DBException;
	
	//public abstract void markAsConsistent(Connection conn) throws DBException;
		
	public abstract void changeParentValue(String parentAttName, OpenMRSObject newParent);
	
	public abstract void changeObjectId(SyncTableConfiguration syncTableConfiguration, Connection conn) throws DBException;
	public abstract void changeParentForAllChildren(OpenMRSObject newParent, SyncTableConfiguration syncTableInfo, Connection conn) throws DBException;
	public abstract Date getDateChanged();

}
