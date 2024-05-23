package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;

public class MissingParentException extends EtlException {
	
	private static final long serialVersionUID = -2435762700151634050L;
	
	private Integer parentId;
	
	private String parentTable;
	
	private String originAppLocationConde;
	
	private ParentTable refInfo;
	
	private EtlDatabaseObject etlObject;
	
	public MissingParentException() {
		super("On or more parents are missing");
	}
	
	public MissingParentException(EtlDatabaseObject etlObject, Integer parentId, String parentTable,
	    String originAppLocationConde, ParentTable refInfo) {
		super("Missing Parent of record " + etlObject + "!!! Parent [table: " + parentTable + ", id: " + parentId + ", from:"
		        + originAppLocationConde + "]");
		
		this.parentId = parentId;
		this.parentTable = parentTable;
		this.originAppLocationConde = originAppLocationConde;
		this.refInfo = refInfo;
		this.etlObject = etlObject;
	}
	
	public EtlDatabaseObject getEtlObject() {
		return etlObject;
	}
	
	public MissingParentException(EtlDatabaseObject etlObject, Oid parentId, String parentTable,
	    String originAppLocationConde, ParentTable refInfo) {
		super("Missing parent of record " + etlObject + "!!! Parent: [table: " + parentTable + ", id: " + parentId
		        + ", from:" + originAppLocationConde + "]");
		
		this.parentTable = parentTable;
		this.originAppLocationConde = originAppLocationConde;
		this.refInfo = refInfo;
		this.etlObject = etlObject;
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
	
	public ParentTable getRefInfo() {
		return refInfo;
	}
	
	public void setRefInfo(ParentTableImpl refInfo) {
		this.refInfo = refInfo;
	}
	
	public MissingParentException(String msg) {
		super(msg);
	}
}
