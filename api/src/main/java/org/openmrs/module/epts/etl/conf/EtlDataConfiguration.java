package org.openmrs.module.epts.etl.conf;

public abstract class EtlDataConfiguration extends BaseConfiguration {
	
	private EtlConfiguration relatedSyncConfiguration;
	
	public EtlConfiguration getRelatedSyncConfiguration() {
		return relatedSyncConfiguration;
	}
	
	public void setRelatedSyncConfiguration(EtlConfiguration relatedSyncConfiguration) {
		this.relatedSyncConfiguration = relatedSyncConfiguration;
	}
	
	public AppInfo getMainApp() {
		return this.relatedSyncConfiguration.getMainApp();
	}
}
