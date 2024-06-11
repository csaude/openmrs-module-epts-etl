package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.conf.interfaces.ParentTable;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class MissingParentException extends DBException {
	
	private static final long serialVersionUID = -2435762700151634050L;
	
	private Integer parentId;
	
	private String parentTable;
	
	private String originAppLocationConde;
	
	private ParentTable refInfo;
	
	private EtlDatabaseObject etlObject;
	
	public MissingParentException(DBException e) {
		super("On or more parents are missing", e);
	}
	
	public MissingParentException(String msg, DBException e) {
		super(msg, e);
	}
	
	public MissingParentException(EtlDatabaseObject etlObject, Integer parentId, String parentTable,
	    String originAppLocationConde, ParentTable refInfo, DBException e) {
		super("Missing Parent of record " + etlObject + "!!! Parent [table: " + parentTable + ", id: " + parentId + ", from:"
		        + originAppLocationConde + "]", e);
		
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
	    String originAppLocationConde, ParentTable refInfo, DBException e) {
		super("Missing parent of record " + etlObject + "!!! Parent: [table: " + parentTable + ", id: " + parentId
		        + ", from:" + originAppLocationConde + "]", e);
		
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
	
	@Override
	public boolean isIntegrityConstraintViolationException() throws DBException {
		return true;
	}
	
	@Override
	public boolean isDuplicatePrimaryOrUniqueKeyException() throws DBException {
		return false;
	}
}
