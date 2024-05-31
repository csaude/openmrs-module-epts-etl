package org.openmrs.module.epts.etl.dbquickexport.model;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;

public class DBQuickExportSearchParams extends EtlDatabaseObjectSearchParams {
	
	public DBQuickExportSearchParams(EtlItemConfiguration config, RecordLimits limits) {
		super(config, limits, null);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray());
	}
	

}
