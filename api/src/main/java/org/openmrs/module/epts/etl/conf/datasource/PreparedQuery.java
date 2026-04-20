package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.GenericTableConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.EtlAdditionalDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.EtlDataSource;
import org.openmrs.module.epts.etl.conf.interfaces.TableAliasesGenerator;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.conf.types.DbmsType;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.FieldTransformingInfo;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
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
	
	private List<EtlDatabaseObject> srcObjects;
	
	private EtlConfiguration etlConfig;
	
	private List<QueryParameter> queryParams;
	
	private String mainQuery;
	
	private List<String> subqueries;
	
	private SqlFunctionInfo countFunctionInfo;
	
	private EtlDataSource dataSource;
	
	private String originalQuery;
	
	private boolean sqlFunctionLoaded;
	
	private DbmsType dbmsType;
	
	private boolean original;
	
	private boolean ensuredDynamicElementsLoaded;
	
	PreparedQuery() {
	}
	
	EtlConfiguration getRelatedEtlConfiguration() {
		return getDataSource().getRelatedEtlConf();
	}
	
	PreparedQuery(EtlDataSource dataSource, List<EtlDatabaseObject> srcObject, EtlConfiguration configuration,
	    boolean ignoreMissingParameters, DbmsType dbmsType) {
		this.dbmsType = dbmsType;
		this.setDataSource(dataSource);
		
		this.logTrace("Starting Query preparation... " + dataSource.getName());
		
		this.original = true;
		
		this.setQuery(dataSource.getQuery());
		this.setEtlConfig(configuration);
		this.setSrcObject(srcObject);
		this.tryToLoadSQLFunctionInfo();
		
		logTrace("Loading Query Parameters..");
		
		ensureDynamicElementsLoadedAsParameteres();
		
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
	
	private void ensureDynamicElementsLoadedAsParameteres() {
		if (hasDynamicElements()) {
			for (String element : this.getDataSource().getDynamicElements()) {
				setQuery(getQuery().replaceAll(element, "@(" + element + ")"));
			}
		}
	}
	
	public boolean isEnsuredDynamicElementsLoaded() {
		return ensuredDynamicElementsLoaded;
	}
	
	public boolean isOriginal() {
		return original;
	}
	
	PreparedQuery(EtlAdditionalDataSource queryDs, EtlConfiguration config, boolean ignoreMissingParameters,
	    DbmsType dbmsType) {
		this(queryDs, null, config, ignoreMissingParameters, dbmsType);
	}
	
	void loadQueryParamValues() {
		List<String> missingParameters = new ArrayList<>();
		
		for (QueryParameter field : this.getQueryParams()) {
			
			if (field.isDynamicElement(this.getDataSource())) {
				continue;
			}
			
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
		
		return param != null ? param.getValue() : null;
		
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
			logTrace("Masking found Subqueries");
			for (String sQuery : this.getSubqueries()) {
				this.setMainQuery(utilities.maskToken(this.getMainQuery(), sQuery, '#'));
			}
			logTrace("Masking done");
		}
	}
	
	public EtlDataSource getDataSource() {
		return dataSource;
	}
	
	public void setDataSource(EtlDataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	private void setMainQuery(String mainQuery) {
		this.mainQuery = mainQuery;
	}
	
	private String getMainQuery() {
		return mainQuery;
	}
	
	private boolean hasSubQueries() {
		return utilities.listHasElement(this.getSubqueries());
	}
	
	private List<String> getSubqueries() {
		return subqueries;
	}
	
	private void setSubqueries(List<String> subqueries) {
		this.subqueries = subqueries;
	}
	
	private List<EtlDatabaseObject> getSrcObjects() {
		return srcObjects;
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
		this.srcObjects = srcObject;
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
					
					if (param.hasValue()) {
						pQuery = utilities.replaceNthOccurrenceWithString(pQuery, "?", param.getValue().toString(),
						    questionMarkToBeReplaced);
					} else {
						throw new ForbiddenOperationException("The parameter '" + param.getName()
						        + "' has no value and its needed to generate prepared query!!");
					}
					
				}
			}
		}
		
		if (hasDynamicElements() && !isEnsuredDynamicElementsLoaded()) {
			for (String element : this.getDataSource().getDynamicElements()) {
				pQuery = pQuery.replaceAll(element, "null");
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
					
					if (param.getValue() == null)
						throw new EtlExceptionImpl("No value was provided to parameter " + param.getName());
					
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
	
	public static PreparedQuery prepare(EtlDataSource queryDs, List<EtlDatabaseObject> srcObject,
	        EtlConfiguration configuration, DbmsType dbmsType) {
		
		return new PreparedQuery(queryDs, srcObject, configuration, false, dbmsType);
	}
	
	public static PreparedQuery prepare(EtlAdditionalDataSource queryDs, EtlConfiguration etlConfig,
	        List<EtlDatabaseObject> auxLoadObjects, boolean ignoreMissingParameters, DbmsType dbmsType)
	        throws ForbiddenOperationException {
		return new PreparedQuery(queryDs, auxLoadObjects, etlConfig, ignoreMissingParameters, dbmsType);
	}
	
	public PreparedQuery cloneAndLoadValues(EtlProcessor processor, EtlDatabaseObject srcObject, EtlDatabaseObject dstObject,
	        List<EtlDatabaseObject> srcObjects, Connection srcConn) throws EtlTransformationException, DBException {
		
		PreparedQuery cloned = new PreparedQuery();
		cloned.setSqlFunctionLoaded(this.isSqlFunctionLoaded());
		cloned.setDataSource(this.getDataSource());
		cloned.setQuery(this.getQuery());
		cloned.setEtlConfig(this.getEtlConfig());
		cloned.setSrcObject(srcObjects);
		cloned.setCountFunctionInfo(this.getCountFunctionInfo());
		
		if (this.hasQueryParams()) {
			cloned.setQueryParams(QueryParameter.cloneAll(this.getQueryParams()));
			
			cloned.loadQueryParamValues();
		}
		
		if (hasDynamicElements()) {
			cloned.ensureDynamicElementsLoaded(processor, srcObject, dstObject, srcObjects, srcConn);
		}
		
		return cloned;
	}
	
	private void ensureDynamicElementsLoaded(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject dstObject, List<EtlDatabaseObject> srcObjects, Connection srcConn)
	        throws EtlTransformationException, DBException {
		
		if (!hasDynamicElements()) {
			return;
		}
		
		if (isOriginal()) {
			throw new EtlExceptionImpl("Only cloned query can be loaded with dynamic elements");
		}
		
		for (String element : this.getDataSource().getDynamicElements()) {
			FieldsMapping map = FieldsMapping.fastCreate(element);
			
			FieldTransformingInfo f = map.getTransformerInstance().transform(processor, srcObject, dstObject, srcObjects,
			    map, srcConn, srcConn);
			
			if (f.getTransformedValue() != null) {
				getQueryParam(parseToDynamicParameter(element)).setValue(f.getTransformedValue());
			} else {
				throw new EtlExceptionImpl(
				        "The transformation of dynamic element '" + element + "' resulted on an empty value!!!");
			}
		}
		
		this.ensuredDynamicElementsLoaded = true;
	}
	
	private String parseToDynamicParameter(String element) {
		return "(" + element + ")";
	}
	
	private QueryParameter getQueryParam(String name) {
		if (hasQueryParams()) {
			for (QueryParameter p : this.getQueryParams()) {
				if (p.getName().equals(name)) {
					return p;
				}
			}
		}
		
		throw new EtlExceptionImpl("Query parameter '" + name + "' not found!");
	}
	
	private boolean hasDynamicElements() {
		return this.getDataSource().hasDynamicElements();
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
			
			if (utilities.listHasExactlyOneElement(this.getDataSource().getFields())
			        && utilities.listHasExactlyOneElement(avaliableFunction)) {
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
		return utilities.listHasElement(this.getQueryParams());
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
		if (this.getSrcObjects() == null)
			throw new ForbiddenOperationException("The main object is not defined");
		
		Object paramValue = null;
		
		boolean paramExists = false;
		
		for (EtlDatabaseObject obj : this.getSrcObjects()) {
			try {
				paramValue = obj.getFieldValue(paramName);
				
				paramExists = true;
				
				if (paramValue != null) {
					break;
				}
				
			}
			catch (ForbiddenOperationException e) {
				//Ignore if the object does not contain the parameter
			}
		}
		
		if (!paramExists) {
			throw new MissingParameterException(paramName);
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
			
			if (utilities.listHasElement(parametersInSubQuery)) {
				parameters.addAll(parametersInSubQuery);
			}
		}
		
		return parameters;
	}
	
	private List<QueryParameter> extractQueryParametersInSubQuery(String subQuery) {
		
		List<QueryParameter> parameters = new ArrayList<>();
		
		Pattern pattern = Pattern.compile("@\\s*(\\w+)|@\\s*\\(");
		Matcher matcher = pattern.matcher(subQuery);
		
		int minAllowedParamStart = 0;
		
		while (matcher.find()) {
			
			int paramStart = matcher.start();
			
			if (paramStart < minAllowedParamStart) {
				continue;
			}
			
			String paramName;
			int paramEnd;
			
			// 🔥 CASO 1: parâmetro composto @(....)
			if (matcher.group().contains("(")) {
				
				int openParenIndex = subQuery.indexOf("(", matcher.start());
				
				String fullParam = extractCompositeParameter(subQuery, openParenIndex);
				
				paramName = fullParam; // mantém expressão inteira
				paramEnd = openParenIndex + fullParam.length();
				
				logTrace("Found COMPOSITE parameter: " + paramName);
				
			}
			// 🔥 CASO 2: parâmetro simples
			else {
				
				paramName = matcher.group(1);
				paramEnd = matcher.end();
				
				logTrace("Found parameter: " + paramName);
			}
			
			QueryParameter params = new QueryParameter(paramName);
			
			logTrace("Trying to extract subquery from starting position " + paramStart + "\nOn Query\n--------------\n"
			        + subQuery);
			
			String containgSubquery = tryToExtractParameterContaingSubQuery(subQuery, paramStart, this.dbmsType);
			
			if (utilities.stringHasValue(containgSubquery)) {
				
				logTrace("Found subquery within the parameter " + params.getName());
				
				int paramStartInSubQuery = determineFirstParameterPositionInQuery(containgSubquery);
				
				int subQueryStart = paramStart - paramStartInSubQuery;
				
				minAllowedParamStart = subQueryStart + containgSubquery.length();
				
				List<QueryParameter> subqueryParams = extractParamOnQuery(containgSubquery);
				
				if (utilities.listHasElement(subqueryParams)) {
					parameters.addAll(subqueryParams);
				}
				
			} else {
				
				logTrace("No subquery found within the parameter on the current query");
				
				minAllowedParamStart = paramStart;
				
				params.determineParameterContext(subQuery, paramStart, paramEnd, this.dbmsType);
				
				logTrace("Context for " + paramName + " is " + params.getContextType());
				
				parameters.add(params);
			}
		}
		
		return parameters;
	}
	
	private String extractCompositeParameter(String query, int startIndex) {
		
		int open = 0;
		int i = startIndex;
		
		for (; i < query.length(); i++) {
			char c = query.charAt(i);
			
			if (c == '(')
				open++;
			else if (c == ')')
				open--;
			
			if (open == 0) {
				return query.substring(startIndex, i + 1);
			}
		}
		
		throw new EtlExceptionImpl("Unclosed composite parameter starting at position " + startIndex);
	}
	
	private int determineFirstParameterPositionInQuery(String sqlQuery) {
		
		Pattern pattern = Pattern.compile("@\\s*(\\w+)|@\\s*\\(");
		Matcher matcher = pattern.matcher(sqlQuery);
		
		if (matcher.find()) {
			return matcher.start();
		}
		
		throw new ForbiddenOperationException("The query does not contain parameters");
	}
	
	private static String tryToExtractParameterContaingSubQuery(String sqlQuery, int paramStart, DbmsType dbmsType) {
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
				if (DBUtilities.isValidSelectSqlQuery(subQuery, dbmsType)) {
					return subQuery;
				}
			}
		}
		
		return null;
		
	}
	
	public static String replaceSqlParametersWithQuestionMarks(String sqlQuery) {
		
		if (sqlQuery == null || sqlQuery.isBlank()) {
			return sqlQuery;
		}
		
		StringBuilder result = new StringBuilder();
		
		int i = 0;
		int length = sqlQuery.length();
		
		while (i < length) {
			
			char c = sqlQuery.charAt(i);
			
			// 🔹 detectar início de parâmetro
			if (c == '@') {
				
				//int start = i;
				
				i++; // skip '@'
				
				// 🔥 ignorar espaços
				while (i < length && Character.isWhitespace(sqlQuery.charAt(i))) {
					i++;
				}
				
				// 🔥 CASO 1: parâmetro composto @(....)
				if (i < length && sqlQuery.charAt(i) == '(') {
					
					int open = 0;
					
					do {
						char ch = sqlQuery.charAt(i);
						
						if (ch == '(')
							open++;
						else if (ch == ')')
							open--;
						
						i++;
						
					} while (i < length && open > 0);
					
					// substituir tudo por ?
					result.append("?");
					
				}
				// 🔥 CASO 2: parâmetro simples
				else {
					
					while (i < length && (Character.isLetterOrDigit(sqlQuery.charAt(i)) || sqlQuery.charAt(i) == '_')) {
						i++;
					}
					
					result.append("?");
				}
				
			} else {
				result.append(c);
				i++;
			}
		}
		
		return result.toString();
	}
	
	public boolean isCountQuery() {
		return this.getCountFunctionInfo() != null;
	}
	
	public void detemineLimits(Engine<? extends EtlDatabaseObject> engine, Connection conn) throws DBException {
		if (!this.isCountQuery()) {
			throw new ForbiddenOperationException("The query does not use count function!");
		}
		
		this.getCountFunctionInfo().detemineLimits(engine, conn);
	}
	
	@SuppressWarnings("unchecked")
	public List<EtlDatabaseObject> query(Engine<? extends EtlDatabaseObject> engine, Connection conn) throws DBException {
		
		if (this.isCountQuery()) {
			this.detemineLimits(engine, conn);
			
			PreparedCountQuerySearchParams searchParams = new PreparedCountQuerySearchParams(this);
			
			long count = searchParams.countAllRecords(this.getCountFunctionInfo().getMinRecordId(),
			    this.getCountFunctionInfo().getMaxRecordId(), conn);
			
			EtlDatabaseObject obj = this.getDataSource().newInstance();
			obj.setRelatedConfiguration(this.getDataSource());
			
			obj.setFieldValue(this.getCountFunctionInfo().getAliasName(), count);
			
			return utilities.parseToList(obj);
		}
		
		List<Object> paramsAsList = this.generateQueryParameters();
		
		Object[] params = paramsAsList != null ? paramsAsList.toArray() : null;
		
		return (List<EtlDatabaseObject>) DatabaseObjectDAO.search(this.getDataSource().getLoadHealper(),
		    this.getDataSource().getSyncRecordClass(), this.generatePreparedQuery(), params, conn);
	}
	
	@Override
	public String toString() {
		return this.mainQuery;
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
