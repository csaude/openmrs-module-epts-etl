package org.openmrs.module.eptssync.utilities.db.conn;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

/**
 * @author jpboane
 *
 */
public class DBConnectionService {
	private static final org.apache.log4j.Logger logger = Logger.getLogger(DBConnectionService.class);
	
	private static DBConnectionService service;
	
	private String driveClassName;
	private String connectionURI;
	private String dataBaseUserName; 
	private String dataBaseUserPassword;
	
	private DBConnectionService(String driveClassName, String connectionURI, String dataBaseUserName, String dataBaseUserPassword){
		this.driveClassName = driveClassName;
		this.connectionURI = connectionURI;
		this.dataBaseUserName = dataBaseUserName;
		this.dataBaseUserPassword = dataBaseUserPassword;
	}
	
	public static synchronized void init(String driveClassName, String connectionURI, String dataBaseUserName, String dataBaseUserPassword) {
		 if (service == null) {
	         service = new DBConnectionService(driveClassName, connectionURI, dataBaseUserName, dataBaseUserPassword);
	     }
	}
	

	public static void init(DBConnectionInfo connInfo) {
		 init(connInfo.getDriveClassName(), connInfo.getConnectionURI(), connInfo.getDataBaseUserName(), connInfo.getDataBaseUserPassword());
	}
	
    public static DBConnectionService getInstance() {
        if (service == null) throw new ForbiddenOperationException("The service is not initialized. Initialize it using DBConnectionService.init (...) method ");
        
        return service;
    }
	
	public OpenConnection openConnection(){
		return new OpenConnection(openConnection(5));
	}
	
	public OpenConnection openConnection(String context){
		return openConnection(context, 10);
	}
	
	private Connection openConnection(int qtyTry){
		if (qtyTry <= 0) throw new ForbiddenOperationException("The connection service could stablish a valid connection"); 
		
		try {
			TimeZone timeZone = TimeZone.getTimeZone("Africa/Johannesburg");
			TimeZone.setDefault(timeZone);
		
			Class.forName(this.driveClassName);
			
			Connection conn = DriverManager.getConnection(this.connectionURI, this.dataBaseUserName, this.dataBaseUserPassword);
			conn.setAutoCommit(false);
			
			//conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			
			return conn;
		} catch (ClassNotFoundException e) {
				e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		
			logger.warn("Tentando novamente Obter uma conexao");
			
			TimeCountDown.sleep(5);
			
			return openConnection(--qtyTry);
	
		}
		
		/**
		 * Exemplos:
		 * 		1. SQLServer Connection conn = BaseDAO.openConnection("com.microsoft.jdbc.sqlserver.SQLServerDriver", "jbdc.microsoft:sqlserver://192.168.0.220:1422;databaseName=SifinSql", "sifin", "sifin");

		 */
		
		return null;
	 }
	
	private OpenConnection openConnection(String context, int qtyTry){
		/*if (qtyTry <= 0) throw new ForbidenOperationException("The connection service could stablish a valid connection"); 
		
		DBConnectionService conn = defaultDBConnection.openConnection(context);
		
		if(conn == null){
			logger.warn(errMsg);
			logger.warn("Tentando novamente Obter uma conexao");
			
			TimeCountDown.sleep(5);
			
			return openConnection(context, --qtyTry);
		}
		else{
			OpenConnection openConnection = new OpenConnection(conn);
	
			return openConnection;
		}*/
		
		return null;
	}

	
}
