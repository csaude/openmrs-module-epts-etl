package org.openmrs.module.epts.etl.exceptions;

public abstract class EtlException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public EtlException(){
	}
	
	public EtlException(String msg){
		super(msg);
	}
	
	public EtlException(Exception e){
		this(e.getLocalizedMessage());
	}
}
