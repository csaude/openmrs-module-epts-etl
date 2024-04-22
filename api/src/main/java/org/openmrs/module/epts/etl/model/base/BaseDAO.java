package org.openmrs.module.epts.etl.model.base;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.EptsEtlLogger;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBOperation;

/**
 * Base DAO This class provides the base datasource & common functionaloty for all DAO's in the
 * application. All DAO's must extend this class to get access to the datasource.
 * 
 * @author Juliano Ipolito
 */
public abstract class BaseDAO {
	
	public static EptsEtlLogger logger = EptsEtlLogger.getLogger(BaseDAO.class);
	
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	public static final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	
	public static final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	/**
	 * The datasource (we need only one for the whole program).
	 */
	//private static List<DataSourceInfo> dataSources = new ArrayList<>();
	
	/**
	 * Returns the datasource. Looks it up in context on the first call.
	 * 
	 * @return The datasource.
	 * @throws DBException
	 */
	/*public static DataSource getDataSource(String  context) throws DBException{
		return  tryToGetDataSourceFromContext(context);
	}
		
	private static synchronized DataSource tryToGetDataSourceFromContext(String  context) throws DBException{
		DataSource dataSource = DataSourceInfo.findDataSource(dataSources, context);
		
		if(dataSource == null){
			try{
				TimeZone timeZone = TimeZone.getTimeZone("Africa/Johannesburg");
				TimeZone.setDefault(timeZone);
			     
				Context initContext = new InitialContext();				
				dataSource = (DataSource) initContext.lookup(context);
				
				dataSources.add(new DataSourceInfo(dataSource, context));
			}
			catch(NamingException e){
				throw new DBException(e.getMessage());
			}
			if(dataSource == null) throw new DBException("DataSource not found.");
		}	
		
		return dataSource;
	} */
	
	/**
	 * Perform an SQL Select, using prepared statement.
	 * 
	 * @param loaderHelper the helper for loading extra information
	 * @param sql string to perform.
	 * @param params Array of objects to fill question marks in the update string.
	 * @return generated object with retrieved data from DB
	 */
	
	public static <T extends VO> T find(VOLoaderHelper loaderHelper, Class<T> voClass, String sql, Object[] params,
	        Connection conn) throws DBException {
		
		List<T> result;
		result = search(voClass, sql, params, conn);
		
		if (utilities.arrayHasElement(result))
			return result.get(0);
		
		return null;
	}
	
	/**
	 * Perform an database querying on the table related to the passed class.
	 * 
	 * @param condition the condition to use.
	 * @param params Array of objects to fill question marks in the update string.
	 * @return generated object with retrieved data from DB
	 */
	
	public static <T extends VO> T find(Class<T> voClass, String condition, Connection conn) throws DBException {
		return find(null, voClass, condition, conn);
	}
	
	/**
	 * Perform an SQL Select, using prepared statement.
	 * 
	 * @param sql string to perform.
	 * @param params Array of objects to fill question marks in the update string.
	 * @return generated object with retrieved data from DB
	 */
	
	public static <T extends VO> T find(Class<T> voClass, String sql, Object[] params, Connection conn) throws DBException {
		return find(null, voClass, sql, params, conn);
	}
	
	/**
	 * Perform an database querying on the table related to the passed class.
	 * 
	 * @param condition the condition to use.
	 * @param params Array of objects to fill question marks in the update string.
	 * @return generated object with retrieved data from DB
	 */
	
	public static <T extends VO> T find(VOLoaderHelper loaderHelper, Class<T> voClass, String condition, Connection conn)
	        throws DBException {
		T obj;
		
		try {
			obj = voClass.newInstance();
			
			if (loaderHelper != null) {
				loaderHelper.beforeLoad(obj);
			}
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		
		String sql = "";
		
		sql += " select * ";
		sql += " from " + obj.generateTableName();
		sql += " where " + condition;
		
		List<T> result;
		result = search(loaderHelper, voClass, sql, null, conn);
		
		if (utilities.arrayHasElement(result))
			return result.get(0);
		
		return null;
	}
	
	/**
	 * Perform an SQL Select, using prepared statement.
	 * 
	 * @param sql to perform.
	 * @param params Array of objects to fill question marks in the update string.
	 * @return generated list os object using retrived data from DB.
	 */
	public static <T extends VO> List<T> search(Class<T> voClass, String sql, Object[] params, Connection conn)
	        throws DBException {
		return search(null, voClass, sql, params, conn);
	}
	
	/**
	 * Perform an SQL Select, using prepared statement.
	 * 
	 * @param voDataLoader the data loader for searched objects
	 * @param voClass the class of searched records
	 * @param sql to perform.
	 * @param params Array of objects to fill question marks in the update string.
	 * @return generated list os object using retrived data from DB.
	 */
	public static <T extends VO> List<T> search(VOLoaderHelper voDataLoader, Class<T> voClass, String sql, Object[] params,
	        Connection conn) throws DBException {
		ArrayList<T> result = new ArrayList<T>();
		ResultSet rs = null;
		
		PreparedStatement st = null;
		
		try {
			Constructor<T> factory = voClass.getConstructor();
			
			st = conn.prepareStatement(sql.toLowerCase());
			
			loadParamsToStatment(st, params, conn);
			
			st.executeQuery();
			
			rs = st.getResultSet();
			
			while (rs.next()) {
				T instance = factory.newInstance();
				
				result.add(instance);
				
				if (voDataLoader != null) {
					voDataLoader.beforeLoad(instance);
				}
				
				instance.load(rs);
				
				if (voDataLoader != null) {
					voDataLoader.afterLoad(instance);
				}
				
			}
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			releaseDBResources(st, rs, conn);
		}
		
		return result;
	}
	
	private static void releaseDBResources(PreparedStatement st, ResultSet rs, Connection conn) {
		try {
			st.close();
			rs.close();
		}
		catch (NullPointerException e) {
			rs = null;
			st = null;
		}
		catch (Exception e) {
			rs = null;
			st = null;
			
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		rs = null;
		st = null;
	}
	
	/**
	 * Helper function, for filling in executeUpdate(params)
	 * 
	 * @param in
	 * @return if(in == 0) retunr null; else Integer(in);
	 */
	public static Integer intOrNull(int in) {
		if (in == 0)
			return null;
		return Integer.valueOf(in);
	}
	
	/**
	 * Perform an SQL, using prepared statement.
	 * 
	 * @param sql string to perform.
	 * @param params array of objects to fill question marks in the update string.
	 * @param connection to use
	 */
	public static Integer executeQueryWithRetryOnError(String sql, Object[] params, Connection connection)
	        throws DBException {
		try {
			return executeQueryWithoutRetry(sql, params, connection);
		}
		catch (DBException e) {
			if (!tryToSolveIssues(e, sql, params, connection)) {
				throw e;
			} else
				return 0;
		}
	}
	
	public static void executeBatch(Connection conn, String... batches) throws DBException {
		
		try {
			Statement st = conn.createStatement();
			
			for (String batch : batches) {
				st.addBatch(batch.toLowerCase());
			}
			
			st.executeBatch();
			
			st.close();
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static Integer executeQueryWithoutRetry(String sql, Object[] params, Connection connection) throws DBException {
		PreparedStatement st = null;
		
		try {
			st = connection.prepareStatement(sql.toLowerCase(), Statement.RETURN_GENERATED_KEYS);
			
			loadParamsToStatment(st, params, connection);
			
			st.execute();
			
			ResultSet rs = st.getGeneratedKeys();
			
			if (rs != null && rs.next()) {
				return rs.getInt(1);
			} else
				return 0;
			
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		finally {
			try {
				st.close();
				st = null;
			}
			catch (NullPointerException e) {
				st = null;
			}
			catch (SQLException e) {
				st = null;
				throw new DBException(e);
			}
			st = null;
		}
	}
	
	public static void loadParamsToStatment(PreparedStatement st, Object[] params, Connection conn)
	        throws SQLException, IOException {
		if (params == null)
			return;
		
		for (int i = 0; i < params.length; i++) {
			if (params[i] instanceof InputStream) {
				st.setBinaryStream(i + 1, (InputStream) params[i], ((InputStream) params[i]).available());
			} else if (params[i] instanceof FileItem) {
				st.setBinaryStream(i + 1, ((FileItem) params[i]).getInputStream(),
				    ((FileItem) params[i]).getInputStream().available());
			} else if (params[i] instanceof Date) {
				st.setTimestamp(i + 1, new Timestamp(((Date) params[i]).getTime()));
			} else if (params[i] instanceof Character) {
				st.setString(i + 1, params[i].toString());
			} else
				st.setObject(i + 1, params[i]);
		}
	}
	
	private static boolean tryToSolveIssues(DBException e, String sql, Object[] params, Connection conn) throws DBException {
		if (e.isDeadLock(conn)) {
			logger.warn("DEADLOCK DETECTED");
			
			DBOperation dbOp = new DBOperation(sql, params, conn, 50);
			dbOp.retryDueTemporaryDBError("DEADLOCK");
			
			return true;
		} else if (e.isLockWaitTimeExceded(conn)) {
			logger.warn("LOCK WAIT TIME EXCEED...");
			DBOperation dbOp = new DBOperation(sql, params, conn, 50);
			dbOp.retryDueTemporaryDBError("LOCK WAIT TIME EXCEED");
			
			return true;
		}
		
		return false;
	}
	
	public static void commitConnection(Connection conn) {
		try {
			if (conn == null)
				return;
			if (conn.isClosed())
				return;
			
			conn.commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void commitAndCloseConnection(Connection conn) {
		try {
			
			if (conn == null)
				return;
			if (conn.isClosed())
				return;
			
			conn.commit();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void rollBackAndCloseConnection(Connection conn) {
		try {
			
			if (conn == null)
				return;
			if (conn.isClosed())
				return;
			
			conn.rollback();
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeConnection(Connection conn) {
		try {
			if (conn == null)
				return;
			if (conn.isClosed())
				return;
			
			conn.close();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void commit(Connection conn) {
		try {
			if (conn != null)
				conn.commit();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void rollback(Connection conn) {
		rollback(conn, null);
	}
	
	public static void rollback(Connection conn, Savepoint savepoint) {
		try {
			if (conn.isClosed())
				return;
			
			if (savepoint != null)
				conn.rollback(savepoint);
			else
				conn.rollback();
			
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Insere na BD o registo em vo. NOTE: > O método irá tentar gerar o selfId se este for igual a
	 * zero. > O id da reparticao é obrigatório caso o selfId não tenha sido definido. Se este nao
	 * for especificado, uma excepcao sera lancada.
	 * 
	 * @param vo
	 * @param sql
	 * @param params
	 * @param conn
	 * @throws DBException
	 */
	public static void insert(BaseVO vo, String sql, Object[] params, Connection conn) throws DBException {
		//logger.trace("INSERTING RECORD ON ["+vo.generateTableName() + "]");
		
		executeQueryWithRetryOnError(sql, params, conn);
	}
	
	private static void setParamsOnCallableStatement(CallableStatement call, int startFrom, Object... params)
	        throws SQLException, IOException {
		if (params != null && params.length > 0) {
			for (int i = 0; i < params.length; i++) {
				if (params[i] instanceof InputStream) {
					call.setBinaryStream(i + startFrom, (InputStream) params[i], ((InputStream) params[i]).available());
				} else if (params[i] instanceof FileItem) {
					call.setBinaryStream(i + startFrom, ((FileItem) params[i]).getInputStream(),
					    ((FileItem) params[i]).getInputStream().available());
				} else if (params[i] instanceof Date) {
					call.setTimestamp(i + startFrom, new Timestamp(((Date) params[i]).getTime()));
				} else
					call.setObject(i + startFrom, params[i]);
			}
		}
	}
	
	private static String generateCallStatementString(String function, Object... params) {
		String strCall = function + "(";
		
		if (params != null && params.length > 0) {
			
			strCall += "?";
			for (int i = 1; i < params.length; i++) {
				strCall += ",?";
			}
		}
		
		strCall += ")";
		
		return strCall;
	}
	
	public static Object executeBDFunction(Connection conn, String functionName, Object... params)
	        throws SQLException, IOException {
		CallableStatement call = conn.prepareCall("{? = call " + generateCallStatementString(functionName, params) + "}");
		
		call.registerOutParameter(1, Types.DOUBLE);
		
		setParamsOnCallableStatement(call, 2, params);
		
		call.execute();
		
		Object result = call.getObject(1);
		
		call.close();
		
		return result;
	}
	
	public static Object executeNonNumericBDFunction(Connection conn, String functionName, Object... params)
	        throws SQLException, IOException {
		CallableStatement call = conn.prepareCall("{? = call " + generateCallStatementString(functionName, params) + "}");
		
		call.registerOutParameter(1, Types.VARCHAR);
		
		setParamsOnCallableStatement(call, 2, params);
		
		call.execute();
		
		Object result = call.getObject(1);
		
		call.close();
		
		return result;
	}
	
	public static void executeBDProcedure(Connection conn, String procedureName, Object... params)
	        throws SQLException, IOException {
		CallableStatement call = conn.prepareCall("{call " + generateCallStatementString(procedureName, params) + "}");
		
		setParamsOnCallableStatement(call, 1, params);
		
		call.execute();
		
		call.close();
	}
	
	public static Object executeBDProcedureWithReturnValue(Connection conn, String procedureName, Object... params)
	        throws SQLException, IOException {
		Double valor = null;
		
		params = utilities.addToParams(params.length, params, valor);
		
		CallableStatement call = conn.prepareCall("{call " + generateCallStatementString(procedureName, params) + "}");
		
		setParamsOnCallableStatement(call, 1, params);
		
		call.registerOutParameter(params.length, Types.DOUBLE);
		
		call.execute();
		
		Object result = call.getObject(params.length);
		
		call.close();
		
		return result;
	}
	
	public static <T extends BaseVO> T createInstance(Class<T> classe) {
		return BaseVO.createInstance(classe);
	}
	
}
