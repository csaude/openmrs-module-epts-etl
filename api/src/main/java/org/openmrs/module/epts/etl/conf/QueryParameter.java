package org.openmrs.module.epts.etl.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.types.ParameterContextType;
import org.openmrs.module.epts.etl.conf.types.ParameterValueType;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;

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
	
}
