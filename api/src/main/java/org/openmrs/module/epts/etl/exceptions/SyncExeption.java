package org.openmrs.module.epts.etl.exceptions;

public abstract class SyncExeption extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public SyncExeption(){
	}
	
	public SyncExeption(String msg){
		super(msg);
	}
	
	public SyncExeption(Exception e){
		this(e.getLocalizedMessage());
	}
}
