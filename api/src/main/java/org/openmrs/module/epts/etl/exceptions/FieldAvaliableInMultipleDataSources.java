package org.openmrs.module.epts.etl.exceptions;

public class FieldAvaliableInMultipleDataSources extends EtlException {
	
	private static final long serialVersionUID = -1844265774975547582L;
	
	public FieldAvaliableInMultipleDataSources(String fieldName) {
		super("The field '" + fieldName + "' is avaliable in multiple DataSources");
	}
}
