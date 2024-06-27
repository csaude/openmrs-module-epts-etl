package org.openmrs.module.epts.etl.exceptions;

public class EtlExceptionImpl extends RuntimeException implements EtlException {
	
	private static final long serialVersionUID = 1L;
	
	public EtlExceptionImpl() {
	}
	
	public EtlExceptionImpl(String msg) {
		super(msg);
	}
	
	public EtlExceptionImpl(Exception e) {
		this(e.getLocalizedMessage());
	}
	
	@Override
	public Throwable getException() {
		return this;
	}
	
}
