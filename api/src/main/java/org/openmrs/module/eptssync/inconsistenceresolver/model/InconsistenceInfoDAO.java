package org.openmrs.module.eptssync.inconsistenceresolver.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class InconsistenceInfoDAO extends BaseDAO{
	public static void insert(InconsistenceInfo record, SyncTableConfiguration tableConfiguration, Connection conn) throws DBException{
		try {
			String syncStageSchema = tableConfiguration.getRelatedSynconfiguration().getSyncStageSchema();
			
			Object[] params = {record.getTableName(),
							   record.getRecordId(),
							   record.getParentTableName(),
							   record.getParentId(),
							   record.getRecordOriginLocationCode(),
							   record.getDefaultParentId() == 0 ? null : record.getDefaultParentId(),
								};
			
			String sql = "";
			
			sql += "INSERT INTO " + syncStageSchema + ".inconsistence_info(	table_name,\n";
			sql += "														record_id,\n";
			sql += "														parent_table_name,\n";
			sql += "														parent_id,\n";
			sql += "														record_origin_location_code,\n";
			sql += "														default_parent_id)\n";
			sql += "	VALUES(?,\n";
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
