package org.openmrs.module.epts.etl.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class TableOperationProgressInfoDAO extends BaseDAO {
	
	public static void insert(TableOperationProgressInfo record, EtlItemConfiguration config, Connection conn)
	        throws DBException {
		String syncStageSchema = config.getRelatedEtlConf().getSyncStageSchema();
		
		//@// @formatter:off
		Object[] params = { record.getOperationId(), 
							record.getOperationName(), 
							record.getEtlConfiguration().getConfigCode(),
							record.getOriginAppLocationCode(), 
							DateAndTimeUtilities.getCurrentSystemDate(conn),
							DateAndTimeUtilities.getCurrentSystemDate(conn), 
							record.getProgressMeter().getTotal(),
							record.getProgressMeter().getMinRecordId(),
							record.getProgressMeter().getMaxRecordId(),
							record.getProgressMeter().getProcessed(), 
							record.getProgressMeter().getStatus() };
		
		 
		// @formatter:on
		String sql = "";
		
		sql += "INSERT INTO " + syncStageSchema + ".table_operation_progress_info(operation_id,\n";
		sql += "																  operation_name,\n";
		sql += "																  table_name,\n";
		sql += "																  record_origin_location_code,\n";
		sql += "																  started_at,\n";
		sql += "																  last_refresh_at,\n";
		sql += "																  total_records,\n";
		sql += "																  min_record_id,\n";
		sql += "																  max_record_id,\n";
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
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?);";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void update(TableOperationProgressInfo record, EtlItemConfiguration config, Connection conn)
	        throws DBException {
		String syncStageSchema = config.getRelatedEtlConf().getSyncStageSchema();
		
		//@// @formatter:off
		Object[] params = { DateAndTimeUtilities.getCurrentSystemDate(conn), 
							record.getProgressMeter().getTotal(),
							record.getProgressMeter().getMinRecordId(),
							record.getProgressMeter().getMaxRecordId(),
							record.getProgressMeter().getProcessed(), 
							record.getProgressMeter().getStatus(), 
							record.getOperationId() };
		
		String sql = "";
		
		sql += " UPDATE " + syncStageSchema + ".table_operation_progress_info\n";
		sql += " SET	last_refresh_at = ?,\n";
		sql += "		total_records = ?,\n";
		sql += "		min_record_id = ?,\n";
		sql += "		max_record_id = ?,\n";		
		sql += "		total_processed_records=?,";
		sql += "		status=?";
		sql += " WHERE operation_id = ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static <T extends EtlDatabaseObject> TableOperationProgressInfo find(OperationController<T> controller, EtlItemConfiguration config,
	        Connection conn) throws DBException {
		String syncStageSchema = config.getRelatedEtlConf().getSyncStageSchema();
		
		Object[] params = { TableOperationProgressInfo.generateOperationId(controller, config) };
		
		String sql = "SELECT * FROM " + syncStageSchema + ".table_operation_progress_info WHERE operation_id = ?";
		
		TableOperationProgressInfo vo = BaseDAO.find(TableOperationProgressInfo.class, sql, params, conn);
		
		if (vo != null) {
			vo.setEtlConfiguration(config);
			vo.setController(controller);
		}
		
		return vo;
		
	}
	
	public static void delete(TableOperationProgressInfo record, EtlItemConfiguration config, Connection conn)
	        throws DBException {
		String syncStageSchema = config.getRelatedEtlConf().getSyncStageSchema();
		
		Object[] params = { record.getOperationId() };
		
		String sql = "";
		
		sql += " DELETE FROM  " + syncStageSchema + ".table_operation_progress_info\n";
		sql += " WHERE operation_id = ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
}
