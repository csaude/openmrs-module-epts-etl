package org.openmrs.module.epts.etl.controller.conf;

public enum ParameterValueType {
	
	/**
	 * Indicates that the parameter value is a constant
	 */
	CONSTANT_PARAM,
	/**
	 * Indicates that the parameter value is from configuration file
	 */
	CONFIGURATION_PARAM,
	
	/**
	 * Indicate that the parameter value source is undefined
	 */
	UNDEFINED,
	
	/**
	 * Indicate that the parameter value if from the main source object
	 */
	MAIN_OBJECT_PARAM;
	
	public boolean isConstant() {
		return this.equals(CONSTANT_PARAM);
	}
	
	public boolean isConfiguration() {
		return this.equals(CONFIGURATION_PARAM);
	}
	
	public boolean isMainObject() {
		return this.equals(MAIN_OBJECT_PARAM);
	}
	
	public boolean isUndefined() {
		return this.equals(UNDEFINED);
	}
}
