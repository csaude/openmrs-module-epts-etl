package org.openmrs.module.epts.etl.exceptions;


public class FieldNotAvaliableInAnyDataSource extends EtlException {

	private static final long serialVersionUID = -4799473775930078338L;
	
	
	public FieldNotAvaliableInAnyDataSource(String fieldName){
		super("The field '" + fieldName + "' could not be found in any avaliable DataSource");
	}
}
