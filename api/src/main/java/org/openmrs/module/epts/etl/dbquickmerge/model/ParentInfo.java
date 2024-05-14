package org.openmrs.module.epts.etl.dbquickmerge.model;

import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public class ParentInfo {
	
	private EtlDatabaseObject parentRecordInOrigin;
	
	private ParentTable parentTabConfInDst;
	
	public ParentInfo(ParentTable parentTableConfInDestination, EtlDatabaseObject parentRecordInOrigin) {
		this.parentRecordInOrigin = parentRecordInOrigin;
		this.parentTabConfInDst = parentTableConfInDestination;
	}
	
	public void setParentTableConfInDestination(ParentTable parentTableConfInDestination) {
		this.parentTabConfInDst = parentTableConfInDestination;
	}
	
	public EtlDatabaseObject getParentRecordInOrigin() {
		return parentRecordInOrigin;
	}
	
	public void setParentRecordInOrigin(EtlDatabaseObject parentRecordInOrigin) {
		this.parentRecordInOrigin = parentRecordInOrigin;
	}
	
	@Override
	public String toString() {
		return "parentTable:" + parentTabConfInDst.getTableName() + ", objectId " + parentRecordInOrigin;
	}
	
	public ParentTable getParentTableConfInDst() {
		return this.parentTabConfInDst;
	}
}
