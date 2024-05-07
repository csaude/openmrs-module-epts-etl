package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.conf.RefInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;

public class MissingParentException extends EtlException {
	private static final long serialVersionUID = -2435762700151634050L;
	
	private Integer parentId;
	private String parentTable;
	private String originAppLocationConde;
	private RefInfo refInfo;
	
	public MissingParentException() {
		super("On or more parents are missing");
	}
	
	public MissingParentException(Integer parentId, String parentTable, String originAppLocationConde, RefInfo refInfo) {
		super("Missing parent! Parent: [table: " + parentTable +", id: " + parentId + ", from:" +originAppLocationConde +  "]");
		
		this.parentId = parentId;
		this.parentTable = parentTable;
		this.originAppLocationConde = originAppLocationConde;
		this.refInfo = refInfo;
	}
	
	
	public MissingParentException(Oid parentId, String parentTable, String originAppLocationConde, RefInfo refInfo) {
		super("Missing parent! Parent: [table: " + parentTable +", id: " + parentId + ", from:" +originAppLocationConde +  "]");
		
		this.parentTable = parentTable;
		this.originAppLocationConde = originAppLocationConde;
		this.refInfo = refInfo;
	}
	
	public Integer getParentId() {
		return parentId;
	}
	
	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}
	
	public String getParentTable() {
		return parentTable;
	}
	
	public void setParentTable(String parentTable) {
		this.parentTable = parentTable;
	}
	
	public String getOriginAppLocationConde() {
		return originAppLocationConde;
	}
	
	public void setOriginAppLocationConde(String originAppLocationConde) {
		this.originAppLocationConde = originAppLocationConde;
	}
	
	public RefInfo getRefInfo() {
		return refInfo;
	}
	
	public void setRefInfo(RefInfo refInfo) {
		this.refInfo = refInfo;
	}

	public MissingParentException(String msg) {
		super(msg);
	}	
}
