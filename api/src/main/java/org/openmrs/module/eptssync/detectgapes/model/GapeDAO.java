package org.openmrs.module.eptssync.detectgapes.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class GapeDAO extends BaseDAO {
	
	public static void insert(SyncTableConfiguration config, int recordId, Connection conn) throws DBException {
		String tableName = config.getSyncStageSchema() + ".sync_table_gape";
		
		String sql = "INSERT INTO " + tableName + "(table_name, record_id) values (?, ?)";
		Object[] params = { config.getTableName(), recordId };
		
		executeDBQuery(sql, params, conn);
	}
}
