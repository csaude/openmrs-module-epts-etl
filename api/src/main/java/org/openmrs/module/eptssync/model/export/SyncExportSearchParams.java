package org.openmrs.module.eptssync.model.export;

import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.OpenMRSObject;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SyncExportSearchParams extends SyncSearchParams<OpenMRSObject>{
	private SyncTableInfo tableInfo;
	
	public SyncExportSearchParams(SyncTableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		SearchClauses<OpenMRSObject> searchClauses = new SearchClauses<OpenMRSObject>(this);
		
		searchClauses.addColumnToSelect("*");
		searchClauses.addToClauseFrom(tableInfo.getTableName());
		searchClauses.addToClauses("date_changed > last_sync_date or last_sync_date is null");
		
		
		if (this.tableInfo.getExtraConditionForExport() != null) {
			searchClauses.addToClauses(tableInfo.getExtraConditionForExport());
		}
		
		return searchClauses;
	}	
	
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		return this.tableInfo.getRecordClass();
	}
}
