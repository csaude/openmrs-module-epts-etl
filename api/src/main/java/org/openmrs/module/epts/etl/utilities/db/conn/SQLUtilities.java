package org.openmrs.module.epts.etl.utilities.db.conn;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.datasource.SqlConditionElement;
import org.openmrs.module.epts.etl.conf.datasource.SqlFunctionInfo;
import org.openmrs.module.epts.etl.conf.interfaces.SqlFunctionType;
import org.openmrs.module.epts.etl.conf.types.DbmsType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.FuncoesGenericas;

/**
 * Esta classe possui funções utilitárias para operações na base de dados
 * 
 * @author Artur Henriques
 */

public class SQLUtilities {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	/**
	 * Esta função devolve o próximo valor da sequencia passado por parâmetro
	 * 
	 * @param strSequencia A sequencia que se pretende retirar o próximo valor
	 * @param oDataSource O datasource da base de dados pretendida
	 * @return int - O valor seguinte da sequência pretendida
	 */
	static public long getNextValueKey(String strSequencia, Connection conn) {
		long nKeyNova = 0;
		
		Statement st = null;
		ResultSet rs = null;
		
		List<String> logs = new ArrayList<String>();
		
		try {
			
			st = conn.createStatement();
			logs.add("Opened st");
			
			rs = st.executeQuery("SELECT " + strSequencia.trim() + ".nextval FROM dual");
			logs.add("Opened rs");
			
			while (rs.next()) {
				nKeyNova = rs.getLong(1);
			}
			
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			ensureResourceAreClosed(logs, (PreparedStatement) st, rs);
		}
		
		return nKeyNova;
	}
	
	static public Date getCurrentSystemDate(Connection conn) {
		return DateAndTimeUtilities.getCurrentSystemDate(conn);
	}
	
	public static boolean isInUseID(String tabela, String campoID, long ID, Connection conn) throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		String codigo = "";
		try {
			
			String sql = " SELECT count(" + campoID + ") " + " FROM        " + tabela + " WHERE       " + campoID + " =? ";
			
			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.setObject(1, "" + ID);
			
			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			while (rs.next()) {
				codigo = rs.getString(1);
			}
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			ensureResourceAreClosed(logs, st, rs);
		}
		
		if (!(codigo.equals("0") || codigo.isEmpty()))
			return true;
		return false;
	}
	
	public static boolean isInUseID(String tabela, String campoID, String ID, Connection conn) throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		String codigo = "";
		
		String sql = " SELECT count(" + campoID + ") " + " FROM        " + tabela + " WHERE       " + campoID + " =? ";
		
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
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			ensureResourceAreClosed(logs, st, rs);
		}
		
		if (!codigo.equals("0"))
			return true;
		
		return false;
	}
	
	/**
	 * @param tabela
	 * @param campo
	 * @return Retorna o proximo numero da sequencia de ids da tabela em 'tabela' do campo em
	 *         'campo' com o campo fixo 'campoFixo' e valor 'valorFixo'
	 */
	static public long getNextId(String tabela, String campoFixo, String valorFixo, String campoSequencial, Connection conn)
	        throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		long codigo = 0;
		try {
			
			String sql = " SELECT count( " + campoSequencial + " ) " + " FROM        " + tabela + " WHERE       " + campoFixo
			        + " =? ";
			
			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.setObject(1, valorFixo);
			
			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			while (rs.next()) {
				codigo = rs.getLong(1);
			}
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			ensureResourceAreClosed(logs, st, rs);
			
			rs = null;
			st = null;
		}
		return codigo + 1;
	}
	
	static public long getNextId(String tabela, String campoSequencial, String condition, Connection conn)
	        throws DBException {
		try {
			return getNextId(tabela, campoSequencial, condition, true, conn);
		}
		catch (Exception e) {
			return getNextId(tabela, campoSequencial, condition, false, conn);
		}
	}
	
	/**
	 * @param tabela
	 * @param campo
	 * @return Retorna o proximo numero da sequencia de ids da tabela em 'tabela' do campo em
	 *         'campo' com o campo fixo 'campoFixo' e valor 'valorFixo'
	 */
	static public long getNextId(String tabela, String campoSequencial, String condition, boolean putSchemaOnTableName,
	        Connection conn) throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		long codigo = 0;
		
		try {
			String sql = " SELECT max(" + campoSequencial + ") " + " FROM "
			        + (putSchemaOnTableName ? SQLUtilities.tryToPutSchemaOnDatabaseObject(tabela, conn) : tabela) + " WHERE "
			        + condition;
			
			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			while (rs.next()) {
				try {
					codigo = rs.getLong(1);
				}
				catch (Exception e) {
					codigo = countQtdRecords(tabela, campoSequencial, condition, conn);
				}
			}
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			ensureResourceAreClosed(logs, st, rs);
			
			rs = null;
			st = null;
		}
		return codigo + 1;
	}
	
	static public long countQtdRecords(String tabela, String campoSequencial, String condition, Connection conn)
	        throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		long codigo = 0;
		try {
			
			String sql = " SELECT count(" + campoSequencial + ") " + " FROM " + tabela + " WHERE " + condition;
			
			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			while (rs.next()) {
				codigo = rs.getLong(1);
			}
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			ensureResourceAreClosed(logs, st, rs);
			
			rs = null;
			st = null;
		}
		return codigo;
	}
	
	/**
	 * Busca o valor máximo na tabela dada com a condição dada. Note que, se o campo em causa não
	 * for do tipo numérico, o método irá retornar a quantidade de registos que satisfaçam aquela
	 * condição.
	 * 
	 * @param tabela
	 * @param coluna
	 * @param condition
	 * @param conn
	 * @return
	 * @throws SQLException
	 */
	public static long getMaxID(String tabela, String coluna, String condition, Connection conn) throws DBException {
		ArrayList<String> logs = new ArrayList<String>();
		PreparedStatement st = null;
		ResultSet rs = null;
		
		String sql;
		long maxID = 0;
		
		condition = condition.toUpperCase();
		
		sql = " SELECT  MAX(" + coluna + ") AS maxID FROM  " + tabela;
		
		if (!condition.equals(""))
			sql = sql + " WHERE " + condition;
		
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
					else
						getNextId(tabela, coluna, condition, conn);
					
				}
			}
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			ensureResourceAreClosed(logs, st, rs);
			
			rs = null;
			st = null;
		}
		
		return maxID;
	}
	
	public static boolean isInUseID(String tabela, String coluna, String condition, long findingID, Connection conn)
	        throws DBException {
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
		
		if (!condition.equals(""))
			auxCondicao = auxCondicao + " AND " + condition;
		
		if (!auxCondicao.equals(""))
			sql = sql + " WHERE " + auxCondicao;
		
		try {
			st = conn.prepareStatement(sql);
			logs.add("Opened st");
			
			st.execute();
			rs = st.getResultSet();
			logs.add("Opened rs");
			
			isInUse = rs.next();
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			ensureResourceAreClosed(logs, st, rs);
		}
		
		rs = null;
		st = null;
		
		return isInUse;
	}
	
	private static void ensureResourceAreClosed(List<String> logs, PreparedStatement st, ResultSet rs) {
		try {
			if (st != null) {
				st.close();
				logs.add("Closed st");
			}
			
			if (rs != null) {
				rs.close();
				logs.add("Closed rs");
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			//File.write("C:\\sqlHelperLogs.txt", logs);
		}
	}
	
	/**
	 * @param tabela
	 * @param campo
	 * @return Retorna o proximo numero da sequencia de ids da tabela em 'tabela' do campo em
	 *         'campo'
	 */
	static public long getNextCod(String tabela, String campo, Connection conn) throws DBException {
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
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		finally {
			ensureResourceAreClosed(logs, st, rs);
		}
		return codigo + 1;
	}
	
	/**
	 * Gera as colunas[table fields] presentes numa instrucao dump insert
	 * 
	 * @param insertInstrunction
	 * @return um array de Strings contendo as colunas [table fields] da instrucao SQL recebida pelo
	 *         parametro
	 * @author JPBOANE 17/12/2012
	 */
	public static String[] parseTableFieldsOnInserInstrunction(String insertInstrunction) {
		insertInstrunction = removeOccurence(insertInstrunction, (char) 34, ' ');
		
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
	public static String transformPreparedStatmentToFullSQLStatment(String preparedStatment, Object[] params,
	        Connection conn) throws ForbiddenOperationException {
		if (!CommonUtilities.getInstance().stringHasValue(preparedStatment))
			return "";
		
		String fullSQL = "";
		
		int currParam = 0;
		
		String dateFormat;
		try {
			dateFormat = DBUtilities.isOracleDB(conn) ? DateAndTimeUtilities.ORACLE_DATE_TIME_FORMAT
			        : DateAndTimeUtilities.DATE_TIME_FORMAT;
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
		
		for (int i = 0; i < preparedStatment.length(); i++) {
			
			if (preparedStatment.charAt(i) == '?') {
				fullSQL = fullSQL + convertParamToStringValueForSQLParam(params[currParam++], dateFormat);
			} else
				fullSQL = fullSQL + preparedStatment.charAt(i);
		}
		
		fullSQL = FuncoesGenericas.removeDuplicatedEmptySpace(fullSQL);
		
		return fullSQL;
	}
	
	/**
	 * Transforma um objecto numa string preparada para uma instrução SQL
	 * 
	 * @param param
	 * @return
	 * @throws TipoDeDadosNaoSuportadoException
	 */
	private static String convertParamToStringValueForSQLParam(Object param, String dateFormat)
	        throws ForbiddenOperationException {
		if (param == null)
			return null;
		
		if (param instanceof Integer)
			return "" + ((Integer) param).intValue();
		if (param instanceof Long)
			return "" + ((Long) param).longValue();
		if (param instanceof Double)
			return "" + ((Double) param).doubleValue();
		if (param instanceof Character)
			return "'" + ((Character) param).charValue() + "'";
		if (param instanceof Boolean)
			return "" + (((Boolean) param).booleanValue() ? 1 : 0);
		
		if (param instanceof Date) {
			return "TO_DATE('" + DateAndTimeUtilities.formatToDDMMYYYY_HHMISS((Date) param) + "' , '" + dateFormat + "')";
		}
		
		if (param instanceof String)
			return "'" + removeForbiddenSymbols((String) param) + "'";
		
		throw new ForbiddenOperationException("Tipo de dados não suportado [" + param.getClass() + "]");
	}
	
	private static String removeForbiddenSymbols(String msg) {
		return msg.replaceAll("'", "");
	}
	
	@SuppressWarnings("unused")
	private static Connection tempOpenConnection() {
		String OracleClasse = "oracle.jdbc.driver.OracleDriver";
		String OracleURL = "jdbc:oracle:thin:@gov.minag.lims:1521:lims";
		String OracleUser = "lims";
		String OraclePass = "exi2k12";
		
		return null;//BaseDAO.openConnection(OracleClasse, OracleURL, OracleUser, OraclePass);
	}
	
	@SuppressWarnings("unused")
	private static Connection tempOpenPostgresConnection() {
		String OracleClasse = "org.postgresql.Driver";
		String OracleURL = "jdbc:postgresql://127.0.0.1:5432/sigbarpi";
		String OracleUser = "postgres";
		String OraclePass = "exi2k12";
		
		return null; //BaseDAO.openConnection(OracleClasse, OracleURL, OracleUser, OraclePass);
	}
	
	private static String removeOccurence(String string, char oldChar, char newChar) {
		string.replace(oldChar, newChar);
		
		return string;
	}
	
	/**
	 * Dado um SQL do tipo SELECT, cria um novo select (select faseado) que permite seleccionar
	 * apenas registos a partir de um ponto ate outro
	 * 
	 * @param originalSelect
	 * @param start
	 * @param qtdRecordSuported
	 * @param currParams
	 * @throws RuntimeException se o select original (originalSelect) não tiver a coluna ROWNUM
	 * @return
	 * @throws DBException
	 */
	public static String createPhasedSelectOracle(String originalSelect, int start, int qtdRecordSuported, Connection conn)
	        throws DBException {
		originalSelect = originalSelect.toUpperCase();
		
		if (originalSelect.contains("ROW_NUM"))
			throw new IllegalArgumentException("Rever a dump. Remova a coluna ROW_NUM");
		
		String sqlPlusRowNum = " SELECT TAB_TEMP.*, ROWNUM ROW_NUM ";
		sqlPlusRowNum += " FROM (";
		sqlPlusRowNum += originalSelect;
		sqlPlusRowNum += ") TAB_TEMP ";
		
		String phasedSQL = " SELECT * ";
		phasedSQL += " FROM (";
		phasedSQL += sqlPlusRowNum;
		phasedSQL += ")";
		phasedSQL += " WHERE ROW_NUM BETWEEN " + start + " AND " + (start + qtdRecordSuported - 1);
		
		return phasedSQL;
	}
	
	/**
	 * Dado um SQL do tipo SELECT, cria um novo select (select faseado) que permite seleccionar
	 * apenas registos a partir de um ponto ate outro
	 * 
	 * @param originalSelect
	 * @param start
	 * @param qtdRecordSuported
	 * @throws RuntimeException se o select original (originalSelect) não tiver a coluna ROWNUM
	 * @return
	 * @throws DBException
	 */
	public static String createPhasedSelectPostgres(String originalSelect, int start, int qtdRecordSuported, Connection conn)
	        throws DBException {
		try {
			originalSelect = originalSelect.toUpperCase();
			
			originalSelect = originalSelect.replace("ROW_NUM", "9");
			originalSelect = originalSelect.replace("ROWNUM", "");
			
			String fasedCondition = "";
			
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.ORACLE_DATABASE)) {
				fasedCondition = " WHERE ROWNUM >= " + start + " AND ROWNUM <= " + (start + qtdRecordSuported - 1);
			} else if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.POSTGRESQL_DATABASE)) {
				fasedCondition = " OFFSET " + (start - 1) + " LIMIT  " + qtdRecordSuported;
			}
			
			originalSelect = " SELECT * " + " FROM (" + originalSelect + ") MYSUBQUERY " + fasedCondition;
			
			return originalSelect;
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	/**
	 * Dado um SQL do tipo SELECT, cria um novo select (select faseado) que permite seleccionar
	 * apenas registos a partir de um ponto ate outro
	 * 
	 * @param originalSelect
	 * @param start
	 * @param qtdRecordSuported
	 * @throws RuntimeException se o select original (originalSelect) não tiver a coluna ROWNUM
	 * @return
	 * @throws DBException
	 */
	public static String createPhasedSelectMySQL(String originalSelect, int start, int qtdRecordSuported, Connection conn)
	        throws DBException {
		boolean isDistinct = originalSelect.contains("DISTINCT");
		
		String fasedCondition = "";
		
		fasedCondition = " LIMIT " + (start - 1) + "," + qtdRecordSuported;
		
		if (isDistinct) {
			originalSelect = " SELECT * " + " FROM (" + originalSelect + ") MYSUBQUERY ";
		}
		
		originalSelect += fasedCondition;
		
		return originalSelect;
	}
	
	/**
	 * /** Dado um SQL do tipo SELECT, cria um novo select (select faseado) que permite seleccionar
	 * apenas registos a partir de um ponto ate outro
	 * 
	 * @param originalSelect
	 * @param start
	 * @param qtdRecordSuported
	 * @throws RuntimeException se o select original (originalSelect) não tiver a coluna ROWNUM
	 * @return
	 * @throws DBException
	 */
	public static String createPhasedSelect(String originalSelect, int start, int qtdRecordSuported, Connection conn)
	        throws DBException {
		try {
			
			if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.ORACLE_DATABASE)) {
				return createPhasedSelectOracle(originalSelect, start, qtdRecordSuported, conn);
			} else if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.POSTGRESQL_DATABASE)) {
				return createPhasedSelectPostgres(originalSelect, start, qtdRecordSuported, conn);
			} else if (DBUtilities.determineDataBaseFromConnection(conn).equals(DBUtilities.MYSQL_DATABASE)) {
				return createPhasedSelectMySQL(originalSelect, start, qtdRecordSuported, conn);
			}
			
			throw new ForbiddenOperationException("SGBD nao suportado!");
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	/**
	 * Transforma em String
	 * 
	 * @param tableName
	 * @return
	 * @throws SQLException
	 */
	public static String toStringTableColumns(String tableName, Connection conn) throws SQLException {
		String colunas = "";
		
		PreparedStatement st = conn.prepareStatement("SELECT * FROM " + tableName + " WHERE 1 != 1");
		
		ResultSet rs = st.executeQuery();
		ResultSetMetaData rsMetaData = rs.getMetaData();
		
		for (int i = 1; i <= rsMetaData.getColumnCount(); i++) {
			colunas = CommonUtilities.getInstance().addAtributeToValidationString(colunas, rsMetaData.getColumnName(i), ",");
		}
		
		return colunas;
	}
	
	public static String qualifyUnqualifiedSqlFields(String sql, String tableName) {
		
		if (sql == null || sql.isBlank()) {
			return sql;
		}
		
		Set<String> SQL_KEYWORDS = Set.of("and", "or", "not", "in", "is", "null", "like", "between", "exists", "select",
		    "from", "where", "join", "left", "right", "inner", "outer", "on", "as", "case", "when", "then", "else", "end",
		    "distinct", "group", "by", "order", "having", "limit", "offset", "true", "false", "asc", "desc");
		
		Pattern tokenPattern = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");
		
		StringBuilder result = new StringBuilder();
		
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		int lastIndex = 0;
		
		for (int i = 0; i < sql.length(); i++) {
			char c = sql.charAt(i);
			
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			} else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			}
			
			// Quando entramos ou saímos de string, processamos o trecho anterior
			if ((c == '\'' && !inDoubleQuote) || (c == '"' && !inSingleQuote)) {
				
				if (!inSingleQuote && !inDoubleQuote) {
					// acabou string → copiar direto
					result.append(sql, lastIndex, i + 1);
					lastIndex = i + 1;
				} else {
					// começou string → processar antes dela
					String segment = sql.substring(lastIndex, i);
					result.append(processSegment(segment, tableName, SQL_KEYWORDS, tokenPattern));
					lastIndex = i;
				}
			}
		}
		
		// último trecho
		if (lastIndex < sql.length()) {
			if (inSingleQuote || inDoubleQuote) {
				result.append(sql.substring(lastIndex));
			} else {
				result.append(processSegment(sql.substring(lastIndex), tableName, SQL_KEYWORDS, tokenPattern));
			}
		}
		
		return result.toString();
	}
	
	private static String processSegment(String segment, String tableName, Set<String> SQL_KEYWORDS, Pattern pattern) {
		
		Matcher matcher = pattern.matcher(segment);
		StringBuffer sb = new StringBuffer();
		
		boolean expectTableName = false;
		boolean expectTableAlias = false;
		
		while (matcher.find()) {
			
			String token = matcher.group();
			String lower = token.toLowerCase();
			
			int start = matcher.start();
			int end = matcher.end();
			
			boolean hasDotBefore = start > 0 && segment.charAt(start - 1) == '.';
			boolean hasDotAfter = end < segment.length() && segment.charAt(end) == '.';
			boolean isKeyword = SQL_KEYWORDS.contains(lower);
			boolean isFunction = end < segment.length() && segment.charAt(end) == '(';
			boolean isParameter = start > 0 && segment.charAt(start - 1) == '@';
			
			// detectar contexto FROM / JOIN
			if (lower.equals("from") || lower.equals("join")) {
				expectTableName = true;
				expectTableAlias = false;
				matcher.appendReplacement(sb, Matcher.quoteReplacement(token));
				continue;
			}
			
			// nome da tabela
			if (expectTableName) {
				expectTableName = false;
				expectTableAlias = true;
				matcher.appendReplacement(sb, Matcher.quoteReplacement(token));
				continue;
			}
			
			// suporte ao "AS"
			if (expectTableAlias && lower.equals("as")) {
				// mantém estado de alias
				matcher.appendReplacement(sb, Matcher.quoteReplacement(token));
				continue;
			}
			
			// alias (com ou sem AS)
			if (expectTableAlias) {
				
				expectTableAlias = false;
				
				// se for keyword → não era alias
				if (!isKeyword) {
					matcher.appendReplacement(sb, Matcher.quoteReplacement(token));
					continue;
				}
			}
			
			if (hasDotBefore || hasDotAfter || isKeyword || isFunction || isParameter) {
				matcher.appendReplacement(sb, Matcher.quoteReplacement(token));
			} else {
				matcher.appendReplacement(sb, Matcher.quoteReplacement(tableName + "." + token));
			}
		}
		
		matcher.appendTail(sb);
		return sb.toString();
	}
	
	public static List<SqlConditionElement> extractSqlConditionElements(String sql) {
		
		List<SqlConditionElement> result = new ArrayList<>();
		
		if (sql == null || sql.isBlank()) {
			return result;
		}
		
		Pattern normalPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_\\.]*)\\s*(=|!=|<>|<=|>=|<|>)\\s*(.+)",
		    Pattern.CASE_INSENSITIVE);
		
		Pattern isNullPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_\\.]*)\\s+is\\s+null", Pattern.CASE_INSENSITIVE);
		
		Pattern isNotNullPattern = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_\\.]*)\\s+is\\s+not\\s+null",
		    Pattern.CASE_INSENSITIVE);
		
		// 1. Extrair WHEREs
		List<String> whereClauses = extractWhereClauses(sql);
		
		for (String where : whereClauses) {
			
			// 2. Split correto
			List<String> parts = splitConditions(where);
			
			for (String part : parts) {
				
				String trimmed = part.trim();
				
				Matcher mNotNull = isNotNullPattern.matcher(trimmed);
				if (mNotNull.find()) {
					result.add(new SqlConditionElement(mNotNull.group(1), "IS NOT NULL", null));
					continue;
				}
				
				Matcher mNull = isNullPattern.matcher(trimmed);
				if (mNull.find()) {
					result.add(new SqlConditionElement(mNull.group(1), "IS NULL", null));
					continue;
				}
				
				Matcher m = normalPattern.matcher(trimmed);
				if (m.find()) {
					result.add(new SqlConditionElement(m.group(1), m.group(2), cleanValue(m.group(3))));
				}
			}
		}
		
		return result;
	}
	
	private static String cleanValue(String value) {
		
		if (value == null)
			return null;
		
		String v = value.trim();
		
		// remove parênteses exteriores
		if (v.startsWith("(") && v.endsWith(")")) {
			v = v.substring(1, v.length() - 1).trim();
		}
		
		return v;
	}
	
	private static List<String> extractWhereClauses(String sql) {
		
		List<String> result = new ArrayList<>();
		
		Pattern pattern = Pattern.compile("(?i)\\bwhere\\b");
		
		Matcher matcher = pattern.matcher(sql);
		
		while (matcher.find()) {
			
			int start = matcher.end();
			
			int end = findEndOfWhereClause(sql, start);
			
			String whereContent = sql.substring(start, end).trim();
			
			result.add(whereContent);
		}
		
		return result;
	}
	
	private static int findEndOfWhereClause(String sql, int start) {
		
		int parentheses = 0;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		for (int i = start; i < sql.length(); i++) {
			
			char c = sql.charAt(i);
			
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			} else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			}
			
			if (inSingleQuote || inDoubleQuote)
				continue;
			
			if (c == '(')
				parentheses++;
			else if (c == ')') {
				if (parentheses == 0) {
					return i;
				}
				parentheses--;
			}
			
			// parar em keywords comuns (fora de subqueries)
			if (parentheses == 0) {
				if (startsWithKeyword(sql, i, "group by") || startsWithKeyword(sql, i, "order by")
				        || startsWithKeyword(sql, i, "limit")) {
					return i;
				}
			}
		}
		
		return sql.length();
	}
	
	private static List<String> splitConditions(String condition) {
		
		List<String> parts = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		
		int parentheses = 0;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		String lower = condition.toLowerCase();
		
		for (int i = 0; i < condition.length(); i++) {
			
			char c = condition.charAt(i);
			
			if (c == '\'' && !inDoubleQuote)
				inSingleQuote = !inSingleQuote;
			else if (c == '"' && !inSingleQuote)
				inDoubleQuote = !inDoubleQuote;
			
			if (!inSingleQuote && !inDoubleQuote) {
				
				if (c == '(')
					parentheses++;
				else if (c == ')')
					parentheses--;
				
				if (parentheses == 0) {
					
					if (lower.startsWith(" and ", i)) {
						parts.add(current.toString().trim());
						current.setLength(0);
						i += 4;
						continue;
					}
					
					if (lower.startsWith(" or ", i)) {
						parts.add(current.toString().trim());
						current.setLength(0);
						i += 3;
						continue;
					}
				}
			}
			
			current.append(c);
		}
		
		if (!current.isEmpty()) {
			parts.add(current.toString().trim());
		}
		
		return parts;
	}
	
	private static boolean startsWithKeyword(String sql, int index, String keyword) {
		return sql.regionMatches(true, index, keyword, 0, keyword.length());
	}
	
	public static boolean checkIfFieldDefinitionIncludeQualifier(String fieldName) {
		String[] fieldParts = fieldName.toString().split("\\.");
		
		return fieldParts.length > 1;
		
	}
	
	public static boolean isValidSqlCondition(String str) {
		
		if (str == null || str.isBlank()) {
			return false;
		}
		
		String s = str.trim();
		
		if (!areParenthesesBalanced(s) || !areQuotesBalanced(s)) {
			return false;
		}
		
		String normalized = s.replaceAll("\\s+", " ").trim().toLowerCase();
		
		if (normalized.startsWith("and ") || normalized.startsWith("or ")) {
			return false;
		}
		
		if (normalized.endsWith(" and") || normalized.endsWith(" or")) {
			return false;
		}
		
		// aceita EXISTS (...)
		if (normalized.matches("^exists\\s*\\(.+\\)$")) {
			return true;
		}
		
		// aceita FIELD IS NULL / IS NOT NULL
		if (normalized.matches("^[a-zA-Z_][a-zA-Z0-9_.]*\\s+is\\s+(not\\s+)?null$")) {
			return true;
		}
		
		// aceita FIELD IN (...)
		if (normalized.matches("^[a-zA-Z_][a-zA-Z0-9_.]*\\s+in\\s*\\(.+\\)$")) {
			return true;
		}
		
		// aceita comparação simples ou com subquery/expressão à direita
		if (normalized.matches("^[a-zA-Z_][a-zA-Z0-9_.]*\\s*(=|!=|<>|>=|<=|>|<|like|between)\\s+.+$")) {
			return true;
		}
		
		// aceita múltiplas condições ligadas por AND/OR sem tentar parsear o conteúdo interno
		if (containsTopLevelLogicalOperator(s)) {
			return true;
		}
		
		return false;
	}
	
	private static boolean areParenthesesBalanced(String s) {
		int count = 0;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			} else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			} else if (!inSingleQuote && !inDoubleQuote) {
				if (c == '(')
					count++;
				if (c == ')')
					count--;
				if (count < 0)
					return false;
			}
		}
		
		return count == 0 && !inSingleQuote && !inDoubleQuote;
	}
	
	private static boolean areQuotesBalanced(String s) {
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			} else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			}
		}
		
		return !inSingleQuote && !inDoubleQuote;
	}
	
	private static boolean containsTopLevelLogicalOperator(String s) {
		int parentheses = 0;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		String lower = s.toLowerCase();
		
		for (int i = 0; i < lower.length(); i++) {
			char c = lower.charAt(i);
			
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
				continue;
			}
			
			if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
				continue;
			}
			
			if (inSingleQuote || inDoubleQuote) {
				continue;
			}
			
			if (c == '(')
				parentheses++;
			else if (c == ')')
				parentheses--;
			
			if (parentheses == 0) {
				if (lower.startsWith(" and ", i) || lower.startsWith(" or ", i)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean isValidSelectSqlQuery(String query, DbmsType dbType) {
		
		if (!utilities.stringHasValue(query)) {
			return false;
		}
		
		String normalized = normalizeSql(query);
		
		// 🔹 1. SELECT ou WITH
		if (!startsWithSelectOrWith(normalized)) {
			return false;
		}
		
		// 🔹 2. Verificar FROM (mais robusto)
		boolean hasFrom = containsKeyword(normalized, "from");
		
		if (!hasFrom) {
			// MySQL permite SELECT 1
			return dbType.isMysql() && normalized.matches("(?is)^select\\s+.+$");
		}
		
		// 🔹 3. Estrutura básica
		if (!hasSelectFromStructure(normalized)) {
			return false;
		}
		
		// 🔹 4. Validar WHERE (leve)
		String whereClause = extractWhereClause(normalized);
		
		if (whereClause != null) {
			if (!isBalanced(whereClause)) {
				return false;
			}
		}
		
		return true;
	}
	
	private static String normalizeSql(String sql) {
		return sql.replaceAll("--.*?(\\r?\\n|$)", " ") // remove comentários --
		        .replaceAll("/\\*.*?\\*/", " ") // remove comentários /*
		        .replaceAll("\\s+", " ").trim().toLowerCase();
	}
	
	private static boolean startsWithSelectOrWith(String sql) {
		return sql.matches("^(\\(*\\s*)*(select|with)\\b.*");
	}
	
	private static boolean containsKeyword(String sql, String keyword) {
		return sql.matches("(?s).*\\b" + keyword + "\\b.*");
	}
	
	private static boolean hasSelectFromStructure(String sql) {
		return sql.matches("(?is)^\\s*select\\s+.+?\\s+from\\s+.+");
	}
	
	private static String extractWhereClause(String sql) {
		
		Matcher m = Pattern.compile("(?i)\\bwhere\\b").matcher(sql);
		
		if (!m.find()) {
			return null;
		}
		
		int start = m.end();
		
		String afterWhere = sql.substring(start).trim();
		
		return cutAfterKeywords(afterWhere);
	}
	
	private static String cutAfterKeywords(String clause) {
		
		String lower = clause.toLowerCase();
		
		String[] keywords = { "group by", "order by", "limit", "having", "union", "intersect", "except" };
		
		int parentheses = 0;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		for (int i = 0; i < clause.length(); i++) {
			
			char c = clause.charAt(i);
			
			//  controlar aspas
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
				continue;
			} else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
				continue;
			}
			
			if (inSingleQuote || inDoubleQuote) {
				continue;
			}
			
			// controlar parênteses
			if (c == '(') {
				parentheses++;
				continue;
			} else if (c == ')') {
				parentheses--;
				continue;
			}
			
			// só cortar se estiver fora de parênteses
			if (parentheses == 0) {
				
				for (String kw : keywords) {
					
					if (lower.startsWith(kw, i)) {
						
						// garantir boundary (evitar cortar "limitador")
						boolean validStart = i == 0 || Character.isWhitespace(lower.charAt(i - 1));
						int endIdx = i + kw.length();
						boolean validEnd = endIdx >= lower.length() || Character.isWhitespace(lower.charAt(endIdx));
						
						if (validStart && validEnd) {
							return clause.substring(0, i).trim();
						}
					}
				}
			}
		}
		
		return clause.trim();
	}
	
	private static boolean isBalanced(String s) {
		
		int parentheses = 0;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;
		
		for (int i = 0; i < s.length(); i++) {
			
			char c = s.charAt(i);
			
			if (c == '\'' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			} else if (c == '"' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			}
			
			if (inSingleQuote || inDoubleQuote) {
				continue;
			}
			
			if (c == '(')
				parentheses++;
			else if (c == ')')
				parentheses--;
			
			if (parentheses < 0)
				return false;
		}
		
		return parentheses == 0 && !inSingleQuote && !inDoubleQuote;
	}
	
	public static List<SqlFunctionInfo> extractSqlFunctionsInSelect(String query) {
		List<SqlFunctionInfo> functions = new ArrayList<>();
		
		// Normalize the query to make it case insensitive
		String normalizedQuery = query.toLowerCase();
		
		// Regex to find the SELECT clause and extract its fields
		Pattern selectPattern = Pattern.compile("select(.*?)from", Pattern.DOTALL);
		Matcher selectMatcher = selectPattern.matcher(normalizedQuery);
		
		if (selectMatcher.find()) {
			// Extract the fields part of the SELECT clause
			String fieldsPart = selectMatcher.group(1).trim();
			
			// Regex to identify SQL function calls and their aliases, with or without the "AS" keyword
			Pattern functionPattern = Pattern
			        .compile("(\\b\\w+\\s*\\([^\\)]*\\))(\\s+as\\s+(\\w+))?|\\b(\\w+)\\s*\\(([^\\)]*)\\)\\s*(\\w+)?");
			Matcher functionMatcher = functionPattern.matcher(fieldsPart);
			
			// Find all function calls in the fields part
			while (functionMatcher.find()) {
				String function = functionMatcher.group(1) != null ? functionMatcher.group(1).trim()
				        : functionMatcher.group(4).trim() + "(" + functionMatcher.group(5).trim() + ")";
				String alias = functionMatcher.group(3) != null ? functionMatcher.group(3).trim()
				        : (functionMatcher.group(6) != null ? functionMatcher.group(6).trim() : null);
				try {
					functions.add(new SqlFunctionInfo(SqlFunctionType.determine(function), alias));
				}
				catch (ForbiddenOperationException e) {
					e.printStackTrace();
				}
			}
		}
		
		return functions;
	}
	
	/**
	 * Extracts the table(s) or subquery part from the FROM clause of a SQL SELECT query.
	 *
	 * @param query the SQL query to be parsed
	 * @return the part after the FROM clause, or null if not found
	 */
	public static String extractFromClauseOnSqlSelectQuery(String query) {
		// Normalize the query to lowercase for case-insensitive matching
		String normalizedQuery = query.toLowerCase();
		
		// Regex to match the FROM clause up to the next SQL keyword or end of the query
		Pattern fromPattern = Pattern.compile("from\\s+([^\\s,]+(?:\\s+[^\\s,]+)*?)\\s*(where|group by|having|order by|$)",
		    Pattern.CASE_INSENSITIVE);
		Matcher fromMatcher = fromPattern.matcher(normalizedQuery);
		
		if (fromMatcher.find()) {
			// Extract the matched group excluding the FROM keyword
			return query.substring(fromMatcher.start(1), fromMatcher.end(1)).trim();
		}
		
		return null;
	}
	
	/**
	 * Extracts the content of the WHERE clause from a SQL SELECT query, excluding the WHERE
	 * keyword.
	 *
	 * @param query the SQL query to be parsed
	 * @return the content of the WHERE clause, or null if not found
	 */
	public static String extractWhereClauseInASelectQuery(String query) {
		// Normalize the query to lowercase for case-insensitive matching
		String normalizedQuery = query.toLowerCase();
		
		// Regex to match the WHERE clause up to the next SQL keyword or end of the query
		Pattern wherePattern = Pattern.compile("where\\s+(.+?)(\\s*(group by|having|order by|$))", Pattern.CASE_INSENSITIVE);
		Matcher whereMatcher = wherePattern.matcher(normalizedQuery);
		
		if (whereMatcher.find()) {
			// Extract the matched group excluding the WHERE keyword
			return query.substring(whereMatcher.start(1), whereMatcher.end(1)).trim();
		}
		
		return null;
	}
	
	/**
	 * Extracts the alias of the first table in the FROM clause of a SQL SELECT query.
	 *
	 * @param query the SQL query to be parsed
	 * @return the alias of the first table, or null if not found
	 */
	public static String extractFirstTableAliasOnSqlQuery(String query) {
		
		query = utilities.removeDuplicatedEmptySpace(query);
		
		String from = query.toLowerCase().split("from ")[1];
		
		String[] parts = from.split(" ");
		
		if (parts.length == 1) {
			return null;
		}
		
		if (parts.length >= 2) {
			
			if ((parts[1]).equals("as")) {
				return parts[2];
			}
			
			if (!isReserverdWord(parts[1])) {
				return parts[1];
			}
		}
		
		return null;
	}
	
	private static boolean isReserverdWord(String alias) {
		return utilities.isStringIn(alias.toLowerCase(), "inner", "left", "right", "full", "join", "where", "exists", "not",
		    "select", "order", "group", "by");
	}
	
	public static List<String> tryToSplitQueryByUnions(String query) {
		// Normalize the query by removing extra spaces
		query = query.trim().replaceAll("\\s+", " ");
		List<String> splitQueries = new ArrayList<>();
		StringBuilder currentQuery = new StringBuilder();
		int parenthesesLevel = 0;
		String lowerQuery = query.toLowerCase();
		
		int i = 0;
		while (i < query.length()) {
			char currentChar = query.charAt(i);
			currentQuery.append(currentChar);
			
			// Track parentheses to determine the context (main query vs subquery)
			if (currentChar == '(') {
				parenthesesLevel++;
			} else if (currentChar == ')') {
				parenthesesLevel--;
			}
			
			// Check for " UNION " (with spaces around) outside of subqueries
			if (parenthesesLevel == 0 && i + 6 < query.length() && lowerQuery.substring(i).startsWith(" union ")
			        && (i + 6 == query.length() || Character.isWhitespace(query.charAt(i + 6)))) {
				
				// Add the current query part to the list and reset the currentQuery
				splitQueries.add(currentQuery.toString().trim());
				currentQuery.setLength(0); // Reset the StringBuilder
				
				// Skip the " UNION " part in the main query
				i += 6;
				continue; // Continue without incrementing i to ensure the loop works correctly
			}
			
			i++;
		}
		
		// Add the remaining part of the query
		splitQueries.add(currentQuery.toString().trim());
		
		return splitQueries;
	}
	
	public static int getQtyQuestionMarksOnQuery(String query) {
		String[] parts = query.trim().split("\\?");
		
		if (query.endsWith("?")) {
			return parts.length;
		} else {
			return parts.length - 1;
		}
	}
	
	public static List<Field> determineFieldsFromQuery(String query, Object[] params, Connection conn) throws DBException {
		List<Field> fields = new ArrayList<Field>();
		
		PreparedStatement st;
		ResultSet rs;
		ResultSetMetaData rsMetaData;
		
		try {
			
			query = normalizeQuery(query);
			
			st = conn.prepareStatement(query);
			
			int qtyQuestionMarksOnQuery = getQtyQuestionMarksOnQuery(query);
			
			if (qtyQuestionMarksOnQuery > 0) {
				if (params == null) {
					params = new Object[qtyQuestionMarksOnQuery];
					
					for (int i = 0; i < qtyQuestionMarksOnQuery; i++) {
						params[i] = null;
					}
				}
				
				BaseDAO.loadParamsToStatment(st, params, conn);
			}
			
			rs = st.executeQuery();
			rsMetaData = rs.getMetaData();
			
			int qtyAttrs = rsMetaData.getColumnCount();
			
			for (int i = 1; i <= qtyAttrs; i++) {
				Field field = new Field(rsMetaData.getColumnLabel(i));
				field.setDataType(rsMetaData.getColumnTypeName(i));
				
				fields.add(field);
			}
			
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return fields;
	}
	
	public static String removeWhereConditionOnQuery(String query) {
		return query.toLowerCase().split("where ")[0];
	}
	
	public static String normalizeQuery(String query) {
		
		String normalized = query.trim();
		normalized = utilities.removeDuplicatedEmptySpace(normalized);
		normalized = utilities.removeSpacesBeforeAndAfterPeriod(normalized);
		
		normalized = normalized.replaceAll("\\s+", " ");
		
		String commentRegex = "(--.*?$)|(/\\*.*?\\*/)";
		
		// Remove comments from the SQL string
		normalized = normalized.replaceAll(commentRegex, "");
		
		return normalized;
	}
	
	private static boolean containsSelectWildcard(String selectClause) {
		
		// remove conteúdo entre parênteses (funções, subqueries)
		String cleaned = selectClause.replaceAll("\\([^)]*\\)", "");
		
		// 🔹 SELECT *
		if (cleaned.matches("(?i)^\\s*\\*\\s*$")) {
			return true;
		}
		
		// 🔹 SELECT table.*
		if (cleaned.matches("(?i).*\\b\\w+\\.\\*\\b.*")) {
			return true;
		}
		
		return false;
	}
	
	public static List<Field> determineFieldsFromQuery(String query) {
		
		String normalizedQuery = normalizeQuery(query);
		
		String selectRegex = "(?i)select\\s+(.+?)\\s+from";
		Pattern selectPattern = Pattern.compile(selectRegex);
		Matcher selectMatcher = selectPattern.matcher(normalizedQuery);
		
		List<Field> fields = new ArrayList<>();
		
		if (selectMatcher.find()) {
			
			String selectClause = selectMatcher.group(1).trim();
			
			// 🔥 NOVA VALIDAÇÃO
			if (containsSelectWildcard(selectClause)) {
				throw new IllegalArgumentException("Query contains a wildcard '*' in the SELECT clause");
			}
			
			// 🔥 split seguro (respeitando parênteses)
			List<String> fieldsName = splitSelectFields(selectClause);
			
			for (String s : fieldsName) {
				
				s = utilities.removeDuplicatedEmptySpace(s.trim());
				
				String fieldName;
				
				if (s.toLowerCase().contains(" as ")) {
					fieldName = s.split("(?i) as ")[1];
				} else {
					String[] parts = s.split("\\s+");
					fieldName = parts[parts.length - 1];
				}
				
				fields.add(new Field(fieldName.trim()));
			}
		}
		
		return fields;
	}
	
	private static List<String> splitSelectFields(String selectClause) {
		
		List<String> result = new ArrayList<>();
		
		StringBuilder current = new StringBuilder();
		
		int parentheses = 0;
		
		for (char c : selectClause.toCharArray()) {
			
			if (c == '(')
				parentheses++;
			else if (c == ')')
				parentheses--;
			
			if (c == ',' && parentheses == 0) {
				result.add(current.toString());
				current.setLength(0);
				continue;
			}
			
			current.append(c);
		}
		
		if (!current.isEmpty()) {
			result.add(current.toString());
		}
		
		return result;
	}
	
	public static String tryToPutSchemaOnDatabaseObject(String tableName, Connection conn) throws DBException {
		
		try {
			String[] tableNameComposition = tableName.split("\\.");
			
			if (tableNameComposition != null && tableNameComposition.length > 1)
				return tableName;
			
			return DBUtilities.determineSchemaName(conn) + "." + tableName;
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	public static boolean checkIfTableIsPresentInSqlExpretion(String sqlExpression) {
		String[] tableNameComposition = sqlExpression.split("\\.");
		
		return tableNameComposition != null && tableNameComposition.length > 1;
	}
	
	public static String tryToPutSchemaOnInsertScript_(String sql, Connection conn) throws DBException {
		String tableName = (sql.toLowerCase().split("insert into")[1]).split("\\(")[0];
		
		String[] tableNameComposition = tableName.split("\\.");
		
		if (tableNameComposition != null && tableNameComposition.length > 1)
			return sql;
		
		String fullTableName = tryToPutSchemaOnDatabaseObject(utilities.removeAllEmptySpace(tableName), conn);
		
		return sql.toLowerCase().replaceFirst(tableName, " " + fullTableName);
	}
	
	public static String addInsertIgnoreOnInsertScript(String sql, Connection conn) throws DBException {
		if (!DBUtilities.isMySQLDB(conn))
			return sql;
		
		return sql.toLowerCase().replaceFirst("insert", "insert ignore");
	}
	
	public static String tryToPutSchemaOnUpdateScript(String sql, Connection conn) throws DBException {
		String tableName = (sql.toLowerCase().split("update ")[1]).split(" ")[0];
		
		String[] tableNameComposition = tableName.split("\\.");
		
		if (tableNameComposition != null && tableNameComposition.length > 1)
			return sql;
		
		String fullTableName = tryToPutSchemaOnDatabaseObject(utilities.removeAllEmptySpace(tableName), conn);
		
		return sql.toLowerCase().replaceFirst(tableName, " " + fullTableName);
	}
	
	public static String determineSchemaFromFullTableName(String fullTableName) {
		
		String[] tabDef = fullTableName.split("\\.");
		
		/* If the table definition already come with schema
		 */
		if (tabDef.length > 1) {
			return tabDef[0];
		}
		
		return null;
	}
	
	/**
	 * Extracts the first table name from the FROM clause of an SQL SELECT query.
	 *
	 * @param query the SQL query to be parsed
	 * @return the first table name in the FROM clause, or null if not found
	 */
	public static String extractFirstTableFromSelectQuery(String query) {
		String normalizedQuery = query.toLowerCase();
		
		Pattern fromPattern = Pattern.compile("from\\s+([^\\s,]+)", Pattern.CASE_INSENSITIVE);
		Matcher fromMatcher = fromPattern.matcher(normalizedQuery);
		
		if (fromMatcher.find()) {
			return fromMatcher.group(1);
		}
		
		return null;
	}
	
	public static String extractTableNameFromFullTableName(String fullTableName) {
		
		String[] tabDef = fullTableName.split("\\.");
		
		/* If the table definition already come with schema
		 */
		if (tabDef.length > 1) {
			return tabDef[1];
		} else {
			return fullTableName;
		}
	}
	
	public static List<String> extractFieldsInClauses(String condition) {
		List<String> fields = new ArrayList<>();
		
		// Regex pattern to match field names in IN, BETWEEN, and comparison clauses
		String patternString = "(?i)\\b([\\w\\.]+)\\s*(=|IN|<|>|BETWEEN|LIKE)";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(condition);
		
		// Find all matches and add the field names to the list
		while (matcher.find()) {
			fields.add(matcher.group(1));
		}
		
		return fields;
	}
	
	public static List<String> extractFieldsInClauses(String condition, String tableName) {
		List<String> fields = new ArrayList<>();
		
		// Regex pattern to match field names in WHERE, IN, BETWEEN, and comparison clauses
		String patternString = "(?i)\\b([\\w\\.]+)\\s*(=|IN|<|>|BETWEEN|LIKE)";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(condition);
		
		// Find all matches and add the field names to the list
		while (matcher.find()) {
			String field = matcher.group(1);
			if (!field.contains(".")) {
				field = tableName + "." + field;
			}
			fields.add(field);
		}
		
		return fields;
	}
	
	public static String replaceFieldsInCondition(String condition, List<String> fields) {
		String updatedCondition = condition;
		
		// Regex pattern to match field names in WHERE, IN, BETWEEN, and comparison clauses
		String patternString = "(?i)\\b([\\w\\.]+)\\s*(=|IN|<|>|BETWEEN|LIKE)";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(condition);
		
		// Replace each matched field with its fully qualified name
		int fieldIndex = 0;
		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			// Construct the replacement with the field and a space before the token
			String replacement = fields.get(fieldIndex) + " " + matcher.group(2);
			matcher.appendReplacement(result, replacement);
			fieldIndex++;
		}
		matcher.appendTail(result);
		
		updatedCondition = result.toString();
		return updatedCondition;
	}
	
	/**
	 * Add a table name in fields that does not explicitly indicate the table name. The table name
	 * will be added if the current table contains the field e.g. given the clause "col1 = 123 and
	 * tab2.col2 > 1000"<br>
	 * the result will be "tableName.col1 = 123 and tab2.col2 > 1000"
	 * 
	 * @param clauseContent the clause content: e.g. "col1 = 123 and tab2.col2 > 1000";
	 * @param tableName the table name which will be added
	 * @param tableFields all the fields of table
	 * @return the modified clause which include the table name.
	 */
	public static String tryToPutTableNameInFieldsInASqlClause(String clauseContent, String tableName,
	        List<Field> tableFields) {
		
		if (!utilities.listHasElement(tableFields)) {
			throw new ForbiddenOperationException("The tableFields is empty!");
		}
		
		List<String> fields = SQLUtilities.extractFieldsInClauses(clauseContent);
		List<String> fieldsWithTabName = new ArrayList<>();
		
		for (String field : fields) {
			if (SQLUtilities.checkIfTableIsPresentInSqlExpretion(field)) {
				fieldsWithTabName.add(field);
			} else {
				
				if (tableFields.contains(Field.fastCreateField(field))) {
					fieldsWithTabName.add(tableName + "." + field);
				}
			}
		}
		
		return SQLUtilities.replaceFieldsInCondition(clauseContent, fieldsWithTabName);
	}
	
	public static List<String> findSubqueries(String query) {
		List<String> subqueries = new ArrayList<>();
		Stack<Integer> parenthesisStack = new Stack<>();
		StringBuilder currentSubquery = new StringBuilder();
		
		query = query.replaceAll("\\s+", " "); // Normalize whitespace
		
		for (int i = 0; i < query.length(); i++) {
			char c = query.charAt(i);
			
			if (c == '(') {
				parenthesisStack.push(i);
				if (parenthesisStack.size() == 1) {
					currentSubquery = new StringBuilder();
				}
			}
			
			if (!parenthesisStack.isEmpty()) {
				currentSubquery.append(c);
			}
			
			if (c == ')') {
				if (parenthesisStack.size() == 1) {
					String subquery = currentSubquery.toString();
					// Validate subquery starts with "select"
					if (subquery.trim().toLowerCase().startsWith("(select")
					        || subquery.trim().toLowerCase().startsWith("( select")) {
						subqueries.add(subquery);
					}
				}
				parenthesisStack.pop();
			}
		}
		
		return subqueries;
	}
	
	public static boolean startsWithSqlOperation(String str) {
		
		if (!utilities.stringHasValue(str)) {
			return false;
		}
		
		String trimmed = str.trim().toLowerCase();
		
		// 🔹 remover parênteses iniciais (caso venha subquery ou wrapper)
		while (trimmed.startsWith("(")) {
			trimmed = trimmed.substring(1).trim();
		}
		
		return trimmed.startsWith("select") || trimmed.startsWith("insert") || trimmed.startsWith("update")
		        || trimmed.startsWith("delete") || trimmed.startsWith("drop") || trimmed.startsWith("create")
		        || trimmed.startsWith("alter") || trimmed.startsWith("truncate") || trimmed.startsWith("with"); // CTE
	}
	
	public static boolean startsWithSelectSqlOperation(String str) {
		
		if (!utilities.stringHasValue(str)) {
			return false;
		}
		
		String trimmed = str.trim().toLowerCase();
		
		while (trimmed.startsWith("(")) {
			trimmed = trimmed.substring(1).trim();
		}
		
		return trimmed.startsWith("select");
	}
	
	public static <T extends Object> T tryToReplaceParamsInQuery(T query, EtlDatabaseObject paramSrc) {
		return tryToReplaceParamsInQuery(query, null, paramSrc);
	}
	
	public static <T extends Object> T tryToReplaceParamsInQuery(T query, EtlConfiguration paramSrc) {
		return tryToReplaceParamsInQuery(query, paramSrc, null);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> T tryToReplaceParamsInQuery(T query, EtlConfiguration etlConfParamSrc,
	        EtlDatabaseObject etlObjectParamSrc) {
		
		if (!(query instanceof String))
			return query;
		
		String strQuery = query.toString();
		
		if (!utilities.stringHasValue(strQuery))
			return (T) strQuery;
		
		String paramRegex = "@(\\w+)";
		Pattern pattern = Pattern.compile(paramRegex);
		Matcher matcher = pattern.matcher(strQuery);
		StringBuffer result = new StringBuffer();
		
		while (matcher.find()) {
			String paramName = matcher.group(1);
			
			try {
				
				Object paramValue = null;
				
				if (etlObjectParamSrc != null) {
					paramValue = etlObjectParamSrc.getFieldValue(paramName);
				} else if (etlConfParamSrc != null) {
					paramValue = etlConfParamSrc.getParamValue(paramName);
				} else {
					throw new ForbiddenOperationException("You need to specify the source of param");
				}
				
				if (paramValue != null && !paramValue.toString().isEmpty()) {
					matcher.appendReplacement(result, paramValue.toString());
				} else {
					matcher.appendReplacement(result, "@" + paramName);
				}
			}
			catch (ForbiddenOperationException e) {}
			
		}
		matcher.appendTail(result);
		
		return (T) result.toString();
	}
	
}
