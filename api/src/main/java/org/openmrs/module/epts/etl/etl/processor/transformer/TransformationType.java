package org.openmrs.module.epts.etl.etl.processor.transformer;

public enum TransformationType {
	
	/**
	 * The principal or main transformation
	 */
	PRINCIPAL,
	
	/**
	 * The inner transformation, usually for parents loading
	 */
	INNER,
	
	/**
	 * Happens within the load of child when its parent is being created on demand
	 */
	ON_DEMAND;
	
	public boolean isPrincipal() {
		return this.equals(PRINCIPAL);
	}
	
	public boolean isInner() {
		return this.equals(INNER);
	}
	
	public boolean onDemand() {
		return this.equals(ON_DEMAND);
	}
}
