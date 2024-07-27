package org.openmrs.module.epts.etl.etl.processor.transformer;

public enum TransformationType {
	
	/**
	 * The principal or main transformation
	 */
	PRINCIPAL,
	
	/**
	 * The inner transformation, usually for parents loading
	 */
	INNER;
	
	public boolean isPrincipal() {
		return this.equals(PRINCIPAL);
	}
	
	public boolean isInner() {
		return this.equals(INNER);
	}
}
