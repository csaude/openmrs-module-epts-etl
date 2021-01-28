package org.openmrs.module.eptssync.model;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.SyncProgressMeter;
import org.openmrs.module.eptssync.monitor.EngineMonitor;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ItemProgressInfo {
	private SyncTableConfiguration tableConfiguration;
	private EngineMonitor engineMonitor;
	private SyncProgressMeter progressMeter;
	
	public ItemProgressInfo(SyncTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
		
		tryToReloadProgressMeter();
	}
	
	public SyncProgressMeter getProgressMeter() {
		if (this.progressMeter == null) {
			tryToReloadProgressMeter();
		}
		
		return progressMeter;
	}
	
	public void setProgressMeter(SyncProgressMeter progressMeter) {
		this.progressMeter = progressMeter;
	}
	
	public void tryToReloadProgressMeter() {
		if (this.engineMonitor != null && this.getEngineMonitor().getProgressMeter() != null) {
			SyncProgressMeter sourceProgressMeter = this.engineMonitor.getProgressMeter();
			
			if (this.progressMeter == null) this.progressMeter = new SyncProgressMeter(sourceProgressMeter.getMonitor(), sourceProgressMeter.getStatusMsg(), sourceProgressMeter.getTotal(), sourceProgressMeter.getProcessed());
			
			this.progressMeter.refresh(sourceProgressMeter.getStatusMsg(), sourceProgressMeter.getTotal(), sourceProgressMeter.getProcessed());
		}
	}
	
	public void setEngineMonitor(EngineMonitor engineMonitor) {
		this.engineMonitor = engineMonitor;
	}
	
	@JsonIgnore
	public EngineMonitor getEngineMonitor() {
		return engineMonitor;
	}
	
	@JsonIgnore
	public SyncTableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}
	
	public String getSyncTableName() {
		return this.tableConfiguration.getTableName();
	}
	
	public void doLastProgressMeterRefresh(int totalRecords) {
		if (this.progressMeter == null) this.progressMeter = new SyncProgressMeter(this.engineMonitor, "Finished", totalRecords, totalRecords); 
	}
	
	
}
