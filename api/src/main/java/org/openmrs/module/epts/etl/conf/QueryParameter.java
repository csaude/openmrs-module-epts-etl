package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;

/**
 * Describe a query parameter in a {@link QueryDataSourceConfig}
 */
public class QueryParameter extends Field {
	
	private static final long serialVersionUID = 6278318760048730611L;
	
	private ParameterValueType valueType;
	
	public QueryParameter() {
	}
	
	public QueryParameter(String paramName, Object paramValue) {
		super(paramName, paramValue);
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
		}else {
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
	
}
