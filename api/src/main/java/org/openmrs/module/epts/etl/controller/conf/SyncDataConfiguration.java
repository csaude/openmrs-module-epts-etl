package org.openmrs.module.epts.etl.controller.conf;

public abstract class SyncDataConfiguration extends BaseConfiguration {
	
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
