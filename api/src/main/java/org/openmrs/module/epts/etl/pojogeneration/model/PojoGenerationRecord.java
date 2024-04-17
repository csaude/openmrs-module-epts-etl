package org.openmrs.module.epts.etl.pojogeneration.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.model.base.SyncRecord;

public class PojoGenerationRecord implements SyncRecord{

	private AbstractTableConfiguration tableConfiguration;
	
	public PojoGenerationRecord(AbstractTableConfiguration tableConfiguration) {
		this.tableConfiguration = tableConfiguration;
	}
	
	public AbstractTableConfiguration getTableConfiguration() {
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
