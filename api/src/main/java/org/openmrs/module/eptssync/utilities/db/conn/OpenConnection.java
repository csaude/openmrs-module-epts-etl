/**
 * 
 */
package org.openmrs.module.eptssync.utilities.db.conn;

import java.io.Closeable;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.openmrs.module.eptssync.utilities.CommonUtilities;

/**
 * @author jpboane
 *
 */
public class OpenConnection implements Connection, Closeable{
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Connection connection;
	private boolean operationTerminatedSuccessifully;
	private String id;
	private Date openDate;
	private Date closeDate;
	private DBConnectionService connService;
	
	protected OpenConnection(Connection conn, DBConnectionService service){
		this.connection = conn;
		this.operationTerminatedSuccessifully = false;
		this.id = "CONN_"+hashCode();
		
		this.connService = service;
   }
	
	public DBConnectionService getConnService() {
		return connService;
	}
	
	public void markAsSuccessifullyTerminected(){
		//if (this.operationTerminatedSuccessifully) throw new ForbiddenOperationException("This connection is already marcked as successifuly terminated!");
		
		this.operationTerminatedSuccessifully = true;
	}
	
	public static void markAllAsSuccessifullyTerminected(List<OpenConnection> conns){
		if (!CommonUtilities.getInstance().arrayHasElement(conns)) return;
		
		for (OpenConnection conn : conns){
			conn.markAsSuccessifullyTerminected();
		}
	}
	
	public static void finalizeAllConnections(List<OpenConnection> conns){
		if (!CommonUtilities.getInstance().arrayHasElement(conns)) return;
		
		for (OpenConnection conn : conns){
			conn.finalizeConnection();
		}
	}
	
	/**
	 * @return o valor do atributo {@link #id}
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return o valor do atributo {@link #connection}
	 */
	public Connection getConnection() {
		return connection;
	}

	/**
	 * Modifica o valor do atributo {@link #connection} para o valor fornecido pelo par�metro <code>connection</code>
	 * 
	 * @param connection novo valor para o atributo {@link #connection}
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	
	/**
	 * @return o valor do atributo {@link #operationTerminatedSuccessifully}
	 */
	public boolean isOperationTerminatedSuccessifully() {
		return operationTerminatedSuccessifully;
	}

	/**
	 * Modifica o valor do atributo {@link #operationTerminatedSuccessifully} para o valor fornecido pelo par�metro <code>operationTerminatedSuccessifully</code>
	 * 
	 * @param operationTerminatedSuccessifully novo valor para o atributo {@link #operationTerminatedSuccessifully}
	 */
	public void setOperationTerminatedSuccessifully(boolean operationTerminatedSuccessifully) {
		this.operationTerminatedSuccessifully = operationTerminatedSuccessifully;
	}

	/**
     * Finaliza a conexao associada a este objecto
     * 
     */
    public void finalizeConnection(){
    	if (connection == null) return;
    	
    	//if (isFinalized()) throw new ForbiddenOperationException("A conexao ja se encontra finalizada!");
    	
    	//this.closeDate = UtilitarioDeDatas.getCurrentSystemDate(connection);
    	
    	try {
			if (this.operationTerminatedSuccessifully){
				commit();
			}
			else{
				rollback();
			}
			
			connection.close();
			
			//DBConnectionService.increseClosedConnections();
		} catch (SQLException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
    	
    	connection = null;
    
    	//Ficheiro.write("D:\\connections.txt", "FINALIZED CONN ["+ this.id + "] ON [" +  CallerInfo.getStackInfo(3)+ "]");
    }

    private void tryToReopen() throws SQLException{
    	if (!this.isFinalized()){
    		if (isClosed()){
    			this.connection =  connService.openConnection();
    		}
    	}
    }
    
	/**
	 * @return
	 */
	public boolean isFinalized() {
		return connection == null;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		tryToReopen();
		
		return connection.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		tryToReopen();
		
		return connection.isWrapperFor(iface);
	}

	@Override
	public Statement createStatement() throws SQLException {
		tryToReopen();
		
		return connection.createStatement();
	}

	@Override
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		tryToReopen();
		
		return connection.prepareStatement(sql);
	}

	@Override
	public CallableStatement prepareCall(String sql) throws SQLException {
		tryToReopen();
		
		return connection.prepareCall(sql);
	}

	@Override
	public String nativeSQL(String sql) throws SQLException {
		tryToReopen();
		
		return connection.nativeSQL(sql);
	}

	@Override
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		tryToReopen();
		
		connection.setAutoCommit(autoCommit);
	}

	@Override
	public boolean getAutoCommit() throws SQLException {
		tryToReopen();
		
		return connection.getAutoCommit();
	}

	@Override
	public void commit() throws SQLException {
		if (!isClosed()) connection.commit();
	}
	
	public void commitCurrentWork() throws DBException {
		try {
			this.commit();
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	@Override
	public void rollback() throws SQLException {
		if (!isClosed()) connection.rollback();
	}

	@Override
	public void close() {
		try {
			if(!isClosed()) connection.close();
		} catch (SQLException e) {
			throw new RuntimeException();
		}
	}

	@Override
	public boolean isClosed() throws SQLException {
		return connection != null && connection.isClosed();
	}

	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		tryToReopen();
		
		return connection.getMetaData();
	}

	@Override
	public void setReadOnly(boolean readOnly) throws SQLException {
		if (!isClosed()) connection.setReadOnly(readOnly);
	}

	@Override
	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	@Override
	public void setCatalog(String catalog) throws SQLException {
		tryToReopen();
		
		connection.setCatalog(catalog);
	}

	@Override
	public String getCatalog() throws SQLException {
		tryToReopen();
		
		return connection.getCatalog();
	}

	@Override
	public void setTransactionIsolation(int level) throws SQLException {
		tryToReopen();
		
		connection.setTransactionIsolation(TRANSACTION_NONE);
	}

	@Override
	public int getTransactionIsolation() throws SQLException {
		tryToReopen();
		
		return connection.getTransactionIsolation();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		tryToReopen();
		
		return connection.getWarnings();
	}

	@Override
	public void clearWarnings() throws SQLException {
		tryToReopen();
		
		connection.clearWarnings();
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		tryToReopen();
		
		return connection.createStatement(resultSetType, resultSetConcurrency);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		tryToReopen();
		
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		tryToReopen();
		
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		tryToReopen();
		
		return connection.getTypeMap();
	}

	@Override
	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		tryToReopen();
		
		connection.setTypeMap(map);
	}

	@Override
	public void setHoldability(int holdability) throws SQLException {
		tryToReopen();
		
		connection.setHoldability(holdability);
	}

	@Override
	public int getHoldability() throws SQLException {
		tryToReopen();
		
		return connection.getHoldability();
	}

	@Override
	public Savepoint setSavepoint() throws SQLException {
		tryToReopen();
		
		return connection.setSavepoint();
	}

	@Override
	public Savepoint setSavepoint(String name) throws SQLException {
		tryToReopen();
		
		return connection.setSavepoint(name);
	}

	@Override
	public void rollback(Savepoint savepoint) throws SQLException {
		tryToReopen();
		
		connection.rollback(savepoint);
	}

	@Override
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		tryToReopen();
		
		connection.releaseSavepoint(savepoint);
	}

	@Override
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		tryToReopen();
		
		
		return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		tryToReopen();
		
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		tryToReopen();
		
		
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		tryToReopen();
		
		return connection.prepareStatement(sql, autoGeneratedKeys);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		tryToReopen();
		
		return connection.prepareStatement(sql, columnIndexes);
	}

	@Override
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		tryToReopen();
		
		return connection.prepareStatement(sql, columnNames);
	}

	@Override
	public Clob createClob() throws SQLException {
		tryToReopen();
		
		return connection.createClob();
	}

	@Override
	public Blob createBlob() throws SQLException {
		tryToReopen();
		
		return connection.createBlob();
	}

	@Override
	public NClob createNClob() throws SQLException {
		tryToReopen();
		
		return connection.createNClob();
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		tryToReopen();
		
		return connection.createSQLXML();
	}

	@Override
	public boolean isValid(int timeout) throws SQLException {
		tryToReopen();
		
		return connection.isValid(timeout);
	}

	@Override
	public void setClientInfo(String name, String value) throws SQLClientInfoException {
		try {
			tryToReopen();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		connection.setClientInfo(name, value);
	}

	@Override
	public void setClientInfo(Properties properties) throws SQLClientInfoException {
		try {
			tryToReopen();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		connection.setClientInfo(properties);
	}

	@Override
	public String getClientInfo(String name) throws SQLException {
		tryToReopen();
		
		return connection.getClientInfo(name);
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		tryToReopen();
		
		return connection.getClientInfo();
	}

	@Override
	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
		tryToReopen();
		
		return connection.createArrayOf(typeName, elements);
	}

	@Override
	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
		tryToReopen();
		
		return connection.createStruct(typeName, attributes);
	}

	@Override
	public void setSchema(String schema) throws SQLException {
		tryToReopen();
		
		connection.setSchema(schema);
	}

	@Override
	public String getSchema() throws SQLException {
		String schema = connService.getDbConnInfo().getSchema();
		
		if (utilities.stringHasValue(schema)) {
			return schema;
		}
		
		tryToReopen();
		
		if ( DBUtilities.isMySQLDB(this))
			return this.getCatalog();
		if (DBUtilities.isOracleDB(this))
			return this.getMetaData().getUserName();
		if (DBUtilities.isPostgresDB(this))
			return "public";	
		
		return connection.getSchema();
	}

	@Override
	public void abort(Executor executor) throws SQLException {
		tryToReopen();
		
		connection.abort(executor);
	}

	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		tryToReopen();
		
		connection.setNetworkTimeout(executor, milliseconds);
	}

	@Override
	public int getNetworkTimeout() throws SQLException {
		tryToReopen();
		
		return connection.getNetworkTimeout();
	}
	
	@Override
	public String toString() {
		return "id["+id+"] openDate["+CommonUtilities.getInstance().formatDateToDDMMYYYY_HHMISS(openDate)+"] closeDate["+CommonUtilities.getInstance().formatDateToDDMMYYYY_HHMISS(closeDate)+"]";
	}
}
