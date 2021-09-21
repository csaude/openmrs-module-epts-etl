package org.openmrs.module.eptssync.changesdetector.model;

import java.sql.Connection;
import java.util.Date;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
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
	
	public static long getFirstChangedRecord(SyncTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "min", conn);
	}
	
	public static long getLastChangedRecord(SyncTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "max", conn);
	}
	
	public static long getChangedRecord(SyncTableConfiguration tableConf, String appCode, Date observationDate, String function, Connection conn) throws DBException, ForbiddenOperationException {
		String dateChangedCondition = !tableConf.getTableName().equalsIgnoreCase("obs") ? " or date_changed >= ? " : "";
		String dateVoidedCondition  = !tableConf.isMetadata() && tableConf.getTableName().equalsIgnoreCase("users") ? " or date_voided >= ? " : "";
			
		String 	sql =  " SELECT " + function + "("+ tableConf.getPrimaryKey() +") value \n";
				sql += " FROM " + tableConf.getTableName();
				sql += " WHERE 1 = 1 \n";
				//sql += " WHERE NOT EXISTS (	SELECT * \n";
				//sql += "					FROM detected_record_info \n";
				//sql += "					WHERE table_name = ? \n";
				//sql += "							AND app_code = ? \n";
				//sql += "							AND record_origin_location_code = ? \n";
				//sql += "							AND record_id = " + tableConf.getPrimaryKey() + ")";
				
				sql += " 		AND (date_created >= ? " + dateChangedCondition + " "+ dateVoidedCondition + ")";
			
		/*
		Object[] params = {	tableConf.getTableName(),
							appCode,
							tableConf.getOriginAppLocationCode(),
							observationDate, observationDate};*/
				
		Object[] params = {observationDate};
		
		if (!dateVoidedCondition.isEmpty()) params = CommonUtilities.getInstance().addToParams(params.length, params, observationDate);
		if (!dateChangedCondition.isEmpty()) params = CommonUtilities.getInstance().addToParams(params.length, params, observationDate);
		
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v != null && v.hasValue() ? v.longValue() : 0;
	}	
}
