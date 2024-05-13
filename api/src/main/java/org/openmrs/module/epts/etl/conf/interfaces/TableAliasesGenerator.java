package org.openmrs.module.epts.etl.conf.interfaces;

public interface TableAliasesGenerator {
	
	String generateAlias(TableConfiguration tabConfig);
}
