package org.openmrs.module.epts.etl.utilities.db.conn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.openmrs.module.epts.etl.conf.interfaces.BaseConfiguration;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.concurrent.TimeCountDown;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author jpboane
 */
public class DBConnectionService {
	
	private static final EptsEtlLogger logger = EptsEtlLogger.getLogger(DBConnectionService.class);
	
	private static final Object LOCK = new Object();
	
	private static List<DBConnectionService> services = new ArrayList<DBConnectionService>();
	
	private DBConnectionInfo dbConnInfo;
	
	private DataSource dataSource;
	
	private List<OpenConnection> openConnections;
	
	private DBConnectionService(DBConnectionInfo dbConnInfo) {
		this.dbConnInfo = dbConnInfo;
		
		this.dataSource = new DataSource();
		
		this.dataSource.setDriverClassName(dbConnInfo.getDriveClassName());
		this.dataSource.setUrl(dbConnInfo.getConnectionURI());
		this.dataSource.setUsername(dbConnInfo.getDataBaseUserName());
		this.dataSource.setPassword(dbConnInfo.getDataBaseUserPassword());
		this.dataSource.setInitialSize(10);
		this.dataSource.setMaxActive(dbConnInfo.getMaxActiveConnections() != 0 ? dbConnInfo.getMaxActiveConnections() : 64);
		this.dataSource.setMaxWait(30000);
		this.dataSource.setDefaultAutoCommit(false);
		this.dataSource.setMaxIdle(dbConnInfo.getMaxIdleConnections() != 0 ? dbConnInfo.getMaxIdleConnections() : 64);
		this.dataSource.setMinIdle(dbConnInfo.getMinIdleConnections() != 0 ? dbConnInfo.getMinIdleConnections() : 32);
		this.dataSource.setMinEvictableIdleTimeMillis(15 * 60000);
		this.dataSource.getPoolProperties().getDbProperties().setProperty("connectRetryCount", "" + 255);
		this.dataSource.getPoolProperties().getDbProperties().setProperty("connectRetryInterval", "" + 15);
		
		this.openConnections = new ArrayList<>();
	}
	
	public void finalize() {
		try {
			this.dataSource.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized DBConnectionService init(String driveClassName, String connectionURI, String dataBaseUserName,
	        String dataBaseUserPassword) {
		DBConnectionInfo connInfo = new DBConnectionInfo();
		
		connInfo.setDriveClassName(driveClassName);
		connInfo.setConnectionURI(connectionURI);
		connInfo.setDataBaseUserName(dataBaseUserName);
		connInfo.setDataBaseUserPassword(dataBaseUserPassword);
		
		return init(connInfo);
	}
	
	public DBConnectionInfo getDbConnInfo() {
		return dbConnInfo;
	}
	
	/**
	 * @param connURI the connection URI for new Service
	 * @return
	 */
	public DBConnectionService clone(String connURI) {
		return init(this.dbConnInfo.clone(connURI));
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
			if (service.dbConnInfo.equals(info))
				return service;
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
	
	@JsonIgnore
	public synchronized OpenConnection openConnection(BaseConfiguration openedFrom) throws DBException {
		OpenConnection conn = new OpenConnection(openConnection(50, null), openedFrom, this);
		addOpenConnection(conn);
		
		return conn;
	}
	
	private void addOpenConnection(OpenConnection conn) {
		synchronized (LOCK) {
			this.openConnections.add(conn);
		}
	}
	
	void removeOpenConnection(OpenConnection conn) {
		synchronized (LOCK) {
			this.openConnections.remove(conn);
		}
	}
	
	private synchronized Connection openConnection(int qtyTry, SQLException e) throws DBException {
		if (qtyTry <= 0)
			throw new DBException(e);
		
		try {
			return this.dataSource.getConnection();
		}
		catch (SQLException e1) {
			logger.warn("OpenedConnections: " + OpenConnection.qtyOpenedConnections + ", ClosedConnections: "
			        + OpenConnection.qtyClosedConnections);
			
			if (DBUtilities.determineDataBaseFromException(e1).equals(DBUtilities.MYSQL_DATABASE)) {
				if (DBException.checkIfExceptionContainsMessage(e1, "Unknown database")) {
					throw new DBException(e1);
				}
			}
			
			e1.printStackTrace();
			
			logger.warn("Nao foi possivel obter a conexao. Tentando novamente obter a conexao novamente...");
			
			TimeCountDown.sleep(5);
			
			return openConnection(--qtyTry, e1);
		}
	}
}
