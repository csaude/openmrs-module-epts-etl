package org.openmrs.module.eptssync.merge.model;

import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.RefInfo;

public class ParentInfo {
	private SyncImportInfoVO parentStageInfo;
	private RefInfo refInfo;
	
	public ParentInfo(RefInfo refInfo, SyncImportInfoVO parentStageInfo) {
		this.refInfo = refInfo;
		this.parentStageInfo = parentStageInfo;
	}
	
	
	public RefInfo getRefInfo() {
		return refInfo;
	}
	
	public SyncImportInfoVO getParentStageInfo() {
		return parentStageInfo;
	}
}
