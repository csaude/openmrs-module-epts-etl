package org.openmrs.module.eptssync.utilities.db.conn;

public class DBConnectionInfo {
	private String dataBaseUserName;
	private String dataBaseUserPassword;
	private String connectionURI;
	private String driveClassName;
	
	public DBConnectionInfo(){
		
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
	
}
