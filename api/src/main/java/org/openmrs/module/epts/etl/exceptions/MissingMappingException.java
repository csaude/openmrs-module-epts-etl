package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.etl.processor.transformer.MappingFieldTransformer;
import org.openmrs.module.epts.etl.model.base.EtlObject;

public class MissingMappingException extends EtlTransformationException {
	
	private static final long serialVersionUID = 1L;
	
	public MissingMappingException(EtlObject etlObject, String srcField, Object srcValue, MappingFieldTransformer transformer,
	    ActionOnEtlException actionOnException) {
		
		super("No mapping found for srcValue (" + srcField + "): " + srcValue + " on mapping: " + transformer.getMappingTable(), etlObject,
		        actionOnException);
	}
	
}
