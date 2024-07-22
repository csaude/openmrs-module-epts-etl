package org.openmrs.module.epts.etl.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.types.ParameterContextType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

/**
 * Represents an prepared query ready to be executed. It alwas has a ready query and its parameters
 */
public class PreparedQuery {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String query;
	
	private EtlDatabaseObject srcObject;
	
	private EtlConfiguration etlConfig;
	
	private List<QueryParameter> queryParams;
	
	PreparedQuery() {
	}
	
	PreparedQuery(String query, EtlDatabaseObject srcObject, EtlConfiguration configuration) {
		this.setQuery(query);
		this.setEtlConfig(configuration);
		this.setSrcObject(srcObject);
		
		List<QueryParameter> allParameters = extractAllParamOnQuery(this.getQuery());
		
		this.setQueryParams(allParameters);
		
		if (!hasQueryParams())
			return;
		
		loadQueryParamValues();
	}
	
	PreparedQuery(String query, EtlConfiguration config) {
		this(query, null, config);
	}
	
	void loadQueryParamValues() {
		String error = "";
		
		for (QueryParameter field : this.getQueryParams()) {
			
			try {
				field.setValue(getParamValueFromEtlConfig(field.getName()));
			}
			catch (ForbiddenOperationException e) {
				try {
					field.setValue(getParamValueFromSourceMainObject(field.getName()));
				}
				catch (ForbiddenOperationException e1) {
					if (!error.isEmpty()) {
						error += ",";
					}
					
					error += field.getName();
				}
			}
		}
		
		if (!error.isEmpty()) {
			throw new ForbiddenOperationException("Missing parameters: " + error);
		}
		
	}
	
	public void setQuery(String query) {
		this.query = query;
		this.query = utilities.removeDuplicatedEmptySpace(this.query);
		this.query = utilities.removeSpacesBeforeAndAfterPeriod(this.query);
	}
	
	private EtlDatabaseObject getSrcObject() {
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
	
	private void setQueryParams(List<QueryParameter> queryParams) {
		this.queryParams = queryParams;
	}
	
	private void setSrcObject(EtlDatabaseObject srcObject) {
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
					
					questionMarkToBeReplaced++;
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
	
	public static PreparedQuery prepare(String query, EtlDatabaseObject srcObject, EtlConfiguration configuration) {
		
		return new PreparedQuery(query, srcObject, configuration);
	}
	
	public static PreparedQuery prepare(String query, EtlConfiguration etlConfig) throws ForbiddenOperationException {
		return new PreparedQuery(query, etlConfig);
	}
	
	public PreparedQuery cloneAndLoadValues(EtlDatabaseObject srcObject) {
		PreparedQuery cloned = new PreparedQuery();
		
		cloned.setQuery(this.getQuery());
		cloned.setEtlConfig(this.getEtlConfig());
		
		if (this.hasQueryParams()) {
			cloned.setQueryParams(QueryParameter.cloneAll(this.getQueryParams()));
			
			loadQueryParamValues();
		}
		
		return cloned;
	}
	
	boolean hasQueryParams() {
		return utilities.arrayHasElement(this.getQueryParams());
	}
	
	Object getParamValueFromEtlConfig(String param) throws ForbiddenOperationException {
		if (this.getEtlConfig() == null)
			throw new ForbiddenOperationException("The configuration object is not defined");
		
		Object paramValue = this.getEtlConfig().getParamValue(param);
		
		if (paramValue == null) {
			throw new ForbiddenOperationException("The configuration param '" + param + "' is needed to load source object");
		}
		
		return paramValue;
	}
	
	Object getParamValueFromSourceMainObject(String paramName) throws ForbiddenOperationException {
		if (this.getSrcObject() == null)
			throw new ForbiddenOperationException("The main object is not defined");
		
		Object paramValue = this.getSrcObject().getFieldValue(paramName);
		
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
	private List<QueryParameter> extractAllParamOnQuery(String sqlQuery) {
		
		List<QueryParameter> parameters = new ArrayList<>();
		
		// Regular expression to match parameters starting with @ followed by optional spaces and then the parameter name
		String parameterRegex = "@\\s*(\\w+)";
		Pattern pattern = Pattern.compile(parameterRegex);
		Matcher matcher = pattern.matcher(sqlQuery);
		
		int minAllowedParamStart = 0;
		
		while (matcher.find()) {
			String paramName = matcher.group(1);
			int paramStart = matcher.start();
			
			if (paramStart < minAllowedParamStart) {
				continue;
			}
			
			QueryParameter params = new QueryParameter(paramName);
			
			String containgSubquery = tryToExtractParameterContaingSubQuery(sqlQuery, paramStart);
			
			if (utilities.stringHasValue(containgSubquery)) {
				//Assuming that this is the first parameter on the subquery
				//Try to determine its position
				
				int paramStartInSubQuery = determineFirstParameterPositionInQuery(containgSubquery);
				
				//The position where the subquery starts in the main query
				int subSueryStart = paramStart - paramStartInSubQuery;
				
				//We want to exclude all parameters within the subquery in the main parameters extraction
				minAllowedParamStart = subSueryStart + containgSubquery.length();
				
				List<QueryParameter> subqueyParams = extractAllParamOnQuery(containgSubquery);
				
				if (utilities.arrayHasElement(subqueyParams)) {
					parameters.addAll(subqueyParams);
				}
				
			} else {
				minAllowedParamStart = paramStart;
				
				params.setContextType(determineParameterContext(sqlQuery, paramStart));
				
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
		
		boolean foundPossibleSubQueryStarting = false;
		boolean foundPossibleSubQueryFinishing = false;
		
		for (int i = paramStart; i > 0; i--) {
			char currChar = sqlQuery.charAt(i);
			
			if (currChar == '(') {
				//Found the possible starting sub query
				
				foundPossibleSubQueryStarting = true;
				break;
			}
			if (currChar == ')') {
				//Closing the subquery before found the staring. Abort
				break;
			} else {
				subQuery = currChar + subQuery;
			}
		}
		
		if (foundPossibleSubQueryStarting) {
			for (int i = paramStart + 1; i < sqlQuery.length(); i++) {
				char currChar = sqlQuery.charAt(i);
				
				if (currChar == ')') {
					//Found the possible finishing sub query
					
					foundPossibleSubQueryFinishing = true;
					break;
				}
				if (currChar == '(') {
					//Found the opening subquery before the closing... Abort
					break;
					
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
	
	private static ParameterContextType determineParameterContext(String sqlQuery, int paramStart) {
		
		String beforeParam = sqlQuery.substring(0, paramStart).toLowerCase();
		String afterParam = sqlQuery.substring(paramStart).toLowerCase();
		
		if (beforeParam.contains("select ") && afterParam.contains(" from ")) {
			return ParameterContextType.SELECT_FIELD;
		} else if (beforeParam.matches(".*\\bin\\s*\\($") || afterParam.matches("^\\s*\\)\\s*(and|or|$)")) {
			return ParameterContextType.IN_CLAUSE;
		} else if (beforeParam.matches(".*(=|>|<|>=|<=|!=|<>|like)\\s*$")) {
			return ParameterContextType.COMPARE_CLAUSE;
		} else if (beforeParam.contains(" from ") || beforeParam.contains(" join ") || beforeParam.contains(" exists ")) {
			return ParameterContextType.DB_RESOURCE;
		} else {
			return ParameterContextType.DB_RESOURCE;
		}
	}
	
	public static void main(String[] args) {
		String query = "select count(*) from @person where exists (select @a, b from person_name where person.@person_id = person_name.person_id) and not exists (select * from person_attribute where person.person_id = person_id)";
		
		/*for (int i = 0; i < query.length(); i++) {
			String subquery = tryToExtractParameterContaingSubQuery(query, i);
			
			System.out.println(i + ": " + subquery);
		}*/
		
		EtlConfiguration conf = new EtlConfiguration();
		
		PreparedQuery p = PreparedQuery.prepare(query, conf);
		
		System.out.println(p);
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
}
