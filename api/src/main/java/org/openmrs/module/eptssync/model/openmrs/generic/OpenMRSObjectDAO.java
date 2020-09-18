package org.openmrs.module.eptssync.model.openmrs.generic;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class OpenMRSObjectDAO extends BaseDAO {
	public static void refreshLastSyncDate(OpenMRSObject syncRecord, Connection conn) throws DBException{
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn), 
						   syncRecord.getObjectId()};
		
		String sql = "";
		
		sql += " UPDATE " + syncRecord.generateTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  " + syncRecord.generateDBPrimaryKeyAtt() + " = ? ";
		
		
		executeQuery(sql, params, conn);
	}
	
	public static void refreshLastSyncDate(List<OpenMRSObject> syncRecords, Connection conn) throws DBException{
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn), 
						   syncRecords.get(0).getObjectId(),
						   syncRecords.get(syncRecords.size() - 1).getObjectId()
						   };
		
		String sql = "";
		
		sql += " UPDATE " + syncRecords.get(0).generateTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  " + syncRecords.get(0).generateDBPrimaryKeyAtt() + " between ? and ? ";
		
		executeQuery(sql, params, conn);
	}
	
	public static void insert(OpenMRSObject record, Connection conn) throws DBException{
		Object[] params = record.getInsertParams();
		String sql = record.getInsertSQL();
		
		executeQuery(sql, params, conn);
	}
	

	public static void update(OpenMRSObject record, Connection conn) throws DBException {
		Object[] params = record.getUpdateParams();
		String sql = record.getUpdateSQL();

		executeQuery(sql, params, conn);
	}
	
	public static <T extends OpenMRSObject> T thinGetByOriginRecordId(Class<T> openMRSClass, int originRecordId, String originAppLocationCode, Connection conn) throws DBException{
		try {
			Object[] params = {originRecordId, originAppLocationCode};
			
			String sql = "";
			
			sql += " SELECT * \n";
			sql += " FROM  	" + openMRSClass.newInstance().generateTableName() + "\n";
			sql += " WHERE 	origin_record_id = ? \n";
			sql += "		AND origin_app_location_code = ?;\n";
			
			return find(openMRSClass, sql, params, conn);
		} catch (InstantiationException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
	}

	public static <T extends OpenMRSObject> T thinGetByUuid(Class<T> openMRSClass, String uuid, Connection conn) throws DBException{
		try {
			Object[] params = {uuid};
			
			String sql = "";
			
			sql += " SELECT * \n";
			sql += " FROM  	" + openMRSClass.newInstance().generateTableName() + "\n";
			sql += " WHERE 	uuid = ?;";
			
			return find(openMRSClass, sql, params, conn);
		} catch (InstantiationException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
	}

	public static <T extends OpenMRSObject> T getById(Class<T> openMRSClass, int id, Connection conn) throws DBException{
		try {
			T obj = openMRSClass.newInstance();
			
			Object[] params = {id};
			
			String sql = "";
			
			sql += " SELECT * \n";
			sql += " FROM  	" + obj.generateTableName() + "\n";
			sql += " WHERE 	" + obj.generateDBPrimaryKeyAtt() + " = ?;";
			
			return find(openMRSClass, sql, params, conn);
		} catch (InstantiationException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
	}


	public static OpenMRSObject getFirstRecord(SyncTableInfo tableInfo, Connection conn) throws DBException {
		String sql = "";
		
		OpenMRSObject obj = utilities.createInstance(tableInfo.getRecordClass());
		
		String clause = "";
		
		if (!tableInfo.isFirstExport()) {
			clause = "date_changed > last_sync_date";
		}
		else {
			clause = "last_sync_date is null";
		}

		sql += " SELECT * \n";
		sql += " FROM  	" + obj.generateTableName() + "\n";
		sql += " WHERE 	" + obj.generateDBPrimaryKeyAtt() + "\n";
		sql += " 			= (	SELECT min(" + obj.generateDBPrimaryKeyAtt() + ")\n";
		sql += "				FROM   " + obj.generateTableName() + "\n";
		sql += "				WHERE  " + clause + "\n";
		sql += "				)";
		
		return find(tableInfo.getRecordClass(), sql, null, conn);
	}
	
	public static OpenMRSObject getLastRecord(SyncTableInfo tableInfo,  Connection conn) throws DBException {
		String sql = "";
		
		OpenMRSObject obj = utilities.createInstance(tableInfo.getRecordClass());
		
		String clause = "";
		
		if (!tableInfo.isFirstExport()) {
			clause = "date_changed > last_sync_date";
		}
		else {
			clause = "last_sync_date is null";
		}

		
		sql += " SELECT * \n";
		sql += " FROM  	" + obj.generateTableName() + "\n";
		sql += " WHERE 	" + obj.generateDBPrimaryKeyAtt() + "\n";
		sql += " 			= (	SELECT max(" + obj.generateDBPrimaryKeyAtt() + ")\n";
		sql += "				FROM   " + obj.generateTableName() + "\n";
		sql += "				WHERE  " + clause + "\n";
		sql += "				)";
		
		return find(tableInfo.getRecordClass(), sql, null, conn);
	}
}
