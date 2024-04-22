package org.openmrs.module.epts.etl.exceptions;


public class DuplicateMappingException extends ForbiddenOperationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7678953777161578467L;

	public DuplicateMappingException() {
		
	}
	
	public DuplicateMappingException(String msg) {
		super(msg);
	}
	
}
