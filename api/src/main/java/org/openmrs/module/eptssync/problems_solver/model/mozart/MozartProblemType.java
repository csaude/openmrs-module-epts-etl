package org.openmrs.module.eptssync.problems_solver.model.mozart;


public enum MozartProblemType {
	OLD_STRUCTURE,
	MISSIN_FIELDS,
	MISSING_TABLES,
	EMPTY_TABLES,
	MISSING_DB,
	EMPTY_FIELD,
	MISSING_UNIQUE_KEY,
	WRONG_FIELD_NAME,
	WRONG_TABLE_NAME,
	NOT_FULL_MERGED_DB;
	
	public boolean isOldStructure() {
		return this.equals(OLD_STRUCTURE);
	}
	
	public boolean isMissingFields() {
		return this.equals(MISSIN_FIELDS);
	}
	
}
