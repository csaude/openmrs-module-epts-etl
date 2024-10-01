package org.openmrs.module.epts.etl.etl.model;

import org.openmrs.module.epts.etl.conf.datasource.EtlConfigurationSrcConf;
import org.openmrs.module.epts.etl.conf.datasource.SrcConf;

public class EtlDynamicSearchParams extends EtlDatabaseObjectSearchParams {
	
	EtlConfigurationSrcConf relatedSrcConf;
	
	public EtlDynamicSearchParams(EtlConfigurationSrcConf relatedSrcConf) {
		super(null, null);
		
		this.relatedSrcConf = relatedSrcConf;
	}
	
	@Override
	public SrcConf getSrcConf() {
		return relatedSrcConf;
	}
	
}
