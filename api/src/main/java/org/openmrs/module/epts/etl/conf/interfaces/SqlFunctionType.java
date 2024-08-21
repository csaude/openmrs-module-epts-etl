package org.openmrs.module.epts.etl.conf.interfaces;

public enum SqlFunctionType {
	
	COUNT,
	MAX,
	MIN,
	timestampdiff,
	UNKOWN;
	
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
		} else if (token.toLowerCase().contains("timestampdiff")) {
			return SqlFunctionType.timestampdiff;
		}
		
		else
			return SqlFunctionType.UNKOWN;
	}
}
