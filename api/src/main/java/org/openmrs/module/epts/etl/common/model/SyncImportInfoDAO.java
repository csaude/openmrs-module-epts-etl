package org.openmrs.module.epts.etl.common.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class SyncImportInfoDAO extends BaseDAO {
	
	public static void insertAll(List<EtlStageRecordVO> records, TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		insertAllBatch(records, tableInfo, conn);
	}
	
	public static void insertAllOneByOne(List<EtlStageRecordVO> records, TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		for (EtlStageRecordVO record : records) {
			try {
				insert(record, tableInfo, conn);
			}
			catch (DBException e) {
				e.printStackTrace();
				
				if (e.isDuplicatePrimaryOrUniqueKeyException()) {
					EtlStageRecordVO recordOnDB = getByOriginIdAndLocation(tableInfo, record.getRecordOriginId(),
					    record.getRecordOriginLocationCode(), conn);
					
					updateByRecord(tableInfo, recordOnDB, conn);
				} else
					throw e;
			}
		}
	}
	
	public static void insertAllBatch(List<EtlStageRecordVO> records, TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		String sql = "";
		
		sql += "INSERT INTO \n";
		sql += "	" + tableInfo.generateFullStageTableName() + "(	record_origin_id,\n";
		sql += "											 		json,\n";
		sql += "											 		record_date_created,\n";
		sql += "											 		record_date_changed,\n";
		sql += "											 		record_date_voided,\n";
		sql += "											 		consistent,\n";
		sql += "													record_origin_location_code)\n";
		sql += "VALUES\n";
		
		String values = "";
		
		for (int i = 0; i < records.size(); i++) {
			EtlStageRecordVO record = records.get(i);
			
			if (record.isExcluded())
				continue;
			
			String recordOriginCode = utilities.quote(record.getRecordOriginLocationCode());
			String recordDateCreated = record.getDateCreated() != null
			        ? utilities.quote(DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(record.getDateCreated()))
			        : null;
			String recordDateChanged = record.getDateChanged() != null
			        ? utilities.quote(DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(record.getDateChanged()))
			        : null;
			String recordDateVoide = record.getDateVoided() != null
			        ? utilities.quote(DateAndTimeUtilities.formatToYYYYMMDD_HHMISS(record.getDateVoided()))
			        : null;
			
			String json = record.getJson() != null ? utilities.quote(utilities.scapeQuotationMarks(record.getJson())) : null;
			
			values += "(" + record.getRecordOriginId() + "," + json + "," + recordDateCreated + "," + recordDateChanged + ","
			        + recordDateVoide + "," + record.isConsistent() + "," + recordOriginCode + "),";
		}
		
		if (utilities.stringHasValue(values)) {
			
			sql += utilities.removeLastChar(values);
			
			try {
				executeBatch(conn, sql);
			}
			catch (DBException e) {
				//e.printStackTrace();
				
				if (e.isDuplicatePrimaryOrUniqueKeyException()) {
					//Error Pather... Duplicate Entry 'objectId-origin_app' for bla bla 
					/*
					String[] s = (e.getLocalizedMessage().split("'")[1]).split("-");
					
					int objectId = Integer.parseInt(s[0]);
					String originAppLocationCode = s[1];
					
					EtlStageRecordVO problematicRecord = new EtlStageRecordVO();
					problematicRecord.setRecordId(objectId);
					problematicRecord.setOriginAppLocationCode(originAppLocationCode);
					
					problematicRecord = utilities.findOnArray(records, problematicRecord);
					problematicRecord.setExcluded(true);
					
					updateByRecordIdAndAppOriginCode(tableInfo, problematicRecord, conn);
					
					insertAll(records, tableInfo, conn);*/
					
					insertAllOneByOne(records, tableInfo, conn);
				} else
					throw e;
			}
		}
	}
	
	private static void insertSrcUniqueKeyInfo(TableConfiguration tableInfo, EtlStageRecordVO record, Connection conn)
	        throws DBException {
		List<UniqueKeyInfo> allKeys = new ArrayList<>();
		
		allKeys.add(record.getSrcObject().getObjectId());
		
		if (record.getSrcObject().hasUniqueKeys()) {
			allKeys.addAll(record.getSrcObject().getUniqueKeysInfo());
		}
		
		insertUniqueKeyInfo(allKeys, tableInfo, record, conn);
	}
	
	private static void insertDstUniqueKeyInfo(TableConfiguration tableInfo, EtlStageRecordVO record, Connection conn)
	        throws DBException {
		
		for (EtlDatabaseObject obj : record.getDstObject()) {
			
			List<UniqueKeyInfo> allKeys = new ArrayList<>();
			
			allKeys.add(obj.getObjectId());
			
			if (record.getSrcObject().hasUniqueKeys()) {
				allKeys.addAll(record.getSrcObject().getUniqueKeysInfo());
			}
			
			insertUniqueKeyInfo(allKeys, tableInfo, record, conn);
		}
	}
	
	private static void insertUniqueKeyInfo(List<UniqueKeyInfo> allKeys, TableConfiguration tableInfo,
	        EtlStageRecordVO record, Connection conn) throws DBException {
		
		String tableName = tableInfo.generateFullStageSrcUniqueKeysTableName();
		
		for (UniqueKeyInfo uk : allKeys) {
			
			for (Field field : uk.getFields()) {
				Object[] params = { record.getId(), uk.getKeyName(), field.getName(), field.getValue() };
				
				String sql = "";
				
				sql += " INSERT INTO " + tableName + "(record_id, key_name, column_name, key_value)";
				sql += " 						values (?,?,?,?)";
				
				executeQueryWithRetryOnError(sql, params, conn);
			}
		}
	}
	
	private static void updateByRecord(TableConfiguration tableInfo, EtlStageRecordVO record, Connection conn)
	        throws DBException {
		Object[] params = { record.getJson(), record.getRecordOriginId(), record.getRecordOriginLocationCode() };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET json = ?, ";
		sql += "	 last_sync_date = null, ";
		sql += "	 last_sync_try_err = null  ";
		sql += " WHERE 	record_origin_id = ?  ";
		sql += " 		AND record_origin_location_code = ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void insert(EtlStageRecordVO record, TableConfiguration tableInfo, Connection conn) throws DBException {
		try {
			Object[] params = { record.getJson(), record.getMigrationStatus(), record.getLastSyncTryErr(),
			        record.getRecordOriginLocationCode(), record.getLastUpdateDate(), record.getDateCreated(),
			        record.getDateChanged(), record.getDateVoided(), record.getConsistent() };
			
			String sql = "";
			
			sql += "INSERT INTO \n";
			sql += "	" + tableInfo.generateFullStageTableName() + "( json,\n";
			sql += "											 		migration_status,\n";
			sql += "											 		last_sync_try_err,\n";
			sql += "													record_origin_location_code,\n";
			sql += "													last_update_date,\n";
			sql += "											 		record_date_created,\n";
			sql += "											 		record_date_changed,\n";
			sql += "											 		record_date_voided,\n";
			sql += "													consistent)\n";
			sql += "	VALUES(?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?,\n";
			sql += "		   ?);";
			
			List<Long> ids = executeQueryWithRetryOnError(sql, params, conn);
			
			record.setId(ids.get(0).intValue());
			
			insertSrcUniqueKeyInfo(tableInfo, record, conn);
			
			insertDstUniqueKeyInfo(tableInfo, record, conn);
			
		}
		catch (DBException e) {
			if (!e.isDuplicatePrimaryOrUniqueKeyException()) {
				throw e;
			}
		}
	}
	
	public static void update(EtlStageRecordVO record, TableConfiguration tableInfo, Connection conn) throws DBException {
		Object[] params = { record.getJson(), record.getMigrationStatus(), record.getLastSyncTryErr(), record.getId(),
		        record.getRecordOriginLocationCode() };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName() + "\n";
		sql += " SET	json = ?,\n";
		sql += "		migration_status = ?,\n";
		sql += "		last_sync_try_err = ?\n";
		sql += " WHERE 	id = ? \n";
		sql += " 		AND record_origin_location_code = ? \n";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static EtlStageRecordVO getByOriginIdAndLocation(TableConfiguration tableConfiguration, long originRecordId,
	        String originAppLocationCode, Connection conn) throws DBException {
		Object[] params = { originRecordId, originAppLocationCode };
		
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + tableConfiguration.generateFullStageTableName() + "\n";
		sql += " WHERE 	record_origin_id = ? \n";
		sql += " 		AND record_origin_location_code = ? \n";
		
		return find(EtlStageRecordVO.class, sql, params, conn);
	}
	
	public static EtlStageRecordVO getByUuid(TableConfiguration tableConfiguration, String originRecordUuid,
	        String originAppLocationCode, Connection conn) throws DBException {
		Object[] params = { originRecordUuid, originAppLocationCode };
		
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + tableConfiguration.generateFullStageTableName() + "\n";
		sql += " WHERE 	record_uuid = ? \n";
		sql += " 		AND record_origin_location_code = ? \n";
		
		return find(EtlStageRecordVO.class, sql, params, conn);
	}
	
	@SuppressWarnings("unused")
	public static EtlStageRecordVO getWinRecord(TableConfiguration tableConfiguration, String originRecordUuid,
	        Connection conn) throws DBException {
		
		utilities.throwReviewMethodException();
		
		Object[] params = { originRecordUuid, 1 };
		
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + tableConfiguration.generateFullStageTableName() + "\n";
		sql += " WHERE 	record_uuid = ? \n";
		sql += " 		AND consistent = ? \n";
		
		return find(EtlStageRecordVO.class, sql, params, conn);
	}
	
	public static List<EtlStageRecordVO> getAllByUuid(TableConfiguration tableConfiguration, String originRecordUuid,
	        Connection conn) throws DBException {
		Object[] params = { originRecordUuid };
		
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + tableConfiguration.generateFullStageTableName() + "\n";
		sql += " WHERE 	record_uuid = ? \n";
		
		return search(EtlStageRecordVO.class, sql, params, conn);
	}
	
	private static EtlStageRecordVO getGenericSpecificRecord(SyncImportInfoSearchParams searchParams, String function,
	        Connection conn) throws DBException {
		Object[] params = {};
		
		String extraCondition;
		
		extraCondition = "1=1";
		
		if (searchParams.getSyncStartDate() != null) {
			extraCondition = "last_sync_date IS NULL OR last_sync_date < ?";
			params = utilities.addToParams(params.length, params, searchParams.getSyncStartDate());
		}
		
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + searchParams.getConfig().getSrcConf().generateFullStageTableName() + "\n";
		sql += " WHERE 	id = \n";
		sql += " 			 (	SELECT " + function + "(id)\n";
		sql += "				FROM   " + searchParams.getConfig().getSrcConf().generateFullStageTableName();
		sql += "				WHERE " + extraCondition + ")";
		
		return find(EtlStageRecordVO.class, sql, params, conn);
	}
	
	public static EtlStageRecordVO getFirstRecord(SyncImportInfoSearchParams searchParams, Connection conn)
	        throws DBException {
		return getGenericSpecificRecord(searchParams, "min", conn);
	}
	
	public static EtlStageRecordVO getLastRecord(SyncImportInfoSearchParams searchParams, Connection conn)
	        throws DBException {
		return getGenericSpecificRecord(searchParams, "max", conn);
	}
	
	public static EtlStageRecordVO getFirstMissingRecordInDestination(TableConfiguration tableConfiguration, Connection conn)
	        throws DBException {
		return getMissingRecordInDestination(tableConfiguration, "min", conn);
	}
	
	public static EtlStageRecordVO getLastMissingRecordInDestination(TableConfiguration tableConfiguration, Connection conn)
	        throws DBException {
		return getMissingRecordInDestination(tableConfiguration, "max", conn);
	}
	
	private static EtlStageRecordVO getMissingRecordInDestination(TableConfiguration tableConfiguration, String function,
	        Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " src LEFT JOIN " + table + " dest_ on dest_.uuid = src.record_uuid";
		
		if (tableConfiguration.useSharedPKKey()) {
			tablesToSelect = "LEFT JOIN " + tableConfiguration.getSharedTableConf().generateTableNameWithAlias() + " ON "
			        + tableConfiguration.getSharedKeyRefInfo().generateJoinCondition();
		}
		
		sql += " SELECT * \n";
		sql += " FROM  	" + stageTable + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND id = ";
		sql += " 			 (	SELECT " + function + "(id)\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE " + tableConfiguration.getPrimaryKey() + " IS NULL\n)";
		
		return find(EtlStageRecordVO.class, sql, params, conn);
	}
	
	public static EtlStageRecordVO getFirstRecordInDestination(TableConfiguration tableConfiguration, String appOriginCode,
	        Connection conn) throws DBException {
		return getRecordInDestination(tableConfiguration, "min", appOriginCode, conn);
	}
	
	public static EtlStageRecordVO getLastRecordInDestination(TableConfiguration tableConfiguration, String appOriginCode,
	        Connection conn) throws DBException {
		return getRecordInDestination(tableConfiguration, "max", appOriginCode, conn);
	}
	
	private static EtlStageRecordVO getRecordInDestination(TableConfiguration tableConfiguration, String function,
	        String appOriginCode, Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " src INNER JOIN " + table + " dest_ on dest_.uuid = src.record_uuid";
		
		if (tableConfiguration.useSharedPKKey()) {
			tablesToSelect = "LEFT JOIN " + tableConfiguration.getSharedTableConf().generateTableNameWithAlias() + " ON "
			        + tableConfiguration.getSharedKeyRefInfo().generateJoinCondition();
		}
		
		sql += " SELECT * \n";
		sql += " FROM  	" + stageTable + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND id = ";
		sql += " 			 (	SELECT " + function + "(id)\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE " + tableConfiguration.getPrimaryKey() + " IS NULL\n)";
		
		return find(EtlStageRecordVO.class, sql, params, conn);
	}
	
	/**
	 * For each originAppLocationId retrieve on dstRecord from the diven tableName
	 * 
	 * @param tableName
	 * @param conn
	 * @return
	 * @throws DBException
	 */
	public static List<EtlStageRecordVO> getDefaultRecordForEachOriginAppLocatin(String tableName, Connection conn)
	        throws DBException {
		String sql = "";
		sql += " SELECT record_origin_location_code \n";
		sql += " FROM 	" + tableName + "\n";
		sql += " GROUP BY record_origin_location_code";
		
		return BaseDAO.search(EtlStageRecordVO.class, sql, null, conn);
	}
	
	public static EtlStageRecordVO retrieveFromOpenMRSObject(TableConfiguration tableInfo, EtlDatabaseObject object,
	        String recordOriginLocationCode, Connection conn) throws DBException, ForbiddenOperationException {
		Object[] params = { object.getUuid(), recordOriginLocationCode };
		
		String sql = "";
		sql += " SELECT * \n";
		sql += " FROM 	" + tableInfo.generateFullStageTableName() + "\n";
		sql += " WHERE record_uuid = ? ";
		sql += "  	   AND record_origin_location_code = ? ";
		
		EtlStageRecordVO record = BaseDAO.find(EtlStageRecordVO.class, sql, params, conn);
		
		if (record == null) {
			throw new ForbiddenOperationException("This dstRecord: " + tableInfo.getTableName() + "[ uuid="
			        + object.getUuid() + ", origin=" + recordOriginLocationCode + " was not found on staging area");
		}
		
		return record;
	}
	
	public static void markAsFailedToMigrate(EtlStageRecordVO record, TableConfiguration tableInfo, String msg,
	        Connection conn) throws DBException {
		msg = msg.length() <= 250 ? msg : msg.substring(0, 250);
		
		Object[] params = { EtlStageRecordVO.MIGRATION_STATUS_FAILED, msg, DateAndTimeUtilities.getCurrentDate(),
		        record.getId() };
		
		String sql = "";
		
		sql += "UPDATE 	" + tableInfo.generateFullStageTableName() + "\n";
		sql += "SET	   	migration_status = ?, \n";
		sql += "	   	last_sync_try_err = ?, \n";
		sql += "	   	last_sync_date = ? \n";
		sql += "WHERE 	id = ?";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void refreshLastMigrationTrySyncDate(TableConfiguration tableInfo, List<EtlStageRecordVO> syncRecords,
	        Connection conn) throws DBException {
		Object[] params = { DateAndTimeUtilities.getCurrentSystemDate(conn), syncRecords.get(0).getId(),
		        syncRecords.get(syncRecords.size() - 1).getId() };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  id between ? and ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void markAsToBeCompletedInFuture(TableConfiguration tableInfo, List<EtlStageRecordVO> syncRecords,
	        Connection conn) throws DBException {
		Object[] params = { EtlStageRecordVO.MIGRATION_STATUS_INCOMPLETE, DateAndTimeUtilities.getCurrentSystemDate(conn),
		        syncRecords.get(0).getId(), syncRecords.get(syncRecords.size() - 1).getId() };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET    migration_status = ?, ";
		sql += "		last_sync_date = ? ";
		sql += " WHERE  id between ? and ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void refreshLastMigrationTrySyncDate(TableConfiguration tableInfo, EtlStageRecordVO syncRecord,
	        Connection conn) throws DBException {
		Object[] params = { DateAndTimeUtilities.getCurrentSystemDate(conn), syncRecord.getId() };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  id = ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void markAsConsistent(TableConfiguration tableInfo, EtlStageRecordVO syncRecord, Connection conn)
	        throws DBException {
		changeConsistenceStatus(tableInfo, syncRecord, true, conn);
	}
	
	public static void markAsInconsistent(TableConfiguration tableInfo, EtlStageRecordVO syncRecord, Connection conn)
	        throws DBException {
		changeConsistenceStatus(tableInfo, syncRecord, false, conn);
	}
	
	public static void changeConsistenceStatus(TableConfiguration tableInfo, EtlStageRecordVO syncRecord, boolean consistent,
	        Connection conn) throws DBException {
		Object[] params = { consistent, syncRecord.getId() };
		
		String sql = " UPDATE " + tableInfo.generateFullStageTableName() + " SET    consistent = ? " + " WHERE  id =  ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void removeAll(List<EtlStageRecordVO> syncRecords, Connection conn) throws DBException {
		Object[] params = { DateAndTimeUtilities.getCurrentSystemDate(conn), syncRecords.get(0).getId(),
		        syncRecords.get(syncRecords.size() - 1).getId(), EtlStageRecordVO.MIGRATION_STATUS_PENDING, };
		
		String sql = "";
		
		sql += " DELETE FROM " + syncRecords.get(0).generateTableName();
		sql += " WHERE  ID between ? and ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void remove(EtlStageRecordVO record, TableConfiguration tableInfo, Connection conn) throws DBException {
		Object[] params = { record.getId() };
		
		String sql = "";
		
		sql += "DELETE 	\n";
		sql += "FROM	" + tableInfo.generateFullStageTableName() + "\n";
		sql += "WHERE 	id = ?";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void markAsToBeCompletedInFuture(EtlStageRecordVO record, TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		markAsToBeCompletedInFuture(record, tableInfo, null, conn);
	}
	
	private static void markAsToBeCompletedInFuture(EtlStageRecordVO record, TableConfiguration tableInfo, String msg,
	        Connection conn) throws DBException {
		Object[] params = { EtlStageRecordVO.MIGRATION_STATUS_INCOMPLETE,
		        msg == null ? "Migrated BUT still miss some parent info" : msg, DateAndTimeUtilities.getCurrentDate(),
		        record.getId() };
		
		String sql = "";
		
		sql += "UPDATE 	" + tableInfo.generateFullStageTableName() + "\n";
		sql += "SET	   	migration_status = ?, \n";
		sql += "	   	last_sync_try_err = ?, \n";
		sql += "	   	last_sync_date = ? \n";
		sql += "WHERE 	id = ?";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void updateMigrationStatus(TableConfiguration tableInfo, EtlStageRecordVO record, Connection conn)
	        throws DBException {
		markAsToBeCompletedInFuture(record, tableInfo, record.getLastSyncTryErr(), conn);
	}
}
