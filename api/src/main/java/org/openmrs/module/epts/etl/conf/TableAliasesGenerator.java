package org.openmrs.module.epts.etl.conf;

public interface TableAliasesGenerator {
	
	String generateAlias(AbstractTableConfiguration tabConfig);
}
