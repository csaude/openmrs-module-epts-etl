package org.openmrs.module.epts.etl.conf.interfaces;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;

public enum SqlFunctionType {
	
	COUNT,
	MAX,
	MIN;
	
	public boolean isCount() {
		return this.equals(COUNT);
	}
	
	public static SqlFunctionType determine(String token) {
		if (token.toLowerCase().contains("count")) {
			return SqlFunctionType.COUNT;
		}
		if (token.toLowerCase().contains("max")) {
			return SqlFunctionType.MAX;
		}
		if (token.toLowerCase().contains("min")) {
			return SqlFunctionType.MIN;
		}
		
		throw new ForbiddenOperationException("Ansuported function in token " + token);
	}
}
