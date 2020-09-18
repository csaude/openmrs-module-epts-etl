package org.openmrs.module.eptssync.model.load;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.controller.load.SyncDataLoadController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.MinimalSyncJSONInfo;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.synchronization.SynchronizationSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class LoadSyncDataSearchParams extends SyncSearchParams<OpenMRSObject> implements FilenameFilter{
	private SyncTableInfo tableInfo;
	private RecordLimits limits;
	
	private String firstFileName;
	private String lastFileName;
	
	public LoadSyncDataSearchParams(SyncTableInfo tableInfo, RecordLimits limits) {
		this.tableInfo = tableInfo;
		this.limits = limits;
	
		if (limits != null) {
			this.firstFileName = tableInfo.getTableName() + "_" + limits.getFirstRecordId() + ".json"; 
			this.lastFileName = tableInfo.getTableName() + "_" + limits.getLastRecordId() + ".json"; 
		}
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
		
		boolean isInInterval = true;
		
		if (hasLimits()) {
			isInInterval = isInInterval && name.compareTo(this.firstFileName) >= 0;
			isInInterval = isInInterval && name.compareTo(this.lastFileName) <= 0;
		}
		
		return  isJSON && isInInterval;
	}
	
	private boolean hasLimits() {
		return this.limits != null;
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		SynchronizationSearchParams syncSearchParams = new SynchronizationSearchParams(tableInfo, null);
		
		int processed = syncSearchParams.countAllRecords(conn);
		int notProcessed = countNotProcessedRecords(conn);
		
		return processed + notProcessed;
	}

	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		LoadSyncDataSearchParams auxSearchParams = new LoadSyncDataSearchParams(tableInfo, null);
		
		try {
			File[] files = getSyncDirectory().listFiles(auxSearchParams);
			
			int notYetProcessed = 0;
			
			for (File file : files) {
				String json = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
				
				notYetProcessed += MinimalSyncJSONInfo.loadFromJSON(json).getQtyRecords();
			}
			
			return notYetProcessed;
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}

	private File getSyncDirectory() {
		return SyncDataLoadController.getSyncDirectory(tableInfo);
	}
}
