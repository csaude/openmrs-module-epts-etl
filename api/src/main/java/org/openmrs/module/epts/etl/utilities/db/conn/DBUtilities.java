package org.openmrs.module.epts.etl.utilities.db.conn;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.controller.conf.ParameterValueType;
import org.openmrs.module.epts.etl.controller.conf.QueryParameter;
import org.openmrs.module.epts.etl.controller.conf.SyncConfiguration;
import org.openmrs.module.epts.etl.controller.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.exceptions.DatabaseNotSupportedException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * @author JPBOANE
 * @version 1.0 27/12/2012
 */
public class DBUtilities {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static final String ORACLE_DATABASE = "ORACLE";
	
	public static final String MYSQL_DATABASE = "MYSQL";
	
	public static final String POSTGRESQL_DATABASE = "POSTGRES";
	
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
	
	private static String determineDataBaseFromString(String msg) {
		if (msg.toUpperCase().contains("MYSQL")) {
			return MYSQL_DATABASE;
		} else if (msg.toUpperCase().contains("POSTGRESQL")) {
			return POSTGRESQL_DATABASE;
		} else if (msg.toUpperCase().contains("SQLSERVER")) {
			return SQLSERVER_DATABASE;
		} else if (msg.toUpperCase().contains("ORA-")) {
			return ORACLE_DATABASE;
		}
		
		return null;
	}
	
	public static String determineDataBaseFromException(SQLException sqlExcetion) {
		String db = determineDataBaseFromString(sqlExcetion.getClass().getName());
		
		if (db != null)
			return db;
		
		if (sqlExcetion.getCause() != null) {
			db = determineDataBaseFromString(sqlExcetion.getCause().getClass().getName());
			
			if (db != null)
				return db;
		}
		
		if (sqlExcetion.getLocalizedMessage() != null) {
			db = determineDataBaseFromString(sqlExcetion.getLocalizedMessage().toString());
			if (db != null)
				return db;
		}
		
		StackTraceElement[] trace = sqlExcetion.getStackTrace();
		
		for (int i = 0; i < trace.length; i++) {
			db = determineDataBaseFromString((trace[i].toString()));
			
			if (db != null)
				return db;
		}
		
		return UNKNOWN_DATABASE;
	}
	
	public static boolean isPostgresDB(Connection conn) throws DBException {
		try {
			return determineDataBaseFromConnection(conn).equals(POSTGRESQL_DATABASE);
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
	
	public static boolean isSqlServerDB(Connection conn) throws DBException {
		try {
			return determineDataBaseFromConnection(conn).equals(SQLSERVER_DATABASE);
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
			return POSTGRESQL_DATABASE;
		if (str.contains("SQLSERVER") || str.contains("SQL SERVER"))
			return SQLSERVER_DATABASE;
		
		throw new RuntimeException("Impossivel determinar a base de dados a partir da conexao");
	}
	
	public static boolean isSameDatabaseServer(Connection srcConn, Connection dstConn) throws DBException {
		try {
			String srcDatabaseType = DBUtilities.determineDataBaseFromConnection(srcConn);
			String dstDatabaseType = DBUtilities.determineDataBaseFromConnection(dstConn);
			
			if (!srcDatabaseType.equals(dstDatabaseType)) {
				return false;
			}
			
			DatabaseMetaData srcMetadata = srcConn.getMetaData();
			DatabaseMetaData dstMetadata = dstConn.getMetaData();
			
			String srcUrl = srcMetadata.getURL();
			String dstUrl = dstMetadata.getURL();
			
			if (isPostgresDB(srcConn)) {
				return srcUrl.equals(dstUrl);
			} else if (isOracleDB(srcConn)) {
				return srcUrl.equals(dstUrl);
			} else if (isMySQLDB(srcConn)) {
				String srcHostAndPort = srcUrl.split("//")[1].split("/")[0];
				String dstHostAndPort = dstUrl.split("//")[1].split("/")[0];
				
				return srcHostAndPort.equals(dstHostAndPort);
			}
			
			throw new ForbiddenOperationException("Unsupported database [" + srcDatabaseType + "]");
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	/**
	 * Replaces all sql parameters with a question mark. It assumes that the parameters will have
	 * format '@paramName'
	 * 
	 * @param sqlQuery
	 * @return
	 */
	public static String replaceSqlParametersWithQuestionMarks(String sqlQuery) {
		// Regular expression to match parameters starting with @, considering optional spaces or newlines
		String parameterRegex = "@\\s*\\w+";
		Pattern pattern = Pattern.compile(parameterRegex);
		Matcher matcher = pattern.matcher(sqlQuery);
		
		// Replace each parameter with a question mark
		StringBuffer replacedQuery = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(replacedQuery, "?");
		}
		matcher.appendTail(replacedQuery);
		
		return replacedQuery.toString();
	}
	
	public static String tryToPutSchemaOnDatabaseObject(String tableName, Connection conn) throws DBException {
		
		try {
			String[] tableNameComposition = tableName.split("\\.");
			
			if (tableNameComposition != null && tableNameComposition.length > 1)
				return tableName;
			
			return determineSchemaName(conn) + "." + tableName;
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static String tryToPutSchemaOnInsertScript(String sql, Connection conn) throws DBException {
		String tableName = (sql.toUpperCase().split("INSERT INTO")[1]).split("\\(")[0];
		
		String[] tableNameComposition = tableName.split("\\.");
		
		if (tableNameComposition != null && tableNameComposition.length > 1)
			return sql;
		
		String fullTableName = tryToPutSchemaOnDatabaseObject(utilities.removeAllEmptySpace(tableName), conn);
		
		return sql.toUpperCase().replaceFirst(tableName, " " + fullTableName);
	}
	
	public static String addInsertIgnoreOnInsertScript(String sql, Connection conn) throws DBException {
		if (!isMySQLDB(conn))
			return sql;
		
		return sql.toUpperCase().replaceFirst("INSERT", "INSERT IGNORE");
	}
	
	public static String tryToPutSchemaOnUpdateScript(String sql, Connection conn) throws DBException {
		String tableName = (sql.toUpperCase().split("UPDATE ")[1]).split(" ")[0];
		
		String[] tableNameComposition = tableName.split("\\.");
		
		if (tableNameComposition != null && tableNameComposition.length > 1)
			return sql;
		
		String fullTableName = tryToPutSchemaOnDatabaseObject(utilities.removeAllEmptySpace(tableName), conn);
		
		return sql.toUpperCase().replaceFirst(tableName, " " + fullTableName);
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
			
			tableName = tryToPutSchemaOnDatabaseObject(tableName, conn);
			
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
			tableName = tryToPutSchemaOnDatabaseObject(tableName, conn);
			
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
	
	public static boolean checkIfTableUseAutoIcrement(String tableName, Connection conn) throws DBException {
		try {
			tableName = tryToPutSchemaOnDatabaseObject(tableName, conn);
			
			PreparedStatement st = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE 1 != 1");
			
			ResultSet rs = st.executeQuery();
			ResultSetMetaData rsMetaData = rs.getMetaData();
			
			for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
				if (rsMetaData.isAutoIncrement(i)) {
					return true;
				}
			}
			
			return false;
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
		String fullTableName = tryToPutSchemaOnDatabaseObject(tableName, conn);
		
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
	
	public static boolean isResourceExist(String resourceSchema, String resourceTable, String resourceType,
	        String resourceName, Connection conn) throws DBException {
		if (isMySQLDB(conn)) {
			return isMySQLResourceExist(resourceSchema, resourceTable, resourceType, resourceName, conn);
		}
		
		if (isPostgresDB(conn)) {
			return isPostgresResourceExist(resourceSchema, resourceTable, resourceType, resourceName, conn);
		}
		
		if (isSqlServerDB(conn)) {
			return isSqlServerResourceExist(resourceSchema, resourceTable, resourceType, resourceName, conn);
		}
		
		throw new RuntimeException("Database not supported!");
	}
	
	public static boolean isTableExists(String schema, String tableName, Connection conn) throws DBException {
		return isResourceExist(schema, null, RESOURCE_TYPE_TABLE, tableName, conn);
	}
	
	private static boolean isPostgresResourceExist(String resourceSchema, String resourceTable, String resourceType,
	        String resourceName, Connection conn) throws DBException {
		String resourceSchemaCondition = "1 = 1";
		String resourceTableCondition = "1 = 1";
		String resourceNameCondition = "1 = 1";
		String fromClause = "";
		
		if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_INDEX)) {
			fromClause = "pg_indexes";
			resourceSchemaCondition = "schemaname = '" + resourceSchema + "'";
			resourceTableCondition = "tablename = '" + resourceTable + "'";
			resourceNameCondition = "indexname = '" + resourceName + "'";
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
	
	private static boolean isSqlServerResourceExist(String resourceSchema, String resourceTable, String resourceType,
	        String resourceName, Connection conn) throws DBException {
		String resourceSchemaCondition = "1 = 1";
		String resourceTableCondition = "1 = 1";
		String resourceNameCondition = "1 = 1";
		String fromClause = "";
		
		if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_INDEX)) {
			throw new ForbiddenOperationException("Resource not supported");
		} else if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_TRIGGER)) {
			throw new ForbiddenOperationException("Resource not supported");
		} else if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_TABLE)) {
			fromClause = "INFORMATION_SCHEMA.TABLES";
			resourceNameCondition = "TABLE_NAME = '" + resourceName + "'";
			resourceSchemaCondition = "TABLE_SCHEMA = '" + resourceSchema + "'";
		} else if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_SCHEMA)) {
			fromClause = "sys.schemas";
			
			resourceNameCondition = "name = '" + resourceName + "'";
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
	
	private static boolean isMySQLResourceExist(String resourceSchema, String resourceTable, String resourceType,
	        String resourceName, Connection conn) throws DBException {
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
		tableName = tryToPutSchemaOnDatabaseObject(tableName, conn);
		
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
	
	public static int getQtyQuestionMarksOnQuery(String query) {
		String[] parts = query.trim().split("\\?");
		
		if (query.endsWith("?")) {
			return parts.length;
		} else {
			return parts.length - 1;
		}
	}
	
	public static List<Field> determineFieldsFromQuery(String query, Connection conn) throws DBException {
		List<Field> fields = new ArrayList<Field>();
		
		PreparedStatement st;
		ResultSet rs;
		ResultSetMetaData rsMetaData;
		
		try {
			st = conn.prepareStatement(query);
			
			int qtyQuestionMarksOnQuery = getQtyQuestionMarksOnQuery(query);
			
			if (qtyQuestionMarksOnQuery > 0) {
				Object[] params = new Object[qtyQuestionMarksOnQuery];
				
				for (int i = 0; i < qtyQuestionMarksOnQuery; i++) {
					params[i] = null;
				}
				
				BaseDAO.loadParamsToStatment(st, params, conn);
			}
			
			rs = st.executeQuery();
			rsMetaData = rs.getMetaData();
			
			int qtyAttrs = rsMetaData.getColumnCount();
			
			for (int i = 1; i <= qtyAttrs; i++) {
				Field field = new Field(rsMetaData.getColumnLabel(i));
				field.setType(rsMetaData.getColumnTypeName(i));
				
				fields.add(field);
			}
			
		}
		catch (SQLException e) {
			e.printStackTrace();
			
			throw new DBException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return fields;
	}
	
	/**
	 * Extract all the parameters presents in a sql query. This assume that the parameter will start
	 * with @
	 * 
	 * @param sqlQuery the query to extract from
	 * @return the list of extracted parameters name
	 */
	public static List<Field> extractAllParamNamesOnQuery(String sqlQuery) {
		List<Field> parameters = new ArrayList<>();
		
		// Regular expression to match parameters starting with @ followed by optional spaces and then the parameter name
		String parameterRegex = "@\\s*(\\w+)";
		Pattern pattern = Pattern.compile(parameterRegex);
		Matcher matcher = pattern.matcher(sqlQuery);
		
		// Find all matches and add only the parameter names to the list
		while (matcher.find()) {
			parameters.add(new Field(matcher.group(1)));
		}
		
		return parameters;
	}
	
	public static List<Field> determineFieldsFromQuery(String query) {
		
		String sqlFields = (query.toLowerCase().split("select")[1]).split("from")[0];
		
		String[] fieldsName = sqlFields.split(",");
		
		List<Field> fields = new ArrayList<>();
		
		for (String s : fieldsName) {
			if (s.contains("*")) {
				throw new ForbiddenOperationException("Unable to determine field from '*'");
			}
			
			s = utilities.removeDuplicatedEmptySpace(s.trim());
			
			String fieldName = null;
			
			if (s.split(" as ").length > 1) {
				fieldName = s.split(" as ")[1];
			} else if (s.split("\\.").length > 1) {
				fieldName = s.split("\\.")[1];
			} else {
				fieldName = s;
			}
			
			fields.add(new Field(fieldName.trim()));
			
		}
		
		return fields;
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
		tableName = tryToPutSchemaOnDatabaseObject(tableName, conn);
		
		executeBatch(conn, "drop table " + tableName);
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
		} else if (isPostgresDB(conn)) {
			return fieldName + " serial NOT NULL";
		} else if (isSqlServerDB(conn)) {
			return fieldName + " int IDENTITY(1,1)";
		}
		
		throw new DatabaseNotSupportedException(conn);
	}
	
	public static String generateTablePrimaryKeyDefinition(String fieldName, String pkName, Connection conn)
	        throws DBException {
		return " CONSTRAINT " + pkName + " PRIMARY KEY (" + fieldName + ")";
	}
	
	public static String generateTableVarcharField(String fieldName, int precision, String constraint, Connection conn)
	        throws DBException {
		return fieldName + " VARCHAR(" + precision + ") " + constraint;
	}
	
	public static String generateTableTextField(String fieldName, String constraint, Connection conn) throws DBException {
		return fieldName + " text " + constraint;
	}
	
	public static String generateTableIntegerField(String fieldName, String constraint, Connection conn) throws DBException {
		return fieldName + " INTEGER " + constraint;
	}
	
	public static String generateTableNumericField(String fieldName, int precision, String constraint, Number defaultValue,
	        Connection conn) throws DBException {
		return fieldName + " NUMERIC (" + precision + ")" + constraint + " DEFAULT " + defaultValue;
	}
	
	public static String generateTableBigIntField(String fieldName, String constraint, Connection conn) throws DBException {
		return fieldName + " BIGINT " + constraint;
	}
	
	public static String generateTableDateTimeField(String fieldName, String constraint, Connection conn)
	        throws DBException {
		return fieldName + " datetime " + constraint;
	}
	
	public static String generateTableDateTimeFieldWithDefaultValue(String fieldName, Connection conn) throws DBException {
		String definition = generateTableDateTimeField(fieldName, "", conn);
		
		if (isMySQLDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
			
		} else if (isPostgresDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
		} else if (isOracleDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
		} else if (isSqlServerDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
		}
		
		throw new DatabaseNotSupportedException(conn);
	}
	
	public static String generateTableTimeStampField(String fieldName, Connection conn) throws DBException {
		String definition = fieldName + " TIMESTAMP ";
		
		if (isMySQLDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
			
		} else if (isPostgresDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
		} else if (isOracleDB(conn)) {
			return definition += " DEFAULT CURRENT_TIMESTAMP";
		} else if (isSqlServerDB(conn)) {
			return definition;
		}
		
		throw new DatabaseNotSupportedException(conn);
	}
	
	public static String generateTableDateField(String fieldName, String constraint, Connection conn) throws DBException {
		return fieldName + " DATE " + constraint;
	}
	
	public static String generateTableUniqueKeyDefinition(String uniqueKeyName, String uniqueKeyFields, Connection conn)
	        throws DBException {
		if (isMySQLDB(conn)) {
			return "UNIQUE KEY " + uniqueKeyName + "(" + uniqueKeyFields + ")";
			
		} else if (isPostgresDB(conn)) {
			return "CONSTRAINT " + uniqueKeyName + " UNIQUE (" + uniqueKeyFields + ")";
		} else if (isOracleDB(conn)) {
			return "CONSTRAINT " + uniqueKeyName + " UNIQUE (" + uniqueKeyFields + ")";
		} else if (isSqlServerDB(conn)) {
			return "CONSTRAINT " + uniqueKeyName + " UNIQUE (" + uniqueKeyFields + ")";
		}
		
		throw new DatabaseNotSupportedException(conn);
	}
	
	public static String generateTableForeignKeyDefinition(String keyName, String field, String parentTableName,
	        String parentField, Connection conn) throws DBException {
		return "CONSTRAINT " + keyName + " FOREIGN KEY (" + field + ") REFERENCES " + parentTableName + " (" + parentField
		        + ")";
	}
	
	public static String generateTableCheckConstraintDefinition(String keyName, String checkCondition, Connection conn)
	        throws DBException {
		return "CONSTRAINT " + keyName + " CHECK (" + checkCondition + ")";
	}
	
	public static Object[] loadParamsValues(String query, SyncConfiguration relatedSyncConfiguration) {
		List<QueryParameter> paramConfig = new ArrayList<>();
		
		List<Field> params = extractAllParamNamesOnQuery(query);
		
		for (Field param : params) {
			QueryParameter qp = new QueryParameter(param.getName(), null);
			qp.setValueType(ParameterValueType.CONFIGURATION_PARAM);
			
			paramConfig.add(qp);
		}
		
		DatabaseObject srcObject = null;
		
		return loadParamsValues(query, paramConfig, srcObject, relatedSyncConfiguration);
	}
	
	/**
	 * Load values to given query parameters
	 * 
	 * @param queryParameters
	 * @param srcObject
	 * @return
	 */
	public static Object[] loadParamsValues(String query, List<QueryParameter> paramConfig, DatabaseObject srcObject,
	        SyncConfiguration configuration) {
		List<Field> queryParameters = null;
		
		queryParameters = DBUtilities.extractAllParamNamesOnQuery(query);
		
		if (!utilities.arrayHasElement(queryParameters)) {
			return null;
		}
		
		if (paramConfig == null) {
			paramConfig = new ArrayList<>();
		}
		
		//Find missing parameters on configured parameters
		
		for (int i = 0; i < queryParameters.size(); i++) {
			Field param = queryParameters.get(i);
			
			if (!paramConfig.contains(param)) {
				QueryParameter qp = new QueryParameter(param.getName(), null);
				qp.setValueType(ParameterValueType.UNDEFINED);
				
				paramConfig.add(qp);
			}
		}
		
		List<QueryParameter> paramConfigValues = loadParamConfigValue(paramConfig, srcObject, configuration);
		
		Object[] params = new Object[queryParameters.size()];
		
		for (int i = 0; i < queryParameters.size(); i++) {
			Field param = queryParameters.get(i);
			
			params[i] = retrieveParamValue(paramConfigValues, param.getName());
		}
		
		return params;
	}
	
	/*
	 * Retrieves the parameter value from configured parameters
	 */
	static Object retrieveParamValue(List<QueryParameter> queryParameters, String paramName) {
		for (QueryParameter param : queryParameters) {
			if (param.getName().equals(paramName)) {
				return param.getValue();
			}
		}
		
		throw new ForbiddenOperationException("Not found param '" + paramName + "' on configured parameters!");
	}
	
	/**
	 * Loads the values given paramConfig
	 * 
	 * @param paramConfig the Parameters Configuration where the values will be loades
	 * @param srcObject the object holding the source object parameters values
	 * @param configuration the configuration holding configuration values
	 * @return the configuration parameters loaded with values
	 */
	static List<QueryParameter> loadParamConfigValue(List<QueryParameter> paramConfig, DatabaseObject srcObject,
	        SyncConfiguration configuration) {
		List<QueryParameter> params = null;
		
		if (utilities.arrayHasElement(paramConfig)) {
			params = new ArrayList<>(paramConfig.size());
			
			for (int i = 0; i < paramConfig.size(); i++) {
				QueryParameter field = paramConfig.get(i);
				
				Object paramValue = null;
				String paramName = null;
				
				String fieldValueName = field.getValue() != null ? field.getValue().toString() : field.getName();
				
				if (field.getValueType().isConfiguration()) {
					paramName = field.getName();
					
					paramValue = getParamValueFromSyncConfiguration(configuration, fieldValueName);
				} else if (field.getValueType().isMainObject()) {
					if (srcObject == null)
						throw new ForbiddenOperationException("The source object is null!");
					
					paramName = AttDefinedElements.convertTableAttNameToClassAttName(fieldValueName);
					
					paramValue = getParamValueFromSourceMainObject(srcObject, paramName);
				} else if (field.getValueType().isConstant()) {
					paramValue = field.getValue();
				} else if (field.getValueType().isUndefined()) {
					String bkpParamName = paramName;
					
					// First get from configuration
					Object paramValueFromConfig = null;
					
					try {
						paramValueFromConfig = getParamValueFromSyncConfiguration(configuration, fieldValueName);
					}
					catch (ForbiddenOperationException e) {}
					
					//Then get from main object
					paramName = AttDefinedElements.convertTableAttNameToClassAttName(fieldValueName);
					
					Object paramValueFromMainObject = null;
					
					try {
						paramValueFromMainObject = getParamValueFromSourceMainObject(srcObject, paramName);
					}
					catch (ForbiddenOperationException e) {}
					
					if (paramValueFromConfig != null && paramValueFromMainObject != null) {
						throw new ForbiddenOperationException("There was found 2 sources for param [" + bkpParamName
						        + "]. You must specificaly configure this parameter!!!!");
					} else if (paramValueFromConfig != null) {
						paramValue = paramValueFromConfig;
					} else if (paramValueFromMainObject != null) {
						paramValue = paramValueFromMainObject;
					}
					
				}
				
				params.add(new QueryParameter(paramName, paramValue));
			}
		}
		
		return params;
	}
	
	static Object getParamValueFromSyncConfiguration(SyncConfiguration configuration, String param)
	        throws ForbiddenOperationException {
		Object paramValue = utilities.getFieldValue(configuration, param);
		
		if (paramValue == null) {
			throw new ForbiddenOperationException("The configuration param '" + param + "' is needed to load source object");
		}
		
		return paramValue;
	}
	
	static Object getParamValueFromSourceMainObject(DatabaseObject mainObject, String paramName)
	        throws ForbiddenOperationException {
		
		Object paramValue = mainObject.getFieldValue(paramName);
		
		if (paramValue == null) {
			throw new ForbiddenOperationException(
			        "The field '" + paramName + "' has no value and it is needed to load source object");
		}
		
		return paramValue;
	}
	
}
