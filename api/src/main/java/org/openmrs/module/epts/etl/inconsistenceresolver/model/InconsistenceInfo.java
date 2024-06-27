package org.openmrs.module.epts.etl.inconsistenceresolver.model;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.BaseVO;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class InconsistenceInfo extends BaseVO {
	
	private Integer id;
	
	private String tableName;
	
	private Oid recordId;
	
	private String parentTableName;
	
	private Object parentId;
	
	private Object defaultParentId;
	
	private String recordOriginLocationCode;
	
	public InconsistenceInfo() {
	}
	
	public InconsistenceInfo(String tableName, Oid recordId, String parentTableName, Object parentId, Object defaultParentId,
	    String recordOriginLocationCode) {
		this.tableName = tableName;
		this.recordId = recordId;
		this.parentTableName = parentTableName;
		this.recordOriginLocationCode = recordOriginLocationCode;
		this.parentId = parentId;
		this.defaultParentId = defaultParentId;
	}
	
	public boolean isResolved() {
		return this.getDefaultParentId() != null;
	}
	
	public String getRecordOriginLocationCode() {
		return recordOriginLocationCode;
	}
	
	public void setRecordOriginLocationCode(String recordOriginLocationCode) {
		this.recordOriginLocationCode = recordOriginLocationCode;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public Oid getRecordId() {
		return recordId;
	}
	
	public void setRecordId(Oid recordId) {
		this.recordId = recordId;
	}
	
	public String getParentTableName() {
		return parentTableName;
	}
	
	public void setParentTableName(String parentTableName) {
		this.parentTableName = parentTableName;
	}
	
	public Object getParentId() {
		return parentId;
	}
	
	public void setParentId(Object parentId) {
		this.parentId = parentId;
	}
	
	public Object getDefaultParentId() {
		return defaultParentId;
	}
	
	public void setDefaultParentId(Object defaultParentId) {
		this.defaultParentId = defaultParentId;
	}
	
	public static InconsistenceInfo generate(EtlDatabaseObject record, EtlDatabaseObject parent, Object defaultParentId,
	        String recordOriginLocationCode) {
		InconsistenceInfo info = new InconsistenceInfo(record.generateTableName(), record.getObjectId(),
		        parent.generateTableName(), parent.getObjectId().getSimpleValueAsInt(), defaultParentId,
		        recordOriginLocationCode);
		
		return info;
	}
	
	public static InconsistenceInfo generate(String tableName, Oid recordId, String parentTableName, Object parentId,
	        Object defaultParentId, String recordOriginLocationCode) {
		InconsistenceInfo info = new InconsistenceInfo(tableName, recordId, parentTableName, parentId, defaultParentId,
		        recordOriginLocationCode);
		
		return info;
	}
	
	public void save(TableConfiguration tableConfiguration, Connection conn) throws DBException {
		InconsistenceInfoDAO.insert(this, tableConfiguration, conn);
	}
	
	@Override
	public void setFieldValue(String fieldName, Object value) {
		// TODO Auto-generated method stub
		
	}
}
