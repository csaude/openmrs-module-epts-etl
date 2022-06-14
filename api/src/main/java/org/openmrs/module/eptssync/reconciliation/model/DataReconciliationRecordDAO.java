package org.openmrs.module.eptssync.reconciliation.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class DataReconciliationRecordDAO extends BaseDAO{
	public static void insert(DataReconciliationRecord record, Connection conn) throws DBException{
		try {
			Object[] params = {record.getRecordUuid(),
							   record.getRecordOriginLocationCode(),
							   record.getReasonType().name(),
							   record.getTableName()};
			
			String sql = "";
			
			sql += "INSERT INTO \n"; 
			sql += "	" + record.getConfig().generateFullStageTableName() + "(record_uuid,\n";
			sql += "															record_origin_location_code,\n";
			sql += "											 				reasonType,\n";
			sql += "											 				table_name)\n";
			sql += "	VALUES(?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?);";
			
			executeQuery(sql, params, conn);
		} catch (DBException e) {
			if (!e.isDuplicatePrimaryKeyException()) {
				throw e;
			}
		}
	}
}
