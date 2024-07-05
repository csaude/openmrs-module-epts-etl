package org.openmrs.module.epts.etl.etl.model;

public enum LoadingType {
	
	/**
	 * The principal or main loading
	 */
	PRINCIPAL,
	
	/**
	 * The inner loading, usually for parents loading
	 */
	INNER;
	
	public boolean isPrincipal() {
		return this.equals(PRINCIPAL);
	}
	
	public boolean isInner() {
		return this.equals(INNER);
	}
}
