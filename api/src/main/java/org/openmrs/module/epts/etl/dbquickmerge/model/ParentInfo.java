package org.openmrs.module.epts.etl.dbquickmerge.model;

import org.openmrs.module.epts.etl.conf.ParentTable;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;

public class ParentInfo {
	private DatabaseObject parent;
	private ParentTable parentTableConf;
	
	public ParentInfo(ParentTable refInfo, DatabaseObject parent) {
		this.parentTableConf = refInfo;
		this.parent = parent;
	}
	
	
	public ParentTable getParentTableConf() {
		return parentTableConf;
	}
	
	public DatabaseObject getParent() {
		return parent;
	}
}
