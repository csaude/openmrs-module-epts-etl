package org.openmrs.module.epts.etl.controller.conf;

public abstract class SyncDataConfiguration extends BaseConfiguration {
	
	private SyncConfiguration relatedSyncConfiguration;
	
	public SyncConfiguration getRelatedSyncConfiguration() {
		return relatedSyncConfiguration;
	}
	
	public void setRelatedSyncConfiguration(SyncConfiguration relatedSyncConfiguration) {
		this.relatedSyncConfiguration = relatedSyncConfiguration;
	}
	
	public AppInfo getMainApp() {
		return this.relatedSyncConfiguration.getMainApp();
	}
}
