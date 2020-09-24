package org.openmrs.module.eptssync.load.model;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.exceptions.SyncExeption;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SyncImportInfoDAO extends BaseDAO {
	public static void insertAll(List<SyncImportInfoVO> records, SyncTableInfo tableInfo, Connection conn) throws DBException{
			String sql = "";
					
			sql += "INSERT INTO \n"; 
			sql += "	" + tableInfo.generateFullStageTableName() + "(	record_id,\n";
			sql += "											 		json,\n";
			sql += "													origin_app_location_code)\n";
			sql += "VALUES\n";
			
			String values = "";
			
			for (int i = 0; i < records.size(); i++) {
				SyncImportInfoVO record = records.get(i);
				
				if (record.isExcluded()) continue;
					
				values += "(" + record.getRecordId() + ",\"" + utilities.scapeQuotationMarks(record.getJson()) + "\",\"" + record.getOriginAppLocationCode() +"\"),";
			}
			
			if (utilities.stringHasValue(values)) {
				
				sql += utilities.removeLastChar(values);
				
				try {
					executeBatch(conn, sql);
				} catch (DBException e) {
					 if (e.isDuplicatePrimaryKeyException()) {
						 	//Error Pather... Duplicate Entry 'objectId-origin_app' for bla bla 
							String[] s = (e.getLocalizedMessage().split("'")[1]).split("-");
							
							int objectId = Integer.parseInt(s[0]);
							String originAppLocationCode = s[1];
							
							SyncImportInfoVO problematicRecord = new SyncImportInfoVO();
							problematicRecord.setRecordId(objectId);
							problematicRecord.setOriginAppLocationCode(originAppLocationCode);
							
							problematicRecord = utilities.findOnArray(records, problematicRecord);
							problematicRecord.setExcluded(true);
							
							updateByRecordIdAndAppOriginCode(tableInfo, problematicRecord, conn);
							
							insertAll(records, tableInfo, conn);
					 }
					 else throw e;
				}
			}
	}
	
	private static void updateByRecordIdAndAppOriginCode(SyncTableInfo tableInfo, SyncImportInfoVO record, Connection conn) throws DBException {
		Object[] params = {record.getJson(),
						   record.getRecordId(),	
				   		   record.getOriginAppLocationCode()};

			String sql = "";
			
			sql += " UPDATE " + tableInfo.generateFullStageTableName();
			sql += " SET json = ? ";
			sql += " WHERE 	record_id = ? ";
			sql += " 		AND origin_app_location_code = ? ";
			
			executeQuery(sql, params, conn);
	}
	
	public static void insert(SyncImportInfoVO record, SyncTableInfo tableInfo, Connection conn) throws DBException{
		Object[] params = {record.getRecordId(),
						   record.getJson(),
						   record.getMigrationStatus(),
						   record.getLastMigrationTryErr(),
						   record.getOriginAppLocationCode()};
		
		String sql = "";
		
		sql += "INSERT INTO \n"; 
		sql += "	" + tableInfo.generateFullStageTableName() + "(	record_id,\n";
		sql += "											 		json,\n";
		sql += "											 		migration_status,\n";
		sql += "											 		last_migration_try_err,\n";
		sql += "													origin_app_location_code)";
		sql += "	VALUES(?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?,\n";
		sql += "		   ?);";
		
		executeQuery(sql, params, conn);
	}
	
	public static SyncImportInfoVO getFirstRecord(SyncTableInfo tableInfo, Connection conn) throws DBException {
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + tableInfo.generateFullStageTableName() + "\n";
		sql += " WHERE 	id = \n";
		sql += " 			 (	SELECT min(id)\n";
		sql += "				FROM   " + tableInfo.generateFullStageTableName() +  ")";
		
		return find(SyncImportInfoVO.class, sql, null, conn);
	}
	
	public static SyncImportInfoVO getLastRecord(SyncTableInfo tableInfo, Connection conn) throws DBException {
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + tableInfo.generateFullStageTableName() + "\n";
		sql += " WHERE 	id = \n";
		sql += " 			 (	SELECT max(id)\n";
		sql += "				FROM   " + tableInfo.generateFullStageTableName() +  ")";
		
		return find(SyncImportInfoVO.class, sql, null, conn);
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
		sql += " SELECT origin_app_location_code \n";
		sql += " FROM 	" + tableName + "\n";
		sql += " GROUP BY origin_app_location_code";
		
		return  BaseDAO.search(SyncImportInfoVO.class , sql, null, conn);
	}
	
	
	

	public static SyncImportInfoVO retrieveFromOpenMRSObject(SyncTableInfo tableInfo, OpenMRSObject object, Connection conn) throws DBException {
		Object[] params = {	object.getOriginRecordId(), 
							object.getOriginAppLocationCode()};
		
		String sql = "";
		sql += " SELECT * \n";
		sql += " FROM 	" + tableInfo.generateFullStageTableName() + "\n";
		sql += " WHERE record_id = ? ";
		sql += "  	   AND origin_app_location_code = ? ";
		
		return  BaseDAO.find(SyncImportInfoVO.class , sql, params, conn);
	}

	public static void markAsFailedToMigrate(SyncImportInfoVO record, SyncTableInfo tableInfo, SyncExeption e, Connection conn) throws DBException {
		String msg = e.getLocalizedMessage().length() <= 250 ? e.getLocalizedMessage() : e.getLocalizedMessage().substring(0, 250);
		
		Object[] params = { SyncImportInfoVO.MIGRATION_STATUS_FAILED,
							msg,
							DateAndTimeUtilities.getCurrentDate(),
							record.getId()
							};

			String sql = "";
			
			sql += "UPDATE 	" + tableInfo.generateFullStageTableName() + "\n";
			sql += "SET	   	migration_status = ?, \n";
			sql += "	   	last_migration_try_err = ?, \n";
			sql += "	   	last_migration_try_date = ? \n";
			sql += "WHERE 	id = ?";
	
			executeQuery(sql, params, conn);
	}

	public static void refreshLastMigrationTrySync(SyncTableInfo tableInfo, List<SyncImportInfoVO> syncRecords, Connection conn) throws DBException{
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn), 
						   syncRecords.get(0).getId(),
						   syncRecords.get(syncRecords.size() - 1).getId()
						   };
		
		String sql = "";
		
		sql += " UPDATE " + tableInfo.generateFullStageTableName();
		sql += " SET    last_migration_try_date = ? ";
		sql += " WHERE  id between ? and ? ";
		
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
	
	
	public static void remove(SyncImportInfoVO record, SyncTableInfo tableInfo, Connection conn) throws DBException {
		Object[] params = { record.getId()
							};

		String sql = "";
		
		sql += "DELETE 	\n";
		sql += "FROM	" + tableInfo.generateFullStageTableName() + "\n";
		sql += "WHERE 	id = ?";
		
		executeQuery(sql, params, conn);
	}

	public static void markAsToBeCompletedInFuture(SyncImportInfoVO record, SyncTableInfo tableInfo, Connection conn) throws DBException {
		markAsToBeCompletedInFuture(record, tableInfo, null, conn);
	}
	
	private static void markAsToBeCompletedInFuture(SyncImportInfoVO record, SyncTableInfo tableInfo, String msg, Connection conn) throws DBException {
		Object[] params = { SyncImportInfoVO.MIGRATION_STATUS_INCOMPLETE,
							msg == null ? "Migrated BUT still miss some parent info" : msg,
							DateAndTimeUtilities.getCurrentDate(),
							record.getId()
							};

		String sql = "";
		
		sql += "UPDATE 	" + tableInfo.generateFullStageTableName() + "\n";
		sql += "SET	   	migration_status = ?, \n";
		sql += "	   	last_migration_try_err = ?, \n";
		sql += "	   	last_migration_try_date = ? \n";
		sql += "WHERE 	id = ?";
		
		executeQuery(sql, params, conn);
	}

	public static void updateMigrationStatus(SyncTableInfo tableInfo, SyncImportInfoVO record, Connection conn) throws DBException {
		markAsToBeCompletedInFuture(record, tableInfo, record.getLastMigrationTryErr(), conn);		
	}
}
