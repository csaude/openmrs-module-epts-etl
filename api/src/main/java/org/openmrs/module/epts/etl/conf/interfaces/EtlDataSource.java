package org.openmrs.module.epts.etl.conf.interfaces;

import java.util.List;

import org.openmrs.module.epts.etl.conf.datasource.AuxExtractTable;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;

public interface EtlDataSource extends DatabaseObjectConfiguration {
	
	String getName();
	
	List<AuxExtractTable> getAuxExtractTable();
	
	void setAuxExtractTable(List<AuxExtractTable> auxExtractTable);
}
