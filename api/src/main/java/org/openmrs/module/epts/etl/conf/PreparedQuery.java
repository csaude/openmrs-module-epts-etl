package org.openmrs.module.epts.etl.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.types.ParameterContextType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * Represents an prepared query ready to be executed. It alwas has a ready query and its parameters
 */
public class PreparedQuery {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String query;
	
	private EtlDatabaseObject srcObject;
	
	private EtlConfiguration etlConfig;
	
	private List<QueryParameter> queryParams;
	
	PreparedQuery(String query, EtlConfiguration config, boolean ignoreMissingParams) {
		this.setQuery(query);
		this.setEtlConfig(config);
		
		this.setQueryParams(extractAllParamOnQuery(query));
		
		if (!hasQueryParams())
			return;
		
		loadQueryParamValues(ignoreMissingParams);
	}
	
	PreparedQuery(String query, EtlDatabaseObject srcObject, EtlConfiguration configuration, boolean ignoreMissingParams) {
		this.setQuery(query);
		this.setEtlConfig(configuration);
		this.setSrcObject(srcObject);
		this.setQueryParams(extractAllParamOnQuery(query));
		
		if (!hasQueryParams())
			return;
		
		loadQueryParamValues(ignoreMissingParams);
	}
	
	void loadQueryParamValues(boolean ignoreMissingParams) {
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
					
					error += e1.getLocalizedMessage();
				}
			}
		}
		
		if (!error.isEmpty() && !ignoreMissingParams)
			throw new ForbiddenOperationException(error);
		
	}
	
	public void setQuery(String query) {
		this.query = query;
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
	
	public static PreparedQuery prepare(String query, EtlDatabaseObject srcObject, EtlConfiguration configuration,
	        boolean ignoreMissingParams) {
		
		return new PreparedQuery(query, srcObject, configuration, ignoreMissingParams);
	}
	
	public static PreparedQuery prepare(String query, EtlConfiguration etlConfig, boolean ignoreMissingParams) {
		return new PreparedQuery(query, etlConfig, ignoreMissingParams);
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
	public static List<QueryParameter> extractAllParamOnQuery(String sqlQuery) {
		List<QueryParameter> parameters = new ArrayList<>();
		
		// Regular expression to match parameters starting with @ followed by optional spaces and then the parameter name
		String parameterRegex = "@\\s*(\\w+)";
		Pattern pattern = Pattern.compile(parameterRegex);
		Matcher matcher = pattern.matcher(sqlQuery);
		
		while (matcher.find()) {
			String paramName = matcher.group(1);
			int paramStart = matcher.start();
			
			QueryParameter params = new QueryParameter(paramName);
			params.setContextType(determineContext(sqlQuery, paramStart));
			
			parameters.add(params);
		}
		
		return parameters;
	}
	
	private static ParameterContextType determineContext(String sqlQuery, int paramStart) {
		String beforeParam = sqlQuery.substring(0, paramStart).toLowerCase();
		String afterParam = sqlQuery.substring(paramStart).toLowerCase();
		
		if (beforeParam.contains("select ") && !beforeParam.contains(" from ")) {
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
