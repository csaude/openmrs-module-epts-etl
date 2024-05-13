package org.openmrs.module.epts.etl.conf.interfaces;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.EtlConfiguration;

public interface EtlDataConfiguration extends BaseConfiguration {
	
	EtlConfiguration getRelatedSyncConfiguration();
	
	void setRelatedSyncConfiguration(EtlConfiguration relatedSyncConfiguration);
	
	default AppInfo getMainApp() {
		return this.getRelatedSyncConfiguration().getMainApp();
	}
	
}
