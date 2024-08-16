package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.interfaces.SqlFunctionType;
import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class SqlFunctionInfo {
	
	private TableConfiguration mainTable;
	
	private SqlFunctionType type;
	
	private String aliasName;
	
	private long maxRecordId;
	
	private long minRecordId;
	
	public SqlFunctionInfo() {
		
	}
	
	public SqlFunctionInfo(SqlFunctionType type, String aliasName) {
		this.type = type;
		this.aliasName = aliasName;
	}
	
	public long getMaxRecordId() {
		return maxRecordId;
	}
	
	public void setMaxRecordId(long maxRecordId) {
		this.maxRecordId = maxRecordId;
	}
	
	public long getMinRecordId() {
		return minRecordId;
	}
	
	public void setMinRecordId(long minRecordId) {
		this.minRecordId = minRecordId;
	}
	
	public TableConfiguration getMainTable() {
		return mainTable;
	}
	
	public void setMainTable(TableConfiguration mainTable) {
		this.mainTable = mainTable;
	}
	
	public SqlFunctionType getType() {
		return type;
	}
	
	public void setType(SqlFunctionType type) {
		this.type = type;
	}
	
	public String getAliasName() {
		return aliasName;
	}
	
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
	
	@Override
	public String toString() {
		String toString = this.type.toString();
		
		toString += getAliasName() != null ? ", alias: " + this.getAliasName() : "";
		
		toString += getMainTable() != null ? ", mainTable: " + this.getMainTable().getTableName() : "";
		
		return toString;
	}
	
	public boolean isCountFunction() {
		return this.getType().isCount();
	}
	
	public void detemineLimits(Connection conn) throws DBException {
		if (!this.getMainTable().isFullLoaded()) {
			this.getMainTable().fullLoad(conn);
		}
		
		this.setMinRecordId(this.getMainTable().getMinRecordId(conn));
		this.setMaxRecordId(this.getMainTable().getMaxRecordId(conn));
	}
	
}
