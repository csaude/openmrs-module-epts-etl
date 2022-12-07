package org.openmrs.module.eptssync.dbquickmerge.model;

import org.openmrs.module.eptssync.controller.conf.RefInfo;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;

public class ParentInfo {
	private DatabaseObject parent;
	private RefInfo refInfo;
	
	public ParentInfo(RefInfo refInfo, DatabaseObject parent) {
		this.refInfo = refInfo;
		this.parent = parent;
	}
	
	
	public RefInfo getRefInfo() {
		return refInfo;
	}
	
	public DatabaseObject getParent() {
		return parent;
	}
}
