package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.FieldAvaliableInMultipleDataSources;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

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
	
	public void fullLoad(DstConf dstConf, Connection conn) throws FieldAvaliableInMultipleDataSources, DBException {
		this.mappig = FieldsMapping.fastCreate(value, field, dstConf, conn);
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
