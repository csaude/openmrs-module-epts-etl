package org.openmrs.module.epts.etl.problems_solver.model.mozart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DBValidateInfo {
	
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String database;
	
	private List<MozartProblemType> problems;
	
	private List<String> missingTables;
	
	private List<String> emptyTables;
	
	private List<Map<String, Object>> missingFields;
	
	private List<Map<String, Object>> emptyFields;
	
	private List<ResolvedProblem> resolvedProblems;
	
	private List<Map<String, Object>> missingUniqueKeys;
	
	private MozartValidateInfoReport report;
	
	private List<Map<String, Object>> notFullMergedTables;
	
	public DBValidateInfo() {
	}
	
	public DBValidateInfo(String database) {
		this.database = database;
	}
	
	@JsonIgnore
	public MozartValidateInfoReport getReport() {
		return report;
	}
	
	public void setReport(MozartValidateInfoReport report) {
		this.report = report;
	}
	
	public void addResolvedProblem(ResolvedProblem resolvedProblem) {
		if (this.resolvedProblems == null)
			this.resolvedProblems = new ArrayList<ResolvedProblem>();
		
		this.resolvedProblems.add(resolvedProblem);
	}
	
	@JsonIgnore
	public boolean hasProblem() {
		return utilities.arrayHasElement(this.problems);
	}
	
	@JsonIgnore
	public boolean hasResolvedProblem() {
		return utilities.arrayHasElement(this.resolvedProblems);
	}
	
	public void addMissingTable(String tableName) {
		if (missingTables == null)
			missingTables = new ArrayList<String>();
		
		missingTables.add(tableName);
	}
	
	public void addEmptyTable(String tableName) {
		if (emptyTables == null)
			emptyTables = new ArrayList<String>();
		
		emptyTables.add(tableName);
	}
	
	public void addMissingFields(String tableName, List<String> fields) {
		if (this.missingFields == null)
			this.missingFields = new ArrayList<Map<String, Object>>();
		
		this.missingFields.add(utilities.fastCreateMap("tableName", tableName, "missingFields", fields));
	}
	
	public void addEmptyFields(String tableName, List<String> fields) {
		if (this.emptyFields == null)
			this.emptyFields = new ArrayList<Map<String, Object>>();
		
		this.emptyFields.add(utilities.fastCreateMap("tableName", tableName, "emptyFields", fields));
	}
	
	public void addNotFullMergedTables(String tableName, int notMerged) {
		if (this.notFullMergedTables == null)
			this.notFullMergedTables = new ArrayList<Map<String, Object>>();
		
		this.notFullMergedTables.add(utilities.fastCreateMap("tableName", tableName, "notMergedRecords", notMerged));
	}
	
	public List<Map<String, Object>> getNotFullMergedTables() {
		return notFullMergedTables;
	}
	
	public List<Map<String, Object>> getEmptyFields() {
		return emptyFields;
	}
	
	public void addProblemType(MozartProblemType type) {
		if (problems == null) {
			problems = new ArrayList<MozartProblemType>();
		}
		
		if (!utilities.existOnArray(this.problems, type)) {
			problems.add(type);
		}
	}
	
	public String getDatabase() {
		return database;
	}
	
	public void setDatabase(String database) {
		this.database = database;
	}
	
	public List<MozartProblemType> getProblems() {
		return problems;
	}
	
	public void setProblems(List<MozartProblemType> problems) {
		this.problems = problems;
	}
	
	public List<String> getMissingTables() {
		return missingTables;
	}
	
	public void setMissingTables(List<String> missingTables) {
		this.missingTables = missingTables;
	}
	
	public List<String> getEmptyTables() {
		return emptyTables;
	}
	
	public void setEmptyTables(List<String> emptyTables) {
		this.emptyTables = emptyTables;
	}
	
	public List<Map<String, Object>> getMissingFields() {
		return missingFields;
	}
	
	public void setMissingFields(List<Map<String, Object>> missingFields) {
		this.missingFields = missingFields;
	}
	
	public List<ResolvedProblem> getResolvedProblems() {
		return resolvedProblems;
	}
	
	public List<Map<String, Object>> getMissingUniqueKeys() {
		return missingUniqueKeys;
	}
	
	public void setMissingUniqueKeys(List<Map<String, Object>> missingUniqueKeys) {
		this.missingUniqueKeys = missingUniqueKeys;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DBValidateInfo))
			return false;
		
		DBValidateInfo otherObj = (DBValidateInfo) obj;
		
		return this.database.equalsIgnoreCase(otherObj.getDatabase());
	}
	
	public void addMissingUniqueKeys(String tableName, List<String> keyFields) {
		if (this.missingUniqueKeys == null)
			this.missingUniqueKeys = new ArrayList<>();
		
		this.missingUniqueKeys.add(utilities.fastCreateMap("tableName", tableName, "uniqueKey", keyFields));
	}
	
}
