package org.openmrs.module.eptssync.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.status.OperationStatus;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class OperationProgressInfo {
	private static CommonUtilities utilities = CommonUtilities.getInstance();

	private List<ItemProgressInfo> itemsProgressInfo;
	private OperationController controller;
	
	public OperationProgressInfo(OperationController controller) {
		this.controller = controller;
	}
	
	public void updateProgressInfo(EngineMonitor engineMonitor) {
		for (ItemProgressInfo info : this.itemsProgressInfo) {
			if (info.getSyncTableName().equals(engineMonitor.getSyncTableInfo().getTableName())) {
				info.setEngineMonitor(engineMonitor);
			}
		}
	}
	
	public void initProgressMeter() {
		this.itemsProgressInfo = new ArrayList<ItemProgressInfo>();
		
		for (SyncTableConfiguration tabConf: this.getConfiguration().getTablesConfigurations()) {
			ItemProgressInfo pm = new ItemProgressInfo(tabConf);
			
			if (controller.operationTableIsAlreadyFinished(tabConf)) {
				File syncStatus = controller.generateTableProcessStatusFile(tabConf);
				
				//String fileName = generateTableProcessStatusFile(conf).getAbsolutePath();
				
				OperationStatus op = OperationStatus.loadFromFile(syncStatus);
				
				pm.setProgressMeter(op.parseToProgressMeter());
			}
			
			this.itemsProgressInfo.add(pm);
		}
	}
	
	private SyncConfiguration getConfiguration() {
		return this.controller.getConfiguration();
	}

	public void refreshProgressInfo() {
		for (ItemProgressInfo tabConf: this.itemsProgressInfo) {
			tabConf.tryToReloadProgressMeter();
		}
	}
	
	public ItemProgressInfo retrieveProgressInfo(SyncTableConfiguration tableConfiguration) {
		for (ItemProgressInfo progressInfo : this.itemsProgressInfo) {
			if (progressInfo.getSyncTableName().equals(tableConfiguration.getTableName())) {
				return progressInfo;
			}
		}
		
		return null;
	}
	
	public List<ItemProgressInfo> getItemsProgressInfo() {
		return itemsProgressInfo;
	}
	
	@JsonIgnore
	public String parseToJSON() {
		return utilities.parseToJSON(this);
	}
}
