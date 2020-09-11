package org.openmrs.module.eptssync.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.openmrs.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class GenericSyncRecordDAO extends BaseDAO{
	public static void refreshLastSyncDate(OpenMRSObject syncRecord, Connection conn) throws DBException{
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn), 
						   syncRecord.getObjectId()};
		
		String sql = "";
		
		sql += " UPDATE " + syncRecord.generateTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  " + syncRecord.generateDBPrimaryKeyAtt() + " = ? ";
		
		
		executeQuery(sql, params, conn);
	}
}