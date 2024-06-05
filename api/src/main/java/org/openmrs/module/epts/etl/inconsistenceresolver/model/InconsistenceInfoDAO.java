package org.openmrs.module.epts.etl.inconsistenceresolver.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class InconsistenceInfoDAO extends BaseDAO {
	
	//@// @formatter:off
	public static void insert(InconsistenceInfo record, TableConfiguration tableConfiguration, Connection conn) throws DBException{
		try {
			String syncStageSchema = tableConfiguration.getRelatedSyncConfiguration().getSyncStageSchema();
			
			Object[] params = {record.getTableName(),
							   record.getRecordId().getSimpleValue(),
							   record.getParentTableName(),
							   record.getParentId(),
							   record.getRecordOriginLocationCode(),
							   record.getDefaultParentId(),
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
			
			executeQueryWithRetryOnError(sql, params, conn);
		} catch (DBException e) {
			if (!e.isDuplicatePrimaryOrUniqueKeyException()) {
				throw e;
			}
		}
	}
	
}
