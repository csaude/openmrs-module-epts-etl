package org.openmrs.module.eptssync.model.base;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface VO {
	public abstract void load(ResultSet rs) throws SQLException;
	public abstract String generateTableName();
	public abstract int getObjectId();	
	public abstract void setObjectId(int objectId);
}
