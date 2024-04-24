package org.openmrs.module.epts.etl.changedrecordsdetector.model;

import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DetectedRecordInfoDAO extends BaseDAO{
	public static void insert(DetectedRecordInfo record, AbstractTableConfiguration tableConfiguration, Connection conn) throws DBException{
		
		long id = 0;
		
		try {
			Object[] params = {record.getTableName(),
							   record.getObjectId(),
							   record.getUuid(),
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
			
			id =  executeQueryWithRetryOnError(sql, params, conn);
			
			if (tableConfiguration.getPrimaryKey().isSimpleNumericKey()){
				record.setObjectId(new Oid());
				
				record.getObjectId().retrieveSimpleKey().setValue(id);
			}

			
		} catch (DBException e) {
			if (!e.isDuplicatePrimaryOrUniqueKeyException()) {
				throw e;
			} 
		}
		
	}
	
	public static int getFirstNewRecord(AbstractTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "min", EtlOperationType.NEW_RECORDS_DETECTOR, conn);
	}
	
	public static int getLastNewRecord(AbstractTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "max",  EtlOperationType.NEW_RECORDS_DETECTOR, conn);
	}
	
	public static int getFirstChangedRecord(AbstractTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "min", EtlOperationType.CHANGED_RECORDS_DETECTOR, conn);
	}
	
	public static int getLastChangedRecord(AbstractTableConfiguration tableConf, String appCode, Date observationDate, Connection conn) throws DBException, ForbiddenOperationException {
		return getChangedRecord(tableConf, appCode, observationDate, "max",  EtlOperationType.CHANGED_RECORDS_DETECTOR, conn);
	}
	
	public static int getChangedRecord(AbstractTableConfiguration tableConf, String appCode, Date observationDate, String function, EtlOperationType type, Connection conn) throws DBException, ForbiddenOperationException {
		
		List<String> excludedtablesOnVoided =  utilities.parseToList("users", "provider", "location", "orders", "note");
		
		String dateCreatedCondition = type.equals(EtlOperationType.NEW_RECORDS_DETECTOR) ? "date_created >= ?" : ""; 
		String dateChangedCondition = type.equals(EtlOperationType.CHANGED_RECORDS_DETECTOR) && !tableConf.getTableName().equalsIgnoreCase("obs") ? "date_changed >= ?" : "";
		
		String dateVoidedCondition  = ""; 
		
		if (type.equals(EtlOperationType.CHANGED_RECORDS_DETECTOR) && !tableConf.isMetadata() && !excludedtablesOnVoided.contains(tableConf.getTableName())) {
			dateVoidedCondition = "date_voided >= ?";
		}
		
		String extraCondition = "";
			
		extraCondition = utilities.concatCondition(extraCondition, dateCreatedCondition, "or");
		extraCondition = utilities.concatCondition(extraCondition, dateChangedCondition, "or");
		extraCondition = utilities.concatCondition(extraCondition, dateVoidedCondition, "or");
		
		
		String 	sql =  " SELECT " + function + "("+ tableConf.getPrimaryKey() +") value \n";
				sql += " FROM " + tableConf.getTableName();
				sql += " WHERE 1 = 1 \n";
				sql += " 		AND (" + extraCondition + ")";
						
		Object[] params = {};
		
		if (type.equals(EtlOperationType.NEW_RECORDS_DETECTOR)) params = CommonUtilities.getInstance().addToParams(params.length, params, observationDate);
		if (type.equals(EtlOperationType.CHANGED_RECORDS_DETECTOR) && !dateVoidedCondition.isEmpty()) params = CommonUtilities.getInstance().addToParams(params.length, params, observationDate);
		if (type.equals(EtlOperationType.CHANGED_RECORDS_DETECTOR) && !dateChangedCondition.isEmpty()) params = CommonUtilities.getInstance().addToParams(params.length, params, observationDate);
		
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v != null && v.hasValue() ? v.intValue() : 0;
	}	
}
