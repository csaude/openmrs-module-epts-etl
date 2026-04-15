package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;

public class MissingFieldException extends EtlExceptionImpl {
	
	private static final long serialVersionUID = -4799473775930078338L;
	
	public MissingFieldException(String fieldName, DatabaseObjectConfiguration tabConf) {
		super("The field '" + fieldName + "' could not be found within " + tabConf != null ? " the object " + tabConf
		        : "Aknown Object");
	}
}
