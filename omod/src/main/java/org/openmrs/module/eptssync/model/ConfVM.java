package org.openmrs.module.eptssync.model;

import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;

public class ConfVM {
	private static final String INSTALATION_TAB = "1";
	private static final String OPERATIONS_TAB = "2";
	private static String TABLES_TAB = "3";
	
	private SyncConfiguration syncConfiguration;
	private String activeTab;
	
	public ConfVM(String installationType) {
		this.activeTab = ConfVM.INSTALATION_TAB;
	}
	
	public String getActiveTab() {
		return activeTab;
	}
	
	public void activateTab(String tab) {
		this.activeTab = tab;
	}
	
	public SyncConfiguration getSyncConfiguration() {
		return syncConfiguration;
	}
	
	public boolean isInstallationTabActive() {
		return this.activeTab.equals(ConfVM.INSTALATION_TAB);
	}
	
	public boolean isOperationsTabActive() {
		return this.activeTab.equals(ConfVM.OPERATIONS_TAB);
	}
	
	public boolean isTablesTabActive() {
		return this.activeTab.equals(ConfVM.TABLES_TAB);
	}
}
