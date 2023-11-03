package org.openmrs.module.epts.etl.changedrecordsdetector.model;

import java.util.Date;

public class OperationInfo {
	private char operationType;
	private Date operationDate;
	
	
	public static final char OPERATION_TYPE_INSERT = 'c';
	public static final char OPERATION_TYPE_UPDATE = 'u';
	public static final char OPERATION_TYPE_DELETE = 'd';
	public static final char OPERATION_TYPE_VOID= 'u';
	
	public OperationInfo(char operationType, Date operationDate) {
		this.operationType = operationType;
		this.operationDate = operationDate;
	}
	
	public Date getOperationDate() {
		return operationDate;
	}
	
	public char getOperationType() {
		return operationType;
	}

	public static OperationInfo fastCreateVoidOperation(Date operationDate) {
		return new OperationInfo(OperationInfo.OPERATION_TYPE_VOID, operationDate);
	}

	public static OperationInfo fastCreateChangeOperation(Date operationDate) {
		return new OperationInfo(OperationInfo.OPERATION_TYPE_UPDATE, operationDate);
	}

	public static OperationInfo fastCreateInsertOperation(Date operationDate) {
		return new OperationInfo(OperationInfo.OPERATION_TYPE_INSERT, operationDate);
	}
	
}
