package org.openmrs.module.epts.etl.etl.model;

import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;

public class AuxQueryInfo {
	
	private String additionalLeftJoinFields;
	
	private SearchClauses<EtlDatabaseObject> searchClauses;
	
	public AuxQueryInfo(SearchClauses<EtlDatabaseObject> searchClauses) {
		this.additionalLeftJoinFields = "";
		this.searchClauses = searchClauses;
	}
	
	public String getAdditionalLeftJoinFields() {
		return additionalLeftJoinFields;
	}
	
	public void setAdditionalLeftJoinFields(String additionalLeftJoinFields) {
		this.additionalLeftJoinFields = additionalLeftJoinFields;
	}
	
	public SearchClauses<EtlDatabaseObject> getSearchClauses() {
		return searchClauses;
	}
	
	public void setSearchClauses(SearchClauses<EtlDatabaseObject> searchClauses) {
		this.searchClauses = searchClauses;
	}
	
	public void setClauseFrom(String clauseFrom) {
		this.getSearchClauses().setClauseFrom(clauseFrom);
	}
	
	public void addColumnToSelect(String toAdd) {
		this.getSearchClauses().addColumnToSelect(toAdd);
	}
	/*
	public void addToClauseFrom(String toAdd) {
		this.getSearchClauses().addToClauseFrom(toAdd);
	}*/
	
	public String getClauseFrom() {
		return this.getSearchClauses().getClauseFrom();
	}
	
}
