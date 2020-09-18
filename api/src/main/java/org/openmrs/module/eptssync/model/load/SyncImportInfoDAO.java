package org.openmrs.module.eptssync.model.load;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.exceptions.SyncExeption;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class SyncImportInfoDAO extends BaseDAO {
	public static void insertAll(List<SyncImportInfoVO> records, SyncTableInfo tableInfo, Connection conn) throws DBException{
		for (SyncImportInfoVO record: records) {
			insert(record, tableInfo, conn);
		}
	}
	
	public static void insert(SyncImportInfoVO record, SyncTableInfo tableInfo, Connection conn) throws DBException{
		Object[] params = {record.getRecordId(),
						   record.getJson(),
						   record.getOriginAppLocationCode()};
		
		String sql = "";
		
		sql += "INSERT INTO \n"; 
		sql += "	" + tableInfo.generateFullStageTableName() + "(	record_id,\n";
		sql += "											 		json,\n";
		sql += "													origin_app_location_code)";
		sql += "	VALUES(?,\n";
		sql += "		   ?,\n";
		sql += "		   ?);";
		
		executeQuery(sql, params, conn);
	}
	
	/**
	 * For each originAppLocationId retrieve on record from the diven tableName
	 * 
	 * @param tableName
	 * @param conn
	 * @return
	 * @throws DBException
	 */
	public static List<SyncImportInfoVO> getDefaultRecordForEachOriginAppLocatin(String tableName, Connection conn) throws DBException{
		String sql = "";
		sql += " SELECT origin_app_location_code \n";
		sql += " FROM 	" + tableName + "\n";
		sql += " GROUP BY origin_app_location_code";
		
		return  BaseDAO.search(SyncImportInfoVO.class , sql, null, conn);
	}

	public static void markAsFailedToMigrate(SyncImportInfoVO record, SyncTableInfo tableInfo, SyncExeption e, Connection conn) throws DBException {
		Object[] params = { SyncImportInfoVO.MIGRATION_STATUS_FAILED,
							e.getLocalizedMessage(),
							DateAndTimeUtilities.getCurrentDate(),
							record.getId()
							};

			String sql = "";
			
			sql += "UPDATE 	" + tableInfo.generateFullStageTableName() + "\n";
			sql += "SET	   	migration_status = ?, \n";
			sql += "	   	last_migration_try_err = ?, \n";
			sql += "	   	last_migration_try_date = ? \n";
			sql += "WHERE 	id = ?";
	
			executeQuery(sql, params, conn);
	}

	public static void remove(SyncImportInfoVO record, SyncTableInfo tableInfo, Connection conn) throws DBException {
		Object[] params = { record.getId()
							};

		String sql = "";
		
		sql += "DELETE 	\n";
		sql += "FROM	" + tableInfo.generateFullStageTableName() + "\n";
		sql += "WHERE 	id = ?";
		
		executeQuery(sql, params, conn);
	}

	public static void markAsToBeCompletedInFuture(SyncImportInfoVO record, SyncTableInfo tableInfo, OpenConnection conn) throws DBException {
		Object[] params = { SyncImportInfoVO.MIGRATION_STATUS_INCOMPLETE,
							"Migrated BUT still miss some parent info",
							DateAndTimeUtilities.getCurrentDate(),
							record.getId()
							};

		String sql = "";
		
		sql += "UPDATE 	" + tableInfo.generateFullStageTableName() + "\n";
		sql += "SET	   	migration_status = ?, \n";
		sql += "	   	last_migration_try_err = ?, \n";
		sql += "	   	last_migration_try_date = ? \n";
		sql += "WHERE 	id = ?";
		
		executeQuery(sql, params, conn);
	}
}
