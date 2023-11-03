package org.openmrs.module.epts.etl.problems_solver.model.mozart;

public class ResolvedProblem {
	
	private String tableName;
	
	private String originalTableName;
	
	private String columnName;
	
	private String originalColumnName;
	
	private Integer recordId;
	
	private Object originalColumnValue;
	
	private Object newColumnValue;
	
	private MozartProblemType problemType;
	
	public ResolvedProblem() {
	}
	
	public static ResolvedProblem init(String tableName) {
		ResolvedProblem resolvedProblem = new ResolvedProblem();
		resolvedProblem.tableName = tableName;
		
		return resolvedProblem;
	}
	
	public String getOriginalTableName() {
		return originalTableName;
	}
	
	public void setOriginalTableName(String originalTableName) {
		this.originalTableName = originalTableName;
	}
	
	public String getOriginalColumnName() {
		return originalColumnName;
	}
	
	public void setOriginalColumnName(String originalColumnName) {
		this.originalColumnName = originalColumnName;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public String getColumnName() {
		return columnName;
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	public Integer getRecordId() {
		return recordId;
	}
	
	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}
	
	public Object getOriginalColumnValue() {
		return originalColumnValue;
	}
	
	public void setOriginalColumnValue(Object originalColumnValue) {
		this.originalColumnValue = originalColumnValue;
	}
	
	public Object getNewColumnValue() {
		return newColumnValue;
	}
	
	public void setNewColumnValue(Object newColumnValue) {
		this.newColumnValue = newColumnValue;
	}
	
	public MozartProblemType getProblemType() {
		return problemType;
	}
	
	public void setProblemType(MozartProblemType problemType) {
		this.problemType = problemType;
	}
	
}
