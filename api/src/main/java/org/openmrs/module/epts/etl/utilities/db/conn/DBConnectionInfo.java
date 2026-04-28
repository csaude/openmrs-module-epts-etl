package org.openmrs.module.epts.etl.utilities.db.conn;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
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
	
	private String dbHost;
	
	private Integer dbHostPort;
	
	private DBConnectionService dbService;
	
	private EtlConfiguration relatedEtlConf;
	
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
	
	public EtlConfiguration getRelatedEtlConf() {
		return relatedEtlConf;
	}
	
	public void setRelatedEtlConf(EtlConfiguration relatedEtlConf) {
		this.relatedEtlConf = relatedEtlConf;
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
			dbService = DBConnectionService.init(this);
		}
	}
	
	public String getDbHost() {
		return dbHost;
	}
	
	public void setDbHost(String dbHost) {
		this.dbHost = dbHost;
	}
	
	public Integer getDbHostPort() {
		return dbHostPort;
	}
	
	public void setDbHostPort(Integer dbHostPort) {
		this.dbHostPort = dbHostPort;
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
	
	public boolean isMySQLConnection() {
		return this.connectionURI.toUpperCase().contains("MYSQL");
	}
	
	public String getPojoPackageName() {
		throw new ForbiddenOperationException("Rever esta logica");
	}
	
	public void copyFromOther(DBConnectionInfo toCopyFrom) {
		this.setDataBaseUserName(toCopyFrom.getDataBaseUserName());
		this.setDataBaseUserPassword(toCopyFrom.getDataBaseUserPassword());
		this.setConnectionURI(toCopyFrom.getConnectionURI());
		this.setDriveClassName(toCopyFrom.getDriveClassName());
		this.setSchema(toCopyFrom.getSchema());
		this.setMaxActiveConnections(toCopyFrom.getMaxActiveConnections());
		this.setMaxIdleConnections(toCopyFrom.getMaxIdleConnections());
		this.setMinIdleConnections(toCopyFrom.getMinIdleConnections());
		this.setDatabaseSchemaPath(toCopyFrom.getDatabaseSchemaPath());
	}
	
	public void tryToLoadPlaceHolders(EtlDatabaseObject schemaInfoSrc) {
		this.setDataBaseUserName(tryToLoadPlaceHolders(this.getDataBaseUserName(), schemaInfoSrc));
		this.setDataBaseUserPassword(tryToLoadPlaceHolders(this.getDataBaseUserPassword(), schemaInfoSrc));
		this.setConnectionURI(tryToLoadPlaceHolders(this.getConnectionURI(), schemaInfoSrc));
		this.setSchema(tryToLoadPlaceHolders(this.getSchema(), schemaInfoSrc));
	}
	
	public void tryToLoadPlaceHolders(EtlConfiguration schemaInfoSrc) {
		this.setDataBaseUserName(tryToLoadPlaceHolders(this.getDataBaseUserName(), schemaInfoSrc));
		this.setDataBaseUserPassword(tryToLoadPlaceHolders(this.getDataBaseUserPassword(), schemaInfoSrc));
		this.setConnectionURI(tryToLoadPlaceHolders(this.getConnectionURI(), schemaInfoSrc));
		this.setSchema(tryToLoadPlaceHolders(this.getSchema(), schemaInfoSrc));
	}
	
	private String tryToLoadPlaceHolders(String str, EtlConfiguration schemaInfoSrc) {
		return SQLUtilities.tryToReplaceParamsInQuery(str, schemaInfoSrc);
	}
	
	private String tryToLoadPlaceHolders(String str, EtlDatabaseObject schemaInfoSrc) {
		return SQLUtilities.tryToReplaceParamsInQuery(str, schemaInfoSrc);
	}
	
	public boolean hasDatabaseSchemaPath() {
		return utilities.stringHasValue(this.getDatabaseSchemaPath());
	}
	
	public void tryToExtractHostInfoFromMysqlUri() {
		String jdbcUrl = getConnectionURI();
		
		if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:mysql://")) {
			throw new IllegalArgumentException("Invalid MySQL JDBC URL: " + jdbcUrl);
		}
		
		String withoutPrefix = jdbcUrl.substring("jdbc:mysql://".length());
		
		int slashIndex = withoutPrefix.indexOf("/");
		String hostPortPart = (slashIndex != -1) ? withoutPrefix.substring(0, slashIndex) : withoutPrefix;
		
		if (hostPortPart.contains(",")) {
			hostPortPart = hostPortPart.split(",")[0];
		}
		
		String host;
		int port = 3306;
		
		if (hostPortPart.contains(":")) {
			String[] parts = hostPortPart.split(":");
			host = parts[0];
			port = Integer.parseInt(parts[1]);
		} else {
			host = hostPortPart;
		}
		
		this.setDbHost("localhost".equalsIgnoreCase(host) ? "127.0.0.1" : host);
		this.setDbHostPort(port);
	}
	
	public void restoreDump(EtlConfiguration etlConf) throws EtlExceptionImpl, DBException {
		
		String databaseName = this.determineSchema();
		String databaseSchemaFullPath = etlConf.generateDatabaseSchemaFullPath(this);
		
		etlConf.logWarn("Database '" + databaseName + "' Does not exist but schema exists.");
		
		etlConf.logDebug("Database '" + databaseName + "' created!");
		
		OpenConnection dstConn = null;
		
		try {
			DBUtilities.createDb(this, this.determineSchema());
			
			DBUtilities.runScriptOnDbServer(this, databaseSchemaFullPath);
		}
		catch (Exception e) {
			etlConf.logErr("An error occurred restoring dump: " + databaseSchemaFullPath);
			
			try {
				DBUtilities.dropDB(this, this.determineSchema());
			}
			catch (Exception e1) {}
			
			throw new EtlExceptionImpl(e);
		}
		finally {
			if (dstConn != null) {
				dstConn.finalizeConnection();
			}
		}
	}
	
}
