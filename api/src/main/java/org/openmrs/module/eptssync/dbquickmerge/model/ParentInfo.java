package org.openmrs.module.eptssync.dbquickmerge.model;

import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;

public class ParentInfo {
	private OpenMRSObject parent;
	private RefInfo refInfo;
	
	public ParentInfo(RefInfo refInfo, OpenMRSObject parent) {
		this.refInfo = refInfo;
		this.parent = parent;
	}
	
	
	public RefInfo getRefInfo() {
		return refInfo;
	}
	
	public OpenMRSObject getParent() {
		return parent;
	}
}
