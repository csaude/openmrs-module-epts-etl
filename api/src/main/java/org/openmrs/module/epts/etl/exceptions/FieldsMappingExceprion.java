package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.conf.FieldsMappingIssues;

public class FieldsMappingExceprion extends EtlExceptionImpl {
	
	private static final long serialVersionUID = 1505624913800886849L;
	
	public FieldsMappingExceprion(String tableName, FieldsMappingIssues mappingIssue) {
		super(generateIssueMsg(tableName, mappingIssue));
	}
	
	static String generateIssueMsg(String tableName, FieldsMappingIssues mappingIssue) {
		String issue = "";
		
		if (!mappingIssue.getAvaliableInMultiDataSources().isEmpty()) {
			issue = tableName + " The destination fields "
			        + mappingIssue.extractDstFieldInAvaliableInMultiDataSources().toString()
			        + " cannot be automatically mapped as them occurrs in multiple src. Please configure them manually or specify the datasource order preference in prefferredDataSource array ";
		}
		
		if (!mappingIssue.getNotAvaliableInAnyDataSource().isEmpty()) {
			issue = tableName + " The destination fields "
			        + mappingIssue.extractDstFieldInNotAvaliableInAnyDataSource().toString()
			        + " cannot be automatically mapped as them do not occurr in any src. Please configure them manually!";
		}
		
		if (!mappingIssue.getNotAvaliableInSpecifiedDataSource().isEmpty()) {
			issue = tableName + " The source fields for destination fields ["
			        + mappingIssue.extractDstFieldInNotAvaliableInSpecifiedDataSource().toString()
			        + "] do not occurs in specified data sources !";
		}
		
		return issue;
	}
	
}
