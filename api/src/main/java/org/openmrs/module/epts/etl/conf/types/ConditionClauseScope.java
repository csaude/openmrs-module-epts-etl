package org.openmrs.module.epts.etl.conf.types;

public enum ConditionClauseScope {
	
	ON_CLAUSE,
	WHERE_CLAUSE;
	
	public boolean isOnClause() {
		return this.equals(ON_CLAUSE);
	}
	
	public boolean isWhereClause() {
		return this.equals(WHERE_CLAUSE);
	}
	
}
