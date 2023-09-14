package org.openmrs.module.eptssync.model.pojo.openmrs._default;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.pojo.generic.AbstractDatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UserRoleVO extends AbstractDatabaseObject {
	
	private Integer userId;
	private Integer role;
	
	public UserRoleVO() {
	}
	
	public UserRoleVO(Integer userId, Integer role) {
		this.userId = userId;
		this.role = role;
	}
	
	public Integer getUserId() {
		return userId;
	}
	
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public Integer getRole() {
		return role;
	}
	
	public void setRole(Integer role) {
		this.role = role;
	}
	
	@JsonIgnore
	public String getInsertSQLWithoutObjectId() {
		return "INSERT INTO users(user_id, role) VALUES( ?, ?);";
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
}
