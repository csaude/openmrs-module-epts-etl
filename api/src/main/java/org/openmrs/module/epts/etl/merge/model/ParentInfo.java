package org.openmrs.module.epts.etl.merge.model;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.RefInfo;

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
