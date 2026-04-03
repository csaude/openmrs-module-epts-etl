package org.openmrs.module.epts.etl.conf.types;

/**
 * Defines how the ETL engine should handle relationship (foreign key) resolution for a field during
 * transformation.
 * <p>
 * When a field represents a relationship, the ETL engine may need to resolve the corresponding
 * record in the destination database. This strategy controls how that resolution is performed.
 * </p>
 */
public enum RelationshipResolutionStrategy {
	
	/**
	 * Default behavior.
	 * <p>
	 * The ETL engine will attempt to resolve the relationship by looking up the corresponding
	 * record in the destination database.
	 * </p>
	 */
	RESOLVE,
	
	/**
	 * Skips relationship resolution.
	 * <p>
	 * The transformed value is written directly to the destination field without any lookup or
	 * validation against the destination database.
	 * </p>
	 * <p>
	 * Use this when the value is already a valid destination identifier or when performance is
	 * critical.
	 * </p>
	 */
	SKIP,
	
	/**
	 * Validates the relationship without resolving it.
	 * <p>
	 * The ETL engine verifies that the value corresponds to an existing record in the destination
	 * database, but does not attempt to transform or resolve it.
	 * </p>
	 * <p>
	 * If the record does not exist, an error is raised according to the ETL exception handling
	 * strategy.
	 * </p>
	 */
	VALIDATE_ONLY;
	
	public boolean resolve() {
		return this.equals(RESOLVE);
	}
	
	public boolean skip() {
		return this.equals(SKIP);
	}
	
	public boolean validateOnly() {
		return this.equals(VALIDATE_ONLY);
	}
	
}
