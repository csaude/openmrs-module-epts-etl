package org.openmrs.module.epts.etl.pojogeneration.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.model.base.SyncRecord;

public class PojoGenerationRecord implements SyncRecord{

	private SyncTableConfiguration tableConfiguration;
	
	public PojoGenerationRecord(SyncTableConfiguration tableConfiguration) {
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
}
