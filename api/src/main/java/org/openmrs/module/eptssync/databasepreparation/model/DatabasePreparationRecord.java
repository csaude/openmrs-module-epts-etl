package org.openmrs.module.eptssync.databasepreparation.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.model.base.SyncRecord;

public class DatabasePreparationRecord implements SyncRecord{

	private SyncTableConfiguration tableConfiguration;
	
	public DatabasePreparationRecord(SyncTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}
	
	public SyncTableConfiguration getTableConfiguration() {
		return tableConfiguration;
	}
	
	@Override
	public void load(ResultSet rs) throws SQLException {
	}

	@Override
	public String generateTableName() {
		return null;
	}

	@Override
	public boolean isExcluded() {
		return false;
	}

	@Override
	public void setExcluded(boolean excluded) {
	}

	@Override
	public int getObjectId() {
		return 0;
	}

	@Override
	public void setObjectId(int objectId) {
	}
}
