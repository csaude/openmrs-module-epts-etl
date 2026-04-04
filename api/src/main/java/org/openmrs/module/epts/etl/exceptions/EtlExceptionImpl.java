package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.model.base.EtlObject;

public class EtlExceptionImpl extends RuntimeException implements EtlException {
	
	private static final long serialVersionUID = 1L;
	
	private EtlObject etlObject;
	
	private ActionOnEtlException action;
	
	public EtlExceptionImpl() {
		this.action = ActionOnEtlException.ABORT_PROCESS;
	}
	
	public EtlExceptionImpl(String msg) {
		super(msg);
	}
	
	public EtlExceptionImpl(String msg, EtlObject etlObject, ActionOnEtlException action) {
		super(msg);
		
		this.action = action;
		this.etlObject = etlObject;
	}
	
	public EtlExceptionImpl(String msg, Exception e) {
		super(msg, e);
		
		this.action = ActionOnEtlException.ABORT_PROCESS;
	}
	
	public EtlExceptionImpl(String msg, Exception e, EtlObject etlObject, ActionOnEtlException action) {
		super(e.getLocalizedMessage(), e);
		
		this.action = action;
		this.etlObject = etlObject;
	}
	
	public EtlExceptionImpl(Exception e) {
		this(e.getLocalizedMessage());
		
		this.action = ActionOnEtlException.ABORT_PROCESS;
		
	}
	
	public EtlExceptionImpl(Exception e, EtlObject etlObject, ActionOnEtlException action) {
		this(e.getLocalizedMessage());
		
		this.action = action;
		this.etlObject = etlObject;
	}
	
	public void setEtlObject(EtlObject etlObject) {
		this.etlObject = etlObject;
	}
	
	@Override
	public Throwable getException() {
		return this;
	}
	
	@Override
	public ActionOnEtlException getAction() {
		return this.action;
	}
	
	@Override
	public EtlObject getEtlObject() {
		return this.etlObject;
	}
	
}
