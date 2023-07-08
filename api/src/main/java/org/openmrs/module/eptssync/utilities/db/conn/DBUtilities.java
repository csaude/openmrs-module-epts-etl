package org.openmrs.module.eptssync.utilities.db.conn;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.controller.conf.UniqueKeyInfo;
import org.openmrs.module.eptssync.exceptions.DatabaseNotSupportedException;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.Field;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

/**
 * @author JPBOANE
 * @version 1.0 27/12/2012
 */
public class DBUtilities {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static final String ORACLE_DATABASE = "ORACLE";
	
	public static final String MYSQL_DATABASE = "MYSQL";
	
	public static final String POSTGRES_DATABASE = "POSTGRES";
	
	public static final String SQLSERVER_DATABASE = "SQLSERVER";
	
	public static final String UNKNOWN_DATABASE = "UNKOWN";
	
	public static final String SELECT_SQL_TYPE = "SELECT_SQL_TYPE";
	
	public static final String UPDATE_SQL_TYPE = "UPDATE_SQL_TYPE";
	
	public static final String DELETE_SQL_TYPE = "DELETE_SQL_TYPE";
	
	public static final String DROP_SQL_TYPE = "DROP_SQL_TYPE";
	
	public static final String CREATE_SQL_TYPE = "CREATE_SQL_TYPE";
	
	public static final String TRUNCAT_SQL_TYPE = "TRUNCAT_SQL_TYPE";
	
	public static final String RESOURCE_TYPE_TRIGGER = "TRIGGER";
	
	public static final String RESOURCE_TYPE_TABLE = "TABLE";
	
	public static final String RESOURCE_TYPE_VIEW = "VIEW";
	
	public static final String RESOURCE_TYPE_PROCEDURE = "PROCEDURE";
	
	public static final String RESOURCE_TYPE_INDEX = "INDEX";
	
	public static final String RESOURCE_TYPE_SCHEMA = "SCHEMA";
	
	private static String determineDataBaseString(String msg) {
		if (msg.toUpperCase().contains("ORA")) {
			return ORACLE_DATABASE;
		} else if (msg.toUpperCase().contains("MYSQL")) {
			return MYSQL_DATABASE;
		} else if (msg.toLowerCase().contains("postgresql")) {
			return POSTGRES_DATABASE;
		}
		
		return null;
	}
	
	public static String determineDataBaseFromException(SQLException sqlExcetion) {
		String db = determineDataBaseString(sqlExcetion.getClass().getName());
		
		if (db != null)
			return db;
		
		if (sqlExcetion.getCause() != null) {
			db = determineDataBaseString(sqlExcetion.getCause().getClass().getName());
			
			if (db != null)
				return db;
		}
		
		if (sqlExcetion.getLocalizedMessage() != null) {
			db = determineDataBaseString(sqlExcetion.getLocalizedMessage().toString());
			if (db != null)
				return db;
		}
		
		StackTraceElement[] trace = sqlExcetion.getStackTrace();
		
		for (int i = 0; i < trace.length; i++) {
			db = determineDataBaseString((trace[i].toString()));
			
			if (db != null)
				return db;
		}
		
		return UNKNOWN_DATABASE;
	}
	
	public static boolean isPostgresDB(Connection conn) throws DBException {
		try {
			return determineDataBaseFromConnection(conn).equals(POSTGRES_DATABASE);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static boolean isOracleDB(Connection conn) throws DBException {
		try {
			return determineDataBaseFromConnection(conn).equals(ORACLE_DATABASE);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static boolean isMySQLDB(Connection conn) throws DBException {
		try {
			return determineDataBaseFromConnection(conn).equals(MYSQL_DATABASE);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static String determineDataBaseFromConnection(Connection conn) throws DBException {
		String str;
		try {
			str = conn.getMetaData().getDriverName().toUpperCase();
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		if (str.contains("ORACLE"))
			return ORACLE_DATABASE;
		if (str.contains("MYSQL"))
			return MYSQL_DATABASE;
		if (str.contains("POSTGRES"))
			return POSTGRES_DATABASE;
		if (str.contains("SQLSERVER"))
			return SQLSERVER_DATABASE;
		
		throw new RuntimeException("Impossivel determinar a base de dados a partir da conexao");
	}
	
	public static Connection cloneConnetion(Connection conn) throws SQLException {
		//DatabaseMetaData metadata = conn.getMetaData();
		
		//String userName = metadata.getUserName();
		//String url = metadata.getURL();
		//String className = metadata.getDriverName();
		
		//return BaseDAO.openConnection(className, url, userName, metadata.getPa);
		
		return null;
	}
	
	public static String tryToPutSchemaOnDatabaseObject(String tableName, Connection conn) throws DBException {
		try {
			
			if (isPostgresDB(conn))
				return tableName;
			if (isMySQLDB(conn))
				return tableName;
			
			String[] tableNameComposition = tableName.split("\\.");
			
			if (tableNameComposition != null && tableNameComposition.length == 1) {
				String userName = conn.getMetaData().getUserName().toLowerCase().equals("sisflof") ? "lims"
				        : conn.getMetaData().getUserName();
				
				return userName + "." + tableName;
			}
			
			return tableName;
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static String determineSchemaName(Connection conn) throws DBException {
		try {
			if (conn instanceof OpenConnection) {
				return conn.getSchema();
			}
			
			if (isMySQLDB(conn))
				return conn.getCatalog();
			if (isOracleDB(conn))
				return conn.getMetaData().getUserName();
			if (isPostgresDB(conn))
				return "public";
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		throw new ForbiddenOperationException("Unable to determine the schema");
	}
	
	@SuppressWarnings("unused")
	private static Connection tempOpenConnection() {
		String OracleClasse = "oracle.jdbc.driver.OracleDriver";
		String OracleURL = "jdbc:oracle:thin:@10.5.63.10:1521:dntf";
		String OracleUser = "lims";
		String OraclePass = "exi2k12";
		
		return null;//BaseDAO.openConnection(OracleClasse, OracleURL, OracleUser, OraclePass);
	}
	
	public static boolean isInvalidIdentifierException(DBException e) {
		String dataBase = determineDataBaseFromException(e);
		
		if (dataBase.equals(ORACLE_DATABASE)) {
			return e.getLocalizedMessage().contains("ORA-00904");
		}
		
		throw new ForbiddenOperationException("Unsupported DB");
	}
	
	public static void disableForegnKeyChecks(Connection conn) throws DBException {
		try {
			if (isMySQLDB(conn)) {
				Statement st = conn.createStatement();
				
				st.addBatch("SET FOREIGN_KEY_CHECKS=0");
				
				st.executeBatch();
				
				st.close();
				
			} else
				throw new RuntimeException("Database not supported!");
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new DBException(e);
		}
	}

	
	private static void createRelatedSyncStageAreaUniqueKeysTable(SyncTableConfiguration config, Connection conn) throws DBException {
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		String parentTableName = config.generateFullStageTableName();
		String tableName = config.generateRelatedStageUniqueKeysTableName();
		
		sql += "CREATE TABLE " + config.generateFullStageUniqueKeysTableName() + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("record_id", notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("key_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("column_name", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("key_value", 100, "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		sql += DBUtilities.generateTableUniqueKeyDefinition(tableName + "_unq_record_key".toLowerCase(), "record_id, key_name, column_name", conn) + endLineMarker;
		sql += DBUtilities.generateTableForeignKeyDefinition(tableName + "_parent_record", "record_id", parentTableName, "id", conn) + endLineMarker;
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn) + "\n";
		sql += ")";
		
		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();
			
			st.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private static void createRelatedSyncStageAreaTable(SyncTableConfiguration config, Connection conn) throws DBException {
		String tableName = config.generateRelatedStageTableName();
		
		String sql = "";
		String notNullConstraint = "NOT NULL";
		String endLineMarker = ",\n";
		
		sql += "CREATE TABLE " + config.generateFullStageTableName() + "(\n";
		sql += DBUtilities.generateTableAutoIncrementField("id", conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("record_origin_id", notNullConstraint, conn)+ endLineMarker;
		sql += DBUtilities.generateTableVarcharField("record_origin_location_code", 100, notNullConstraint, conn) + endLineMarker;
		sql += DBUtilities.generateTableTextField("json", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeField("last_sync_date", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableVarcharField("last_sync_try_err", 250, "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeField("last_update_date", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableNumericField("consistent", 1, "NULL", -1, conn) + endLineMarker;
		sql += DBUtilities.generateTableNumericField("migration_status", 1, "NULL", 1, conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeFieldWithDefaultValue("creation_date", conn) + endLineMarker;
		
		sql += DBUtilities.generateTableDateTimeField("record_date_created", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeField("record_date_changed", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableDateTimeField("record_date_voided", "NULL", conn) + endLineMarker;
		sql += DBUtilities.generateTableBigIntField("destination_id", "NULL", conn) + endLineMarker;
		
		String checkCondition = "migration_status = -1 OR migration_status = 0 OR migration_status = 1";
		String keyName = "CHK_" + config.generateRelatedStageTableName() + "_MIG_STATUS";
		
		sql += DBUtilities.generateTableCheckConstraintDefinition(keyName, checkCondition, conn) + endLineMarker;
		
		String uniqueKeyName = tableName + "_UNQ_RECORD_ID".toLowerCase();
		
		if (config.isDestinationInstallationType() || config.isDBQuickLoad()
		        || config.isDBQuickCopy()) {
			
			sql += DBUtilities.generateTableUniqueKeyDefinition(uniqueKeyName, "record_origin_id, record_origin_location_code",
			    conn) + endLineMarker;
			
		} else {
			sql += DBUtilities.generateTableUniqueKeyDefinition(uniqueKeyName, "record_origin_id", conn) + endLineMarker;
		}
		
		sql += DBUtilities.generateTablePrimaryKeyDefinition("id", tableName + "_pk", conn);
		sql += ")";
		
		try {
			Statement st = conn.createStatement();
			st.addBatch(sql);
			st.executeBatch();
			
			st.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public static void main(String[] args) throws IOException, SQLException {
		//Mysql
		/*String dataBaseUserName = "root";
		String dataBaseUserPassword = "#eIPDB123#";
		String connectionURI = "jdbc:mysql://10.10.2.2:53307/test?autoReconnect=true&useSSL=false";
		String driveClassName = "com.mysql.jdbc.Driver";*/
		
		//Oracle
		/*
		String dataBaseUserName = "sys_contas";
		String dataBaseUserPassword = "exi2k12";
		String connectionURI = "jdbc:oracle:thin:@127.0.0.1:1521:xe";
		String driveClassName = "oracle.jdbc.OracleDriver";*/
		
		//Postgres
		/*String dataBaseUserName = "mozart";
		String dataBaseUserPassword = "moZart123";
		String connectionURI = "jdbc:postgresql://10.10.2.2:5433/postgres";
		String driveClassName = "org.postgresql.Driver";*/
		
		/*DBConnectionInfo dbConnInfo = new DBConnectionInfo(dataBaseUserName, dataBaseUserPassword, connectionURI,
	        driveClassName);
	
		DBConnectionService service = DBConnectionService.init(dbConnInfo);
		
		OpenConnection conn = service.openConnection();*/
		
		
		SyncConfiguration syncConfig = SyncConfiguration.loadFromFile(new File("D:\\JEE\\Workspace\\FGH\\eptssync\\conf\\mozart\\db_copy_mozart_postgres.json"));
			
		OpenConnection conn = syncConfig.getAppsInfo().get(1).openConnection();
		
		/*createRelatedSyncStageAreaTable(syncConfig.getTablesConfigurations().get(0), conn);
		conn.markAsSuccessifullyTerminected();
		conn.finalizeConnection();
		
		conn = syncConfig.getAppsInfo().get(1).openConnection();*/
		
		//createRelatedSyncStageAreaUniqueKeysTable(syncConfig.getTablesConfigurations().get(0), conn);
		
		
		conn.markAsSuccessifullyTerminected();
		conn.finalizeConnection();
		
		/*conn = syncConfig.getMainApp().openConnection();
		createRelatedSyncStageAreaTable(syncConfig.getTablesConfigurations().get(0), conn);
		
		conn.markAsSuccessifullyTerminected();
		conn.finalizeConnection();
		*/
		
		conn = syncConfig.getMainApp().openConnection();
		
		
		createRelatedSyncStageAreaUniqueKeysTable(syncConfig.getTablesConfigurations().get(0), conn);
		
		
		conn.markAsSuccessifullyTerminected();
		conn.finalizeConnection();
		
		//conn = syncConfig.getMainApp().openConnection();
		//System.out.println("Schema " + determineSchemaName(conn));
		
		/*
		try {
			String resourceName = "test1_username_key";
			String resourceType = RESOURCE_TYPE_INDEX;
			String resourceTable = "test1";
			String resourceSchema = "mozart2_test";
			
			
			boolean b = isResourceExist(resourceSchema, resourceTable, resourceType, resourceName, conn);
			
			System.out.println(b ? resourceType + " " + resourceSchema + "." + resourceName + " exists" : "Resource does not exist");
			
			
			
		}
		catch (SQLException e) {
			System.out.println(e.getLocalizedMessage());
		}
		*/
	}
	
	public static void enableForegnKeyChecks(Connection conn) throws DBException {
		try {
			if (isMySQLDB(conn)) {
				Statement st = conn.createStatement();
				
				st.addBatch("SET FOREIGN_KEY_CHECKS=1");
				
				st.executeBatch();
				
				st.close();
				
			} else
				throw new RuntimeException("Database not supported!");
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new DBException(e);
		}
	}
	
	public static boolean isTableColumnAllowNull(String tableName, String columnName, Connection conn) throws DBException {
		try {
			PreparedStatement st = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE 1 != 1");
			
			ResultSet rs = st.executeQuery();
			ResultSetMetaData rsMetaData = rs.getMetaData();
			
			for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
				
				if (rsMetaData.getColumnName(i).equalsIgnoreCase(columnName)) {
					return rsMetaData.isNullable(i) == ResultSetMetaData.columnNullable;
				}
			}
			
			throw new ForbiddenOperationException(
			        "There is no such column '" + columnName + "' on table '" + tableName + "'");
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new DBException(e);
		}
	}
	
	public static String determineColunType(String tableName, String columnName, Connection conn) throws DBException {
		try {
			PreparedStatement st = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE 1 != 1");
			
			ResultSet rs = st.executeQuery();
			ResultSetMetaData rsMetaData = rs.getMetaData();
			
			for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
				
				if (rsMetaData.getColumnName(i).equalsIgnoreCase(columnName)) {
					return rsMetaData.getColumnTypeName(i);
				}
			}
			
			throw new ForbiddenOperationException(
			        "There is no such column '" + columnName + "' on table '" + tableName + "'");
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new DBException(e);
		}
	}
	
	public static boolean isIndexExistsOnTable(String tableSchema, String tableName, String indexName, Connection conn)
	        throws SQLException {
		if (isMySQLDB(conn)) {
			return isMySQLIndexExistsOnTable(tableSchema, tableName, indexName, conn);
		}
		
		throw new RuntimeException("Database not supported!");
	}
	
	public static boolean isMySQLIndexExistsOnTable(String tableSchema, String tableName, String indexName, Connection conn)
	        throws SQLException {
		String fullTableName = tableSchema + "/" + tableName;
		
		String selectQuery = "";
		
		selectQuery += " SELECT * \n";
		selectQuery += " FROM INFORMATION_SCHEMA.INNODB_SYS_INDEXES INNER JOIN INFORMATION_SCHEMA.INNODB_SYS_TABLES ON INNODB_SYS_INDEXES.TABLE_ID = INNODB_SYS_TABLES.TABLE_ID";
		selectQuery += " WHERE 	1 = 1\n";
		selectQuery += " 		AND  INNODB_SYS_TABLES.NAME = '" + fullTableName + "'\n";
		selectQuery += " 		AND  INNODB_SYS_INDEXES.NAME = '" + indexName + "'\n";
		
		PreparedStatement statement = conn.prepareStatement(selectQuery);
		
		ResultSet result = statement.executeQuery();
		
		return result.next();
	}
	
	public static boolean isResourceExist(String resourceSchema, String resourceTable, String resourceType, String resourceName, Connection conn)
	        throws DBException {
		if (isMySQLDB(conn)) {
			return isMySQLResourceExist(resourceSchema, resourceTable, resourceType, resourceName, conn);
		}
		
		if (isPostgresDB(conn)) {
			return isPostgresResourceExist(resourceSchema, resourceTable, resourceType, resourceName, conn);
		}
		
		throw new RuntimeException("Database not supported!");
	}

	private static boolean isPostgresResourceExist(String resourceSchema, String resourceTable, String resourceType, String resourceName,
	        Connection conn) throws DBException {
		String resourceSchemaCondition = "1 = 1";
		String resourceTableCondition = "1 = 1";
		String resourceNameCondition = "1 = 1";
		String fromClause = "";
		
		if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_INDEX)) {
			fromClause = "pg_indexes";
			resourceSchemaCondition = "schemaname = '" + resourceSchema + "'";
			resourceTableCondition = "tablename = '" + resourceTable+ "'";
			resourceNameCondition = "indexname = '" + resourceName  + "'";
		} else if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_TRIGGER)) {
			fromClause = "INFORMATION_SCHEMA.TRIGGERS";
			resourceNameCondition = "TRIGGER_NAME = '" + resourceName + "'";
			resourceTableCondition = "EVENT_OBJECT_TABLE = '" + resourceTable + "'";
			resourceSchemaCondition = "EVENT_OBJECT_SCHEMA = '" + resourceSchema + "'";
		} else if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_TABLE)) {
			fromClause = "pg_tables";
			resourceNameCondition = "tablename = '" + resourceName + "'";
			resourceSchemaCondition = "schemaname = '" + resourceSchema + "'";
		} else if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_SCHEMA)) {
			fromClause = "INFORMATION_SCHEMA.SCHEMATA";
			
			resourceNameCondition = "SCHEMA_NAME = '" + resourceName + "'";
		} else
			throw new ForbiddenOperationException("Resource not supported");
		
		String selectQuery = "";
		
		selectQuery += " SELECT * \n";
		selectQuery += " FROM " + fromClause + "\n";
		selectQuery += " WHERE 	1 = 1\n";
		selectQuery += " 		AND  " + resourceSchemaCondition + "\n";
		selectQuery += " 		AND  " + resourceTableCondition + "\n";
		selectQuery += "		AND  " + resourceNameCondition;
		
		try {
			PreparedStatement statement = conn.prepareStatement(selectQuery);
			
			ResultSet result = statement.executeQuery();
			
			return result.next();
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	private static boolean isMySQLResourceExist(String resourceSchema, String resourceTable, String resourceType, String resourceName,
	        Connection conn) throws DBException {
		String resourceSchemaCondition = "";
		String resourceNameCondition = "";
		String fromClause = "";
		
		if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_INDEX)) {
			fromClause = "INFORMATION_SCHEMA.INNODB_SYS_INDEXES";
			resourceNameCondition = "NAME = '" + resourceName + "'";
			resourceSchemaCondition = "1 = 1";
			
		} else if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_TRIGGER)) {
			fromClause = "INFORMATION_SCHEMA.TRIGGERS";
			resourceNameCondition = "TRIGGER_NAME = '" + resourceName + "'";
			resourceSchemaCondition = "EVENT_OBJECT_SCHEMA = '" + resourceSchema + "'";
		} else if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_TABLE)) {
			fromClause = "INFORMATION_SCHEMA.TABLES";
			resourceNameCondition = "TABLE_NAME = '" + resourceName + "'";
			resourceSchemaCondition = "TABLE_SCHEMA = '" + resourceSchema + "'";
		} else if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_SCHEMA)) {
			fromClause = "INFORMATION_SCHEMA.SCHEMATA";
			
			resourceNameCondition = "SCHEMA_NAME = '" + resourceName + "'";
			
			resourceSchemaCondition = "1 = 1";
		} else
			throw new ForbiddenOperationException("Resource not supported");
		
		String selectQuery = "";
		
		selectQuery += " SELECT * \n";
		selectQuery += " FROM " + fromClause + "\n";
		selectQuery += " WHERE 	1 = 1\n";
		selectQuery += " 		AND  " + resourceSchemaCondition + "\n";
		selectQuery += "		AND  " + resourceNameCondition;
		
		try {
			PreparedStatement statement = conn.prepareStatement(selectQuery);
			
			ResultSet result = statement.executeQuery();
			
			return result.next();
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static boolean isColumnExistOnTable(String tableName, String columnName, Connection conn) throws SQLException {
		PreparedStatement st = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE 1 != 1");
		
		ResultSet rs = st.executeQuery();
		ResultSetMetaData rsMetaData = rs.getMetaData();
		
		for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
			String cName = rsMetaData.getColumnName(i);
			
			if (cName.toLowerCase().equals(columnName))
				return true;
		}
		
		return false;
	}
	
	public static Field getPrimaryKey(String tableName, String schema, Connection conn) throws DBException {
		
		Field att = null;
		
		try {
			String db = determineDataBaseFromConnection(conn);
			
			if (!db.equals(MYSQL_DATABASE))
				throw new ForbiddenOperationException("Unsupported DB Type [" + db + "]");
			
			ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, tableName);
			
			if (rs.next()) {
				String primaryKey = rs.getString("COLUMN_NAME");
				
				String primaryKeyType = DBUtilities.determineColunType(tableName, primaryKey, conn);
				
				att = new Field(primaryKey);
				
				att.setType(primaryKeyType);
			}
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		return att;
	}
	
	public static List<UniqueKeyInfo> getUniqueKeys(String tableName, String schema, Connection conn) throws DBException {
		List<UniqueKeyInfo> uniqueKeys = new ArrayList<UniqueKeyInfo>();
		
		try {
			ResultSet rs = conn.getMetaData().getIndexInfo(null, schema, tableName, true, true);
			
			String prevIndexName = null;
			
			List<String> keyElements = new ArrayList<String>();
			
			Field primaryKey = getPrimaryKey(tableName, schema, conn);
			
			String indexName = null;
			
			boolean starting = true;
			
			while (rs.next()) {
				
				indexName = rs.getString("INDEX_NAME");
				
				if (!indexName.equals(prevIndexName)) {
					if (starting) {
						starting = false;
					} else {
						addUniqueKey(prevIndexName, keyElements, uniqueKeys, primaryKey.getName());
					}
					
					prevIndexName = indexName;
					keyElements = new ArrayList<String>();
				}
				
				keyElements.add(rs.getString("COLUMN_NAME"));
			}
			
			addUniqueKey(indexName, keyElements, uniqueKeys, primaryKey.getName());
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		return uniqueKeys;
	}
	
	private static boolean addUniqueKey(String keyName, List<String> keyElements, List<UniqueKeyInfo> uniqueKeys,
	        String primaryKey) {
		if (keyElements == null || keyElements.isEmpty())
			return false;
		
		//Don't add PK as uniqueKey
		if (keyElements.size() == 1 && keyElements.get(0).equals(primaryKey)) {
			return false;
		}
		
		UniqueKeyInfo uk = UniqueKeyInfo.generateFromFieldList(keyElements);
		uk.setKeyName(keyName);
		
		uniqueKeys.add(uk);
		
		return true;
	}
	
	public static List<Field> getTableFields(String tableName, String schema, Connection conn) throws DBException {
		List<Field> fields = new ArrayList<Field>();
		
		PreparedStatement st;
		ResultSet rs;
		ResultSetMetaData rsMetaData;
		
		try {
			st = conn.prepareStatement("SELECT * FROM " + schema + "." + tableName + " WHERE 1 != 1");
			
			rs = st.executeQuery();
			rsMetaData = rs.getMetaData();
			
			int qtyAttrs = rsMetaData.getColumnCount();
			
			for (int i = 1; i <= qtyAttrs; i++) {
				Field field = new Field(rsMetaData.getColumnName(i));
				field.setType(rsMetaData.getColumnTypeName(i));
				
				fields.add(field);
			}
			
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		
		return fields;
	}
	
	public static String convertMySQLTypeTOJavaType(String mySQLTypeName) {
		mySQLTypeName = mySQLTypeName.toUpperCase();
		
		if (utilities.isStringIn(mySQLTypeName, "INT", "MEDIUMINT"))
			return "Integer";
		if (utilities.isStringIn(mySQLTypeName, "TINYINT", "BIT"))
			return "byte";
		if (utilities.isStringIn(mySQLTypeName, "YEAR", "SMALLINT"))
			return "short";
		if (utilities.isStringIn(mySQLTypeName, "BIGINT"))
			return "Long";
		if (utilities.isStringIn(mySQLTypeName, "DECIMAL", "NUMERIC", "SMALLINT", "REAL", "DOUBLE"))
			return "double";
		if (utilities.isStringIn(mySQLTypeName, "FLOAT", "NUMERIC", "SMALLINT"))
			return "float";
		if (utilities.isStringIn(mySQLTypeName, "VARCHAR", "CHAR"))
			return "String";
		if (utilities.isStringIn(mySQLTypeName, "VARBINARY", "BLOB", "TEXT", "LONGBLOB"))
			return "byte[]";
		if (utilities.isStringIn(mySQLTypeName, "DATE", "DATETIME", "TIME", "TIMESTAMP"))
			return "java.util.Date";
		
		throw new ForbiddenOperationException("Unknown data type [" + mySQLTypeName + "]");
	}
	
	public static void dropTable(String schema, String tableName, Connection conn) throws DBException {
		executeBatch(conn, "drop table " + schema + "." + tableName);
	}
	
	public static void renameTable(String schema, String oldTableName, String newTableName, Connection conn)
	        throws DBException {
		try {
			if (isMySQLDB(conn)) {
				renameMySQLTable(schema, oldTableName, newTableName, conn);
			} else
				throw new ForbiddenOperationException(
				        "Unsupported DB Engine.. [" + determineDataBaseFromConnection(conn) + "]");
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	private static void renameMySQLTable(String schema, String oldTableName, String newTableName, Connection conn)
	        throws DBException {
		String sql = "";
		
		sql += "RENAME TABLE " + schema + "." + oldTableName + " TO " + schema + "." + newTableName + ";";
		
		executeBatch(conn, sql);
	}
	
	public static void executeBatch(Connection conn, String... batches) throws DBException {
		
		try {
			Statement st = conn.createStatement();
			
			for (String batch : batches) {
				st.addBatch(batch);
			}
			
			st.executeBatch();
			
			st.close();
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	
	public static String generateTableAutoIncrementField(String fieldName, Connection conn) throws DBException {
		if (isMySQLDB(conn)) {
			return fieldName + " bigint NOT NULL AUTO_INCREMENT"; 
		}
		else if (isPostgresDB(conn)) {
			return fieldName + " serial NOT NULL"; 
		}
		
		throw new DatabaseNotSupportedException(conn); 	
			
	}
	
	public static String generateTablePrimaryKeyDefinition(String fieldName, String pkName, Connection conn) throws DBException {
		return  " CONSTRAINT " + pkName + " PRIMARY KEY (" + fieldName + ")";  
	}
	
	public static String generateTableVarcharField(String fieldName, int precision, String constraint, Connection conn) throws DBException {
		return fieldName + " VARCHAR(" + precision + ") " + constraint; 
	}

	public static String generateTableTextField(String fieldName, String constraint, Connection conn) throws DBException {
		return fieldName + " text " + constraint; 
	}
	
	public static String generateTableIntegerField(String fieldName, String constraint, Connection conn) throws DBException {
		return fieldName + " INTEGER " + constraint; 
	}
	
	public static String generateTableNumericField(String fieldName, int precision, String constraint, Number defaultValue, Connection conn) throws DBException {
		return fieldName + " NUMERIC (" + precision + ")" + constraint + " DEFAULT " + defaultValue; 
	}
	
	public static String generateTableBigIntField(String fieldName, String constraint, Connection conn) throws DBException {
		return fieldName + " BIGINT " + constraint; 
	}
	
	public static String generateTableDateTimeField(String fieldName, String constraint, Connection conn) throws DBException {
		return fieldName + " TIMESTAMP " + constraint; 
	}
	
	public static String generateTableDateTimeFieldWithDefaultValue(String fieldName, Connection conn) throws DBException {
		String definition = generateTableDateTimeField(fieldName, "", conn);
		
		if (isMySQLDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
			
		}
		else if (isPostgresDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
		}
		else if (isOracleDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
		}
		
		throw new DatabaseNotSupportedException(conn); 	 
	}

	public static String generateTableDateField(String fieldName, String constraint, Connection conn) throws DBException {
		return fieldName + " DATE " + constraint; 
	}
	
	public static String generateTableUniqueKeyDefinition(String uniqueKeyName, String uniqueKeyFields, Connection conn) throws DBException {
		if (isMySQLDB(conn)) {
			return "UNIQUE KEY " + uniqueKeyName  + "(" + uniqueKeyFields + ")";
			
		}
		else if (isPostgresDB(conn)) {
			return "CONSTRAINT " + uniqueKeyName  + " UNIQUE (" + uniqueKeyFields + ")";
		}
		else if (isOracleDB(conn)) {
			return "CONSTRAINT " + uniqueKeyName  + " UNIQUE (" + uniqueKeyFields + ")";
		}
		
		throw new DatabaseNotSupportedException(conn); 	
	}

	public static String generateTableForeignKeyDefinition(String keyName, String field, String parentTableName, String parentField, Connection conn) throws DBException {
		return "CONSTRAINT " + keyName + " FOREIGN KEY (" + field + ") REFERENCES " + parentTableName + " (" + parentField + ")";
	}
	

	public static String generateTableCheckConstraintDefinition(String keyName, String checkCondition, Connection conn) throws DBException {
		return "CONSTRAINT " + keyName + " CHECK (" + checkCondition + ")";
	}
		
}
