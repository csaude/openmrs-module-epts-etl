package org.openmrs.module.epts.etl.model.pojo.generic;

import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseOperationItemResult {
	
	private EtlDatabaseObject record;
	
	private DBException exception;
	
	public DatabaseOperationItemResult(EtlDatabaseObject record) {
		this.record = record;
	}
	
	public DatabaseOperationItemResult(EtlDatabaseObject record, DBException exception) {
		this.record = record;
		this.exception = exception;
	}
	
	public DBException getException() {
		return exception;
	}
	
	public EtlDatabaseObject getRecord() {
		return record;
	}
	
	public boolean hasFatalError() throws DBException {
		
		return false;
		
		/*
		if (getException().isDuplicatePrimaryOrUniqueKeyException()) {
			return false;
		}
		
		if (getException().isIntegrityConstraintViolationException()) {
			return false;
		}
		
		return true;*/
	}
}
