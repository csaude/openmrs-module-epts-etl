package org.openmrs.module.epts.etl.exceptions;

public class DatabaseResourceDoesNotExists extends ForbiddenOperationException {
	
	private static final long serialVersionUID = 6133732845334359420L;
	
	public DatabaseResourceDoesNotExists(String resourceName) {
		super("Database resource '" + resourceName + "' does not exists!");
	}
}
