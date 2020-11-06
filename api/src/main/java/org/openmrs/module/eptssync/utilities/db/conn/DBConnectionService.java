package org.openmrs.module.eptssync.utilities.db.conn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.concurrent.TimeCountDown;

/**
 * @author jpboane
 *
 */
public class DBConnectionService {
	
	static int openConnections;
	static int closedConnections;
	
	private static final org.apache.log4j.Logger logger = Logger.getLogger(DBConnectionService.class);

	private static List<DBConnectionService> services = new ArrayList<DBConnectionService>();

	private DBConnectionInfo dbConnInfo;

	private DataSource dataSource;
	
	private DBConnectionService(DBConnectionInfo dbConnInfo) {
		this.dbConnInfo = dbConnInfo;
		
		this.dataSource = new DataSource();
		
		this.dataSource.setDriverClassName(dbConnInfo.getDriveClassName());
		this.dataSource.setUrl(dbConnInfo.getConnectionURI());
		this.dataSource.setUsername(dbConnInfo.getDataBaseUserName());
		this.dataSource.setPassword(dbConnInfo.getDataBaseUserPassword());
		this.dataSource.setInitialSize(20);
		this.dataSource.setMaxActive(150);
		this.dataSource.setMaxWait(30000);
		this.dataSource.setDefaultAutoCommit(false);
		this.dataSource.setMaxIdle(50);
		this.dataSource.setMinIdle(20);
	}

	public static synchronized void init(String driveClassName, String connectionURI, String dataBaseUserName, String dataBaseUserPassword) {
		DBConnectionInfo connInfo = new DBConnectionInfo();

		connInfo.setDriveClassName(driveClassName);
		connInfo.setConnectionURI(connectionURI);
		connInfo.setDataBaseUserName(dataBaseUserName);
		connInfo.setDataBaseUserPassword(dataBaseUserPassword);

		init(connInfo);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (!(obj instanceof DBConnectionService)) {
			return false;
		}

		DBConnectionService objAsService = (DBConnectionService) obj;

		return this.dbConnInfo.equals(objAsService.dbConnInfo);
	}

	private static DBConnectionService retrieveExistingService(DBConnectionInfo info) {
		for (DBConnectionService service : services) {
			if (service.dbConnInfo.equals(info)) return service;
		}
		
		return null;
	}

	public synchronized static DBConnectionService init(DBConnectionInfo dbConnInfo) {
		DBConnectionService service = retrieveExistingService(dbConnInfo);
		
		if (service == null) {
			service = new DBConnectionService(dbConnInfo);
			
			services.add(service);
		}
		
		return service;
	}

	/*public static DBConnectionService getInstance() {
		if (service == null)
			throw new ForbiddenOperationException(
					"The service is not initialized. Initialize it using DBConnectionService.init (...) method ");

		return service;
	}*/
	
	public OpenConnection openConnection() {
		OpenConnection conn = new OpenConnection(openConnection(50), this);
		
		//incriseOpenConnections();
		return conn;
	}
	
	/*
	public synchronized static void incriseOpenConnections() {
		openConnections++;
		
		logger.info("TOTAL Open Connections [" + openConnections + "] Closed [" + closedConnections + "] active [" + (openConnections - closedConnections) + "]");
	}
	
	public synchronized static void increseClosedConnections() {
		closedConnections++;
		
		logger.info("TOTAL Open Connections [" + openConnections + "] Closed [" + closedConnections + "] active [" + (openConnections - closedConnections) + "]");
	}
	*/
	
	public OpenConnection openConnection(String context) {
		return openConnection(context, 10);
	}

	private Connection openConnection(int qtyTry) {
		if (qtyTry <= 0)
			throw new ForbiddenOperationException("The connection service could stablish a valid connection");

		try {
			
			return this.dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();

			logger.warn("Tentando novamente Obter uma conexao");

			TimeCountDown.sleep(10);

			return openConnection(--qtyTry);
		}
	}
	
	@SuppressWarnings("unused")
	private Connection openConnectionOld(int qtyTry) {
		if (qtyTry <= 0)
			throw new ForbiddenOperationException("The connection service could stablish a valid connection");

		try {
			TimeZone timeZone = TimeZone.getTimeZone("Africa/Johannesburg");
			TimeZone.setDefault(timeZone);

			Class.forName(this.dbConnInfo.getDriveClassName());

			
			Connection conn = DriverManager.getConnection(this.dbConnInfo.getConnectionURI(), this.dbConnInfo.getDataBaseUserName(), this.dbConnInfo.getDataBaseUserPassword());
			conn.setAutoCommit(false);

			return this.dataSource.getConnection();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();

			logger.warn("Tentando novamente Obter uma conexao");

			TimeCountDown.sleep(10);

			return openConnection(--qtyTry);

		}

		/**
		 * Exemplos: 1. SQLServer Connection conn =
		 * BaseDAO.openConnection("com.microsoft.jdbc.sqlserver.SQLServerDriver",
		 * "jbdc.microsoft:sqlserver://192.168.0.220:1422;databaseName=SifinSql",
		 * "sifin", "sifin");
		 * 
		 */

		return null;
	}

	private OpenConnection openConnection(String context, int qtyTry) {
		/*
		 * if (qtyTry <= 0) throw new
		 * ForbidenOperationException("The connection service could stablish a valid connection"
		 * );
		 * 
		 * DBConnectionService conn = defaultDBConnection.openConnection(context);
		 * 
		 * if(conn == null){ logger.warn(errMsg);
		 * logger.warn("Tentando novamente Obter uma conexao");
		 * 
		 * TimeCountDown.sleep(5);
		 * 
		 * return openConnection(context, --qtyTry); } else{ OpenConnection
		 * openConnection = new OpenConnection(conn);
		 * 
		 * return openConnection; }
		 */

		return null;
	}

}
