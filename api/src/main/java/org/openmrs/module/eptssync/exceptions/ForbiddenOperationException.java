package org.openmrs.module.eptssync.exceptions;

/**
 * 
 * This exception is throwen when some forbiden operation is performed
 * 
 * @author JP. Boane
 * @version 1.0 07/12/2012
 *
 */
public class ForbiddenOperationException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private Object currentObject;
	
	public ForbiddenOperationException(String msg){
		super(msg);
	}
	
	public ForbiddenOperationException(String msg, Object currentObject){
		super(msg);
		
		this.currentObject = currentObject;
	}
	
	public ForbiddenOperationException(Exception exception){
		super(exception);
	}

	public Object getCurrentObject() {
		return currentObject;
	}

	public void setCurrentObject(Object currentObject) {
		this.currentObject = currentObject;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
