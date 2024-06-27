package org.openmrs.module.epts.etl.model.pojo.generic;

public enum EtlOperationResultItemType {
	// @formatter:off
	
	NO_ERROR,
	UNEXPECTED_ERRORS,
	RECURSIVE_RELATIONSHIPS,
	RESOLVED_INCONSISTENCES,
	UNRESOLVED_INCONSISTENCES;
	
	// @formatter:on
	public boolean isNoError() {
		return this.equals(NO_ERROR);
	}
	
	public boolean isUnresolvedInconsistences() {
		return this.equals(UNRESOLVED_INCONSISTENCES);
	}
	
	public boolean isResolvedInconsistences() {
		return this.equals(RESOLVED_INCONSISTENCES);
	}
	
	public boolean isUnexpectedErros() {
		return this.equals(UNEXPECTED_ERRORS);
	}
	
	public boolean isRecursiverelationships() {
		return this.equals(RECURSIVE_RELATIONSHIPS);
	}
	
}
