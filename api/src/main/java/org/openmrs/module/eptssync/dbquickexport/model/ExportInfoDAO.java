package org.openmrs.module.eptssync.dbquickexport.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class ExportInfoDAO extends BaseDAO{
	public static long getFirstRecord(SyncTableConfiguration tableConf, Connection conn) throws DBException, ForbiddenOperationException {
		return getSpecificRecord(tableConf, "min", conn);
	}
	
	public static long getLastRecord(SyncTableConfiguration tableConf, Connection conn) throws DBException, ForbiddenOperationException {
		return getSpecificRecord(tableConf, "max",  conn);
	}
	
	public static long getSpecificRecord(SyncTableConfiguration tableConf, String function, Connection conn) throws DBException, ForbiddenOperationException {
			
		String 	sql =  " SELECT " + function + "("+ tableConf.getPrimaryKey() +") value\n";
				sql += " FROM " + tableConf.getTableName() + "\n";
				sql += " WHERE 1 = 1;";
						
		Object[] params = {};
		
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v != null && v.hasValue() ? v.longValue() : 0;
	}	
}