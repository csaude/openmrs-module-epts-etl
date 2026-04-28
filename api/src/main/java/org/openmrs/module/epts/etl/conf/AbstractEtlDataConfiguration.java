package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;

public abstract class AbstractEtlDataConfiguration extends AbstractBaseConfiguration implements EtlDataConfiguration {
	
	private EtlConfiguration relatedSyncConfiguration;
	
	public EtlConfiguration getRelatedEtlConf() {
		return relatedSyncConfiguration;
	}
	
	public void setRelatedEtlConfig(EtlConfiguration relatedSyncConfiguration) {
		this.relatedSyncConfiguration = relatedSyncConfiguration;
	}
	
	@Override
	public ActionOnEtlException getGeneralBehaviourOnEtlException() {
		return relatedSyncConfiguration.getGeneralBehaviourOnEtlException();
	}
	
}
