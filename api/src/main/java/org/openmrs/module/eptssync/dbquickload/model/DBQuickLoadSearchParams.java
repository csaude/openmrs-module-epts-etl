package org.openmrs.module.eptssync.dbquickload.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.Connection;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchClauses;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class DBQuickLoadSearchParams extends SyncSearchParams<OpenMRSObject> implements FilenameFilter{
	
	private String firstFileName;
	private String lastFileName;
	
	private String fileNamePathern;
	
	private DBQuickLoadController controller;
	
	public DBQuickLoadSearchParams(DBQuickLoadController controller, SyncTableConfiguration tableInfo, RecordLimits limits) {
		super(tableInfo, limits);
		
		this.controller = controller;
		if (limits != null) {
			this.firstFileName = tableInfo.getTableName() + "_" + utilities.garantirXCaracterOnNumber(limits.getCurrentFirstRecordId(), 10) + ".json"; 
			this.lastFileName = tableInfo.getTableName() + "_" +  utilities.garantirXCaracterOnNumber(limits.getCurrentLastRecordId(), 10) + ".json"; 
		}
	}
	
	public void setFileNamePathern(String fileNamePathern) {
		this.fileNamePathern = fileNamePathern;
	}
	
	public String getFileNamePathern() {
		return fileNamePathern;
	}
	
	@Override
	public SearchClauses<OpenMRSObject> generateSearchClauses(Connection conn) throws DBException {
		return null;
	}	
	
	@Override
	public Class<OpenMRSObject> getRecordClass() {
		return this.tableInfo.getSyncRecordClass(this.controller.getDefaultApp());
	}
	
	@Override
	public boolean accept(File dir, String name) {
		
		boolean isJSON = name.toLowerCase().endsWith("json");
		boolean isNotMinimal = !name.toLowerCase().contains("minimal");
		
		boolean isInInterval = true;
		
		if (hasLimits()) {
			isInInterval = isInInterval && name.compareTo(this.firstFileName) >= 0;
			isInInterval = isInInterval && name.compareTo(this.lastFileName) <= 0;
		}
		
		boolean pathernOk = true;
		
		if (utilities.stringHasValue(this.fileNamePathern)) {
			pathernOk = name.contains(this.fileNamePathern);
		}
		
		return  isJSON && isNotMinimal && isInInterval && pathernOk;
	}

	@Override
	public int countAllRecords(Connection conn) throws DBException {
		LoadedRecordsSearchParams syncSearchParams = new LoadedRecordsSearchParams(tableInfo, null, controller.getAppOriginLocationCode());
		
		int processed = syncSearchParams.countAllRecords(conn);
		
		int notProcessed = countNotProcessedRecords(conn);
		
		return processed + notProcessed;
	}

	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		try {
			File[] files = getSyncDirectory().listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					boolean isJSON = name.toLowerCase().endsWith("json");
					boolean isMinimal = name.toLowerCase().contains("minimal");
				
					return isJSON && isMinimal;
				}
			});
			
			int notYetProcessed = 0;
			
			if (files == null) return 0;
			
			for (File file : files) {
				try {
					String json = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
					
					notYetProcessed += SyncJSONInfo.loadFromJSON(json).getQtyRecords();
				} catch (NoSuchFileException e) {
				}
			}
			
			return notYetProcessed;
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}

	private File getSyncDirectory() {
		return this.controller.getSyncDirectory(tableInfo);
	}
}
