package org.openmrs.module.eptssync.exceptions;

import org.openmrs.module.eptssync.model.base.SyncRecord;

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
