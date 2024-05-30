package org.openmrs.module.epts.etl.dbextract.model;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.dbextract.controller.DbExtractController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public class DbExtractSearchParams extends EtlDatabaseObjectSearchParams {
	
	public DbExtractSearchParams(EtlItemConfiguration config, RecordLimits limits, DbExtractController relatedController) {
		super(config, limits, relatedController);
	}
	
	@Override
	public DbExtractController getRelatedController() {
		return (DbExtractController) super.getRelatedController();
	}
	
	@Override
	protected AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		DbExtractSearchParams cloned = new DbExtractSearchParams(getConfig(), null, getRelatedController());
		
		return cloned;
	}
	
}
