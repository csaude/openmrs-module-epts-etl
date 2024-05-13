package org.openmrs.module.epts.etl.dbquickmerge.model;

import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public class ParentInfo {
	
	private EtlDatabaseObject parentRecord;
	
	private ParentTable parentTableConf;
	
	public ParentInfo(ParentTable refInfo, EtlDatabaseObject parentRecord) {
		this.parentTableConf = refInfo;
		this.parentRecord = parentRecord;
	}
	
	public ParentTable getParentTableConf() {
		return parentTableConf;
	}
	
	public EtlDatabaseObject getParentRecord() {
		return parentRecord;
	}
	
	@Override
	public String toString() {
		return "parentTable:" + parentTableConf.getTableName() + ", parentId " + parentRecord;
	}
}
