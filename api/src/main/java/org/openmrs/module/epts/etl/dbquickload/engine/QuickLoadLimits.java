package org.openmrs.module.epts.etl.dbquickload.engine;

import org.openmrs.module.epts.etl.dbquickload.model.DBQuickLoadSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class QuickLoadLimits extends ThreadRecordIntervalsManager <EtlDatabaseObject>{
	
	private DBQuickLoadSearchParams searchParams;
	
	public QuickLoadLimits() {
	}
	
	@Override
	public boolean canGoNext() {
		try {
			return searchParams.countNotProcessedRecords(null) > 0;
		}
		catch (DBException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public synchronized void moveNext() {
	}
	
	@JsonIgnore
	public DBQuickLoadSearchParams getRelatedSearchParams() {
		return searchParams;
	}
	
	public void setRelatedSearchParams(DBQuickLoadSearchParams searchParams) {
		this.searchParams = searchParams;
	}
}
