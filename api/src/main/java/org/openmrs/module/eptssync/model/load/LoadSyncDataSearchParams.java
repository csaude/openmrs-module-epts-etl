package org.openmrs.module.eptssync.model.load;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.openmrs.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class LoadSyncDataSearchParams extends SyncSearchParams<OpenMRSObject> implements FilenameFilter{
	private SyncTableInfo tableInfo;
	
	public LoadSyncDataSearchParams(SyncTableInfo tableInfo) {
		this.tableInfo = tableInfo;
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		return null;
	}	
	
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		return this.tableInfo.getRecordClass();
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith("json");
	}
}
