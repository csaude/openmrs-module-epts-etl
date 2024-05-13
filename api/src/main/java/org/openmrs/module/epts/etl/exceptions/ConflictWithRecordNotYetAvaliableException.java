package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.model.base.EtlObject;

public class ConflictWithRecordNotYetAvaliableException extends EtlException {
	private static final long serialVersionUID = 1L;
	private EtlObject record;
	
	public ConflictWithRecordNotYetAvaliableException(EtlObject record) {
		super("The current thread tried to created a record that exists but is still not avaliable. Record " + record);
		
		this.record = record;
	}
	
	public EtlObject getRecord() {
		return record;
	}
}
