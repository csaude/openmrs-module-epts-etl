package org.openmrs.module.epts.etl.merge.model;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.ParentTable;

public class ParentInfo {
	private SyncImportInfoVO parentStageInfo;
	private ParentTable refInfo;
	
	public ParentInfo(ParentTable refInfo, SyncImportInfoVO parentStageInfo) {
		this.refInfo = refInfo;
		this.parentStageInfo = parentStageInfo;
	}
	
	
	public ParentTable getRefInfo() {
		return refInfo;
	}
	
	public SyncImportInfoVO getParentStageInfo() {
		return parentStageInfo;
	}
}
