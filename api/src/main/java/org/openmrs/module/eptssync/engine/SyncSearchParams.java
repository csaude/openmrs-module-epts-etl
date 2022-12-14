package org.openmrs.module.eptssync.engine;

import java.sql.Connection;
import java.util.Date;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.AbstractSearchParams;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.CommonUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public abstract class SyncSearchParams<T extends SyncRecord> extends AbstractSearchParams <T> {
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Date syncStartDate;
	
	protected RecordLimits limits;
	protected SyncTableConfiguration tableInfo;
	
	public SyncSearchParams(SyncTableConfiguration tableInfo, RecordLimits limits) {
		this.tableInfo = tableInfo;
		this.limits = limits;
	}

	
	public Date getSyncStartDate() {
		return syncStartDate;
	}
	
	public void setSyncStartDate(Date syncStartDate) {
		this.syncStartDate = syncStartDate;
	}
	
	public SyncTableConfiguration getTableInfo() {
		return tableInfo;
	}
	
	public void setTableInfo(SyncTableConfiguration tableInfo) {
		this.tableInfo = tableInfo;
	}
	
	public RecordLimits getLimits() {
		return limits;
	}
	
	public void setLimits(RecordLimits limits) {
		this.limits = limits;
	}
	
	protected boolean hasLimits() {
		return this.limits != null;
	}
	
	public abstract int countAllRecords(Connection conn) throws DBException;
	public abstract int countNotProcessedRecords(Connection conn) throws DBException;
}
