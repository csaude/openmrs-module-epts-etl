package org.openmrs.module.epts.etl.conf.types;

public enum ObjectLanguageType {
	
	groovy,
	java;
	
	public boolean isJava() {
		return this.equals(java);
	}
	
	public boolean isGroovy() {
		return this.equals(groovy);
	}
	
}
