package org.openmrs.module.epts.etl.dbextract.model;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.dbextract.controller.DbExtractController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.model.EtlSearchParams;

public class DbExtractSearchParams extends EtlSearchParams {
	
	public DbExtractSearchParams(EtlItemConfiguration config, RecordLimits limits, DbExtractController relatedController) {
		super(config, limits, relatedController);
	}
	
}
