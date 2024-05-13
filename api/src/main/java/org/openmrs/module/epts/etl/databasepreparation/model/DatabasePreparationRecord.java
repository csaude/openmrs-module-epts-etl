package org.openmrs.module.epts.etl.databasepreparation.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.model.base.EtlObject;

public class DatabasePreparationRecord implements EtlObject{

	private AbstractTableConfiguration tableConfiguration;
	
	public DatabasePreparationRecord(AbstractTableConfiguration tableConfiguration) {
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
