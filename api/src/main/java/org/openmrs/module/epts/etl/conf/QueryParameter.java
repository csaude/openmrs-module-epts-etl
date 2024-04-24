package org.openmrs.module.epts.etl.conf;

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
	
}
