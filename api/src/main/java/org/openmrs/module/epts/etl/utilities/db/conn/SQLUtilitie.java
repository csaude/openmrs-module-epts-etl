package org.openmrs.module.epts.etl.utilities.db.conn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.FuncoesGenericas;

/**
 * Esta classe possui funções utilitárias para operações na base de dados
 * 
 * @author Artur Henriques
 */

public class SQLUtilitie  {

	/**
	 * Esta função devolve o próximo valor da sequencia passado por parâmetro
	 * 
	 * @param strSequencia
	 *            A sequencia que se pretende retirar o próximo valor
	 * 
	 * @param oDataSource
	 *            O datasource da base de dados pretendida
	 * 
	 * @return int - O valor seguinte da sequência pretendida
	 */
	static public long getNextValueKey(String strSequencia, Connection conn) {
		long nKeyNova = 0;
		
		Statement st = null;
		ResultSet rs = null;
		ArrayList<String> logs = new ArrayList<String>();
		
		try {

			st = conn.createStatement();
			logs.add("Opened st");
			
			rs = st.executeQuery("SELECT " + strSequencia.trim()
					+ ".nextval FROM dual");
			logs.add("Opened rs");
			
			while (rs.next()) {
				nKeyNova = rs.getLong(1);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				st.close();
				logs.add("Closed st");
				rs.close();
				logs.add("Closed rs");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
		}

		return nKeyNova;
	}

	
	static public Date getCurrentSystemDate(Connection conn) {
		return DateAndTimeUtilities.getCurrentSystemDate(conn);
	}

	public static boolean isInUseID(String tabela, String campoID, long ID,Connection conn) throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		String codigo = "";
		try {

			String sql = " SELECT count(" + campoID + ") " + " FROM        "
					+ tabela + " WHERE       " + campoID + " =? ";

			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.setObject(1, "" + ID);

			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");

			while (rs.next()) {
				codigo = rs.getString(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {			
			try {
				st.close();
				logs.add("Closed st");
				rs.close();
				logs.add("Closed rs");
			} catch (Exception e) {
				//e.printStackTrace();
				throw new RuntimeException(e);
			}
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
		}
		
		if ( !(codigo.equals("0") || codigo.isEmpty()))
			return true;
		return false;
	}

	public static boolean isInUseID(String tabela, String campoID, String ID, Connection conn) throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		String codigo = "";
			
		String sql = " SELECT count(" + campoID + ") " + " FROM        "
					+ tabela + " WHERE       " + campoID + " =? ";

			try {
				st = conn.prepareStatement(sql);
				logs.add("Opened st");
				
				st.setString(1, "" + ID);

				st.execute();
				rs = st.getResultSet();
				logs.add("Opened rs");
				
				while (rs.next()) {
					codigo = rs.getString(1);
				}
			} catch (SQLException e) {
				throw new DBException(e);
			}
			finally{
				try {
					st.close();
					logs.add("Closed st");
					rs.close();
					logs.add("Closed rs");
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}	
				finally{
					//File.write("C:\\sqlHelperLogs.txt", logs);
				}
			}
		
		if (!codigo.equals("0")) return true;
		
		return false;
	}


	/**
	 * 
	 * @param tabela
	 * @param campo
	 * @return Retorna o proximo numero da sequencia de ids da tabela em
	 *         'tabela' do campo em 'campo' com o campo fixo 'campoFixo' e valor
	 *         'valorFixo'
	 */
	static public long getNextId(String tabela, String campoFixo, String valorFixo, String campoSequencial, Connection conn) throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		long codigo = 0;
		try {

			
			String sql = " SELECT count( " + campoSequencial + " ) " + 
						 " FROM        " + tabela + 
						 " WHERE       " + campoFixo + " =? ";

			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.setObject(1, valorFixo);

			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			while (rs.next()) {
				codigo = rs.getLong(1);
			}			
		} catch (SQLException e) {
			throw new DBException(e);
		} 
		finally{
			try {
				st.close();
				logs.add("Closed st");
				rs.close();
				logs.add("Closed rs");
			} catch (Exception e) {
				e.printStackTrace();
			}	
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
			
			rs = null;
			st = null;
		}
		return codigo + 1;
	}
	
	
	static public long getNextId(String tabela, String campoSequencial, String condition, Connection conn) throws DBException {
		try {
			return getNextId(tabela, campoSequencial, condition, true, conn);
		} catch (Exception e) {
			return getNextId(tabela, campoSequencial, condition, false, conn);
		}
	}
	
	/**
	 * 
	 * @param tabela
	 * @param campo
	 * @return Retorna o proximo numero da sequencia de ids da tabela em
	 *         'tabela' do campo em 'campo' com o campo fixo 'campoFixo' e valor
	 *         'valorFixo'
	 */
	static public long getNextId(String tabela, String campoSequencial, String condition, boolean putSchemaOnTableName, Connection conn) throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		long codigo = 0;
		
		try {
			String sql = " SELECT max("+campoSequencial+") " +
						 " FROM " + (putSchemaOnTableName ? DBUtilities.tryToPutSchemaOnDatabaseObject(tabela, conn) : tabela) + 
						 " WHERE " + condition;

			st = conn.prepareStatement(sql);
			logs.add("Opened st");

			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			while (rs.next()) {
				try {
					codigo = rs.getLong(1);
				} catch (Exception e) {
					codigo = countQtdRecords(tabela, campoSequencial, condition, conn);
				}
			}			
		} catch (SQLException e) {
			throw new DBException(e);
		} 
		finally{
			try {
				st.close();
				logs.add("Closed st");
				rs.close();
				logs.add("Closed rs");
			} catch (Exception e) {
				e.printStackTrace();
			}	
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
			
			rs = null;
			st = null;
		}
		return codigo + 1;
	}
	


	static public long countQtdRecords(String tabela, String campoSequencial, String condition, Connection conn) throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		long codigo = 0;
		try {

			
			String sql = " SELECT count("+campoSequencial+") " +
						 " FROM " + tabela + 
						 " WHERE " + condition;

			st = conn.prepareStatement(sql);
			logs.add("Opened st");

			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			while (rs.next()) {
				codigo = rs.getLong(1);
			}			
		} catch (SQLException e) {
			throw new DBException(e);
		} 
		finally{
			try {
				st.close();
				logs.add("Closed st");
				rs.close();
				logs.add("Closed rs");
			} catch (Exception e) {
				e.printStackTrace();
			}	
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
			
			rs = null;
			st = null;
		}
		return codigo;
	}
	
	/**
	 * Busca o valor máximo na tabela dada com a condição dada.
	 * Note que, se o campo em causa não for do tipo numérico, o método irá retornar a quantidade 
	 * de registos que satisfaçam aquela condição.
	 * @param tabela
	 * @param coluna
	 * @param condition
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static long getMaxID(String tabela, String coluna, String condition, Connection conn) throws DBException{
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		
	    String sql;
	    long maxID = 0;
	    
	    condition = condition.toUpperCase();
	    
	    sql = " SELECT  MAX(" + coluna + ") AS maxID FROM  " + tabela;
	
	    if (!condition.equals("")) sql = sql + " WHERE "+condition;
	   
	    try {
			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			if (rs.next()) {
				if (rs.getString("maxID") != null) {
					if (FuncoesGenericas.isNumeric(rs.getString("maxID")))
						maxID = Long.parseLong(rs.getString("maxID"));
					else getNextId(tabela, coluna, condition, conn);
						
				}
			}
		} catch (SQLException e) {
			throw new DBException(e);
		}
		finally{
			try {
				st.close();
				logs.add("Closed st");
				rs.close();
				logs.add("Closed rs");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
			
			rs = null;
			st = null;
		}
		
		return maxID;
	}

	public static boolean isInUseID(String tabela,String coluna, String condition , long findingID, Connection conn) throws DBException{
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		boolean isInUse = false;
	    String sql;
	    String auxCondicao;
	    //numero_processo = '2345678'
	    //
	    auxCondicao = coluna + " = " + findingID;
    
	    sql = " SELECT  " + coluna + " FROM    " + tabela;

    
        if (!condition.equals("")) auxCondicao = auxCondicao + " AND " + condition;
          
        if (!auxCondicao.equals("")) sql = sql + " WHERE " + auxCondicao;
          
		
		
		try {
			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			isInUse = rs.next();
		} catch (SQLException e) {
			throw new DBException(e);
		}
		finally{
			try {
				st.close();
				logs.add("Closed st");
				if (rs!=null) rs.close();
			   
				logs.add("Closed rs");
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
		}
		
		rs = null;
		st = null;
		
		return isInUse;
	}

	/**
	 * 
	 * @param tabela
	 * @param campo
	 * @return Retorna o proximo numero da sequencia de ids da tabela em
	 *         'tabela' do campo em 'campo'
	 */
	static public long getNextCod(String tabela, String campo, Connection conn) throws DBException{
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		long codigo = 0;
		try {

			st = conn.prepareStatement("SELECT MAX(" + campo + ") FROM " + tabela);
			logs.add("Opened st");
			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			while (rs.next()) {
				codigo = rs.getLong(1);
			}
		} catch (SQLException e) {
			throw new DBException(e);
		} finally {
			try {
				if (st != null) st.close();
				logs.add("Closed st");
				if (rs != null) rs.close();
				logs.add("Closed rs");
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			finally{
				//File.write("C:\\sqlHelperLogs.txt", logs);
			}
		}
		return codigo + 1;
	}
	
	/**
	 * Gera as colunas[table fields] presentes numa instrucao sql insert 
	 * @param insertInstrunction
	 * @return um array de Strings contendo as colunas [table fields] da instrucao SQL recebida pelo parametro
	 * @author JPBOANE 17/12/2012
	 */
	public static String[] parseTableFieldsOnInserInstrunction(String insertInstrunction){
		insertInstrunction = removeOccurence(insertInstrunction, (char)34, ' ');
		
		String[] columns = insertInstrunction.split("\\(")[1].split("\\)")[0].split(",");
		 
		return FuncoesGenericas.trimAll(columns);
	}
	
	/**
	 * Transforma uma 'prepared statment' numa instrução SQL pronta para ser executada num Statment
	 * 
	 * @param insertInstrunction
	 * @return
	 * @throws TipoDeDadosNaoSuportadoException 
	 */
	public static String transformPreparedStatmentToFullSQLStatment(String preparedStatment, Object[] params, Connection conn) throws ForbiddenOperationException{
		if (!CommonUtilities.getInstance().stringHasValue(preparedStatment)) return "";
		
		String fullSQL = "";
		
		int currParam=0;
		
		String dateFormat ;
		try {
			dateFormat = DBUtilities.isOracleDB(conn) ? DateAndTimeUtilities.ORACLE_DATE_TIME_FORMAT : DateAndTimeUtilities.DATE_TIME_FORMAT;
		} catch (DBException e) {
			throw new RuntimeException(e);
		}
		
		for (int i = 0; i < preparedStatment.length(); i ++){
			
			if (preparedStatment.charAt(i) == '?'){
				fullSQL = fullSQL + convertParamToStringValueForSQLParam(params[currParam++], dateFormat);
			}
			else fullSQL = fullSQL+preparedStatment.charAt(i);
		}
		
		fullSQL = FuncoesGenericas.removeDuplicatedEmptySpace(fullSQL);
		
		return fullSQL;
	}
	
	/**
	 * Transforma um objecto numa string preparada para uma instrução SQL
	 * @param param
	 * @return
	 * @throws TipoDeDadosNaoSuportadoException 
	 */
	private static String convertParamToStringValueForSQLParam(Object param, String dateFormat) throws ForbiddenOperationException{
		if (param == null) return null;
		
		if (param instanceof Integer) return ""+((Integer)param).intValue();
		if (param instanceof Long) return ""+((Long)param).longValue();
		if (param instanceof Double) return ""+((Double)param).doubleValue();
		if (param instanceof Character) return "'" + ((Character)param).charValue() + "'";
		if (param instanceof Boolean) return "" + (((Boolean)param).booleanValue() ? 1 : 0);
		
		
		if (param instanceof Date) {
			return "TO_DATE('"+DateAndTimeUtilities.formatToDDMMYYYY_HHMISS((Date)param)+"' , '"+dateFormat+"')";
		}
		
		if(param instanceof String) return "'" + removeForbiddenSymbols((String)param) + "'";
		
		throw new ForbiddenOperationException("Tipo de dados não suportado ["+param.getClass()+"]");
	}
	
	private static String removeForbiddenSymbols(String msg){
		return msg.replaceAll("'", "");
	}
		
	@SuppressWarnings("unused")
	private static Connection tempOpenConnection(){
		String OracleClasse = "oracle.jdbc.driver.OracleDriver";
		String OracleURL 	= "jdbc:oracle:thin:@gov.minag.lims:1521:lims";
		String OracleUser	= "lims";
		String OraclePass	= "exi2k12";
		
		return  null;//BaseDAO.openConnection(OracleClasse, OracleURL, OracleUser, OraclePass);
	}
	
	@SuppressWarnings("unused")
	private static Connection tempOpenPostgresConnection(){
		String OracleClasse = "org.postgresql.Driver";
		String OracleURL 	= "jdbc:postgresql://127.0.0.1:5432/sigbarpi";
		String OracleUser	= "postgres";
		String OraclePass	= "exi2k12";
		
		return null; //BaseDAO.openConnection(OracleClasse, OracleURL, OracleUser, OraclePass);
	}
	
	
	private static String removeOccurence(String string, char oldChar, char newChar){
		string.replace(oldChar , newChar);
		
		return string;
	}
	
	/**
	 * Dado um SQL do tipo SELECT, cria um novo select (select faseado) que permite seleccionar apenas registos a partir de um ponto ate outro
	 * 
	 * @param originalSelect
	 * @param start
	 * @param qtdRecordSuported
	 * @param currParams
	 * @throws RuntimeException se o select original (originalSelect) não tiver a coluna ROWNUM
	 * 
	 * @return
	 * @throws DBException 
	 */
	public static String createPhasedSelectOracle(String originalSelect, int start, int qtdRecordSuported, Connection conn) throws DBException{
		originalSelect = originalSelect.toUpperCase();
		
		if (originalSelect.contains("ROW_NUM")) throw new IllegalArgumentException("Rever a sql. Remova a coluna ROW_NUM"); 
		
		String sqlPlusRowNum =  " SELECT TAB_TEMP.*, ROWNUM ROW_NUM ";
			   sqlPlusRowNum += " FROM ("; 
			   sqlPlusRowNum +=	 originalSelect;
			   sqlPlusRowNum +=  ") TAB_TEMP ";
			   
	   String phasedSQL = " SELECT * ";
			  phasedSQL += " FROM ("; 
	   		  phasedSQL +=  sqlPlusRowNum; 
	   		  phasedSQL += ")";
	   		  phasedSQL += " WHERE ROW_NUM BETWEEN " + start + " AND " + (start+qtdRecordSuported-1); 
		
		return phasedSQL;
	}
	
	/**
	 * Dado um SQL do tipo SELECT, cria um novo select (select faseado) que permite seleccionar apenas registos a partir de um ponto ate outro
	 * 
	 * @param originalSelect
	 * @param start
	 * @param qtdRecordSuported
	 * @throws RuntimeException se o select original (originalSelect) não tiver a coluna ROWNUM
	 * 
	 * @return
	 * @throws DBException 
	 */
	public static String createPhasedSelectPostgres(String originalSelect, int start, int qtdRecordSuported, Connection conn) throws DBException{
		try {
			originalSelect = originalSelect.toUpperCase();
			
			originalSelect = originalSelect.replace("ROW_NUM", "9");
			originalSelect = originalSelect.replace("ROWNUM", "");
			
			String fasedCondition = "";
				
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.ORACLE_DATABASE)){
				fasedCondition = " WHERE ROWNUM >= " + start + " AND ROWNUM <= " + (start+qtdRecordSuported-1);
			}
			else	
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.POSTGRESQL_DATABASE)){
				fasedCondition = " OFFSET " + (start-1) + " LIMIT  " + qtdRecordSuported;
			}
			
			originalSelect = " SELECT * " +
							 " FROM (" + originalSelect + ") MYSUBQUERY " +
							   fasedCondition;
			
			return originalSelect;
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	
	
	/**
	 * Dado um SQL do tipo SELECT, cria um novo select (select faseado) que permite seleccionar apenas registos a partir de um ponto ate outro
	 * 
	 * @param originalSelect
	 * @param start
	 * @param qtdRecordSuported
	 * @throws RuntimeException se o select original (originalSelect) não tiver a coluna ROWNUM
	 * 
	 * @return
	 * @throws DBException 
	 */
	public static String createPhasedSelectMySQL(String originalSelect, int start, int qtdRecordSuported, Connection conn) throws DBException{
		boolean isDistinct =  originalSelect.contains("DISTINCT");
		
		String fasedCondition = "";
			
		fasedCondition = " LIMIT " + (start-1) + "," + qtdRecordSuported;
		
		if (isDistinct) {
			originalSelect = " SELECT * " +
						 	 " FROM (" + originalSelect + ") MYSUBQUERY ";
		}
		
		originalSelect += fasedCondition;
		
		return originalSelect;
	}
	
	/**
	
	/**
	 * Dado um SQL do tipo SELECT, cria um novo select (select faseado) que permite seleccionar apenas registos a partir de um ponto ate outro
	 * 
	 * @param originalSelect
	 * @param start
	 * @param qtdRecordSuported
	 * @throws RuntimeException se o select original (originalSelect) não tiver a coluna ROWNUM
	 * 
	 * @return
	 * @throws DBException 
	 */
	public static String createPhasedSelect(String originalSelect, int start, int qtdRecordSuported, Connection conn) throws DBException{
		try {
					
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.ORACLE_DATABASE)){
				return createPhasedSelectOracle(originalSelect, start, qtdRecordSuported, conn);
			}
			else	
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.POSTGRESQL_DATABASE)){
				return createPhasedSelectPostgres(originalSelect, start, qtdRecordSuported, conn);
			}
			else	
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.MYSQL_DATABASE)){
				return createPhasedSelectMySQL(originalSelect, start, qtdRecordSuported, conn);
			}
			
			throw new ForbiddenOperationException("SGBD nao suportado!");
		} catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	/**
	 * Transforma em String 
	 * @param tableName
	 * @return
	 * @throws SQLException 
	 */
	public static String toStringTableColumns(String tableName, Connection conn) throws SQLException{
		String colunas = "";
		
		PreparedStatement st = conn.prepareStatement("SELECT * FROM "+tableName + " WHERE 1 != 1");
		
		ResultSet rs = st.executeQuery();
		ResultSetMetaData rsMetaData = rs.getMetaData();
		
		for (int i=1; i <= rsMetaData.getColumnCount(); i++){
			colunas = CommonUtilities.getInstance().addAtributeToValidationString(colunas, rsMetaData.getColumnName(i), ",");
		}
	
		return colunas;
	}
}
