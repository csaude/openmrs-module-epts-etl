package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.model.base.SyncRecord;

public class ConflictWithRecordNotYetAvaliableException extends SyncExeption {
	private static final long serialVersionUID = 1L;
	private SyncRecord record;
	
	public ConflictWithRecordNotYetAvaliableException(SyncRecord record) {
		super("The current thread tried to created a record that exists but is still not avaliable. Record " + record);
		
		this.record = record;
	}
	
	public SyncRecord getRecord() {
		return record;
	}
}
