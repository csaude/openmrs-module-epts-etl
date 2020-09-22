package org.openmrs.module.eptssync.transport.model;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.load.controller.SyncDataLoadController;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class TransportSyncFilesSearchParams extends SyncSearchParams<OpenMRSObject> implements FilenameFilter{
	private SyncTableInfo tableInfo;
	
	public TransportSyncFilesSearchParams(SyncTableInfo tableInfo) {
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
		boolean isJSON = name.toLowerCase().endsWith("json");
		boolean isNotMinimal = !name.toLowerCase().contains("minimal");
		
		return isJSON && isNotMinimal;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return countNotProcessedRecords(conn);
	}

	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		File[] files = getSyncDirectory().listFiles(this);
		
		if (files != null) return files.length;
		
		return 0;
	}

	private File getSyncDirectory() {
		return SyncDataLoadController.getSyncDirectory(tableInfo);
	}
}
