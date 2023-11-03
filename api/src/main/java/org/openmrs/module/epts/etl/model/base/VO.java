package org.openmrs.module.epts.etl.model.base;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface VO {
	public abstract void load(ResultSet rs) throws SQLException;
	public abstract String generateTableName();
	
	public abstract boolean isExcluded();
	
	public abstract void setExcluded(boolean excluded);
}
