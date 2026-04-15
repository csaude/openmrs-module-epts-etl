package org.openmrs.module.epts.etl.conf.datasource;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;

public class SqlConditionElement {
	
	private FieldsMapping mappig;
	
	private String field;
	
	private String operator;
	
	private String value;
	
	public SqlConditionElement(String field, String operator, String value) {
		this.field = field;
		this.operator = operator;
		this.value = value;
	}
	
	public String getField() {
		return field;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return field + " " + operator + " " + value;
	}
	
	public void fullLoad(DstConf dstConf) {
		this.mappig = FieldsMapping.fastCreate(value, field, dstConf);
	}
	
	public FieldsMapping getMappig() {
		return mappig;
	}
	
	public String parseToQuestionMarked() {
		return field + " " + operator + (this.value != null ? " ?" : "");
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SqlConditionElement))
			return false;
		
		return this.field.equals(((SqlConditionElement) obj).getField());
	}
}
