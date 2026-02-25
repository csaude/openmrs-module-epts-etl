package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.model.base.EtlObject;

public class EtlTransformationException extends EtlExceptionImpl {
	
	private static final long serialVersionUID = 1L;
	
	public EtlTransformationException(EtlObject etlSrcObject, ActionOnEtlException actionOnException) {
		super("An error occured transforming the object " + etlSrcObject, etlSrcObject, actionOnException);
	}
	
	public EtlTransformationException(String msg, EtlObject etlSrcObject, ActionOnEtlException actionOnException) {
		super(msg, etlSrcObject, actionOnException);
	}
	
	public EtlTransformationException(String msg, Exception e, EtlObject etlObject, ActionOnEtlException action) {
		super(msg, e, etlObject, action);
	}
}
