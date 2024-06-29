package org.openmrs.module.epts.etl.utilities.db.conn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class DBConnectionInfo {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String dataBaseUserName;
	
	private String dataBaseUserPassword;
	
	private String connectionURI;
	
	private String driveClassName;
	
	private String schema;
	
	private int maxActiveConnections;
	
	private int maxIdleConnections;
	
	private int minIdleConnections;
	
	private String databaseSchemaPath;
	
	private DBConnectionInfo connInfo;
	
	private DBConnectionService dbService;
	
	public DBConnectionInfo() {
	}
	
	public DBConnectionInfo(String dataBaseUserName, String dataBaseUserPassword, String connectionURI,
	    String driveClassName) {
		this.dataBaseUserName = dataBaseUserName;
		this.dataBaseUserPassword = dataBaseUserPassword;
		this.connectionURI = connectionURI;
		this.driveClassName = driveClassName;
	}
	
	public DBConnectionInfo(String dataBaseUserName, String dataBaseUserPassword, String connectionURI, String schema,
	    String driveClassName) {
		this(dataBaseUserName, dataBaseUserPassword, connectionURI, driveClassName);
		
		this.schema = schema;
	}
	
	public void finalize() {
		if (dbService != null)
			dbService.finalize();
	}
	
	private DBConnectionService getRelatedDBConnectionService() {
		if (this.dbService == null)
			initRelatedDBConnectionService();
		
		return this.dbService;
	}
	
	public OpenConnection openConnection() throws DBException {
		return getRelatedDBConnectionService().openConnection();
	}
	
	private synchronized void initRelatedDBConnectionService() {
		if (dbService == null) {
			dbService = DBConnectionService.init(this.connInfo);
		}
	}
	
	public String getDatabaseSchemaPath() {
		return databaseSchemaPath;
	}
	
	public void setDatabaseSchemaPath(String databaseSchemaPath) {
		this.databaseSchemaPath = databaseSchemaPath;
	}
	
	public int getMaxActiveConnections() {
		return maxActiveConnections;
	}
	
	public void setMaxActiveConnections(int maxActiveConnections) {
		this.maxActiveConnections = maxActiveConnections;
	}
	
	public int getMaxIdleConnections() {
		return maxIdleConnections;
	}
	
	public void setMaxIdleConnections(int maxIdleConnections) {
		this.maxIdleConnections = maxIdleConnections;
	}
	
	public int getMinIdleConnections() {
		return minIdleConnections;
	}
	
	public void setMinIdleConnections(int minIdleConnections) {
		this.minIdleConnections = minIdleConnections;
	}
	
	public String getSchema() {
		return schema;
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	public String getDataBaseUserName() {
		return dataBaseUserName;
	}
	
	public void setDataBaseUserName(String dataBaseUserName) {
		this.dataBaseUserName = dataBaseUserName;
	}
	
	public String getDataBaseUserPassword() {
		return dataBaseUserPassword;
	}
	
	public void setDataBaseUserPassword(String dataBaseUserPassword) {
		this.dataBaseUserPassword = dataBaseUserPassword;
	}
	
	public String getConnectionURI() {
		return connectionURI;
	}
	
	public void setConnectionURI(String connectionURI) {
		this.connectionURI = connectionURI;
	}
	
	public String getDriveClassName() {
		return driveClassName;
	}
	
	public void setDriveClassName(String driveClassName) {
		this.driveClassName = driveClassName;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DBConnectionInfo))
			return false;
		
		DBConnectionInfo dbConn = (DBConnectionInfo) obj;
		
		return this.connectionURI.equals(dbConn.connectionURI);
	}
	
	public DBConnectionInfo clone(String connURI) {
		DBConnectionInfo db = new DBConnectionInfo(dataBaseUserName, dataBaseUserPassword, connURI, driveClassName);
		
		return db;
	}
	
	public static DBConnectionInfo loadFromFile(File file) throws IOException {
		DBConnectionInfo conf = utilities.loadObjectFormJSON(DBConnectionInfo.class,
		    new String(Files.readAllBytes(file.toPath())));
		
		return conf;
	}
	
	public static DBConnectionInfo loadFromJson(String json) {
		return utilities.loadObjectFormJSON(DBConnectionInfo.class, json);
	}
	
	public String determineSchema() {
		
		if (utilities.stringHasValue(this.schema))
			return schema;
		
		if (isMySQLConnection()) {
			String[] urlParts = this.getConnectionURI().split("/");
			
			return urlParts[urlParts.length - 1].split("\\?")[0];
		}
		
		throw new ForbiddenOperationException("Unrecognized dbms");
	}
	
	private boolean isMySQLConnection() {
		return this.connectionURI.toUpperCase().contains("MYSQL");
	}
	
	public String getPojoPackageName() {
		throw new ForbiddenOperationException("Rever esta logica");
	}
	
}
