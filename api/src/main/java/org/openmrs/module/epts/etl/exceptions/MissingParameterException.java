package org.openmrs.module.epts.etl.exceptions;

import java.util.List;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class MissingParameterException extends ForbiddenOperationException {
	
	private static final long serialVersionUID = 3807657803283143320L;
	
	List<String> missingParameters;
	
	public MissingParameterException(String... missingParameter) {
		this(CommonUtilities.getInstance().parseArrayToList(missingParameter));
	}
	
	public MissingParameterException(List<String> missingParameters) {
		super(missingParameters != null ? missingParameters.toString() : null);
		
		this.missingParameters = missingParameters;
	}
	
	public List<String> getMissingParameters() {
		return missingParameters;
	}
}
