package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectConfiguration;

public interface EtlDataSource extends DatabaseObjectConfiguration {
	
	String getName();
}
