package org.openmrs.module.eptssync.dbquickmerge.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class MergingRecordDAO {
	
	public static void insert(MergingRecord record, Connection conn) throws DBException {
		String syncStageSchema = record.getConfig().getRelatedSynconfiguration().getSyncStageSchema();
		
		Object[] params = { record.getRecord().getUuid(),
				/*record.getOperationName(),
				record.getOperationTable(),
				record.getOriginAppLocationCode(),
				DateAndTimeUtilities.getCurrentSystemDate(conn),
				DateAndTimeUtilities.getCurrentSystemDate(conn),
				record.getProgressMeter().getTotal(),
				record.getProgressMeter().getProcessed(),
				record.getProgressMeter().getStatus()*/
		};
		
		String sql = "";
		
		sql += "INSERT INTO " + syncStageSchema + ".table_operation_progress_info(operation_id,\n";
		sql += "																  operation_name,\n";
		sql += "																  table_name,\n";
		sql += "																  record_origin_location_code,\n";
		sql += "																  started_at,\n";
		sql += "																  last_refresh_at,\n";
		sql += "																  total_records,\n";
		sql += "																  total_processed_records,\n";
		sql += "																  status)\n";
		sql += "	VALUES(?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?);";
		
		//executeQuery(sql, params, conn);
	}
}
