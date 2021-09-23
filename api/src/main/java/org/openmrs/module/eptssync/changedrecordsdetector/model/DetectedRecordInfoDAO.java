package org.openmrs.module.eptssync.changedrecordsdetector.model;

import java.sql.Connection;
import java.util.Date;

import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
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
	
	public static long getFirstNewRecord(SyncTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "min", SyncOperationConfig.SYNC_OPERATION_NEW_RECORDS_DETECTOR, conn);
	}
	
	public static long getLastNewRecord(SyncTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "max",  SyncOperationConfig.SYNC_OPERATION_NEW_RECORDS_DETECTOR, conn);
	}
	
	public static long getFirstChangedRecord(SyncTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "min", SyncOperationConfig.SYNC_OPERATION_CHANGED_RECORDS_DETECTOR, conn);
	}
	
	public static long getLastChangedRecord(SyncTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "max",  SyncOperationConfig.SYNC_OPERATION_CHANGED_RECORDS_DETECTOR, conn);
	}
	
	public static long getChangedRecord(SyncTableConfiguration tableConf, String appCode, Date observationDate, String function, String type, Connection conn) throws DBException, ForbiddenOperationException {
		
		String dateCreatedCondition = type.equals(SyncOperationConfig.SYNC_OPERATION_NEW_RECORDS_DETECTOR) ? "date_created >= ?" : ""; 
		String dateChangedCondition = type.equals(SyncOperationConfig.SYNC_OPERATION_CHANGED_RECORDS_DETECTOR) && !tableConf.getTableName().equalsIgnoreCase("obs") ? "date_changed >= ?" : "";
		String dateVoidedCondition  = type.equals(SyncOperationConfig.SYNC_OPERATION_CHANGED_RECORDS_DETECTOR) && !tableConf.isMetadata() && !tableConf.getTableName().equalsIgnoreCase("users") ? "date_voided >= ?" : "";
		
		String extraCondition = "";
			
		extraCondition = utilities.concatCondition(extraCondition, dateCreatedCondition, "or");
		extraCondition = utilities.concatCondition(extraCondition, dateChangedCondition, "or");
		extraCondition = utilities.concatCondition(extraCondition, dateVoidedCondition, "or");
		
		
		String 	sql =  " SELECT " + function + "("+ tableConf.getPrimaryKey() +") value \n";
				sql += " FROM " + tableConf.getTableName();
				sql += " WHERE 1 = 1 \n";
				//sql += " WHERE NOT EXISTS (	SELECT * \n";
				//sql += "					FROM detected_record_info \n";
				//sql += "					WHERE table_name = ? \n";
				//sql += "							AND app_code = ? \n";
				//sql += "							AND record_origin_location_code = ? \n";
				//sql += "							AND record_id = " + tableConf.getPrimaryKey() + ")";
				
				sql += " 		AND (" + extraCondition + ")";
			
		/*
		Object[] params = {	tableConf.getTableName(),
							appCode,
							tableConf.getOriginAppLocationCode(),
							observationDate, observationDate};*/
				
		Object[] params = {};
		
		if (type.equals(SyncOperationConfig.SYNC_OPERATION_NEW_RECORDS_DETECTOR)) params = CommonUtilities.getInstance().addToParams(params.length, params, observationDate);
		if (type.equals(SyncOperationConfig.SYNC_OPERATION_CHANGED_RECORDS_DETECTOR) && !dateVoidedCondition.isEmpty()) params = CommonUtilities.getInstance().addToParams(params.length, params, observationDate);
		if (type.equals(SyncOperationConfig.SYNC_OPERATION_CHANGED_RECORDS_DETECTOR) && !dateChangedCondition.isEmpty()) params = CommonUtilities.getInstance().addToParams(params.length, params, observationDate);
		
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v != null && v.hasValue() ? v.longValue() : 0;
	}	
}
