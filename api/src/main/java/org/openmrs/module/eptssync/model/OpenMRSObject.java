package org.openmrs.module.eptssync.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
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
	public abstract void refreshLastSyncDate(OpenConnection conn);
	public abstract String generateDBPrimaryKeyAtt();
	
	public abstract int getMainParentId();
	public abstract String getMainParentTable();
	
	public abstract void setMainParentId(int parentId);
	
	public abstract void setOriginRecordId(int originRecordId);
	public abstract int getOriginRecordId();	
	
	/**
	 * Load the destination parents id to this object
	 * 
	 * @param conn
	 * @throws DBException
	 */
	public abstract void loadDestParentInfo(Connection conn) throws DBException;
	public abstract Object[] getInsertParams();
	public abstract String getInsertSQL();
	
	public abstract String getUpdateSQL();
	public abstract Object[] getUpdateParams();
	
	public abstract String getOriginAppLocationCode();
	public abstract void setOriginAppLocationCode(String originAppLocationCode);
}
