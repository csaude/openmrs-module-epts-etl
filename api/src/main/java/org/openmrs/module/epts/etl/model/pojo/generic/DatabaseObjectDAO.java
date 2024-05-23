package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;
import java.util.List;
import java.util.UUID;

import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

public class DatabaseObjectDAO extends BaseDAO {
	
	private static void refreshLastSyncDate(EtlDatabaseObject syncRecord, TableConfiguration tableConfiguration,
	        String recordOriginLocationCode, Connection conn) throws DBException {
		Object[] params = { DateAndTimeUtilities.getCurrentSystemDate(conn), recordOriginLocationCode,
		        syncRecord.getObjectId() };
		
		String originDestin = tableConfiguration.isDestinationInstallationType() ? "record_destination_id"
		        : "record_origin_id";
		
		String sql = "";
		
		sql += " UPDATE " + tableConfiguration.generateFullStageTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  record_origin_location_code = ? ";
		sql += "		AND " + originDestin + " = ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void refreshLastSyncDateOnDestination(EtlDatabaseObject syncRecord, TableConfiguration tableConfiguration,
	        String recordOriginLocationCode, Connection conn) throws DBException {
		refreshLastSyncDate(syncRecord, tableConfiguration, recordOriginLocationCode, conn);
	}
	
	public static void refreshLastSyncDateOnOrigin(EtlDatabaseObject syncRecord, TableConfiguration tableConfiguration,
	        String recordOriginLocationCode, Connection conn) throws DBException {
		refreshLastSyncDate(syncRecord, tableConfiguration, recordOriginLocationCode, conn);
	}
	
	private static void refreshLastSyncDate(List<EtlDatabaseObject> syncRecords, TableConfiguration tableConfiguration,
	        String recordOriginLocationCode, Connection conn) throws DBException {
		Object[] params = { DateAndTimeUtilities.getCurrentSystemDate(conn), recordOriginLocationCode,
		        syncRecords.get(0).getObjectId(), syncRecords.get(syncRecords.size() - 1).getObjectId() };
		
		String originDestin = tableConfiguration.isDestinationInstallationType() ? "record_destination_id"
		        : "record_origin_id";
		
		String sql = "";
		
		sql += " UPDATE " + tableConfiguration.generateFullStageTableName();
		sql += " SET    last_sync_date = ? ";
		sql += " WHERE  record_origin_location_code = ? ";
		sql += "		AND " + originDestin + " between ? and ? ";
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void refreshLastSyncDateOnDestination(List<EtlDatabaseObject> syncRecords,
	        TableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException {
		refreshLastSyncDate(syncRecords, tableConfiguration, recordOriginLocationCode, conn);
	}
	
	public static void refreshLastSyncDateOnOrigin(List<EtlDatabaseObject> syncRecords,
	        TableConfiguration tableConfiguration, String recordOriginLocationCode, Connection conn) throws DBException {
		refreshLastSyncDate(syncRecords, tableConfiguration, recordOriginLocationCode, conn);
	}
	
	public static void insert(EtlDatabaseObject record, TableConfiguration tableConfiguration, Connection conn)
	        throws DBException {
		Object[] params = null;
		String sql = null;
		
		if (tableConfiguration.isAutoIncrementId()) {
			params = record.getInsertParamsWithoutObjectId();
			sql = record.getInsertSQLWithoutObjectId();
		} else {
			params = record.getInsertParamsWithObjectId();
			sql = record.getInsertSQLWithObjectId();
		}
		
		sql = DBUtilities.tryToPutSchemaOnInsertScript(sql, conn);
		
		long id = executeQueryWithRetryOnError(sql, params, conn);
		
		if (record.getObjectId().isSimpleId()) {
			record.fastCreateSimpleNumericKey(id);
		}
		
	}
	
	public static void insertWithObjectId(EtlDatabaseObject record, Connection conn) throws DBException {
		Object[] params = record.getInsertParamsWithObjectId();
		String sql = record.getInsertSQLWithObjectId();
		
		sql = DBUtilities.tryToPutSchemaOnInsertScript(sql, conn);
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static void update(EtlDatabaseObject record, Connection conn) throws DBException {
		Object[] params = record.getUpdateParams();
		String sql = record.getUpdateSQL();
		
		sql = DBUtilities.tryToPutSchemaOnUpdateScript(sql, conn);
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static EtlDatabaseObject thinGetByRecordOrigin(Integer recordOriginId, String recordOriginLocationCode,
	        TableConfiguration parentTableConfiguration, Connection conn) throws DBException {
		
		if (UUID.randomUUID() != null) {
			throw new ForbiddenOperationException("rever este methodo");
		}
		
		try {
			Object[] params = { recordOriginId, recordOriginLocationCode };
			
			String tableName = parentTableConfiguration.getTableName();
			
			String columnsToSelect = parentTableConfiguration.generateFullAliasedSelectColumns();
			
			String clauseFromStarting = tableName;
			
			if (parentTableConfiguration.useSharedPKKey()) {
				TableConfiguration sharedTabConf = parentTableConfiguration.getSharedTableConf();
				
				clauseFromStarting += " INNER JOIN " + sharedTabConf.generateTableNameWithAlias() + " ON "
				        + parentTableConfiguration.getSharedKeyRefInfo().generateJoinCondition() + "\n";
				
				columnsToSelect += ", \n" + sharedTabConf.generateFullAliasedSelectColumns();
			}
			
			String sql = "";
			
			sql += " SELECT " + columnsToSelect + "\n";
			sql += " FROM  	" + clauseFromStarting + " INNER JOIN " + parentTableConfiguration.generateFullStageTableName()
			        + " ON record_uuid = uuid\n";
			sql += " WHERE 	record_origin_id = ? and record_origin_location_code = ? ";
			
			return find(parentTableConfiguration.getLoadHealper(),
			    parentTableConfiguration.getSyncRecordClass(parentTableConfiguration.getMainApp()), sql, params, conn);
		}
		catch (Exception e) {
			logger.info("Error trying do retrieve record on table " + parentTableConfiguration.getTableName() + "["
			        + e.getMessage() + "]");
			
			TimeCountDown.sleep(2000);
			
			throw new RuntimeException("Error trying do retrieve record on table " + parentTableConfiguration.getTableName()
			        + "[" + e.getMessage() + "]");
		}
	}
	
	public static <T extends EtlDatabaseObject> List<T> getByUniqueKeys(TableConfiguration tableConfiguration, T obj,
	        Connection conn) throws DBException {
		return getByUniqueKeys(tableConfiguration, DBUtilities.determineSchemaName(conn), obj, conn);
	}
	
	public static <T extends EtlDatabaseObject> T getByUniqueKey(TableConfiguration tableConfiguration, UniqueKeyInfo uk,
	        Connection conn) throws DBException {
		return getByUniqueKey(tableConfiguration, uk, DBUtilities.determineSchemaName(conn), conn);
	}
	
	public static <T extends EtlDatabaseObject> T getByUniqueKeysOnSpecificSchema(TableConfiguration tableConfiguration,
	        T obj, String schema, Connection conn) throws DBException {
		List<T> result = getByUniqueKeys(tableConfiguration, schema, obj, conn);
		
		return utilities.arrayHasElement(result) ? result.get(0) : null;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends EtlDatabaseObject> T getByUniqueKey(TableConfiguration tableConfiguration,
	        UniqueKeyInfo uniqueKey, String schema, Connection conn) throws DBException {
		if (!tableConfiguration.isFullLoaded())
			tableConfiguration.fullLoad();
		
		Object[] params = {};
		
		String conditionSQL = "";
		
		try {
			params = utilities.setParam(params, uniqueKey.parseValuesToArray());
			
			conditionSQL = uniqueKey.parseToParametrizedStringConditionWithAlias();
		}
		catch (ForbiddenOperationException e) {}
		
		if (conditionSQL.isEmpty())
			return null;
		
		String sql = "";
		
		sql += " SELECT " + tableConfiguration.generateFullAliasedSelectColumns() + "\n";
		sql += " FROM     " + tableConfiguration.generateFullTableNameWithAlias() + "\n";
		sql += " WHERE 	" + conditionSQL;
		
		T recOnDb = (T) find(tableConfiguration.getLoadHealper(), tableConfiguration.getSyncRecordClass(), sql, params,
		    conn);
		
		if (recOnDb != null) {
			recOnDb.loadObjectIdData(tableConfiguration);
		}
		
		return recOnDb;
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends EtlDatabaseObject> List<T> getByUniqueKeys(TableConfiguration tableConfiguration,
	        String schema, T obj, Connection conn) throws DBException {
		if (!tableConfiguration.isFullLoaded())
			tableConfiguration.fullLoad();
		
		if (!utilities.arrayHasElement(tableConfiguration.getUniqueKeys()))
			return null;
		
		Object[] params = {};
		
		String conditionSQL = "";
		
		for (UniqueKeyInfo uniqueKey : tableConfiguration.getUniqueKeys()) {
			
			String tmpCodition = "";
			
			try {
				params = utilities.setParam(params, uniqueKey.parseValuesToArray());
				
				tmpCodition = uniqueKey.parseToParametrizedStringConditionWithAlias();
			}
			catch (ForbiddenOperationException e) {}
			
			if (!tmpCodition.isEmpty()) {
				if (!conditionSQL.isEmpty())
					conditionSQL += " OR ";
				
				conditionSQL += "(" + tmpCodition + ")";
			}
		}
		
		if (conditionSQL.isEmpty())
			return null;
		
		String sql = "";
		
		sql += " SELECT " + tableConfiguration.generateFullAliasedSelectColumns() + "\n";
		sql += " FROM     " + tableConfiguration.generateFullTableNameWithAlias() + "\n";
		sql += " WHERE 	" + conditionSQL;
		
		List<T> objs = (List<T>) search(tableConfiguration.getLoadHealper(), obj.getClass(), sql, params, conn);
		
		if (utilities.arrayHasElement(objs)) {
			for (T t : objs) {
				t.loadObjectIdData(tableConfiguration);
			}
		}
		
		return objs;
	}
	
	public static <T extends EtlDatabaseObject> List<T> getByField(TableConfiguration tableConfiguration, String fieldName,
	        String fieldValue, Connection conn) throws DBException {
		
		Object[] params = { fieldValue };
		
		@SuppressWarnings("unchecked")
		Class<T> openMRSClass = (Class<T>) tableConfiguration.getSyncRecordClass(tableConfiguration.getRelatedAppInfo());
		
		String sql = "";
		
		sql += " SELECT " + tableConfiguration.generateFullAliasedSelectColumns() + "\n";
		sql += " FROM     " + tableConfiguration.generateSelectFromClauseContent() + "\n";
		sql += " WHERE 	" + fieldName + " = ?";
		sql += " ORDER BY " + fieldName;
		
		return search(tableConfiguration.getLoadHealper(), openMRSClass, sql, params, conn);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends EtlDatabaseObject> T getByOid(TableConfiguration tabConf, Oid oid, Connection conn)
	        throws DBException {
		try {
			
			oid.setTabConf(tabConf);
			
			if (!tabConf.isFullLoaded()) {
				tabConf.fullLoad(conn);
			}
			
			Class<T> openMRSClass = (Class<T>) tabConf.getSyncRecordClass(tabConf.getRelatedAppInfo());
			
			T obj = openMRSClass.newInstance();
			
			obj.setRelatedConfiguration(tabConf);
			
			Object[] params = oid.parseValuesToArray();
			
			String sql = "";
			
			sql += " SELECT " + tabConf.generateFullAliasedSelectColumns() + "\n";
			sql += " FROM  	" + tabConf.generateSelectFromClauseContent() + "\n";
			sql += " WHERE 	" + oid.parseToParametrizedStringConditionWithAlias();
			
			obj = find(tabConf.getLoadHealper(), openMRSClass, sql, params, conn);
			
			if (obj != null) {
				obj.loadObjectIdData(tabConf);
			}
			
			return obj;
			
		}
		catch (InstantiationException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public static List<EtlDatabaseObject> getByParentId(TableConfiguration tableConfiguration, String parentField,
	        Integer parentId, Connection conn) throws DBException {
		Object[] params = { parentField };
		
		String sql = " SELECT * FROM " + tableConfiguration.getTableName() + " WHERE 	" + parentField + " = ?";
		
		return utilities.parseList(
		    search(tableConfiguration.getLoadHealper(), GenericDatabaseObject.class, sql, params, conn),
		    EtlDatabaseObject.class);
	}
	
	public static EtlDatabaseObject getDefaultRecord(TableConfiguration tableConfiguration, Connection conn)
	        throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		sql += " SELECT " + tableConfiguration.generateFullAliasedSelectColumns() + "\n";
		sql += " FROM  	" + tableConfiguration.generateSelectFromClauseContent() + "\n";
		sql += " LIMIT 0, 1";
		
		return find(tableConfiguration.getLoadHealper(),
		    tableConfiguration.getSyncRecordClass(tableConfiguration.getMainApp()), sql, params, conn);
	}
	
	public static long countAll(TableConfiguration tableConfiguration, Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		sql += " SELECT count(*) as value FROM " + tableConfiguration.getTableName();
		
		return find(SimpleValue.class, sql, params, conn).longValue();
	}
	
	public static EtlDatabaseObject getFirstConsistentRecordInOrigin(TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		return getConsistentRecordInOrigin(tableInfo, "min", conn);
	}
	
	public static EtlDatabaseObject getLastConsistentRecordOnOrigin(TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		return getConsistentRecordInOrigin(tableInfo, "max", conn);
	}
	
	private static GenericDatabaseObject getConsistentRecordInOrigin(TableConfiguration tableConfiguration, String function,
	        Connection conn) throws DBException {
		
		utilities.throwReviewMethodException();
		
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " stage_ INNER JOIN " + table + " src_ on src_.uuid = stage_.record_uuid";
		
		String simplePk = tableConfiguration.getTableAlias() + "."
		        + tableConfiguration.getPrimaryKey().retrieveSimpleKeyColumnName();
		
		sql += " SELECT " + simplePk + " object_id \n";
		sql += " FROM  	" + tableConfiguration.generateSelectFromClauseContent() + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND " + simplePk + " = ";
		sql += " 			 (	SELECT " + function + "(" + tableConfiguration.getPrimaryKey() + ")\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE consistent = 1;\n";
		
		return find(tableConfiguration.getLoadHealper(), GenericDatabaseObject.class, sql, params, conn);
	}
	
	public static EtlDatabaseObject getFirstNeverProcessedRecordOnOrigin(TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		return getExtremeNeverProcessedRecordOnOrigin(tableInfo, "min", conn);
	}
	
	public static EtlDatabaseObject getLastNeverProcessedRecordOnOrigin(TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		return getExtremeNeverProcessedRecordOnOrigin(tableInfo, "max", conn);
	}
	
	private static EtlDatabaseObject getExtremeNeverProcessedRecordOnOrigin(TableConfiguration tabConf, String function,
	        Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		sql += " SELECT " + tabConf.generateFullAliasedSelectColumns();
		sql += " FROM  	" + tabConf.generateSelectFromClauseContent() + "\n";
		sql += " WHERE 	" + tabConf.getPrimaryKey() + "	=	 (	SELECT " + function + "(" + tabConf.getPrimaryKey() + ")\n";
		sql += "													FROM   " + tabConf.getTableName() + " \n";
		sql += "													WHERE  NOT EXISTS ( SELECT * \n";
		sql += "																		FROM "
		        + tabConf.generateFullStageTableName() + "\n";
		sql += "																		WHERE record_origin_id = "
		        + tabConf.getPrimaryKey() + "\n)";
		sql += "												   )";
		
		return find(tabConf.getLoadHealper(), tabConf.getSyncRecordClass(tabConf.getMainApp()), sql, params, conn);
	}
	
	public static void remove(EtlDatabaseObject record, Connection conn) throws DBException {
		Object[] params = record.getObjectId().parseValuesToArray();
		
		String sql = " DELETE" + " FROM " + record.generateTableName() + " WHERE  "
		        + record.getObjectId().parseToParametrizedStringConditionWithAlias();
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	public static int countAllOfOriginParentId(String parentField, Integer parentOriginId, String appOriginCode,
	        TableConfiguration tableConfiguration, Connection conn) throws DBException {
		
		Object[] params = { parentOriginId, appOriginCode };
		
		String sql = " SELECT count(*) value";
		sql += " FROM  	" + tableConfiguration.getTableName() + " INNER JOIN "
		        + tableConfiguration.generateFullStageTableName() + " ON record_destination_id = "
		        + tableConfiguration.getPrimaryKey() + "\n";
		sql += " WHERE 	" + parentField + " = ? ";
		sql += "			AND record_origin_location_code = ? ";
		
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v.intValue();
	}
	
	public static Integer countAllOfParentId(Class<? extends EtlDatabaseObject> clazz, String parentField, Integer parentId,
	        Connection conn) throws DBException {
		Object[] params = { parentId };
		
		EtlDatabaseObject obj = utilities.createInstance(clazz);
		
		String sql = " SELECT count(*) value" + " FROM     " + obj.generateTableName() + " WHERE 	" + parentField
		        + " = ? ";
		
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v.IntegerValue();
	}
	
	public static List<? extends EtlDatabaseObject> getByOriginParentId(String parentField, Integer parentOriginId,
	        String appOriginCode, TableConfiguration tableConfiguration, Connection conn) throws DBException {
		Object[] params = { parentOriginId, appOriginCode };
		
		String sql = " SELECT * ";
		sql += " FROM  	" + tableConfiguration.getTableName() + " INNER JOIN "
		        + tableConfiguration.generateFullStageTableName() + " ON record_uuid = uuid\n";
		sql += " WHERE 	" + parentField + " = ? ";
		sql += "			AND record_origin_location_code = ? ";
		
		return search(tableConfiguration.getLoadHealper(),
		    tableConfiguration.getSyncRecordClass(tableConfiguration.getMainApp()), sql, params, conn);
	}
	
	public static List<? extends EtlDatabaseObject> getByParentIdOnSpecificSchema(TableConfiguration tabConf,
	        String parentField, Integer parentId, String schema, Connection conn) throws DBException {
		Object[] params = { parentId };
		
		Class<? extends EtlDatabaseObject> clazz = tabConf.getSyncRecordClass();
		
		EtlDatabaseObject obj = utilities.createInstance(clazz);
		
		String sql = " SELECT " + obj.generateTableName() + ".*"
		        + (obj.generateTableName().equals("patient") ? ", uuid" : "") + " FROM     " + schema + "."
		        + obj.generateTableName()
		        + (obj.generateTableName().equals("patient") ? " inner join " + schema + ".person on person_id = patient_id "
		                : "")
		        + " WHERE 	" + parentField + " = ?";
		
		return search(tabConf.getLoadHealper(), clazz, sql, params, conn);
	}
	
	private static void insertAllMetadata(List<EtlDatabaseObject> records, TableConfiguration tableInfo, Connection conn)
	        throws DBException {
		if (!tableInfo.isMetadata())
			throw new ForbiddenOperationException(
			        "You tried to insert " + tableInfo.getTableName() + " as metadata but it is not a metadata!!!");
		
		for (EtlDatabaseObject record : records) {
			record.save(tableInfo, conn);
		}
	}
	
	public static void insertAll(List<EtlDatabaseObject> objects, TableConfiguration abstractTableConfiguration,
	        String recordOriginLocationCode, Connection conn) throws DBException {
		
		if (abstractTableConfiguration.isMetadata()) {
			insertAllMetadata(objects, abstractTableConfiguration, conn);
		} else {
			
			if (abstractTableConfiguration.isAutoIncrementId()) {
				insertAllDataWithoutId(objects, conn);
			} else {
				insertAllDataWithId(objects, conn);
			}
		}
	}
	
	public static void insertAllDataWithoutId(List<EtlDatabaseObject> objects, Connection conn) throws DBException {
		String sql = DBUtilities
		        .addInsertIgnoreOnInsertScript(objects.get(0).getInsertSQLWithoutObjectId().split("VALUES")[0], conn);
		
		//sql = objects.get(0).getInsertSQLWithoutObjectId().split("VALUES")[0];
		
		sql += " VALUES";
		
		sql = sql.toLowerCase();
		
		String values = "";
		
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).isExcluded())
				continue;
			
			values += "(" + utilities.resolveScapeCharacter(objects.get(i).generateInsertValuesWithoutObjectId()) + "),";
		}
		
		if (utilities.stringHasValue(values)) {
			sql += utilities.removeLastChar(values);
			
			executeQueryWithRetryOnError(sql, null, conn);
		}
	}
	
	public static void insertAllDataWithId(List<EtlDatabaseObject> objects, Connection conn) throws DBException {
		String sql = DBUtilities.addInsertIgnoreOnInsertScript(objects.get(0).getInsertSQLWithObjectId().split("VALUES")[0],
		    conn);
		
		//sql = objects.get(0).getInsertSQLWithObjectId().split("VALUES")[0];
		
		sql += " VALUES";
		
		sql = sql.toLowerCase();
		
		String values = "";
		
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).isExcluded())
				continue;
			
			values += "(" + utilities.resolveScapeCharacter(objects.get(i).generateInsertValuesWithObjectId()) + "),";
		}
		
		if (utilities.stringHasValue(values)) {
			sql += utilities.removeLastChar(values);
			
			executeQueryWithRetryOnError(sql, null, conn);
		}
	}
	
	@SuppressWarnings("unused")
	private static EtlDatabaseObject retrieveProblematicObjectFromExceptionInfo(TableConfiguration tableConfiguration,
	        DBException e, Connection conn) throws DBException {
		//UUID duplication Error Pathern... Duplicate Entry 'objectId-origin_app' for bla bla 
		String s = e.getLocalizedMessage().split("'")[1];
		
		GenericDatabaseObject obj = new GenericDatabaseObject();
		obj.setUuid(s);
		
		//Check if is uuid duplication
		if (utilities.isValidUUID(s)) {
			List<EtlDatabaseObject> recs = getByUniqueKeys(tableConfiguration, obj, conn);
			
			if (utilities.arrayHasElement(recs)) {
				return recs.get(0);
			}
		}
		
		return null;
	}
	
	public static Integer getAvaliableObjectId(TableConfiguration tabConf, Integer maxAcceptableId, Connection conn)
	        throws DBException {
		if (maxAcceptableId <= 0)
			throw new ForbiddenOperationException("There was not find any avaliable id for " + tabConf.getTableName());
		
		if (tabConf.getPrimaryKey().isCompositeKey()) {
			throw new ForbiddenOperationException("The key for table " + tabConf.getTableName()
			        + " is composite. You cannot determine de avaliable ObjectId");
		}
		
		if (!tabConf.getPrimaryKey().retrieveSimpleKey().isNumericColumnType()) {
			throw new ForbiddenOperationException("The key should be numeric...!");
		}
		
		String pkName = tabConf.getPrimaryKey().retrieveSimpleKey().getName();
		
		String sql = "";
		
		sql += " SELECT max(" + pkName + ") " + pkName + " \n";
		sql += " FROM  	" + tabConf.getTableName() + ";\n";
		
		EtlDatabaseObject maxObj = find(GenericDatabaseObject.class, sql, null, conn);
		
		if (maxObj != null) {
			if (maxObj.getObjectId().getSimpleValueAsInt() < maxAcceptableId) {
				return maxAcceptableId;
			} else {
				
				Oid oid = Oid.fastCreate(pkName, maxAcceptableId - 1);
				
				if (getByOid(tabConf, oid, conn) == null) {
					return maxAcceptableId - 1;
				} else
					return getAvaliableObjectId(tabConf, maxAcceptableId - 1, conn);
			}
		} else {
			return maxAcceptableId;
		}
	}
	
	public static Integer getFirstRecord(TableConfiguration tableConf, Connection conn)
	        throws DBException, ForbiddenOperationException {
		return getSpecificRecord(tableConf, "min", conn);
	}
	
	public static Integer getLastRecord(TableConfiguration tableConf, Connection conn)
	        throws DBException, ForbiddenOperationException {
		return getSpecificRecord(tableConf, "max", conn);
	}
	
	public static Integer getSpecificRecord(TableConfiguration tableConf, String function, Connection conn)
	        throws DBException, ForbiddenOperationException {
		
		String sql = " SELECT " + function + "(" + tableConf.getPrimaryKey() + ") value\n";
		sql += " FROM " + tableConf.getTableName() + "\n";
		sql += " WHERE 1 = 1;";
		
		Object[] params = {};
		
		SimpleValue v = find(SimpleValue.class, sql, params, conn);
		
		return v != null && v.hasValue() ? v.IntegerValue() : 0;
	}
	
	public static Integer getFirstRecord(DatabaseObjectSearchParams searchParams, Connection conn)
	        throws DBException, ForbiddenOperationException {
		return getSpecificRecord(searchParams, "min", conn);
	}
	
	public static Integer getLastRecord(DatabaseObjectSearchParams searchParams, Connection conn)
	        throws DBException, ForbiddenOperationException {
		return getSpecificRecord(searchParams, "max", conn);
	}
	
	public static Integer getSpecificRecord(DatabaseObjectSearchParams searchParams, String function, Connection conn)
	        throws DBException, ForbiddenOperationException {
		
		SearchClauses<EtlDatabaseObject> searchClauses = searchParams.generateSearchClauses(conn);
		
		searchClauses.setColumnsToSelect(function + "(" + searchParams.getConfig().getSrcConf().getPrimaryKey() + ") value");
		
		Object[] params = {};
		
		SimpleValue v = find(SimpleValue.class, searchClauses.generateSQL(conn), params, conn);
		
		return v != null && v.hasValue() ? v.IntegerValue() : 0;
	}
	
	public static EtlDatabaseObject getFirstOutDatedRecordInDestination(TableConfiguration tableConfiguration,
	        Connection conn) throws DBException {
		return getOutDatedRecordInDestination(tableConfiguration, "min", conn);
	}
	
	public static EtlDatabaseObject getLastOutDatedRecordInDestination(TableConfiguration tableConfiguration,
	        Connection conn) throws DBException {
		return getOutDatedRecordInDestination(tableConfiguration, "max", conn);
	}
	
	private static GenericDatabaseObject getOutDatedRecordInDestination(TableConfiguration tableConfiguration,
	        String function, Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " src_ INNER JOIN " + tableConfiguration.generateTableNameWithAlias() + "  ON "
		        + tableConfiguration.getTableAlias() + ".uuid = src_.record_uuid";
		
		if (tableConfiguration.useSharedPKKey()) {
			tablesToSelect += "INNER JOIN " + tableConfiguration.getSharedTableConf().generateTableNameWithAlias() + " ON "
			        + tableConfiguration.getSharedKeyRefInfo().generateJoinCondition();
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
			dateChangedClause += " or (dest_.date_changed is null and src_.record_date_changed is not null) ";
			dateChangedClause += " or (dest_.date_changed is not null and src_.record_date_changed is null) ";
			dateChangedClause += " or (dest_.date_changed < src_.record_date_changed)";
		}
		
		String simpleKey = tableConfiguration.getPrimaryKey().retrieveSimpleKeyColumnName();
		
		sql += " SELECT " + simpleKey + " object_id \n";
		sql += " FROM  	" + table + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND " + simpleKey + " = ";
		sql += " 			 (	SELECT " + function + "(" + simpleKey + ")\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE 1= 1\n";
		sql += "					AND (" + startingClause + dateVoidedClause + dateChangedClause + "))";
		
		return find(GenericDatabaseObject.class, sql, params, conn);
	}
	
	public static EtlDatabaseObject getFirstPhantomRecordInDestination(TableConfiguration tableConfiguration,
	        Connection conn) throws DBException {
		return getPhantomRecordInDestination(tableConfiguration, "min", conn);
	}
	
	public static EtlDatabaseObject getLastPhantomRecordInDestination(TableConfiguration tableConfiguration, Connection conn)
	        throws DBException {
		return getPhantomRecordInDestination(tableConfiguration, "max", conn);
	}
	
	private static EtlDatabaseObject getPhantomRecordInDestination(TableConfiguration tableConfiguration, String function,
	        Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " src_ RIGHT JOIN " + tableConfiguration.generateTableNameWithAlias() + "  on "
		        + tableConfiguration.getTableAlias() + ".uuid = src_.record_uuid";
		
		if (tableConfiguration.useSharedPKKey()) {
			tablesToSelect += "\n RIGHT JOIN " + tableConfiguration.getSharedTableConf().generateTableNameWithAlias()
			        + " ON " + tableConfiguration.getSharedKeyRefInfo().generateJoinCondition();
		}
		
		sql += " SELECT " + tableConfiguration.getPrimaryKey().retrieveSimpleKeyColumnName() + " object_id \n";
		sql += " FROM  	" + table + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND " + tableConfiguration.getPrimaryKey() + " = ";
		sql += " 			 (	SELECT " + function + "(" + tableConfiguration.getPrimaryKey().retrieveSimpleKeyColumnName()
		        + ")\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE id IS NULL\n)";
		
		return find(GenericDatabaseObject.class, sql, params, conn);
	}
	
	public static EtlDatabaseObject getFirstOutDatedRecordInDestination_(TableConfiguration tableConfiguration,
	        Connection conn) throws DBException {
		return getOutDatedRecordInDestination_(tableConfiguration, "min", conn);
	}
	
	public static EtlDatabaseObject getLastOutDatedRecordInDestination_(TableConfiguration tableConfiguration,
	        Connection conn) throws DBException {
		return getOutDatedRecordInDestination_(tableConfiguration, "max", conn);
	}
	
	private static GenericDatabaseObject getOutDatedRecordInDestination_(TableConfiguration tableConfiguration,
	        String function, Connection conn) throws DBException {
		Object[] params = {};
		
		String sql = "";
		
		String table = tableConfiguration.getTableName();
		String stageTable = tableConfiguration.generateFullStageTableName();
		
		String tablesToSelect = stageTable + " src_ INNER JOIN " + tableConfiguration.generateTableNameWithAlias() + "  on "
		        + tableConfiguration.getTableAlias() + ".uuid = src_.record_uuid";
		
		if (tableConfiguration.useSharedPKKey()) {
			tablesToSelect += "\n INNER JOIN " + tableConfiguration.getSharedTableConf().generateTableNameWithAlias()
			        + " ON " + tableConfiguration.getSharedKeyRefInfo().generateJoinCondition();
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
			dateChangedClause += " or (dest_.date_changed is null and src_.record_date_changed is not null) ";
			dateChangedClause += " or (dest_.date_changed is not null and src_.record_date_changed is null) ";
			dateChangedClause += " or (dest_.date_changed < src_.record_date_changed)";
		}
		
		sql += " SELECT " + tableConfiguration.getPrimaryKey().parseFieldNamesToCommaSeparatedString() + " \n";
		sql += " FROM  	" + table + "\n";
		sql += " WHERE 	1 = 1 \n";
		sql += "		AND " + tableConfiguration.getPrimaryKey() + " = ";
		sql += " 			 (	SELECT " + function + "(" + tableConfiguration.getPrimaryKey() + ")\n";
		sql += "				FROM   " + tablesToSelect + "\n";
		sql += "				WHERE 1= 1\n";
		sql += "					AND (" + startingClause + dateVoidedClause + dateChangedClause + "))";
		
		return find(tableConfiguration.getLoadHealper(), GenericDatabaseObject.class, sql, params, conn);
	}
}
