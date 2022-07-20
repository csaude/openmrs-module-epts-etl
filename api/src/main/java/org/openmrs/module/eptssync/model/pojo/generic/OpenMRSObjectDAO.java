package org.openmrs.module.eptssync.model.pojo.generic;

import java.sql.Connection;
import java.util.List;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class OpenMRSObjectDAO extends BaseDAO {
	
	private static void refreshLastSyncDate(OpenMRSObject syncRecord, SyncTableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException{
		Object[] params = {	DateAndTimeUtilities.getCurrentSystemDate(conn), 
							recordOriginLocationCode,
							syncRecord.getObjectId()};

		String originDestin = tableConfiguration.isDestinationInstallationType() ? "record_destination_id" : "record_origin_id";
		
		String sql = "";
		
		sql += " UPDATE " + tableConfiguration.generateFullStageTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  record_origin_location_code = ? ";
		sql += "		AND " + originDestin + " = ? ";
		
		executeQuery(sql, params, conn);
	}
	
	public static void refreshLastSyncDateOnDestination(OpenMRSObject syncRecord, SyncTableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException{
		refreshLastSyncDate(syncRecord, tableConfiguration, recordOriginLocationCode, conn);
	}
	
	public static void refreshLastSyncDateOnOrigin(OpenMRSObject syncRecord, SyncTableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException{
		refreshLastSyncDate(syncRecord, tableConfiguration, recordOriginLocationCode, conn);
	}
	
	private static void refreshLastSyncDate(List<OpenMRSObject> syncRecords, SyncTableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException{
		Object[] params = {	DateAndTimeUtilities.getCurrentSystemDate(conn), 
							recordOriginLocationCode,
							syncRecords.get(0).getObjectId(),
							syncRecords.get(syncRecords.size() - 1).getObjectId()};
		
		String originDestin = tableConfiguration.isDestinationInstallationType() ? "record_destination_id" : "record_origin_id";
		
		String sql = "";
		
		sql += " UPDATE " + tableConfiguration.generateFullStageTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  record_origin_location_code = ? ";
		sql += "		AND " + originDestin + " between ? and ? ";
		
		executeQuery(sql, params, conn);
	}
	
	public static void refreshLastSyncDateOnDestination(List<OpenMRSObject> syncRecords, SyncTableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException{
		refreshLastSyncDate(syncRecords, tableConfiguration, recordOriginLocationCode, conn);
	}
	
	public static void refreshLastSyncDateOnOrigin(List<OpenMRSObject> syncRecords, SyncTableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException{
		refreshLastSyncDate(syncRecords, tableConfiguration, recordOriginLocationCode, conn);
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
	
	public static OpenMRSObject thinGetByRecordOrigin(int recordOriginId, String recordOriginLocationCode, SyncTableConfiguration parentTableConfiguration, Connection conn) throws DBException{
		
		try {
			Object[] params = {recordOriginId, recordOriginLocationCode};
			
			String tableName = parentTableConfiguration.getTableName();
			
			String clauseFromStarting = tableName;
			
			if (tableName.equals("patient")) {
				clauseFromStarting += " INNER JOIN person on person_id = patient_id \n";
			}
			
			String sql = "";
			
			
			sql += " SELECT " + tableName + ".*" + (tableName.equals("patient") ? ", uuid" : "") + "\n";
			sql += " FROM  	" + clauseFromStarting + " INNER JOIN " + parentTableConfiguration.generateFullStageTableName() + " ON record_uuid = uuid\n";
			sql += " WHERE 	record_origin_id = ? and record_origin_location_code = ? ";
			
			return find(parentTableConfiguration.getSyncRecordClass(parentTableConfiguration.getMainApp()), sql, params, conn);
		} catch (Exception e) {
			logger.info("Error trying do retrieve record on table " + parentTableConfiguration.getTableName()  + "["+e.getMessage() + "]");
			
			TimeCountDown.sleep(2000);
		
			throw new RuntimeException("Error trying do retrieve record on table " + parentTableConfiguration.getTableName()   + "["+e.getMessage() + "]");
		}
	}
	
	
	public static <T extends OpenMRSObject> T getByUuid(Class<T> openMRSClass, String uuid, Connection conn) throws DBException{
		try {
			Object[] params = {uuid};
			
			T obj = openMRSClass.newInstance();
			
			String sql = "";
			
			sql += " SELECT " + obj.generateTableName() + ".*" + (obj.generateTableName().equals("patient") ? ", uuid" : "") + "\n";
			sql += " FROM     " + obj.generateTableName() + (obj.generateTableName().equals("patient") ? " inner join person on person_id = patient_id " : "") + "\n";
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
			
			sql += " SELECT " + obj.generateTableName() + ".* " + (obj.generateTableName().equals("patient") ? ", person.uuid" : "") +  "\n";
			sql += " FROM  	" + obj.generateTableName() + (obj.generateTableName().equals("patient") ? " inner join person on person_id = patient_id" : "") + "\n";
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
	
	public static <T extends OpenMRSObject> T getByIdOnSpecificSchema(Class<T> openMRSClass, Integer objectId, String schema, Connection conn) throws DBException{
		try {
			T obj = openMRSClass.newInstance();
			
			Object[] params = {objectId};
			
			
			String tableName = obj.generateTableName();
			
			String sql = "";
			
			sql += " SELECT " + tableName + ".*" + (tableName.equals("patient") ? ", uuid" : "") + "\n";
			sql += " FROM  	" + schema + "." + tableName +  (tableName.equals("patient") ? " left join person on person_id = patient_id" : "") + "\n";
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
	
	public static OpenMRSObject getDefaultRecord(SyncTableConfiguration tableConfiguration,  Connection conn) throws DBException{
		Object[] params = {};
		
		String sql = "";
		
		sql += " SELECT *\n";
		sql += " FROM  	" +  tableConfiguration.getTableName() + "\n";
		sql += " LIMIT 0, 1";
		
		return find(tableConfiguration.getSyncRecordClass(tableConfiguration.getMainApp()), sql, params, conn);		
	}
	
	
	public static GenericOpenMRSObject getById(String tableName, String pkColumnName, int id, Connection conn) throws DBException{
		Object[] params = {id};
		
		String sql = "";
		
		sql += " SELECT " + pkColumnName + " as object_id";
		sql += " FROM  	" +  tableName + "\n";
		sql += " WHERE 	" + pkColumnName + " = ?;";
		
		return find(GenericOpenMRSObject.class, sql, params, conn);		
	}
	
	public static OpenMRSObject getFirstConsistentRecordInOrigin(SyncTableConfiguration tableInfo, Connection conn) throws DBException {
		return getConsistentRecordInOrigin(tableInfo, "min", conn);
	}
	
	public static OpenMRSObject getLastConsistentRecordOnOrigin(SyncTableConfiguration tableInfo,  Connection conn) throws DBException {
		return getConsistentRecordInOrigin(tableInfo, "max", conn);
	}
	
	
	private static GenericOpenMRSObject getConsistentRecordInOrigin(SyncTableConfiguration tableConfiguration, String function, Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " stage_ INNER JOIN " + table + " src_ on src_.uuid = stage_.record_uuid";
		
		if (table.equalsIgnoreCase("patient")) {
			tablesToSelect = stageTable + " src_ INNER JOIN person on person.uuid = src_.record_uuid INNER JOIN patient dest_ ON patient_id = person_id ";
		}

		sql += " SELECT " + tableConfiguration.getPrimaryKey() + " object_id \n";
		sql += " FROM  	" + table + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND " + tableConfiguration.getPrimaryKey() + " = ";
		sql += " 			 (	SELECT " + function+ "(" + tableConfiguration.getPrimaryKey()   + ")\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE consistent = 1;\n";
		
		return find(GenericOpenMRSObject.class, sql, params, conn);		
	}	
	
	public static OpenMRSObject getFirstNeverProcessedRecordOnOrigin(SyncTableConfiguration tableInfo, Connection conn) throws DBException {
		return getExtremeNeverProcessedRecordOnOrigin(tableInfo, "min", conn);
	}
	
	public static OpenMRSObject getLastNeverProcessedRecordOnOrigin(SyncTableConfiguration tableInfo, Connection conn) throws DBException {
		return getExtremeNeverProcessedRecordOnOrigin(tableInfo, "max", conn);
	}

	private static OpenMRSObject getExtremeNeverProcessedRecordOnOrigin(SyncTableConfiguration tableInfo, String function, Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		sql += " SELECT * \n";
		sql += " FROM  	" + tableInfo.getTableName() + "\n";
		sql += " WHERE 	" + tableInfo.getPrimaryKey() + "	=	 (	SELECT " + function + "(" + tableInfo.getPrimaryKey() + ")\n";
		sql += "													FROM   " + tableInfo.getTableName() + " \n";
		sql += "													WHERE  NOT EXISTS ( SELECT * \n";
		sql += "																		FROM " + tableInfo.generateFullStageTableName() + "\n";
		sql += "																		WHERE record_origin_id = " + tableInfo.getPrimaryKey() + "\n)";
		sql += "												   )";
		
		return find(tableInfo.getSyncRecordClass(tableInfo.getMainApp()), sql, params, conn);
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
		  		sql +=	" FROM  	" + tableConfiguration.getTableName() + " INNER JOIN " + tableConfiguration.generateFullStageTableName() + " ON record_destination_id = " + tableConfiguration.getPrimaryKey() + "\n";
		  		sql +=	" WHERE 	" + parentField + " = ? ";
				sql +=  "			AND record_origin_location_code = ? ";
	
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
		Object[] params = {parentOriginId, 
						   appOriginCode};
		
		String 	sql = 	" SELECT * ";
				sql +=	" FROM  	" + tableConfiguration.getTableName() + " INNER JOIN " + tableConfiguration.generateFullStageTableName() + " ON record_uuid = uuid\n";
  				sql +=	" WHERE 	" + parentField + " = ? ";
				sql +=	"			AND record_origin_location_code = ? ";
		
		return search(tableConfiguration.getSyncRecordClass(tableConfiguration.getMainApp()), sql, params, conn);
	}
	
	public static List<OpenMRSObject> getByParentId(Class<OpenMRSObject> clazz, String parentField, int parentId, Connection conn) throws DBException {
		Object[] params = {parentId};
		
		OpenMRSObject obj = utilities.createInstance(clazz);
		
		String sql = " SELECT " + obj.generateTableName() + ".*" + (obj.generateTableName().equals("patient") ? ", uuid" : "") +
					 " FROM     " + obj.generateTableName() + (obj.generateTableName().equals("patient") ? " inner join person on person_id = patient_id " : "") +
					 " WHERE 	" + parentField + " = ?";
		
		return search(clazz, sql, params, conn);
	}
	
	private static void insertAllMetadata(List<OpenMRSObject> records, SyncTableConfiguration tableInfo, Connection conn) throws DBException {
		if (!tableInfo.isMetadata()) throw new ForbiddenOperationException("You tried to insert " + tableInfo.getTableName() + " as metadata but it is not a metadata!!!");
		
		for (OpenMRSObject record : records) {
			record.save(tableInfo, conn);
		}
	}

	public static void insertAll(List<OpenMRSObject> objects, SyncTableConfiguration syncTableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException {
		boolean isInMetadata = utilities.isStringIn(syncTableConfiguration.getTableName(), "location", "concept_datatype", "concept", "person_attribute_type", "provider_attribute_type", "program", "program_workflow", "program_workflow_state", "encounter_type", "visit_type", "relationship_type", "patient_identifier_type");
		
		if (syncTableConfiguration.isMetadata() && !isInMetadata) {
			throw new ForbiddenOperationException("The table " + syncTableConfiguration.getTableName() + " is been treated as metadata but it is not");
		}
		
		if (syncTableConfiguration.isMetadata()) {
			insertAllMetadata(objects, syncTableConfiguration, conn);
		}
		else {
			insertAllData(objects, syncTableConfiguration, recordOriginLocationCode, conn);
		}
	}

	private static void insertAllData(List<OpenMRSObject> objects, SyncTableConfiguration syncTableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException {
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
				//updateDestinationRecordId(objects, syncTableConfiguration, conn);
			} catch (DBException e) {
				insertAllDataOneByOne(objects, syncTableConfiguration, recordOriginLocationCode, conn);
			}
		}
	}
	
	private static void insertAllDataOneByOne(List<OpenMRSObject> objects, SyncTableConfiguration tableC, String recordOriginLocationCode, Connection conn) throws DBException {
		for (OpenMRSObject record : objects) {
			try {
				record.save(tableC, conn);
			} catch (DBException e) {
				/*if (e.isDuplicatePrimaryKeyException()) {
					OpenMRSObject problematicRecordOnDB = retrieveProblematicObjectFromExceptionInfo(syncTableConfiguration, e, conn);
					
					if (problematicRecordOnDB.getObjectId() == record.getObjectId()) {
						update(problematicRecordOnDB, conn);
						continue;
					}
				}*/
				
				SyncImportInfoVO source = record.retrieveRelatedSyncInfo(tableC, recordOriginLocationCode, conn);
				
				source.markAsSyncFailedToMigrate(tableC, e.getLocalizedMessage(), conn);
			}
		}
	}

	@SuppressWarnings("unused")
	private static OpenMRSObject retrieveProblematicObjectFromExceptionInfo(SyncTableConfiguration tableConfiguration, DBException e, Connection conn) throws DBException {
	 	//UUID duplication Error Pathern... Duplicate Entry 'objectId-origin_app' for bla bla 
		String s = e.getLocalizedMessage().split("'")[1];
		
		//Check if is uuid duplication
		if (utilities.isValidUUID(s)) {
			return getByUuid(tableConfiguration.getSyncRecordClass(tableConfiguration.getMainApp()), s, conn);
		}	
		/*else {
		 	//ORIGIN duplication Error Pathern... Duplicate Entry 'objectId-origin_app' for bla bla 
			String[] idParts = (e.getLocalizedMessage().split("'")[1]).split("-");
			
			int objectId = Integer.parseInt(idParts[0]);
			String originAppLocationCode = idParts[1];
			
			
			return thinGetByRecordOrigin(objectId, originAppLocationCode, tableConfiguration.getSyncRecordClass(), tableConfiguration, conn);
		}*/
		
		return null;
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
				if (getById(syncTableInfo.getSyncRecordClass(syncTableInfo.getMainApp()), maxAcceptableId - 1, conn) == null) {
					return maxAcceptableId - 1;
				}
				else return getAvaliableObjectId(syncTableInfo, maxAcceptableId - 1, conn);
			}
		}
		else{
			return maxAcceptableId;
		}
	}

	public static int getFirstRecord(SyncTableConfiguration tableConf, Connection conn) throws DBException, ForbiddenOperationException {
		return getSpecificRecord(tableConf, "min", conn);
	}
	
	public static int getLastRecord(SyncTableConfiguration tableConf, Connection conn) throws DBException, ForbiddenOperationException {
		return getSpecificRecord(tableConf, "max",  conn);
	}
	
	public static int getSpecificRecord(SyncTableConfiguration tableConf, String function, Connection conn) throws DBException, ForbiddenOperationException {
			
		String 	sql =  " SELECT " + function + "("+ tableConf.getPrimaryKey() +") value\n";
				sql += " FROM " + tableConf.getTableName() + "\n";
				sql += " WHERE 1 = 1;";
						
		Object[] params = {};
		
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v != null && v.hasValue() ? v.intValue() : 0;
	}	
	

	public static OpenMRSObject getFirstOutDatedRecordInDestination(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		return getOutDatedRecordInDestination(tableConfiguration, "min", conn);
	}
	
	public static OpenMRSObject getLastOutDatedRecordInDestination(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		return getOutDatedRecordInDestination(tableConfiguration, "max", conn);
	}
	
	private static GenericOpenMRSObject getOutDatedRecordInDestination(SyncTableConfiguration tableConfiguration, String function, Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " src_ INNER JOIN " + table + " dest_ on dest_.uuid = src_.record_uuid";
		
		if (table.equalsIgnoreCase("patient")) {
			tablesToSelect = stageTable + " src_ INNER JOIN person on person.uuid = src_.record_uuid INNER JOIN patient dest_ ON patient_id = person_id ";
		}
		
		
		
		String startingClause = "1 != 1 ";
		String dateVoidedClause = "";
		String dateChangedClause = "";
		
		if (!tableConfiguration.hasNoDateVoidedField()) {
			dateVoidedClause += " or (dest_.date_voided is null and src_.record_date_voided is not null) ";
			dateVoidedClause += " or (dest_.date_voided is not null and src_.record_date_voided is null) ";
			dateVoidedClause += " or (dest_.date_voided < src_.record_date_voided)";
		}
		
		
		if (!tableConfiguration.hasNotDateChangedField()) {
			dateChangedClause +=  " or (dest_.date_changed is null and src_.record_date_changed is not null) ";
			dateChangedClause +=  " or (dest_.date_changed is not null and src_.record_date_changed is null) "; 
			dateChangedClause +=  " or (dest_.date_changed < src_.record_date_changed)";
		}
		
		
		sql += " SELECT " + tableConfiguration.getPrimaryKey() + " object_id \n";
		sql += " FROM  	" + table + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND " + tableConfiguration.getPrimaryKey() + " = ";
		sql += " 			 (	SELECT " + function+ "(" + tableConfiguration.getPrimaryKey()   + ")\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE 1= 1\n";
		sql += "					AND (" + startingClause + dateVoidedClause + dateChangedClause + "))";
		
		return find(GenericOpenMRSObject.class, sql, params, conn);		
	}
	
	public static OpenMRSObject getFirstPhantomRecordInDestination(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		return getPhantomRecordInDestination(tableConfiguration, "min", conn);
	}
	
	public static OpenMRSObject getLastPhantomRecordInDestination(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		return getPhantomRecordInDestination(tableConfiguration, "max", conn);
	}
	
	private static OpenMRSObject getPhantomRecordInDestination(SyncTableConfiguration tableConfiguration, String function, Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " src_ RIGHT JOIN " + table + " dest_ on dest_.uuid = src_.record_uuid";
		
		if (table.equalsIgnoreCase("patient")) {
			tablesToSelect = stageTable + " src_ RIGHT JOIN person dest_ on dest_.uuid = src_.record_uuid RIGHT JOIN patient ON patient_id = person_id ";
		}
		
		sql += " SELECT " + tableConfiguration.getPrimaryKey() + " object_id \n";
		sql += " FROM  	" + table + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND " + tableConfiguration.getPrimaryKey() + " = ";
		sql += " 			 (	SELECT " + function+ "(" + tableConfiguration.getPrimaryKey()   + ")\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE id IS NULL\n)";
		
		return find(GenericOpenMRSObject.class, sql, params, conn);		
	}
	
	
	public static OpenMRSObject getFirstOutDatedRecordInDestination_(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		return getOutDatedRecordInDestination_(tableConfiguration, "min", conn);
	}
	
	public static OpenMRSObject getLastOutDatedRecordInDestination_(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		return getOutDatedRecordInDestination_(tableConfiguration, "max", conn);
	}
	
	private static GenericOpenMRSObject getOutDatedRecordInDestination_(SyncTableConfiguration tableConfiguration, String function, Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " src_ INNER JOIN " + table + " dest_ on dest_.uuid = src_.record_uuid";
		
		if (table.equalsIgnoreCase("patient")) {
			tablesToSelect = stageTable + " src_ INNER JOIN person on person.uuid = src_.record_uuid INNER JOIN patient dest_ ON patient_id = person_id ";
		}
		
		
		
		String startingClause = "1 != 1 ";
		String dateVoidedClause = "";
		String dateChangedClause = "";
		
		if (!tableConfiguration.hasNoDateVoidedField()) {
			dateVoidedClause += " or (dest_.date_voided is null and src_.record_date_voided is not null) ";
			dateVoidedClause += " or (dest_.date_voided is not null and src_.record_date_voided is null) ";
			dateVoidedClause += " or (dest_.date_voided < src_.record_date_voided)";
		}
		
		
		if (!tableConfiguration.hasNotDateChangedField()) {
			dateChangedClause +=  " or (dest_.date_changed is null and src_.record_date_changed is not null) ";
			dateChangedClause +=  " or (dest_.date_changed is not null and src_.record_date_changed is null) "; 
			dateChangedClause +=  " or (dest_.date_changed < src_.record_date_changed)";
		}
		
		
		sql += " SELECT " + tableConfiguration.getPrimaryKey() + " object_id \n";
		sql += " FROM  	" + table + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND " + tableConfiguration.getPrimaryKey() + " = ";
		sql += " 			 (	SELECT " + function+ "(" + tableConfiguration.getPrimaryKey()   + ")\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE 1= 1\n";
		sql += "					AND (" + startingClause + dateVoidedClause + dateChangedClause + "))";
		
		return find(GenericOpenMRSObject.class, sql, params, conn);		
	}
}
