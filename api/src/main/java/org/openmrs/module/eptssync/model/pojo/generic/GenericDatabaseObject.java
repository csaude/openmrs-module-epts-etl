package org.openmrs.module.eptssync.model.pojo.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.InconsistentStateException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenericDatabaseObject extends AbstractDatabaseObject {
	private Integer objectId;
	
	private SyncTableConfiguration syncTableConfiguration;
	
	public GenericDatabaseObject() {
	}
	
	public GenericDatabaseObject(Integer objectId) {
		this.objectId = objectId;
	}
	
	public void load(ResultSet rs) throws SQLException{ 
		try {
			super.load(rs);
			
			this.objectId = rs.getInt(1);
			
		} catch (SQLException e) {}
	}
	
	public GenericDatabaseObject(SyncTableConfiguration syncTableConfiguration) {
		this.syncTableConfiguration = syncTableConfiguration;
	}
	
	public void setSyncTableConfiguration(SyncTableConfiguration syncTableConfiguration) {
		this.syncTableConfiguration = syncTableConfiguration;
	}
	
	@Override
	@JsonIgnore
	public String generateDBPrimaryKeyAtt() {
		return this.syncTableConfiguration.getPrimaryKey();
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithoutObjectId() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	@JsonIgnore
	public String getInsertSQLWithoutObjectId() {
		throw new ForbiddenOperationException("Forbidden Method");
	}
	
	@Override
	@JsonIgnore
	public Object[] getInsertParamsWithObjectId() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	@JsonIgnore
	public String getInsertSQLWithObjectId() {
		throw new ForbiddenOperationException("Forbidden Method");
	}


	@Override
	@JsonIgnore
	public String getUpdateSQL() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	@JsonIgnore
	public Object[] getUpdateParams() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	@JsonIgnore
	public String generateInsertValues() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public String getUuid() {
		return this.uuid;
	}

	@Override
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@Override
	@JsonIgnore
	public boolean hasParents() {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public Integer getParentValue(String parentAttName) {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public Integer getObjectId() {
		return this.objectId;
	}

	@Override
	public void setObjectId(Integer objectId) {
		this.objectId = objectId;
	}
	
	@Override
	public String generateTableName() {
		return this.syncTableConfiguration.getTableName();
	}

	@Override
	public void resolveInconsistence(SyncTableConfiguration tableInfo, Connection conn) throws InconsistentStateException, DBException {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {
		throw new ForbiddenOperationException("Forbidden Method");
	}
	
	public static GenericDatabaseObject fastCreate(SyncImportInfoVO syncImportInfo, SyncTableConfiguration syncTableConfiguration) {
		GenericDatabaseObject obj = new GenericDatabaseObject(syncTableConfiguration);
		obj.setObjectId(syncImportInfo.getRecordOriginId());
		obj.setUuid(syncImportInfo.getRecordUuid());
		
		return obj;
	}

	@Override
	public void setParentToNull(String parentAttName) {
		throw new ForbiddenOperationException("Forbidden Method");
	}
}
