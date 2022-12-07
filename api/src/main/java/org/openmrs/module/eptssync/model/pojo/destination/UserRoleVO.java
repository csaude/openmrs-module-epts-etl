package org.openmrs.module.eptssync.model.pojo.destination;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserRoleVO extends AbstractDatabaseObject {
	
	private Integer userId;
	private String role;
	
	public UserRoleVO() {
	}
	
	public UserRoleVO(Integer userId, String role) {
		this.userId = userId;
		this.role = role;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public String getRole() {
		return role;
	}
	
	public void setRole(String role) {
		this.role = role;
	}
	
	@JsonIgnore
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO user_role(user_id, role) VALUES( ?, ?);";
	}
	
	@JsonIgnore
	public Object[] getInsertParamsWithoutObjectId() {
		Object[] params = { this.userId, this.role };
		return params;
	}
	
	@JsonIgnore
	public String getInsertSQLWithObjectId() {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@JsonIgnore
	public Object[] getInsertParamsWithObjectId() {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@JsonIgnore
	public Object[] getUpdateParams() {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@JsonIgnore
	public String getUpdateSQL() {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@JsonIgnore
	public String generateInsertValues() {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@Override
	public String generateDBPrimaryKeyAtt() {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@Override
	public Integer getObjectId() {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@Override
	public void setObjectId(Integer objectId) {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@Override
	public boolean hasParents() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Integer getParentValue(String parentAttName) {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@Override
	public void changeParentValue(String parentAttName, DatabaseObject newParent) {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@Override
	public void setParentToNull(String parentAttName) {
		throw new ForbiddenOperationException("Forbidden");
	}
	
	@Override
	public void save(SyncTableConfiguration tableConfiguration, Connection conn) throws DBException {
		DatabaseObjectDAO.insert(this, conn);
	}
}
