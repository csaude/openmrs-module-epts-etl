package org.openmrs.module.eptssync.model.pojo.generic;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class OpenMRSObjectDAO extends BaseDAO {
	public static void refreshLastSyncDate(OpenMRSObject syncRecord, Connection conn) throws DBException{
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn), 
						   syncRecord.getObjectId()};
		
		String sql = "";
		
		sql += " UPDATE " + syncRecord.generateTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  " + syncRecord.generateDBPrimaryKeyAtt() + " = ? ";
		
		
		executeQuery(sql, params, conn);
	}
	
	public static void refreshLastSyncDate(List<OpenMRSObject> syncRecords, Connection conn) throws DBException{
		Object[] params = {DateAndTimeUtilities.getCurrentSystemDate(conn), 
						   syncRecords.get(0).getObjectId(),
						   syncRecords.get(syncRecords.size() - 1).getObjectId()
						   };
		
		String sql = "";
		
		sql += " UPDATE " + syncRecords.get(0).generateTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  " + syncRecords.get(0).generateDBPrimaryKeyAtt() + " between ? and ? ";
		
		executeQuery(sql, params, conn);
	}
	
	public static void insert(OpenMRSObject record, Connection conn) throws DBException{
		Object[] params = record.getInsertParamsWithoutObjectId();
		String sql = record.getInsertSQLWithoutObjectId();
		
		executeQuery(sql, params, conn);
	}
	
	public static void insertWithObjectId(OpenMRSObject record, Connection conn) throws DBException{
		Object[] params = record.getInsertParamsWithObjectId();
		String sql = record.getInsertSQLWithObjectId();
		
		executeQuery(sql, params, conn);
	}
	
	public static void update(OpenMRSObject record, Connection conn) throws DBException {
		Object[] params = record.getUpdateParams();
		String sql = record.getUpdateSQL();

		executeQuery(sql, params, conn);
	}
	
	static Logger logger = Logger.getLogger(OpenMRSObjectDAO.class);
	
	public static <T extends OpenMRSObject> T thinGetByRecordOrigin(int recordOriginId, String recordOriginLocationCode, Class<T> openMRSClass, SyncTableConfiguration tableInfo, Connection conn) throws DBException{
		T instance = null;
		
		try {
			instance = openMRSClass.newInstance();
			
			Object[] params = {recordOriginId, recordOriginLocationCode};
			
			String sql = "";
			
			sql += " SELECT " + instance.generateTableName() + ".* \n";
			sql += " FROM  	" + instance.generateTableName() + " INNER JOIN " + tableInfo.generateFullStageTableName() + " ON destination_record_id = " + tableInfo.getPrimaryKey() + "\n";
			sql += " WHERE 	record_origin_id = ? \n";
			sql += "		AND origin_app_location_code = ?;\n";
			
			return find(openMRSClass, sql, params, conn);
		} catch (Exception e) {
			logger.info("Error trying do retrieve record on table " + instance.generateTableName()  + "["+e.getMessage() + "]");
			
			TimeCountDown.sleep(2000);
		
			throw new RuntimeException("Error trying do retrieve record on table " + instance.generateTableName()  + "["+e.getMessage() + "]");
			
		}
	}
	
	
	public static <T extends OpenMRSObject> T thinGetByUuid(Class<T> openMRSClass, String uuid, Connection conn) throws DBException{
		try {
			Object[] params = {uuid};
			
			String sql = "";
			
			sql += " SELECT * \n";
			sql += " FROM  	" + openMRSClass.newInstance().generateTableName() + "\n";
			sql += " WHERE 	uuid = ?;";
			
			return find(openMRSClass, sql, params, conn);
		} catch (InstantiationException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
	}

	public static <T extends OpenMRSObject> T getById(Class<T> openMRSClass, int id, Connection conn) throws DBException{
		try {
			T obj = openMRSClass.newInstance();
			
			Object[] params = {id};
			
			String sql = "";
			
			sql += " SELECT * \n";
			sql += " FROM  	" + obj.generateTableName() + "\n";
			sql += " WHERE 	" + obj.generateDBPrimaryKeyAtt() + " = ?;";
			
			return find(openMRSClass, sql, params, conn);
		} catch (InstantiationException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
	}
	
	public static List<OpenMRSObject> getByParentId(SyncTableConfiguration tableConfiguration, String parentField, int parentId, Connection conn) throws DBException {
		Object[] params = {parentField};
		
		String sql = " SELECT " + tableConfiguration.getPrimaryKey() + " as object_id "+
					 " FROM     " + tableConfiguration.getTableName() +
					 " WHERE 	" + parentField + " = ?";
		
		return utilities.parseList(search(GenericOpenMRSObject.class, sql, params, conn), OpenMRSObject.class);
	}
	
	public static GenericOpenMRSObject getById(String tableName, String pkColumnName, int id, Connection conn) throws DBException{
		Object[] params = {id};
		
		String sql = "";
		
		sql += " SELECT " + pkColumnName + " as object_id";
		sql += " FROM  	" +  tableName + "\n";
		sql += " WHERE 	" + pkColumnName + " = ?;";
		
		return find(GenericOpenMRSObject.class, sql, params, conn);		
	}

	public static OpenMRSObject getFirstRecord(SyncTableConfiguration tableInfo, String originAppLocationCode, Connection conn) throws DBException {
		String sql = "";
		
		OpenMRSObject obj = utilities.createInstance(tableInfo.getSyncRecordClass());
		
		String clause = "";
		
		if (!tableInfo.isFirstExport()) {
			clause = "date_changed > last_sync_date";
		}
		else {
			clause = "last_sync_date is null";
		}
		
		if (utilities.stringHasValue(originAppLocationCode)) {
			clause = utilities.concatCondition(clause, "origin_app_location_code = '" + originAppLocationCode + "'");
		}

		sql += " SELECT * \n";
		sql += " FROM  	" + obj.generateTableName() + "\n";
		sql += " WHERE 	" + obj.generateDBPrimaryKeyAtt() + "\n";
		sql += " 			= (	SELECT min(" + obj.generateDBPrimaryKeyAtt() + ")\n";
		sql += "				FROM   " + obj.generateTableName() + "\n";
		sql += "				WHERE  " + clause + "\n";
		sql += "				)";
		
		return find(tableInfo.getSyncRecordClass(), sql, null, conn);
	}
	
	public static OpenMRSObject getLastRecord(SyncTableConfiguration tableInfo, String originAppLocationCode, Connection conn) throws DBException {
		String sql = "";
		
		OpenMRSObject obj = utilities.createInstance(tableInfo.getSyncRecordClass());
		
		String clause = "";
		
		if (!tableInfo.isFirstExport()) {
			clause = "date_changed > last_sync_date";
		}
		else {
			clause = "last_sync_date is null";
		}

		if (utilities.stringHasValue(originAppLocationCode)) {
			clause = utilities.concatCondition(clause, "origin_app_location_code = '" + originAppLocationCode + "'");
		}
		
		sql += " SELECT * \n";
		sql += " FROM  	" + obj.generateTableName() + "\n";
		sql += " WHERE 	" + obj.generateDBPrimaryKeyAtt() + "\n";
		sql += " 			= (	SELECT max(" + obj.generateDBPrimaryKeyAtt() + ")\n";
		sql += "				FROM   " + obj.generateTableName() + "\n";
		sql += "				WHERE  " + clause + "\n";
		sql += "				)";
		
		return find(tableInfo.getSyncRecordClass(), sql, null, conn);
	}

	public static void remove(OpenMRSObject record, Connection conn) throws DBException{
		Object[] params = {record.getObjectId()};
		
		String sql = " DELETE" +
					 " FROM " + record.generateTableName() +
					 " WHERE  " + record.generateDBPrimaryKeyAtt() + " =  ? ";
		
		executeQuery(sql, params, conn);
	}
	
	public static int countAllOfOriginParentId(String parentField, int parentOriginId, String appOriginCode, SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		
		Object[] params = { parentOriginId, 
							appOriginCode};

		String 	sql =	" SELECT count(*) value";
		  		sql +=	" FROM  	" + tableConfiguration.getTableName() + " INNER JOIN " + tableConfiguration.generateFullStageTableName() + " ON destination_record_id = " + tableConfiguration.getPrimaryKey() + "\n";
		  		sql +=	" WHERE 	" + parentField + " = ? ";
				sql +=  "			AND origin_app_location_code = ? ";
	
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v.intValue();
	}	
	
	
	public static int countAllOfParentId(Class<OpenMRSObject> clazz, String parentField, int parentId, Connection conn) throws DBException {
		Object[] params = {parentId};

		OpenMRSObject obj = utilities.createInstance(clazz);
		
		String sql = " SELECT count(*) value" +
					 " FROM     " + obj.generateTableName() +
					 " WHERE 	" + parentField + " = ? " ;
		
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v.intValue();
	}	
	
	public static List<OpenMRSObject> getByOriginParentId(String parentField, int parentOriginId, String appOriginCode, SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		//OpenMRSObject parentOnDest = thinGetByOriginRecordId(openMRSClass, relatedSyncInfo, tableInfo, conn);
		
		Object[] params = {parentOriginId, 
						   appOriginCode};
		
		String 	sql = 	" SELECT * ";
				sql +=	" FROM  	" + tableConfiguration.getTableName() + " INNER JOIN " + tableConfiguration.generateFullStageTableName() + " ON destination_record_id = " + tableConfiguration.getPrimaryKey() + "\n";
  				sql +=	" WHERE 	" + parentField + " = ? ";
				sql +=	"			AND origin_app_location_code = ? ";
		
		return search(tableConfiguration.getSyncRecordClass(), sql, params, conn);
	}
	
	
	public static List<OpenMRSObject> getByParentId(Class<OpenMRSObject> clazz, String parentField, int parentId, Connection conn) throws DBException {
		Object[] params = {parentField};
		
		OpenMRSObject obj = utilities.createInstance(clazz);
		
		String sql = " SELECT * " +
					 " FROM     " + obj.generateTableName() +
					 " WHERE 	" + parentField + " = ?";
		
		return search(clazz, sql, params, conn);
	}
	
	private static void insertAllMetadata(List<OpenMRSObject> records, SyncTableConfiguration tableInfo, Connection conn) throws DBException {
		if (!tableInfo.isMetadata()) throw new ForbiddenOperationException("You tried to insert " + tableInfo.getTableName() + " as metadata but it is not a metadata!!!");
		
		for (OpenMRSObject record : records) {
			record.save(tableInfo, conn);
		}
	}

	public static void insertAll(List<OpenMRSObject> objects, SyncTableConfiguration syncTableConfiguration, Connection conn) throws DBException {
		boolean isInMetadata = utilities.isStringIn(syncTableConfiguration.getTableName(), "location", "concept_datatype", "concept", "person_attribute_type", "provider_attribute_type", "program", "program_workflow", "program_workflow_state", "encounter_type", "visit_type", "relationship_type", "patient_identifier_type");
		
		if (syncTableConfiguration.isMetadata() && !isInMetadata) {
			throw new ForbiddenOperationException("The table " + syncTableConfiguration.getTableName() + " is been treated as metadata but it is not");
		}
		
		if (syncTableConfiguration.isMetadata()) {
			insertAllMetadata(objects, syncTableConfiguration, conn);
		}
		else {
			insertAllData(objects, syncTableConfiguration, conn);
		}
	}
	
	private static void insertAllData(List<OpenMRSObject> objects, SyncTableConfiguration syncTableConfiguration, Connection conn) throws DBException {
		String sql = "";
		sql += objects.get(0).getInsertSQLWithoutObjectId().split("VALUES")[0];
		sql += " VALUES";
		
		String values = "";
		
		for (int i=0; i < objects.size(); i++) {
			if (objects.get(i).isExcluded()) continue;
			
			values += "(" + objects.get(i).generateInsertValues() + "),";
		}
		
		if (utilities.stringHasValue(values)) {
			sql += utilities.removeLastChar(values);
		
			try {
				executeBatch(conn, sql);
			} catch (DBException e) {
				insertAllDataOneByOne(objects, syncTableConfiguration, conn);
			}
		}
	}
	
	private static void insertAllDataOneByOne(List<OpenMRSObject> objects, SyncTableConfiguration syncTableConfiguration, Connection conn) throws DBException {
		for (OpenMRSObject record : objects) {
			try {
				insert(record, conn);
			} catch (DBException e) {
				if (e.isDuplicatePrimaryKeyException()) {
					OpenMRSObject problematicRecordOnDB = retrieveProblematicObjectFromExceptionInfo(syncTableConfiguration, e, conn);
					
					if (problematicRecordOnDB.getObjectId() == record.getObjectId()) {
						//update(problematicRecordOnDB, conn);
						continue;
					}
				}
				
				SyncImportInfoVO source = record.retrieveRelatedSyncInfo(syncTableConfiguration, conn);
				source.markAsSyncFailedToMigrate(syncTableConfiguration, e.getLocalizedMessage(), conn);
			}
		}
	}

	private static OpenMRSObject retrieveProblematicObjectFromExceptionInfo(SyncTableConfiguration tableConfiguration, DBException e, Connection conn) throws DBException {
	 	//UUID duplication Error Pathern... Duplicate Entry 'objectId-origin_app' for bla bla 
		String s = e.getLocalizedMessage().split("'")[1];
		
		//Check if is uuid duplication
		if (utilities.isValidUUID(s)) {
			return thinGetByUuid(tableConfiguration.getSyncRecordClass(), s, conn);
		}	
		else {
		 	//ORIGIN duplication Error Pathern... Duplicate Entry 'objectId-origin_app' for bla bla 
			String[] idParts = (e.getLocalizedMessage().split("'")[1]).split("-");
			
			int objectId = Integer.parseInt(idParts[0]);
			String originAppLocationCode = idParts[1];
			
			
			return thinGetByRecordOrigin(objectId, originAppLocationCode, tableConfiguration.getSyncRecordClass(), tableConfiguration, conn);
		}
	}
	
	public static int getAvaliableObjectId(SyncTableConfiguration syncTableInfo, int maxAcceptableId, Connection conn) throws DBException {
		if (maxAcceptableId <= 0) throw new ForbiddenOperationException("There was not find any avaliable id for " + syncTableInfo.getTableName());
		
		String sql = "";  
		
		sql += " SELECT max(" + syncTableInfo.getPrimaryKey() + ") object_id \n";
		sql += " FROM  	" + syncTableInfo.getTableName() + ";\n";
		 
		OpenMRSObject maxObj = find(GenericOpenMRSObject.class, sql, null, conn);
		
		if (maxObj != null) {
			if (maxObj.getObjectId() < maxAcceptableId) {
				return maxAcceptableId;
			}
			else {
				if (getById(syncTableInfo.getSyncRecordClass(), maxAcceptableId - 1, conn) == null) {
					return maxAcceptableId - 1;
				}
				else return getAvaliableObjectId(syncTableInfo, maxAcceptableId - 1, conn);
			}
		}
		else{
			return maxAcceptableId;
		}
	}
	
}
