package org.openmrs.module.epts.etl.dbquickmerge.model;

import org.openmrs.module.epts.etl.controller.conf.RefInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;

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
