package org.openmrs.module.eptssync.changesdetector.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class DetectedRecordInfoDAO extends BaseDAO{
	public static void insert(DetectedRecordInfo record, SyncTableConfiguration tableConfiguration, Connection conn) throws DBException{
		try {
			Object[] params = {record.getTableName(),
							   record.getRecordId(),
							   record.getRecordUuid(),
							   record.getOperationType(),
							   record.getOperationDate(),
							   record.getAppCode(),
							   record.getRecordOriginLocationCode()
							 };
			
			String sql = "";
			
			sql += "INSERT INTO detected_record_info(	table_name,\n";
			sql += "									record_id,\n";
			sql += "									record_uuid,\n";
			sql += "									operation_type,\n";
			sql += "									operation_date,\n";
			sql += "									app_code,\n";
			sql += "									record_origin_location_code)\n";
			sql += "	VALUES(?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
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
