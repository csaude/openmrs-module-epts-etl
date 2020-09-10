package org.openmrs.module.eptssync.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class OpenMRSObjectDAO extends BaseDAO {
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

}
