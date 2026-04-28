package org.openmrs.module.epts.etl.utilities.db.conn;

import java.sql.Connection;

public enum DbmsType {
	
	ORACLE,
	MYSQL,
	POSTGRESQL,
	SQLSERVER;
	
	public boolean isOracle() {
		return this.equals(ORACLE);
	}
	
	public boolean isMysql() {
		return this.equals(MYSQL);
	}
	
	public boolean isPostgres() {
		return this.equals(POSTGRESQL);
	}
	
	public boolean issSqlServer() {
		return this.equals(SQLSERVER);
	}
	
	public static DbmsType determineFromConnection(Connection conn) throws DBException {
		return DbmsType.valueOf(DBUtilities.determineDataBaseFromConnection(conn));
	}
}
