package org.openmrs.module.eptssync.model.base;

import java.sql.ResultSet;

public interface VO {
	public abstract void load(ResultSet rs);
	public abstract String generateTableName();
	public abstract int getObjectId();	
	public abstract void setObjectId(int objectId);
}
