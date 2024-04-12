package org.openmrs.module.epts.etl.controller.conf;

import java.util.List;

public class TableParent extends SyncTableConfiguration {
	
	private List<RefInfo> refInfo;
	
	public List<RefInfo> getRefInfo() {
		return refInfo;
	}
	
	public void setRefInfo(List<RefInfo> refInfo) {
		this.refInfo = refInfo;
	}
	
}
