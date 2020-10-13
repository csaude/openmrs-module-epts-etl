package org.openmrs.module.eptssync.model.openmrs.generic;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ParentNotYetMigratedException;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.InconsistentStateException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This interface represent any openMRS class subject of synchronization records. Ex. "Patient", "Enconter", "Obs"
 * 
 * @author jpboane
 *
 */

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "className")
public interface OpenMRSObject extends SyncRecord{
	public static final int CONSISTENCE_STATUS = 1;
	public static final int INCONSISTENCE_STATUS = -1;
	
	public abstract void refreshLastSyncDate(OpenConnection conn);
	public abstract String generateDBPrimaryKeyAtt();
	
	public abstract void setOriginRecordId(int originRecordId);
	public abstract int getOriginRecordId();	
	
	/**
	 * Load the destination parents id to this object
	 * 
	 * @param conn
	 * @throws DBException
	 */
	public abstract void loadDestParentInfo(Connection conn) throws ParentNotYetMigratedException, DBException;
	public abstract Object[] getInsertParams();
	public abstract String getInsertSQL();
	
	public abstract String getUpdateSQL();
	public abstract Object[] getUpdateParams();
	
	public abstract String generateInsertValues();
	
	public abstract String getOriginAppLocationCode();
	public abstract void setOriginAppLocationCode(String originAppLocationCode);
	
	public abstract boolean hasIgnoredParent();
	public abstract void save(Connection conn) throws DBException;
	
	public abstract boolean isMetadata();
		
	/**
	 * Consolidate this object if it is an metadata object
	 * 
	 * @param conn
	 * @throws DBException
	 */
	public abstract void consolidateMetadata(Connection conn) throws DBException;	
	
	public abstract String getUuid();
	
	public abstract void markAsInconsistent();
	public abstract void markAsConsistent();
	public abstract void setConsistent(int consistent);
	public abstract boolean isConsistent();
	public abstract int getConsistent();
	public abstract boolean hasParents();
	public abstract int retrieveSharedPKKey(Connection conn)  throws ParentNotYetMigratedException, DBException;
	
	public abstract int getParentValue(String parentAttName);
	
	/**
	 * Indicate if this object was generated or not using an eskeleton class
	 * @return
	 */
	public abstract boolean isGeneratedFromSkeletonClass();
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
	//public abstract void moveToStageAreaDueInconsistency(SyncTableInfo syncTableInfo, InconsistentStateException exception, Connection conn) throws DBException;
}
