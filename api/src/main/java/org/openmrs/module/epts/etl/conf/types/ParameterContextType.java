package org.openmrs.module.epts.etl.conf.types;

/**
 * The Context were a parameter appear in a query
 */
public enum ParameterContextType {
	
	// @formatter:off 
	
	/**
	 * Indicates that the parameter appear in a select area
	 * 
	 *  Eg. SELECT @param1 as value FROM tab1;
	 */
	SELECT_FIELD,
	
	/**
	 * Indicates that the parameter appear in IN clause.
	 * Eg. SELECT * FROM tab1 WHERE att1 in (@param2);
	 */
	IN_CLAUSE,
	
	/**
	 * Indicates that the parameter appear as part of comparison in a clause
	 * Eg. SELECT * FROM WHERE att2 = @param3;
	 */
	COMPARE_CLAUSE,
	
	/**
	 * Indicates that the parameter apper as a resource
	 * Eg. SELECT * FROM @table_name WHERE att1 = value1;  
	 */
	DB_RESOURCE,
	
	
	ANKOWN;
	
	// @formatter:on 
	
	public boolean selectField() {
		return this.equals(SELECT_FIELD);
	}
	
	public boolean inClause() {
		return this.equals(IN_CLAUSE);
	}
	
	public boolean compareClause() {
		return this.equals(COMPARE_CLAUSE);
	}
	
	public boolean dbResource() {
		return this.equals(DB_RESOURCE);
	}
	
	public boolean aknown() {
		return this.equals(ANKOWN);
	}
}
