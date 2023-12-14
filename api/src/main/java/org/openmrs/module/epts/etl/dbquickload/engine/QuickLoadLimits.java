package org.openmrs.module.epts.etl.dbquickload.engine;

import org.openmrs.module.epts.etl.dbquickload.model.DBQuickLoadSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class QuickLoadLimits extends RecordLimits {
	
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
	public synchronized void moveNext(int qtyRecords) {
	}
	
	@JsonIgnore
	public DBQuickLoadSearchParams getRelatedSearchParams() {
		return searchParams;
	}
	
	public void setRelatedSearchParams(DBQuickLoadSearchParams searchParams) {
		this.searchParams = searchParams;
	}
}
