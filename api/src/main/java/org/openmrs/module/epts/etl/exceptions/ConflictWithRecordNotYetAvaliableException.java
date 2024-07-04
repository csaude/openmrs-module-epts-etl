package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ConflictWithRecordNotYetAvaliableException extends DBException {
	
	private static final long serialVersionUID = 1L;
	
	private EtlObject record;
	
	public ConflictWithRecordNotYetAvaliableException(EtlObject record, DBException e) {
		super("The current thread tried to created a dstRecord that exists but is still not avaliable. Record " + record, e);
		
		this.record = record;
	}
	
	public EtlObject getRecord() {
		return record;
	}
	
	@Override
	public boolean isDuplicatePrimaryOrUniqueKeyException() throws DBException {
		return true;
	}
	
	@Override
	public boolean isIntegrityConstraintViolationException() throws DBException {
		return false;
	}
}
