package org.openmrs.module.eptssync.inconsistenceresolver.model;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.base.BaseVO;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class InconsistenceInfo extends BaseVO{
	private int id;
	private String tableName;
	private int recordId;
	private String parentTableName;
	private int parentId;
	private int defaultParentId;
	private String recordOriginLocationCode;
	
	public InconsistenceInfo() {
	}
	
	
	public InconsistenceInfo(String tableName, int recordId, String parentTableName, int parentId, int defaultParentId, String recordOriginLocationCode) {
		this.tableName = tableName;
		this.recordId = recordId;
		this.parentTableName = parentTableName;
		this.recordOriginLocationCode = recordOriginLocationCode;
		this.parentId = parentId;
		this.defaultParentId = defaultParentId;
	}

	public String getRecordOriginLocationCode() {
		return recordOriginLocationCode;
	}


	public void setRecordOriginLocationCode(String recordOriginLocationCode) {
		this.recordOriginLocationCode = recordOriginLocationCode;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getRecordId() {
		return recordId;
	}

	public void setRecordId(int recordId) {
		this.recordId = recordId;
	}

	public String getParentTableName() {
		return parentTableName;
	}

	public void setParentTableName(String parentTableName) {
		this.parentTableName = parentTableName;
	}

	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	
	public int getDefaultParentId() {
		return defaultParentId;
	}
	
	public void setDefaultParentId(int defaultParentId) {
		this.defaultParentId = defaultParentId;
	}
	
	public static InconsistenceInfo generate(OpenMRSObject record, OpenMRSObject parent, int defaultParentId, String recordOriginLocationCode) {
		InconsistenceInfo info = new InconsistenceInfo(record.generateTableName(), record.getObjectId(), parent.generateTableName(), parent.getObjectId(), defaultParentId, recordOriginLocationCode);
	
		return info;
	}
	
	public static InconsistenceInfo generate(String tableName, int recordId, String parentTableName, int parentId, int defaultParentId, String recordOriginLocationCode) {
		InconsistenceInfo info = new InconsistenceInfo(tableName, recordId, parentTableName, parentId, defaultParentId, recordOriginLocationCode);
	
		return info;
	}


	public void save(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		InconsistenceInfoDAO.insert(this, tableConfiguration, conn);
	}
}
