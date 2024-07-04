package org.openmrs.module.epts.etl.merge.model;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;

public class ParentInfo {
	private EtlStageRecordVO parentStageInfo;
	private ParentTable refInfo;
	
	public ParentInfo(ParentTable refInfo, EtlStageRecordVO parentStageInfo) {
		this.refInfo = refInfo;
		this.parentStageInfo = parentStageInfo;
	}
	
	
	public ParentTable getRefInfo() {
		return refInfo;
	}
	
	public EtlStageRecordVO getParentStageInfo() {
		return parentStageInfo;
	}
}
