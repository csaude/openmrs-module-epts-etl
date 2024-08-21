package org.openmrs.module.epts.etl.conf.datasource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.types.ParameterContextType;
import org.openmrs.module.epts.etl.conf.types.ParameterValueType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.db.conn.DbmsType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Describe a query parameter in a {@link QueryDataSourceConfig}
 */
public class QueryParameter extends Field {
	
	private static final long serialVersionUID = 6278318760048730611L;
	
	private ParameterValueType valueType;
	
	private ParameterContextType contextType;
	
	public QueryParameter() {
		contextType = ParameterContextType.ANKOWN;
	}
	
	public QueryParameter(String name) {
		super(name);
	}
	
	public static QueryParameter fastCreateWithValue(String paramName, Object paramValue) {
		QueryParameter q = new QueryParameter(paramName);
		q.setValue(paramValue);
		
		return q;
	}
	
	public ParameterContextType getContextType() {
		return contextType;
	}
	
	public void setContextType(ParameterContextType contextType) {
		this.contextType = contextType;
	}
	
	public ParameterValueType getValueType() {
		return valueType;
	}
	
	public void setValueType(ParameterValueType valueType) {
		this.valueType = valueType;
	}
	
	public static int getPosOnArrayParameter(String paramName) throws ForbiddenOperationException {
		validateParam(paramName);
		
		if (isArrayParameter(paramName)) {
			String strValue = (paramName.split("[")[1]).split("\\]")[0];
			
			return Integer.parseInt(strValue);
		} else {
			throw new ForbiddenOperationException("The param " + paramName + " is not an array!");
		}
		
	}
	
	public static boolean isArrayParameter(String paramName) {
		validateParam(paramName);
		
		return paramName.split("\\[").length > 1;
		
	}
	
	public static void validateParam(String paramName) throws ForbiddenOperationException {
		String[] paramElements = paramName.split("\\.");
		
		if (paramElements.length > 1) {
			for (String element : paramElements) {
				String[] arrayParamElements = element.split("\\[");
				
				if (arrayParamElements.length > 1) {
					String[] arrayParamsLeft = (arrayParamElements[1]).split("]");
					
					if (arrayParamsLeft.length == 1) {
						if (!utilities.isNumeric(arrayParamsLeft[0])) {
							throw new ForbiddenOperationException(
							        "The argument on param '" + paramName + "' must be numeric!");
						}
					} else {
						throw new ForbiddenOperationException("The argument on param '" + paramName + "' must be numeric!");
					}
				}
				
			}
		}
		
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		String toString = "[Name: " + getName();
		
		if (hasValue())
			toString += ", Value " + getValue();
		
		toString += ", Context: " + this.getContextType() + "]";
		
		return toString;
	}
	
	@Override
	public void copyFrom(Field f) {
		super.copyFrom(f);
		
		if (f instanceof QueryParameter) {
			QueryParameter other = (QueryParameter) f;
			
			this.valueType = other.valueType;
			
			this.contextType = other.contextType;
		}
	}
	
	public static List<QueryParameter> cloneAll(List<QueryParameter> configParams) {
		if (configParams == null)
			return null;
		
		List<QueryParameter> allCloned = new ArrayList<>(configParams.size());
		
		for (QueryParameter configuredParam : configParams) {
			QueryParameter cloned = new QueryParameter();
			cloned.copyFrom(configuredParam);
			
			allCloned.add(cloned);
		}
		
		return allCloned;
	}
	
	public void determineParameterContext(String sqlQuery, int paramStart, int paramEnd, DbmsType dbmsType) {
		String beforeParam = sqlQuery.substring(0, paramStart).toLowerCase();
		String afterParam = sqlQuery.substring(paramEnd).toLowerCase();
		
		// Pattern to check if the comparison token exists before the parameter
		Pattern beforeComparisonTokenPattern = Pattern.compile(".*\\s*(=|>|<|>=|<=|!=|<>|like|between|and)\\s*$",
		    Pattern.CASE_INSENSITIVE);
		Matcher beforeCompareClauseMatcher = beforeComparisonTokenPattern.matcher(beforeParam);
		
		// Pattern to check if the comparison token exists after the parameter
		Pattern afterComparisonTokenPattern = Pattern.compile("^\\s*(=|>|<|>=|<=|!=|<>|like|between|and)\\s*",
		    Pattern.CASE_INSENSITIVE);
		Matcher afterCompareClauseMatcher = afterComparisonTokenPattern.matcher(afterParam);
		
		Pattern inClauseBeforePattenern = Pattern.compile(".*\\bin\\s*\\(\\s*$", Pattern.DOTALL);
		Matcher inClauseBeforeMatcher = inClauseBeforePattenern.matcher(beforeParam.toLowerCase());
		
		Pattern inClauseAfterPattenern = Pattern.compile("(?s)^\\s*\\)\\s*(and|or|$)", Pattern.DOTALL);
		Matcher inClauseAfterMatcher = inClauseAfterPattenern.matcher(afterParam.toLowerCase());
		
		ParameterContextType type = null;
		
		if (beforeCompareClauseMatcher.find() || afterCompareClauseMatcher.find()) {
			type = ParameterContextType.COMPARE_CLAUSE;
		} else if (isSelectStarting(beforeParam) && (afterParam.contains(" from ") || dbmsType.isMysql())) {
			type = ParameterContextType.SELECT_FIELD;
		} else if (inClauseBeforeMatcher.matches() || inClauseAfterMatcher.matches()) {
			type = ParameterContextType.IN_CLAUSE;
		} else if (beforeParam.contains(" from ") || beforeParam.contains(" join ") || beforeParam.contains(" exists ")) {
			type = ParameterContextType.DB_RESOURCE;
		} else {
			type = ParameterContextType.DB_RESOURCE;
		}
		
		this.setContextType(type);
	}
	
	boolean isSelectStarting(String query) {
		
		if (query.toLowerCase().startsWith("select")) {
			return true;
		}
		
		String selectRegex = "(?i)\\s*select\\s+.+?\\s*";
		
		return query.toLowerCase().matches(selectRegex);
	}
	
}
