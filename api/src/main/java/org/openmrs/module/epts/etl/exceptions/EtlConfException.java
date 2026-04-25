package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.model.base.EtlObject;

public class EtlConfException extends EtlExceptionImpl {
	
	private static final long serialVersionUID = 1L;
	
	public EtlConfException() {
	}
	
	public EtlConfException(String msg) {
		super(msg);
	}
	
	public EtlConfException(String msg, EtlObject etlObject) {
		super(msg, etlObject, ActionOnEtlException.ABORT_PROCESS);
	}
	
	public EtlConfException(String msg, Exception e) {
		this(msg, e, null);
	}
	
	public EtlConfException(String msg, Exception e, EtlObject etlObject) {
		super(msg, e, etlObject, ActionOnEtlException.ABORT_PROCESS);
	}
	
	public EtlConfException(Exception e) {
		super(e);
	}
	
	public EtlConfException(Exception e, EtlObject etlObject) {
		super(e, etlObject, ActionOnEtlException.ABORT_PROCESS);
	}
	
}
