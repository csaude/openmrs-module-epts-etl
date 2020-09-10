package org.openmrs.module.eptssync.utilities.db.conn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;

/**
 * 
 * @author JPBOANE
 * @version 1.0 27/12/2012
 *
 */
public class DBUtilities {
	public static final String ORACLE_DATABASE="ORACLE";
	public static final String MYSQL_DATABASE="MYSQL";
	public static final String POSTGRES_DATABASE="POSTGRES";
	public static final String SQLSERVER_DATABASE="SQLSERVER";
	public static final String UNKNOWN_DATABASE="UNKOWN";
	
	
	public static final String SELECT_SQL_TYPE = "SELECT_SQL_TYPE";
	public static final String UPDATE_SQL_TYPE = "UPDATE_SQL_TYPE";
	public static final String DELETE_SQL_TYPE= "DELETE_SQL_TYPE";
	public static final String DROP_SQL_TYPE = "DROP_SQL_TYPE";
	public static final String CREATE_SQL_TYPE = "CREATE_SQL_TYPE";
	public static final String TRUNCAT_SQL_TYPE = "TRUNCAT_SQL_TYPE";
	
	public static final String RESOURCE_TYPE_TRIGGER = "TRIGGER";
	public static final String RESOURCE_TYPE_TABLE = "TABLE";
	public static final String RESOURCE_TYPE_VIEW = "VIEW";
	public static final String RESOURCE_TYPE_PROCEDURE = "PROCEDURE";
	public static final String RESOURCE_TYPE_INDEX = "INDEX";
	public static final String RESOURCE_TYPE_SCHEMA = "SCHEMA";
	
	
	private static String determineDataBaseString(String msg){
		if (msg.toUpperCase().contains("ORA")){
            return  ORACLE_DATABASE;
        }
        else
        if (msg.toUpperCase().contains("MYSQL")){
        	return MYSQL_DATABASE;
        }
        else
        if (msg.toLowerCase().contains("postgresql")){
        	return POSTGRES_DATABASE;
        }	
		
		return null;
	}
	
	public static String determineDataBaseFromException(SQLException sqlExcetion){
		String db=determineDataBaseString(sqlExcetion.getClass().getName());
		
		if (db != null) return db;
		
		if (sqlExcetion.getCause() != null){
			db=determineDataBaseString(sqlExcetion.getCause().getClass().getName());
			
			if (db != null) return db;
		}
		
		if (sqlExcetion.getLocalizedMessage() != null){
			db = determineDataBaseString(sqlExcetion.getLocalizedMessage().toString());
			if (db != null) return db;
		}
		
		StackTraceElement[] trace = sqlExcetion.getStackTrace(); 
		
		 for (int i=0; i < trace.length; i++){
			db = determineDataBaseString((trace[i].toString()));
			
			if (db != null) return db;
		 }
		  
		 return UNKNOWN_DATABASE;
	}
	
	public static boolean isPostgresDB(Connection conn) throws DBException{
		try {
			return determineDataBaseFromConnection(conn).equals(POSTGRES_DATABASE);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static boolean isOracleDB(Connection conn) throws DBException{
		try {
			return determineDataBaseFromConnection(conn).equals(ORACLE_DATABASE);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static boolean isMySQLDB(Connection conn) throws DBException{
		try {
			return determineDataBaseFromConnection(conn).equals(MYSQL_DATABASE);
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static String determineDataBaseFromConnection(Connection conn) throws SQLException{
		  String str = conn.getMetaData().getDriverName().toUpperCase();
		  
		  if (str.contains("ORACLE")) return ORACLE_DATABASE;
		  if (str.contains("MYSQL")) return MYSQL_DATABASE;
		  if (str.contains("POSTGRES")) return POSTGRES_DATABASE;
		  if (str.contains("SQLSERVER")) return SQLSERVER_DATABASE;
			  
		  throw new RuntimeException("Impossivel determinar a base de dados a partir da conexao");
	}
	
	public static Connection cloneConnetion(Connection conn) throws SQLException{
		//DatabaseMetaData metadata = conn.getMetaData();
		
		//String userName = metadata.getUserName();
		//String url = metadata.getURL();
		//String className = metadata.getDriverName();
		
		//return BaseDAO.openConnection(className, url, userName, metadata.getPa);
		
		return null;
	}
	
	public static String tryToPutSchemaOnDatabaseObject(String tableName, Connection conn) throws DBException{
		try {
			
			if (isPostgresDB(conn)) return tableName;
			if (isMySQLDB(conn)) return tableName;
				
			String[] tableNameComposition = tableName.split("\\.");
			
			if (tableNameComposition != null && tableNameComposition.length == 1) {
				String userName = conn.getMetaData().getUserName().toLowerCase().equals("sisflof") ? "lims" : conn.getMetaData().getUserName(); 
				
				return  userName + "." + tableName;
			}
			
			return tableName;
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	
	
	@SuppressWarnings("unused")
	private static Connection tempOpenConnection(){
		String OracleClasse = "oracle.jdbc.driver.OracleDriver";
		String OracleURL 	= "jdbc:oracle:thin:@10.5.63.10:1521:dntf";
		String OracleUser	= "lims";
		String OraclePass	= "exi2k12";
		
		return null;//BaseDAO.openConnection(OracleClasse, OracleURL, OracleUser, OraclePass);
	}
	
	public static boolean isInvalidIdentifierException(DBException e){
		String dataBase = determineDataBaseFromException(e);
		
		if (dataBase.equals(ORACLE_DATABASE)){
			return e.getLocalizedMessage().contains("ORA-00904");
		}
		
		throw new ForbiddenOperationException("Unsupported DB");
	}
	
	public static boolean disableForegnKeyChecks(Connection conn) throws DBException {
		try {
			if (isMySQLDB(conn)) {
				Statement st = conn.createStatement();

				st.addBatch("SET FOREIGN_KEY_CHECKS=0");
				
				st.executeBatch();

				st.close();

			}
			
			throw new RuntimeException("Database not supported!");
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new DBException(e);
		}
	}

	public static boolean enableForegnKeyChecks(Connection conn) throws DBException {
		try {
			if (isMySQLDB(conn)) {
				Statement st = conn.createStatement();

				st.addBatch("SET FOREIGN_KEY_CHECKS=1");
				
				st.executeBatch();

				st.close();

			}
			
			throw new RuntimeException("Database not supported!");
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new DBException(e);
		}
	}

	
	public static boolean isResourceExist(String resourceSchema, String resourceType, String resourceName, Connection conn) throws SQLException {
		if (isMySQLDB(conn)) {
			return isMySQLResourceExist(resourceSchema, resourceType, resourceName, conn);
		}
		
		throw new RuntimeException("Database not supported!");
	}
	
	
	private static boolean isMySQLResourceExist(String resourceSchema, String resourceType, String resourceName, Connection conn) throws SQLException {
		String resourceSchemaCondition = "";
		String resourceNameCondition = "";
		String fromClause = "";
			
		if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_TRIGGER)) {
			fromClause = "INFORMATION_SCHEMA.TRIGGERS";
			resourceNameCondition = "TRIGGER_NAME = '" + resourceName + "'" ;
			resourceSchemaCondition = "EVENT_OBJECT_SCHEMA = '" + resourceSchema + "'";
		}
		else
		if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_TABLE)) {
			fromClause = "INFORMATION_SCHEMA.TABLES";
			resourceNameCondition = "TABLE_NAME = '" + resourceName + "'" ;
			resourceSchemaCondition = "TABLE_SCHEMA = '" + resourceSchema + "'";
		}
		else
		if (resourceType.equalsIgnoreCase(DBUtilities.RESOURCE_TYPE_SCHEMA)) {
			fromClause = "INFORMATION_SCHEMA.SCHEMATA";
			
			resourceNameCondition = "SCHEMA_NAME = '" + resourceName + "'" ;
			
			resourceSchemaCondition = "1 = 1";
		}
		else throw new ForbiddenOperationException("Resource not supported");
		
		String selectQuery = "";
		
		selectQuery += " SELECT * \n";
		selectQuery += " FROM " + fromClause + "\n";
		selectQuery += " WHERE 	1 = 1\n";
		selectQuery += " 		AND  " + resourceSchemaCondition + "\n";
		selectQuery += "		AND  " + resourceNameCondition;
	    
		PreparedStatement statement = conn.prepareStatement(selectQuery); 
		    
	    ResultSet result = statement.executeQuery();
		
		return result.next();
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
}
