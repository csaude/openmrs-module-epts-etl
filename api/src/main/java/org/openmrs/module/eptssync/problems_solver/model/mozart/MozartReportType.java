package org.openmrs.module.eptssync.problems_solver.model.mozart;


public enum MozartReportType {
	RESOLVED_PROBLEMS, DETECTED_PROBLEMS, NO_ISSUE; 

	public boolean isResolvedProblemsReport() {
		return this.equals(MozartReportType.RESOLVED_PROBLEMS);
	}
	
	public boolean isDetectedProblemsReport() {
		return this.equals(MozartReportType.DETECTED_PROBLEMS);
	}
	
	public boolean isNoIssueReport() {
		return this.equals(MozartReportType.NO_ISSUE);
	}
}
