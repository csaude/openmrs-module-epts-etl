package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.FieldsMappingIssues;

public class FieldsMappingException extends EtlExceptionImpl {
	
	private static final long serialVersionUID = 1505624913800886849L;
	
	public FieldsMappingException(DstConf conf, FieldsMappingIssues mappingIssue) {
		super(generateIssueMsg(conf, mappingIssue));
	}
	
	static String generateIssueMsg(DstConf conf, FieldsMappingIssues mappingIssue) {
		String tableName = conf.getTableName();
		String confName = conf.getParentConf().getConfigCode();
		String fullObjectName = confName + ">" + tableName + ": ";
		String issue = "";
		
		if (!mappingIssue.getAvaliableInMultiDataSources().isEmpty()) {
			issue = fullObjectName + " The destination fields "
			        + mappingIssue.extractDstFieldInAvaliableInMultiDataSources().toString()
			        + " cannot be automatically mapped as them occurrs in multiple src. Please configure them manually or specify the datasource order preference in prefferredDataSource array ";
		}
		
		if (!mappingIssue.getNotAvaliableInAnyDataSource().isEmpty()) {
			issue = fullObjectName + " The destination fields "
			        + mappingIssue.extractDstFieldInNotAvaliableInAnyDataSource().toString()
			        + " cannot be automatically mapped as them do not occurr in any src. Please configure them manually!";
		}
		
		if (!mappingIssue.getNotAvaliableInSpecifiedDataSource().isEmpty()) {
			issue = fullObjectName + " The source fields for destination fields ["
			        + mappingIssue.extractDstFieldInNotAvaliableInSpecifiedDataSource().toString()
			        + "] do not occurs in specified data sources !";
		}
		
		return issue;
	}
	
}
