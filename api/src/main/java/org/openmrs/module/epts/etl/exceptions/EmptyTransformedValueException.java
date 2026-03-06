package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.etl.processor.transformer.FastSqlFieldTransformer;
import org.openmrs.module.epts.etl.model.base.EtlObject;

public class EmptyTransformedValueException extends EtlTransformationException {
	
	private static final long serialVersionUID = 1L;
	
	public EmptyTransformedValueException(EtlObject etlObject, String srcField, FastSqlFieldTransformer transformer,
	    ActionOnEtlException actionOnException) {
		
		super("Empty value returned for (" + srcField + "): by fast query " + transformer.getSqlQuery(), etlObject,
		        actionOnException);
	}
	
}
