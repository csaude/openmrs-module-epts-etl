package org.openmrs.module.eptssync.model.load;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SyncImportInfoDAO extends BaseDAO {
	public static void insertAll(List<SyncImportInfoVO> records, SyncTableInfo tableInfo, Connection conn) throws DBException{
		for (SyncImportInfoVO record: records) {
			insert(record, tableInfo, conn);
		}
	}
	
	public static void insert(SyncImportInfoVO record, SyncTableInfo tableInfo, Connection conn) throws DBException{
		Object[] params = {record.getRecordId(),
						   record.getSyncTableName(),
						   record.getMainParentId(),
						   record.getMainParentTable(),
						   record.getJson(),
						   record.getOriginAppLocationCode()};
		
		String sql = "";
		
		sql += "INSERT INTO \n"; 
		sql += "	" + tableInfo.generateFullStageTableName() + "(	record_id,\n";
		sql += "											 		sync_table_name,\n";
		sql += "											 		main_parent_id,\n";
		sql += "											 		main_parent_table,\n";
		sql += "											 		json,\n";
		sql += "													origin_app_location_code)";
		sql += "	VALUES(?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
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
}
