package org.openmrs.module.epts.etl.conf.types;

public enum ConditionClauseScope {
	
	JOIN_CLAUSE,
	WHERE_CLAUSE;
	
	public boolean isJoinClause() {
		return this.equals(JOIN_CLAUSE);
	}
	
	public boolean isWhereClause() {
		return this.equals(WHERE_CLAUSE);
	}
	
}
