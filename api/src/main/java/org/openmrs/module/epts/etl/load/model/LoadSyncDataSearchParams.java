package org.openmrs.module.epts.etl.load.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.load.controller.DataLoadController;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.synchronization.model.DataBaseMergeFromJSONSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class LoadSyncDataSearchParams extends SyncSearchParams<DatabaseObject> implements FilenameFilter {
	
	private String firstFileName;
	
	private String lastFileName;
	
	private String fileNamePathern;
	
	private DataLoadController controller;
	
	public LoadSyncDataSearchParams(DataLoadController controller, EtlConfiguration config, RecordLimits limits) {
		super(config, limits);
		
		this.controller = controller;
		
		if (limits != null) {
			this.firstFileName = getSrcTableConf().getTableName() + "_"
			        + utilities.garantirXCaracterOnNumber(limits.getCurrentFirstRecordId(), 10) + ".json";
			this.lastFileName = getSrcTableConf().getTableName() + "_"
			        + utilities.garantirXCaracterOnNumber(limits.getCurrentLastRecordId(), 10) + ".json";
		}
	}
	
	public void setFileNamePathern(String fileNamePathern) {
		this.fileNamePathern = fileNamePathern;
	}
	
	public String getFileNamePathern() {
		return fileNamePathern;
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		return null;
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
		
		return isJSON && isNotMinimal && isInInterval && pathernOk;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		DataBaseMergeFromJSONSearchParams syncSearchParams = new DataBaseMergeFromJSONSearchParams(getConfig(), null,
		        controller.getAppOriginLocationCode());
		
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
			
			if (files == null)
				return 0;
			
			for (File file : files) {
				try {
					String json = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
					
					notYetProcessed += SyncJSONInfo.loadFromJSON(json).getQtyRecords();
				}
				catch (NoSuchFileException e) {}
			}
			
			return notYetProcessed;
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private File getSyncDirectory() {
		return this.controller.getSyncDirectory(getSrcTableConf());
	}
}
