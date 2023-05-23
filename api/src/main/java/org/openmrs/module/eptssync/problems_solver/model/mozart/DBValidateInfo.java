package org.openmrs.module.eptssync.problems_solver.model.mozart;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openmrs.module.eptssync.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DBValidateInfo {
	static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private String database;
	
	private List<MozartProblemType> problems;
	
	private List<String> missingTables;
	
	private List<String> emptyTables;
	
	private List<Map<String, Object>> missingFields;
	
	private List<ResolvedProblem> resolvedProblems;
	private MozartValidateInfoReport report;
	
	public DBValidateInfo() {
	}
	
	public DBValidateInfo(MozartValidateInfoReport report, String database) {
		this.database = database;
		this.report = report;
		
		if (this.report != null) this.report.addReport(this);
	}
	
	public DBValidateInfo(String database) {
		this.database = database;
	}
	
	public void setReport(MozartValidateInfoReport report) {
		if (this.report != null) this.report.removeDBValidateInfo(this);
		
		this.report = report;
		
		
		
		if (this.report != null) this.report.addReport(this);
	}
	
	@JsonIgnore
	public MozartValidateInfoReport getReport() {
		return report;
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
	
	public void addProblemType(MozartProblemType type) {
		if (problems == null)
			problems = new ArrayList<MozartProblemType>();
		
		if (!utilities.existOnArray(this.problems, type))
			problems.add(type);
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
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DBValidateInfo)) return false;
		
		DBValidateInfo otherObj = (DBValidateInfo)obj;
		
		return this.database.equalsIgnoreCase(otherObj.getDatabase()) && this.report.equals(otherObj.report);
	}
	
}
