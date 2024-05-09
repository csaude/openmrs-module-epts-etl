package org.openmrs.module.epts.etl.dbquickmerge.model;

import org.openmrs.module.epts.etl.conf.ParentTable;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;

public class ParentInfo {
	
	private DatabaseObject parentRecord;
	
	private ParentTable parentTableConf;
	
	public ParentInfo(ParentTable refInfo, DatabaseObject parentRecord) {
		this.parentTableConf = refInfo;
		this.parentRecord = parentRecord;
	}
	
	public ParentTable getParentTableConf() {
		return parentTableConf;
	}
	
	public DatabaseObject getParentRecord() {
		return parentRecord;
	}
	
	@Override
	public String toString() {
		return "parentTable:" + parentTableConf.getTableName() + ", parentId " + parentRecord;
	}
}
