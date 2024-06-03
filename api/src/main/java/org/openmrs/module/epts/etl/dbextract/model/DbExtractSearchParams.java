package org.openmrs.module.epts.etl.dbextract.model;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.dbextract.controller.DbExtractController;
import org.openmrs.module.epts.etl.dbextract.engine.DbExtractEngine;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public class DbExtractSearchParams extends EtlDatabaseObjectSearchParams {
	
	public DbExtractSearchParams(EtlItemConfiguration config, ThreadRecordIntervalsManager limits, DbExtractEngine relatedEngine) {
		super(config, limits, relatedEngine);
	}
	
	@Override
	public DbExtractController getRelatedController() {
		return (DbExtractController) super.getRelatedController();
	}
	
	@Override
	public DbExtractEngine getRelatedEngine() {
		return (DbExtractEngine) super.getRelatedEngine();
	}
	
	@Override
	protected AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		DbExtractSearchParams cloned = new DbExtractSearchParams(getConfig(), null, getRelatedEngine());
		
		return cloned;
	}
	
}
