package org.openmrs.module.epts.etl.utilities.db.conn;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * Exception is thrown when any DB error occurs.
 */
public class DBException extends SQLException {
	
	private static final long serialVersionUID = 1L;
	
	public static final CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static Logger logger = Logger.getLogger(DBException.class);
	
	/**
	 * Os atributos abaixo só se aplicam no caso de
	 */
	private int SQLCodeError;
	
	private String SQLState;
	
	private String dataBaseName;
	
	public static final int ORACLE_UNIQUE_CONSTRAINTS_VIOLATED_COD = 1;
	
	public static final int ORACLE_NAME_IS_ALREADY_USED_BY_AN_EXISTING_OBJECT = 17081;
	
	public static final int ORACLE_TABLE_OR_VIEW_DOES_NOT_EXIST = 942;
	
	public static final int ORACLE_INTEGRITY_CONSTRAINT_VIOLATION = 2291;
	
	public static final int MYSQL_UNIQUE_CONSTRAINTS_VIOLATED_COD = 1062;
	
	public static final int MYSQL_INTEGRITY_CONSTRAINT_VIOLATION = 1452;
	
	public static final int SQLSERVER_UNIQUE_CONSTRAINTS_VIOLATED_COD = 2601;
	
	public static final int SQLSERVER_PRIMARY_KEY_CONSTRAINTS_VIOLATED_COD = 2627;
	
	public DBException(String errorMessage, SQLException e) {
		super(errorMessage, e);
		
		this.SQLState = e.getSQLState();
		
		this.SQLCodeError = e.getErrorCode();
		
		this.dataBaseName = DBUtilities.determineDataBaseFromException(e);
		
		/*if (this.SQLCodeError == 0) {
			throw new ForbiddenOperationException("The SQLCodeError could not be zero", e);
		}*/
	}
	
	public DBException(SQLException e) {
		this(e.getMessage(), e);
	}
	
	public int getSQLCodeError() {
		return SQLCodeError;
	}
	
	public void setSQLCodeError(int codeError) {
		SQLCodeError = codeError;
	}
	
	public String getSQLState() {
		return SQLState;
	}
	
	public void setSQLState(String state) {
		SQLState = state;
	}
	
	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
	
	public String getDataBaseName() {
		return dataBaseName;
	}
	
	public void setDataBaseName(String dataBaseName) {
		this.dataBaseName = dataBaseName;
	}
	
	public static void main(String[] args) throws DBException {
		String dataBaseUserName = "root";
		String dataBaseUserPassword = "#eIPDB123#";
		String connectionURI = "jdbc:mysql://10.10.2.2:53307/test?autoReconnect=true&useSSL=false";
		String driveClassName = "com.mysql.jdbc.Driver";
		
		DBConnectionInfo dbConnInfo = new DBConnectionInfo(dataBaseUserName, dataBaseUserPassword, connectionURI,
		        driveClassName);
		
		DBConnectionService service = DBConnectionService.init(dbConnInfo);
		
		OpenConnection conn = service.openConnection();
		
		try {
			BaseDAO.insert(null, "INSERT INTO header (date_created, uuid) VALUES(now(), '123') ", null, conn);
			BaseDAO.insert(null, "INSERT INTO header (date_created, uuid) VALUES(now(), '123') ", null, conn);
		}
		catch (DBException e) {
			System.out.println(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Determina se o erro foi causado pela tentativa de duplicação de dados num campo chave
	 * ATENCAO: Este método é limitado, apenas retorna resultados se a base de dados for ORACLE,
	 * MYSQL, POSTGRES ou SQL Server
	 * 
	 * @author JPBOANE. 17/12/2012, Updated 10/08/2022
	 * @return
	 * @throws DBException
	 */
	public boolean isDuplicatePrimaryOrUniqueKeyException() throws DBException {
		if (this.dataBaseName == null || this.dataBaseName.isEmpty())
			throw new ForbiddenOperationException(
			        "Impossivel determinar o tipo de erro pois o nome da base de dados nao foi definido");
		
		if (isOracleDatabase()) {
			return this.SQLCodeError == ORACLE_UNIQUE_CONSTRAINTS_VIOLATED_COD;
		}
		
		if (isMysqlDatabase()) {
			return this.SQLCodeError == MYSQL_UNIQUE_CONSTRAINTS_VIOLATED_COD;
		}
		
		if (isSqlServerDatabase()) {
			return utilities.isStringIn("" + this.SQLCodeError, "" + SQLSERVER_PRIMARY_KEY_CONSTRAINTS_VIOLATED_COD,
			    "" + SQLSERVER_UNIQUE_CONSTRAINTS_VIOLATED_COD);
		}
		
		if (isPostgresSqlDatabase()) {
			return this.getMessage().contains("duplicate");
		}
		
		return false;
		
	}
	
	public boolean isMysqlDatabase() {
		return this.dataBaseName.equals(DBUtilities.MYSQL_DATABASE);
	}
	
	public boolean isOracleDatabase() {
		return this.dataBaseName.equals(DBUtilities.ORACLE_DATABASE);
	}
	
	public boolean isPostgresSqlDatabase() {
		return this.dataBaseName.equals(DBUtilities.POSTGRESQL_DATABASE);
	}
	
	public boolean isSqlServerDatabase() {
		return this.dataBaseName.equals(DBUtilities.SQLSERVER_DATABASE);
	}
	
	/**
	 * @author JPBOANE. 10/08/2022
	 * @return
	 * @throws DBException
	 */
	public boolean isIntegrityConstraintViolationException() throws DBException {
		if (this.dataBaseName == null || this.dataBaseName.isEmpty())
			throw new ForbiddenOperationException(
			        "Impossivel determinar o tipo de erro pois o nome da base de dados nao foi definido");
		
		if (this.dataBaseName.equals(DBUtilities.ORACLE_DATABASE)) {
			return this.SQLCodeError == ORACLE_INTEGRITY_CONSTRAINT_VIOLATION;
		}
		
		if (this.dataBaseName.equals(DBUtilities.MYSQL_DATABASE)) {
			return this.SQLCodeError == MYSQL_INTEGRITY_CONSTRAINT_VIOLATION;
		}
		//SQL Error [1062] [23000]: Duplicate entry '1' for key 'tmp_unq'
		//SQL Error [1062] [23000]: Duplicate entry '1' for key 'PRIMARY'
		
		System.err.println("WARNING: Nao foi possivel determinar a base de dados");
		return false;
	}
	
	/**
	 * Determina se o erro foi causado pela tentativa de duplicação de dados num campo chave
	 * ATENCAO: Este método é limitado, apenas retorna resultados se a base de dados for ORACLE
	 * 
	 * @author JPBOANE. 17/12/2012
	 * @return
	 * @throws DBException
	 */
	public boolean isAlredyExistTableException() throws DBException {
		if (this.dataBaseName == null || this.dataBaseName.isEmpty())
			throw new ForbiddenOperationException(
			        "Impossivel determinar o tipo de erro pois o nome da base de dados nao foi definido");
		
		if (this.dataBaseName.equals(DBUtilities.ORACLE_DATABASE)) {
			return this.SQLCodeError == ORACLE_NAME_IS_ALREADY_USED_BY_AN_EXISTING_OBJECT;
		}
		
		return false;
	}
	
	/**
	 * Determina se o erro foi causado pela tentativa de duplicação de dados num campo chave
	 * ATENCAO: Este método é limitado, apenas retorna resultados se a base de dados for ORACLE
	 * 
	 * @author JPBOANE. 17/12/2012
	 * @return
	 * @throws DBException
	 */
	public boolean isTableOrViewDoesNotExistException() throws DBException {
		if (this.dataBaseName == null || this.dataBaseName.isEmpty())
			throw new ForbiddenOperationException(
			        "Impossivel determinar o tipo de erro pois o nome da base de dados nao foi definido");
		
		if (this.dataBaseName.equals(DBUtilities.ORACLE_DATABASE)) {
			return this.SQLCodeError == ORACLE_TABLE_OR_VIEW_DOES_NOT_EXIST;
		}
		
		if (this.dataBaseName.equals(DBUtilities.MYSQL_DATABASE)) {
			boolean containsTable = this.getMessage().toUpperCase().contains("TABLE");
			boolean containsDoesNotExists = this.getMessage().toUpperCase().contains("DOESN'T EXIST");
			
			return containsTable && containsDoesNotExists;
		}
		
		return false;
	}

	public boolean isTemporaryDBErrr(Connection conn) throws DBException {
		return isDeadLock(conn) || isLockWaitTimeExceded(conn); 
	}
	
	public boolean isDeadLock(Connection conn) throws DBException {
		if (!CommonUtilities.getInstance().stringHasValue(getMessage()))
			return false;
		
		if (DBUtilities.isOracleDB(conn) && getMessage().contains("ORA-00060")) {
			return true;
		} else if (DBUtilities.isMySQLDB(conn) && getMessage().toLowerCase().contains("deadlock")) {
			return true;
		} else
			return false;
	}
	
	public boolean isConnectionClosedException() {
		return getMessage().contains("The connection is closed");
	}
	
	public boolean isLockWaitTimeExceded(Connection conn) throws DBException {
		
		if (!CommonUtilities.getInstance().stringHasValue(getMessage()))
			return false;
		
		if (DBUtilities.isMySQLDB(conn) && getMessage().toLowerCase().contains("lock wait timeout exceeded")) {
			return true;
		}
		
		return false;
	}
}
