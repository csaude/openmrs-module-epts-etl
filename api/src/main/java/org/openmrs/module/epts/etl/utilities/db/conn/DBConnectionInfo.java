package org.openmrs.module.epts.etl.utilities.db.conn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class DBConnectionInfo {
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String dataBaseUserName;
	
	private String dataBaseUserPassword;
	
	private String connectionURI;
	
	private String driveClassName;
	
	private String schema;
	
	public DBConnectionInfo() {
	}
	
	public DBConnectionInfo(String dataBaseUserName, String dataBaseUserPassword, String connectionURI, String driveClassName) {
		this.dataBaseUserName = dataBaseUserName;
		this.dataBaseUserPassword = dataBaseUserPassword;
		this.connectionURI = connectionURI;
		this.driveClassName = driveClassName;
	}
	
	public DBConnectionInfo(String dataBaseUserName, String dataBaseUserPassword, String connectionURI, String schema, String driveClassName) {
		this(dataBaseUserName, dataBaseUserPassword, connectionURI, driveClassName);
		
		this.schema = schema;
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
		if (!(obj instanceof DBConnectionInfo)) return false;
		
		DBConnectionInfo dbConn = (DBConnectionInfo)obj;
		
		return this.connectionURI.equals(dbConn.connectionURI);
	}
	
	public DBConnectionInfo clone(String connURI) {
		DBConnectionInfo db = new DBConnectionInfo(dataBaseUserName, dataBaseUserPassword, connURI, driveClassName);
		
		return db;
	}
	
	public static DBConnectionInfo loadFromFile(File file) throws IOException {
		DBConnectionInfo conf =  utilities.loadObjectFormJSON(DBConnectionInfo.class, new String(Files.readAllBytes(file.toPath())));
		
		return conf;
	}
}
