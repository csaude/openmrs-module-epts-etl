package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.interfaces.TableAliasesGenerator;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParameterException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

/**
 * Represents an prepared query ready to be executed. It alwas has a ready query and its parameters
 */
public class PreparedQuery {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String query;
	
	private List<EtlDatabaseObject> srcObject;
	
	private EtlConfiguration etlConfig;
	
	private List<QueryParameter> queryParams;
	
	private String mainQuery;
	
	private List<String> subqueries;
	
	private SqlFunctionInfo countFunctionInfo;
	
	private QueryDataSourceConfig dataSource;
	
	private String originalQuery;
	
	private boolean sqlFunctionLoaded;
	
	PreparedQuery() {
	}
	
	EtlConfiguration getRelatedEtlConfiguration() {
		return getDataSource().getRelatedEtlConf();
	}
	
	PreparedQuery(QueryDataSourceConfig dataSource, List<EtlDatabaseObject> srcObject, EtlConfiguration configuration,
	    boolean ignoreMissingParameters) {
		
		this.setDataSource(dataSource);
		
		this.logTrace("Starting Query preparation... " + dataSource.getName());
		
		this.setQuery(dataSource.getQuery());
		this.setEtlConfig(configuration);
		this.setSrcObject(srcObject);
		this.tryToLoadSQLFunctionInfo();
		logTrace("Loading Query Parameters..");
		
		this.setQueryParams(extractParamOnQuery(this.getQuery()));
		
		logTrace("Query Parameters Loaded!");
		if (!hasQueryParams())
			return;
		
		try {
			
			logTrace("Loading Query Parameters Values...");
			
			loadQueryParamValues();
			
			logTrace("Query parameters loaded!");
		}
		catch (MissingParameterException e) {
			if (!ignoreMissingParameters) {
				throw e;
			}
			
		}
		
	}
	
	PreparedQuery(QueryDataSourceConfig queryDs, EtlConfiguration config, boolean ignoreMissingParameters) {
		this(queryDs, null, config, ignoreMissingParameters);
	}
	
	void loadQueryParamValues() {
		List<String> missingParameters = new ArrayList<>();
		
		for (QueryParameter field : this.getQueryParams()) {
			
			try {
				field.setValue(getParamValueFromEtlConfig(field.getName()));
			}
			catch (ForbiddenOperationException e) {
				try {
					field.setValue(getParamValueFromSourceMainObject(field.getName()));
				}
				catch (ForbiddenOperationException e1) {
					missingParameters.add(field.getName());
				}
			}
		}
		
		if (!missingParameters.isEmpty()) {
			throw new MissingParameterException(missingParameters);
		}
	}
	
	Object retrieveParamValue(String paramName) {
		List<String> missingParameters = new ArrayList<>();
		
		QueryParameter param = null;
		
		for (QueryParameter field : this.getQueryParams()) {
			
			if (!field.getName().equals(paramName))
				continue;
			
			try {
				field.setValue(getParamValueFromEtlConfig(field.getName()));
			}
			catch (ForbiddenOperationException e) {
				try {
					field.setValue(getParamValueFromSourceMainObject(field.getName()));
				}
				catch (ForbiddenOperationException e1) {
					missingParameters.add(field.getName());
				}
			}
			
			param = field;
			
			break;
		}
		
		if (!missingParameters.isEmpty()) {
			throw new MissingParameterException(missingParameters);
		}
		
		return param.getValue();
		
	}
	
	void logTrace(String msg) {
		getRelatedEtlConfiguration().logTrace(msg);
	}
	
	void logDebug(String msg) {
		getRelatedEtlConfiguration().logDebug(msg);
	}
	
	public void setQuery(String query) {
		this.originalQuery = query;
		this.query = utilities.removeDuplicatedEmptySpace(this.getOriginalQuery());
		this.query = utilities.removeSpacesBeforeAndAfterPeriod(this.getQuery());
		
		this.query = this.getQuery().replaceAll("\\s+", " ");
		
		logTrace("Discovering subqueries on quey");
		
		this.setSubqueries(DBUtilities.findSubqueries(this.getQuery()));
		
		if (hasSubQueries()) {
			logTrace("Found Subqueries \n" + this.getSubqueries());
		} else {
			logTrace("No subquery found");
		}
		
		setMainQuery(this.getQuery());
		
		if (hasSubQueries()) {
			for (String sQuery : this.getSubqueries()) {
				this.setMainQuery(utilities.maskToken(this.getMainQuery(), sQuery, '#'));
			}
		}
	}
	
	public QueryDataSourceConfig getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(QueryDataSourceConfig dataSource) {
		this.dataSource = dataSource;
	}
	
	private void setMainQuery(String mainQuery) {
		this.mainQuery = mainQuery;
	}
	
	private String getMainQuery() {
		return mainQuery;
	}
	
	private boolean hasSubQueries() {
		return utilities.arrayHasElement(this.getSubqueries());
	}
	
	private List<String> getSubqueries() {
		return subqueries;
	}
	
	private void setSubqueries(List<String> subqueries) {
		this.subqueries = subqueries;
	}
	
	private List<EtlDatabaseObject> getSrcObject() {
		return srcObject;
	}
	
	private List<QueryParameter> getQueryParams() {
		return queryParams;
	}
	
	private EtlConfiguration getEtlConfig() {
		return etlConfig;
	}
	
	public void setEtlConfig(EtlConfiguration etlConfig) {
		this.etlConfig = etlConfig;
	}
	
	public String getQuery() {
		return query;
	}
	
	public String getOriginalQuery() {
		return originalQuery;
	}
	
	public void setOriginalQuery(String originalQuery) {
		this.originalQuery = originalQuery;
	}
	
	private void setQueryParams(List<QueryParameter> queryParams) {
		this.queryParams = queryParams;
	}
	
	private void setSrcObject(List<EtlDatabaseObject> srcObject) {
		this.srcObject = srcObject;
	}
	
	public String generatePreparedQuery() {
		String pQuery = replaceSqlParametersWithQuestionMarks(this.getQuery());
		
		int questionMarkToBeReplaced = 1;
		
		if (hasQueryParams()) {
			for (int i = 0; i < getQueryParams().size(); i++) {
				QueryParameter param = this.getQueryParams().get(i);
				
				if (param.getContextType().compareClause() || param.getContextType().selectField()) {
					questionMarkToBeReplaced++;
				} else if (param.getContextType().inClause()) {
					
					String parts[] = param.getValue().toString().split(",");
					
					String quetionMarks = "";
					
					for (int j = 0; j < parts.length; j++) {
						if (!quetionMarks.isEmpty()) {
							quetionMarks += ",";
						}
						
						quetionMarks += "?";
					}
					
					pQuery = utilities.replaceNthOccurrenceWithString(pQuery, "?", quetionMarks, questionMarkToBeReplaced);
					
					questionMarkToBeReplaced += parts.length;
				} else if (param.getContextType().dbResource()) {
					pQuery = utilities.replaceNthOccurrenceWithString(pQuery, "?", param.getValue().toString(),
					    questionMarkToBeReplaced);
				}
			}
		}
		
		return pQuery;
		
	}
	
	public List<Object> generateQueryParameters() {
		List<Object> queryParams = new ArrayList<>();
		
		if (hasQueryParams()) {
			for (QueryParameter param : this.getQueryParams()) {
				
				if (param.getContextType().compareClause() || param.getContextType().selectField()) {
					queryParams.add(param.getValue());
				} else if (param.getContextType().inClause()) {
					
					String parts[] = param.getValue().toString().split(",");
					
					for (String part : parts) {
						queryParams.add(part);
					}
				} else if (param.getContextType().dbResource()) {
					continue;
				}
			}
		}
		
		return queryParams;
	}
	
	public static PreparedQuery prepare(QueryDataSourceConfig queryDs, List<EtlDatabaseObject> srcObject,
	        EtlConfiguration configuration) {
		
		return new PreparedQuery(queryDs, srcObject, configuration, false);
	}
	
	public static PreparedQuery prepare(QueryDataSourceConfig queryDs, EtlConfiguration etlConfig,
	        boolean ignoreMissingParameters) throws ForbiddenOperationException {
		return new PreparedQuery(queryDs, etlConfig, ignoreMissingParameters);
	}
	
	public PreparedQuery cloneAndLoadValues(List<EtlDatabaseObject> srcObject) {
		PreparedQuery cloned = new PreparedQuery();
		cloned.setSqlFunctionLoaded(this.isSqlFunctionLoaded());
		cloned.setDataSource(this.getDataSource());
		cloned.setQuery(this.getQuery());
		cloned.setEtlConfig(this.getEtlConfig());
		cloned.setSrcObject(srcObject);
		cloned.setCountFunctionInfo(this.getCountFunctionInfo());
		
		if (this.hasQueryParams()) {
			cloned.setQueryParams(QueryParameter.cloneAll(this.getQueryParams()));
			
			cloned.loadQueryParamValues();
		}
		
		return cloned;
	}
	
	private void setCountFunctionInfo(SqlFunctionInfo countFunctionInfo) {
		this.countFunctionInfo = countFunctionInfo;
	}
	
	public SqlFunctionInfo getCountFunctionInfo() {
		return countFunctionInfo;
	}
	
	public boolean isSqlFunctionLoaded() {
		return sqlFunctionLoaded;
	}
	
	public void setSqlFunctionLoaded(boolean sqlFunctionLoaded) {
		this.sqlFunctionLoaded = sqlFunctionLoaded;
	}
	
	private void tryToLoadSQLFunctionInfo() {
		
		if (!isSqlFunctionLoaded()) {
			
			List<SqlFunctionInfo> avaliableFunction = DBUtilities.extractSqlFunctionsInSelect(getMainQuery());
			
			if (utilities.arrayHasExactlyOneElement(this.getDataSource().getFields())
			        && utilities.arrayHasExactlyOneElement(avaliableFunction)) {
				if (avaliableFunction.get(0).isCountFunction()) {
					this.setCountFunctionInfo(avaliableFunction.get(0));
					
					String mainTableName = DBUtilities.extractFirstTableFromSelectQuery(this.getMainQuery());
					
					if (mainTableName.startsWith("@")) {
						mainTableName = retrieveParamValue(utilities.removeFirsChar(mainTableName)).toString();
					}
					
					this.getCountFunctionInfo().setMainTable(new GenericTableConfiguration(mainTableName));
					
					new MaintableAliasGenerator(this, mainTableName)
					        .generateAliasForTable(this.getCountFunctionInfo().getMainTable());
					
					this.getCountFunctionInfo().getMainTable().setRelatedEtlConfig(this.getEtlConfig());
				}
			}
			
			this.setSqlFunctionLoaded(true);
		}
	}
	
	boolean hasQueryParams() {
		return utilities.arrayHasElement(this.getQueryParams());
	}
	
	Object getParamValueFromEtlConfig(String param) throws ForbiddenOperationException {
		if (this.getEtlConfig() == null)
			throw new ForbiddenOperationException("The configuration object is not defined");
		
		Object paramValue = this.getEtlConfig().getParamValue(param);
		
		if (paramValue == null) {
			throw new MissingParameterException(param);
		}
		
		return paramValue;
	}
	
	Object getParamValueFromSourceMainObject(String paramName) throws ForbiddenOperationException {
		if (this.getSrcObject() == null)
			throw new ForbiddenOperationException("The main object is not defined");
		
		Object paramValue = null;
		
		for (EtlDatabaseObject obj : this.getSrcObject()) {
			try {
				paramValue = obj.getFieldValue(paramName);
			}
			catch (ForbiddenOperationException e) {
				//Ignore if the object does not contain the parameter
			}
		}
		
		if (paramValue == null) {
			throw new ForbiddenOperationException(
			        "The field '" + paramName + "' has no value and it is needed to load source object");
		}
		
		return paramValue;
	}
	
	/**
	 * Extract all the parameters presents in a dump query. This assume that the parameter will
	 * start with @
	 * 
	 * @param sqlQuery the query to extract from
	 * @return the list of extracted parameters name
	 */
	private List<QueryParameter> extractParamOnQuery(String sqlQuery) {
		
		logTrace("Extracting query parameters:\n-----------------\n" + sqlQuery + "\n---------------------");
		
		List<QueryParameter> parameters = new ArrayList<>();
		
		List<String> avaliableSubQueries = DBUtilities.tryToSplitQueryByUnions(sqlQuery);
		
		for (String subQuery : avaliableSubQueries) {
			List<QueryParameter> parametersInSubQuery = extractQueryParametersInSubQuery(subQuery);
			
			if (utilities.arrayHasElement(parametersInSubQuery)) {
				parameters.addAll(parametersInSubQuery);
			}
		}
		
		return parameters;
	}
	
	private List<QueryParameter> extractQueryParametersInSubQuery(String subQuery) {
		List<QueryParameter> parameters = new ArrayList<>();
		
		// Regular expression to match parameters starting with @ followed by optional spaces and then the parameter name
		String parameterRegex = "@\\s*(\\w+)";
		Pattern pattern = Pattern.compile(parameterRegex);
		Matcher matcher = pattern.matcher(subQuery);
		
		int minAllowedParamStart = 0;
		
		while (matcher.find()) {
			String paramName = matcher.group(1);
			
			logTrace("Found parameter: " + paramName);
			
			int paramStart = matcher.start();
			int paramEnd = matcher.end();
			
			if (paramStart < minAllowedParamStart) {
				continue;
			}
			
			QueryParameter params = new QueryParameter(paramName);
			
			logTrace("Trying to extract subquery from starting position " + paramStart + "\nOn Query\n--------------\n"
			        + subQuery);
			
			String containgSubquery = tryToExtractParameterContaingSubQuery(subQuery, paramStart);
			
			if (utilities.stringHasValue(containgSubquery)) {
				
				logTrace("Found subquer within the paratameter " + params.getName());
				
				//Assuming that this is the first parameter on the sub query
				//Try to determine its position
				
				int paramStartInSubQuery = determineFirstParameterPositionInQuery(containgSubquery);
				
				//The position where the sub query starts in the main query
				int subSueryStart = paramStart - paramStartInSubQuery;
				
				//We want to exclude all parameters within the sub query in the main parameters extraction
				minAllowedParamStart = subSueryStart + containgSubquery.length();
				
				List<QueryParameter> subqueyParams = extractParamOnQuery(containgSubquery);
				
				if (utilities.arrayHasElement(subqueyParams)) {
					parameters.addAll(subqueyParams);
				}
				
			} else {
				logTrace("No subquery found within the parameter on the current query");
				
				minAllowedParamStart = paramStart;
				
				logTrace("Determining Parameter context for parameter " + paramName);
				
				params.determineParameterContext(subQuery, paramStart, paramEnd);
				
				logTrace("Context for " + paramName + " is " + params.getContextType().toString());
				
				parameters.add(params);
			}
		}
		
		return parameters;
	}
	
	private int determineFirstParameterPositionInQuery(String sqlQuery) {
		// Regular expression to match parameters starting with @ followed by optional spaces and then the parameter name
		String parameterRegex = "@\\s*(\\w+)";
		Pattern pattern = Pattern.compile(parameterRegex);
		Matcher matcher = pattern.matcher(sqlQuery);
		
		while (matcher.find()) {
			return matcher.start();
		}
		
		throw new ForbiddenOperationException("The query does not contains parameter");
		
	}
	
	private static String tryToExtractParameterContaingSubQuery(String sqlQuery, int paramStart) {
		String subQuery = "";
		
		if (paramStart == 533) {
			System.out.println();
		}
		
		Stack<Integer> parenthesisStack = new Stack<>();
		
		boolean foundPossibleSubQueryStarting = false;
		boolean foundPossibleSubQueryFinishing = false;
		
		for (int i = paramStart; i > 0; i--) {
			char currChar = sqlQuery.charAt(i);
			
			if (currChar == '(') {
				//Found the possible starting sub query
				
				if (parenthesisStack.size() == 0) {
					foundPossibleSubQueryStarting = true;
					break;
				} else {
					parenthesisStack.pop();
					subQuery = ("" + currChar) + subQuery;
				}
			} else if (currChar == ')') {
				parenthesisStack.push(i);
				
				subQuery = ("" + currChar) + subQuery;
			} else {
				subQuery = ("" + currChar) + subQuery;
			}
		}
		
		if (foundPossibleSubQueryStarting) {
			for (int i = paramStart + 1; i < sqlQuery.length(); i++) {
				char currChar = sqlQuery.charAt(i);
				
				if (currChar == ')') {
					if (parenthesisStack.size() == 0) {
						foundPossibleSubQueryFinishing = true;
						break;
					} else {
						parenthesisStack.pop();
						subQuery = subQuery + ("" + currChar);
					}
				} else if (currChar == '(') {
					parenthesisStack.push(i);
					
					subQuery = subQuery + ("" + currChar);
					
				} else {
					subQuery = subQuery + currChar;
				}
			}
			
			if (foundPossibleSubQueryFinishing) {
				if (DBUtilities.isValidSelectSqlQuery(subQuery)) {
					return subQuery;
				}
			}
		}
		
		return null;
		
	}
	
	/**
	 * Replaces all dump parameters with a question mark. It assumes that the parameters will have
	 * format '@paramName'
	 * 
	 * @param sqlQuery
	 * @return
	 */
	public static String replaceSqlParametersWithQuestionMarks(String sqlQuery) {
		// Regular expression to match parameters starting with @, considering optional spaces or newlines
		String parameterRegex = "@\\s*\\w+";
		Pattern pattern = Pattern.compile(parameterRegex);
		Matcher matcher = pattern.matcher(sqlQuery);
		
		// Replace each parameter with a question mark
		StringBuffer replacedQuery = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(replacedQuery, "?");
		}
		matcher.appendTail(replacedQuery);
		
		return replacedQuery.toString();
	}
	
	public boolean isCountQuery() {
		return this.getCountFunctionInfo() != null;
	}
	
	public void detemineLimits(Connection conn) throws DBException {
		if (!this.isCountQuery()) {
			throw new ForbiddenOperationException("The query does not use count function!");
		}
		
		this.getCountFunctionInfo().detemineLimits(conn);
	}
	
	public EtlDatabaseObject query(Connection conn) throws DBException {
		
		if (this.isCountQuery()) {
			this.detemineLimits(conn);
			
			PreparedCountQuerySearchParams searchParams = new PreparedCountQuerySearchParams(this);
			
			long count = searchParams.countAllRecords(this.getCountFunctionInfo().getMinRecordId(),
			    this.getCountFunctionInfo().getMaxRecordId(), conn);
			
			EtlDatabaseObject obj = this.getDataSource().newInstance();
			obj.setRelatedConfiguration(this.getDataSource());
			
			obj.setFieldValue(this.getCountFunctionInfo().getAliasName(), count);
			
			return obj;
		}
		
		List<Object> paramsAsList = this.generateQueryParameters();
		
		Object[] params = paramsAsList != null ? paramsAsList.toArray() : null;
		
		return DatabaseObjectDAO.find(this.getDataSource().getLoadHealper(), this.getDataSource().getSyncRecordClass(),
		    this.generatePreparedQuery(), params, conn);
	}
	
}

class MaintableAliasGenerator implements TableAliasesGenerator {
	
	PreparedQuery pq;
	
	String mainTableName;
	
	public MaintableAliasGenerator(PreparedQuery pq, String mainTableName) {
		this.pq = pq;
		this.mainTableName = mainTableName;
	}
	
	@Override
	public void generateAliasForTable(TableConfiguration tabConfig) {
		String alias = DBUtilities.extractFirstTableAliasOnSqlQuery(this.pq.getQuery());
		
		if (alias != null) {
			tabConfig.setTableAlias(alias);
		} else {
			tabConfig.setTableAlias(mainTableName);
		}
	}
	
}
