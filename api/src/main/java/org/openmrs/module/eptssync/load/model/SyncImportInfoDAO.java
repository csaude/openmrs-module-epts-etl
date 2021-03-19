package org.openmrs.module.eptssync.load.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.synchronization.model.SynchronizationSearchParams;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SyncImportInfoDAO extends BaseDAO {

	public static void insertAll(List<SyncImportInfoVO> records, SyncTableConfiguration tableInfo, Connection conn) throws DBException{
		insertAllBatch(records, tableInfo, conn);
	}
	
	public static void insertAllOneByOne(List<SyncImportInfoVO> records, SyncTableConfiguration tableInfo, Connection conn) throws DBException{
		for (SyncImportInfoVO record : records) {
			try {
				insert(record, tableInfo, conn);
			} catch (DBException e) {
				e.printStackTrace();
			
				 if (e.isDuplicatePrimaryKeyException()) {
					 SyncImportInfoVO recordOnDB = getByOriginIdAndLocation(tableInfo, record.getRecordOriginId(), record.getRecordOriginLocationCode(), conn);
						
					updateByRecordIdAndAppOriginCode(tableInfo, recordOnDB, conn);
				 }
				 else throw e;
			}
		}
	}
	
	public static void insertAllBatch(List<SyncImportInfoVO> records, SyncTableConfiguration tableInfo, Connection conn) throws DBException{
			String sql = "";
					
			sql += "INSERT INTO \n"; 
			sql += "	" + tableInfo.generateFullStageTableName() + "(	record_origin_id,\n";
			sql += "	" + tableInfo.generateFullStageTableName() + "(	record_uuid,\n";
			sql += "											 		json,\n";
			sql += "													record_origin_location_code)\n";
			sql += "VALUES\n";
			
			String values = "";
			
			for (int i = 0; i < records.size(); i++) {
				SyncImportInfoVO record = records.get(i);
				
				if (record.isExcluded()) continue;
					
				values += "(" + record.getRecordOriginId() + ",\""  + record.getRecordUuid() +"\",\"" + utilities.scapeQuotationMarks(record.getJson()) + "\",\"" + record.getRecordOriginLocationCode() +"\"),";
			}
			
			if (utilities.stringHasValue(values)) {
				
				sql += utilities.removeLastChar(values);
				
				try {
					executeBatch(conn, sql);
				} catch (DBException e) {
					//e.printStackTrace();
					
					 if (e.isDuplicatePrimaryKeyException()) {
						 	//Error Pather... Duplicate Entry 'objectId-origin_app' for bla bla 
							/*
						 	String[] s = (e.getLocalizedMessage().split("'")[1]).split("-");
							
							int objectId = Integer.parseInt(s[0]);
							String originAppLocationCode = s[1];
							
							SyncImportInfoVO problematicRecord = new SyncImportInfoVO();
							problematicRecord.setRecordId(objectId);
							problematicRecord.setOriginAppLocationCode(originAppLocationCode);
							
							problematicRecord = utilities.findOnArray(records, problematicRecord);
							problematicRecord.setExcluded(true);
							
							updateByRecordIdAndAppOriginCode(tableInfo, problematicRecord, conn);
							
							insertAll(records, tableInfo, conn);*/
						 
						 insertAllOneByOne(records, tableInfo, conn);
					 }
					 else throw e;
				}
			}
	}
	
	private static void updateByRecordIdAndAppOriginCode(SyncTableConfiguration tableInfo, SyncImportInfoVO record, Connection conn) throws DBException {
		Object[] params = {record.getJson(),
						   record.getRecordOriginId(),	
				   		   record.getRecordOriginLocationCode()};

		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET json = ? ";
		sql += " WHERE 	record_origin_id = ? ";
		sql += " 		AND record_origin_location_code = ? ";
		
		executeQuery(sql, params, conn);
	}
	
	public static void insert(SyncImportInfoVO record, SyncTableConfiguration tableInfo, Connection conn) throws DBException{
		try {
			Object[] params = {record.getRecordOriginId(),
							   record.getRecordUuid(),
							   record.getJson(),
							   record.getMigrationStatus(),
							   record.getLastSyncTryErr(),
							   record.getRecordOriginLocationCode(),
							   record.getLastUpdateDate(),
							   record.getConsistent()};
			
			String sql = "";
			
			sql += "INSERT INTO \n"; 
			sql += "	" + tableInfo.generateFullStageTableName() + "(	record_origin_id,\n";
			sql += "											 		record_uuid,\n";
			sql += "											 		json,\n";
			sql += "											 		migration_status,\n";
			sql += "											 		last_sync_try_err,\n";
			sql += "													record_origin_location_code,";
			sql += "													last_update_date,";
			sql += "													consistent)";
			sql += "	VALUES(?,\n";
			sql += "		   ?,\n";
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
	
	public static void update(SyncImportInfoVO record, SyncTableConfiguration tableInfo, Connection conn) throws DBException{
		Object[] params = {record.getJson(),
						   record.getMigrationStatus(),
						   record.getLastSyncTryErr(),
						   record.getRecordOriginId()};
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName() + "\n";
		sql += " SET	json = ?,\n";
		sql += "		migration_status = ?,\n";
		sql += "		last_sync_try_err = ?\n";
		sql += " WHERE record_origin_id = ? \n";
		
		executeQuery(sql, params, conn);
	}
	
	public static SyncImportInfoVO getByOriginIdAndLocation(SyncTableConfiguration tableConfiguration, int originRecordId, String originAppLocationCode, Connection conn) throws DBException {
		Object[] params = {originRecordId, originAppLocationCode};
		
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + tableConfiguration.generateFullStageTableName() + "\n";
		sql += " WHERE 	record_origin_id = ? \n";
		sql += " 		AND record_origin_location_code = ? \n";
		
		return find(SyncImportInfoVO.class, sql, params, conn);
	}
	
	private static SyncImportInfoVO getGenericSpecificRecord(SynchronizationSearchParams searchParams, String function, Connection conn) throws DBException {
		Object[] params = {};
		
		String extraCondition;
		
		extraCondition = "last_sync_date IS NULL OR last_sync_date < ?";
			
		utilities.setParam(params.length, params, searchParams.getSyncStartDate());
	
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + searchParams.getTableInfo().generateFullStageTableName() + "\n";
		sql += " WHERE 	id = \n";
		sql += " 			 (	SELECT " + function+ "(id)\n";
		sql += "				FROM   " + searchParams.getTableInfo().generateFullStageTableName();
		sql += "				WHERE " +  extraCondition + ")";
		
		return find(SyncImportInfoVO.class, sql, params, conn);		
	}
	
	public static SyncImportInfoVO getFirstRecord(SynchronizationSearchParams searchParams, Connection conn) throws DBException {
		return getGenericSpecificRecord(searchParams, "min", conn);
	}
	
	public static SyncImportInfoVO getLastRecord(SynchronizationSearchParams searchParams, Connection conn) throws DBException {
		return getGenericSpecificRecord(searchParams, "max", conn);
	}
	
	
	/**
	 * For each originAppLocationId retrieve on record from the diven tableName
	 * 
	 * @param tableName
	 * @param conn
	 * @return
	 * @throws DBException
	 */
	public static List<SyncImportInfoVO> getDefaultRecordForEachOriginAppLocatin(String tableName, Connection conn) throws DBException{
		String sql = "";
		sql += " SELECT record_origin_location_code \n";
		sql += " FROM 	" + tableName + "\n";
		sql += " GROUP BY record_origin_location_code";
		
		return  BaseDAO.search(SyncImportInfoVO.class , sql, null, conn);
	}
	
	
	

	public static SyncImportInfoVO retrieveFromOpenMRSObject(SyncTableConfiguration tableInfo, OpenMRSObject object, Connection conn) throws DBException, ForbiddenOperationException {
		Object[] params = {	object.getUuid(), 
							object.getRelatedSyncInfo().getRecordOriginLocationCode()};
		
		String sql = "";
		sql += " SELECT * \n";
		sql += " FROM 	" + tableInfo.generateFullStageTableName() + "\n";
		sql += " WHERE record_uuid = ? ";
		sql += "  	   AND record_origin_location_code = ? ";
		
		SyncImportInfoVO record = BaseDAO.find(SyncImportInfoVO.class , sql, params, conn);
		
		if (record == null) {
			throw new ForbiddenOperationException("This record: "+ tableInfo.getTableName() +"[ uuid="+object.getUuid() + ", origin=" +object.getRelatedSyncInfo().getRecordOriginLocationCode() + " was not found on staging area");
		}
		
		return record;
	}

	public static void markAsFailedToMigrate(SyncImportInfoVO record, SyncTableConfiguration tableInfo, String msg, Connection conn) throws DBException {
		msg = msg.length() <= 250 ? msg : msg.substring(0, 250);
		
		Object[] params = { SyncImportInfoVO.MIGRATION_STATUS_FAILED,
							msg,
							DateAndTimeUtilities.getCurrentDate(),
							record.getId()
							};

			String sql = "";
			
			sql += "UPDATE 	" + tableInfo.generateFullStageTableName() + "\n";
			sql += "SET	   	migration_status = ?, \n";
			sql += "	   	last_sync_try_err = ?, \n";
			sql += "	   	last_sync_date = ? \n";
			sql += "WHERE 	id = ?";
	
			executeQuery(sql, params, conn);
	}

	public static void refreshLastMigrationTrySyncDate(SyncTableConfiguration tableInfo, List<SyncImportInfoVO> syncRecords, Connection conn) throws DBException{
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn), 
						   syncRecords.get(0).getId(),
						   syncRecords.get(syncRecords.size() - 1).getId()
						   };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  id between ? and ? ";
		
		executeQuery(sql, params, conn);
	}
	
	
	public static void markAsToBeCompletedInFuture(SyncTableConfiguration tableInfo, List<SyncImportInfoVO> syncRecords, Connection conn) throws DBException{
		Object[] params = {SyncImportInfoVO.MIGRATION_STATUS_INCOMPLETE,
							DateAndTimeUtilities.getCurrentSystemDate(conn), 
						    syncRecords.get(0).getId(),
						    syncRecords.get(syncRecords.size() - 1).getId()
						   };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET    migration_status = ?, ";
		sql += "		last_sync_date = ? ";
		sql += " WHERE  id between ? and ? ";
		
		executeQuery(sql, params, conn);
	}
	
	
	
	public static void refreshLastMigrationTrySyncDate(SyncTableConfiguration tableInfo, SyncImportInfoVO syncRecord, Connection conn) throws DBException{
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn), 
						   syncRecord.getId()
						   };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  id = ? ";
		
		executeQuery(sql, params, conn);
	}
	
	public static void updateDestinationRecordId(SyncTableConfiguration tableInfo, SyncImportInfoVO syncRecord, SyncRecord destinationRecord, Connection conn) throws DBException{
		Object[] params = {destinationRecord.getObjectId(), 
						   syncRecord.getId()
						   };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET    destination_record_id = ? ";
		sql += " WHERE  id = ? ";
		
		executeQuery(sql, params, conn);
	}
	
	public static void markAsConsistent(SyncTableConfiguration tableInfo, SyncImportInfoVO syncRecord, Connection conn) throws DBException {
		Object[] params = {OpenMRSObject.CONSISTENCE_STATUS, syncRecord.getId()};
		
		String sql = " UPDATE " + tableInfo.generateFullStageTableName() + 
					 " SET    consistent = ? " +
					 " WHERE  id =  ? ";
		
		executeQuery(sql, params, conn);
	}
	
	public static void removeAll(List<SyncImportInfoVO> syncRecords, Connection conn) throws DBException{
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn), 
						   syncRecords.get(0).getId(),
						   syncRecords.get(syncRecords.size() - 1).getId(),
						   SyncImportInfoVO.MIGRATION_STATUS_PENDING,
						   };
		
		String sql = "";
		
		sql += " DELETE FROM " + syncRecords.get(0).generateTableName();
		sql += " WHERE  ID between ? and ? ";
		
		executeQuery(sql, params, conn);
	}
	
	
	public static void remove(SyncImportInfoVO record, SyncTableConfiguration tableInfo, Connection conn) throws DBException {
		Object[] params = { record.getId()
							};

		String sql = "";
		
		sql += "DELETE 	\n";
		sql += "FROM	" + tableInfo.generateFullStageTableName() + "\n";
		sql += "WHERE 	id = ?";
		
		executeQuery(sql, params, conn);
	}

	public static void markAsToBeCompletedInFuture(SyncImportInfoVO record, SyncTableConfiguration tableInfo, Connection conn) throws DBException {
		markAsToBeCompletedInFuture(record, tableInfo, null, conn);
	}
	
	private static void markAsToBeCompletedInFuture(SyncImportInfoVO record, SyncTableConfiguration tableInfo, String msg, Connection conn) throws DBException {
		Object[] params = { SyncImportInfoVO.MIGRATION_STATUS_INCOMPLETE,
							msg == null ? "Migrated BUT still miss some parent info" : msg,
							DateAndTimeUtilities.getCurrentDate(),
							record.getId()
							};

		String sql = "";
		
		sql += "UPDATE 	" + tableInfo.generateFullStageTableName() + "\n";
		sql += "SET	   	migration_status = ?, \n";
		sql += "	   	last_sync_try_err = ?, \n";
		sql += "	   	last_sync_date = ? \n";
		sql += "WHERE 	id = ?";
		
		executeQuery(sql, params, conn);
	}

	public static void updateMigrationStatus(SyncTableConfiguration tableInfo, SyncImportInfoVO record, Connection conn) throws DBException {
		markAsToBeCompletedInFuture(record, tableInfo, record.getLastSyncTryErr(), conn);		
	}

}
