package org.openmrs.module.epts.etl.model.pojo.generic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.conf.RefInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.InconsistentStateException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GenericDatabaseObject extends AbstractDatabaseObject {
	
	private SyncTableConfiguration syncTableConfiguration;
	
	public GenericDatabaseObject() {
	}
	
	public void load(ResultSet rs) throws SQLException{ 
		try {
			super.load(rs);
			
			this.objectId =  Oid.fastCreate("", rs.getInt(1));

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
	public String generateTableName() {
		return this.syncTableConfiguration.getTableName();
	}

	@Override
	public void resolveInconsistence(SyncTableConfiguration tableInfo, Connection conn) throws InconsistentStateException, DBException {
		throw new ForbiddenOperationException("Forbidden Method");
	}

	@Override
	public void changeParentValue(RefInfo refInfo, DatabaseObject newParent) {
		throw new ForbiddenOperationException("Forbidden Method");
	}
	
	public static GenericDatabaseObject fastCreate(SyncImportInfoVO syncImportInfo, SyncTableConfiguration syncTableConfiguration) {
		GenericDatabaseObject obj = new GenericDatabaseObject(syncTableConfiguration);
		obj.setObjectId(Oid.fastCreate("", syncImportInfo.getRecordOriginId()));
		
		return obj;
	}

	@Override
	public void setParentToNull(RefInfo refInfo) {
		throw new ForbiddenOperationException("Forbidden Method");
	}
}
