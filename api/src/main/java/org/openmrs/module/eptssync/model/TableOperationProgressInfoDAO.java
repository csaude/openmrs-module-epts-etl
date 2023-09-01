package org.openmrs.module.eptssync.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class TableOperationProgressInfoDAO extends BaseDAO{
	public static void insert(TableOperationProgressInfo record, SyncTableConfiguration tableConfiguration, Connection conn) throws DBException{
			String syncStageSchema = tableConfiguration.getRelatedSynconfiguration().getSyncStageSchema();
			
			Object[] params = {record.getOperationId(),
							   record.getOperationName(),
							   record.getOperationTable(),
							   record.getOriginAppLocationCode(),
							   DateAndTimeUtilities.getCurrentSystemDate(conn),
							   DateAndTimeUtilities.getCurrentSystemDate(conn),
							   record.getProgressMeter().getTotal(),
							   record.getProgressMeter().getProcessed(),
							   record.getProgressMeter().getStatus()
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
			
			executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void update(TableOperationProgressInfo record, SyncTableConfiguration tableConfiguration, Connection conn) throws DBException{
		String syncStageSchema = tableConfiguration.getRelatedSynconfiguration().getSyncStageSchema();
		
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn),
						   record.getProgressMeter().getTotal(),
						   record.getProgressMeter().getProcessed(),
						   record.getProgressMeter().getStatus(),
						   record.getOperationId()  
						  };
		
		String sql = "";
		
		sql += " UPDATE " + syncStageSchema + ".table_operation_progress_info\n";
		sql += " SET	last_refresh_at = ?,\n";
		sql += "		total_records = ?,\n";
		sql += "		total_processed_records=?,";
		sql += "		status=?";
		sql += " WHERE operation_id = ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static TableOperationProgressInfo find(OperationController controller, SyncTableConfiguration tableConfiguration, Connection conn) throws DBException{
		String syncStageSchema = tableConfiguration.getRelatedSynconfiguration().getSyncStageSchema();
		
		Object[] params = {TableOperationProgressInfo.generateOperationId(controller, tableConfiguration)};
		
		String sql = "SELECT * FROM " + syncStageSchema + ".table_operation_progress_info WHERE operation_id = ?";
		
		TableOperationProgressInfo vo = BaseDAO.find(TableOperationProgressInfo.class, sql, params, conn);
		
		if (vo != null) {
			vo.setTableConfiguration(tableConfiguration);
			vo.setController(controller);
		}
		
		return vo;
		
	}
}
