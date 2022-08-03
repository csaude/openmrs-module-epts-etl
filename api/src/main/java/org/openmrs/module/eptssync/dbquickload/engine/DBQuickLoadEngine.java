package org.openmrs.module.eptssync.dbquickload.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.eptssync.dbquickload.model.DBQuickLoadSearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class DBQuickLoadEngine extends Engine{
	private File currJSONSourceFile;
	
	/*
	 * The current json info which is being processed
	 */
	private SyncJSONInfo currJSONInfo;
	
	
	public DBQuickLoadEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> migrationRecords, Connection conn) throws DBException {
		List<SyncImportInfoVO> migrationRecordAsSyncInfo = utilities.parseList(migrationRecords, SyncImportInfoVO.class);
		
		for (SyncImportInfoVO rec : migrationRecordAsSyncInfo) rec.setConsistent(1);
		
		this.logInfo("WRITING  '"+migrationRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " TO STAGING TABLE");
		
		SyncImportInfoDAO.insertAll(migrationRecordAsSyncInfo, getSyncTableConfiguration(), conn);
		
		this.logInfo("'"+migrationRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " WROTE TO STAGING TABLE");
		
		this.logDebug("MOVING SOURCE JSON ["+this.currJSONSourceFile.getAbsolutePath()+"] TO BACKUP AREA.");
		
		BaseDAO.commit(conn);
		
		moveSoureJSONFileToBackup();
		
		logDebug("SOURCE JSON ["+this.currJSONSourceFile.getAbsolutePath()+"] MOVED TO BACKUP AREA.");
	}

	private void moveSoureJSONFileToBackup() {
		try {
			
			String pathToBkpFile = "";
			
			pathToBkpFile += getSyncBkpDirectory().getAbsolutePath();
			pathToBkpFile += FileUtilities.getPathSeparator();
			pathToBkpFile +=  FileUtilities.generateFileNameFromRealPath(this.currJSONSourceFile.getAbsolutePath());
			
			FileUtilities.renameTo(this.currJSONSourceFile.getAbsolutePath(), pathToBkpFile);
			
			//NOW, MOVE MINIMAL FILE
			
			String[] parts = this.currJSONSourceFile.getAbsolutePath().split(".json");
			String minimalFile = parts[0] + "_minimal.json";
			
			String pathToBkpMinimalFile = "";
			pathToBkpMinimalFile += getSyncBkpDirectory().getAbsolutePath();
			pathToBkpMinimalFile += FileUtilities.getPathSeparator();
			pathToBkpMinimalFile +=  FileUtilities.generateFileNameFromRealPath(minimalFile);
			
			FileUtilities.renameTo(minimalFile, pathToBkpMinimalFile);
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) {
		this.currJSONSourceFile = getNextJSONFileToLoad();
		
		if (this.currJSONSourceFile == null) return null;
		
		getRelatedOperationController().logInfo("Loading content on JSON File "+ this.currJSONSourceFile.getAbsolutePath());
		
		try {
			String json = new String(Files.readAllBytes(Paths.get(currJSONSourceFile.getAbsolutePath())));
			
			this.currJSONInfo = SyncJSONInfo.loadFromJSON(json);
			this.currJSONInfo.setFileName(currJSONSourceFile.getAbsolutePath());
			
			return utilities.parseList(this.currJSONInfo.getSyncInfo(), SyncRecord.class);
			
		} catch (Exception e) {
			getRelatedOperationController().logInfo("Error performing "+ this.currJSONSourceFile.getAbsolutePath());
			
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
    private File getNextJSONFileToLoad(){
    	File[] files = getSyncDirectory().listFiles(this.getSearchParams());
    	
    	if (files != null && files.length >0){
    		return files[0];
    	}
    	
    	return null;
    }
    
	@Override
	public DBQuickLoadSearchParams getSearchParams() {
		return (DBQuickLoadSearchParams) super.getSearchParams();
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new  DBQuickLoadSearchParams(getRelatedOperationController(), this.getSyncTableConfiguration(), limits);
		searchParams.setQtdRecordPerSelected(2500);
		
		return searchParams;
	}
    
    private File getSyncBkpDirectory() throws IOException {
    	String baseDirectory = getRelatedOperationController().getSyncBkpDirectory(getSyncTableConfiguration()).getAbsolutePath();
    	
    	return new File(baseDirectory);
    }
    
    @Override
    public DBQuickLoadController getRelatedOperationController() {
    	return (DBQuickLoadController) super.getRelatedOperationController();
    }
    
    private File getSyncDirectory() {
    	String baseDirectory = getRelatedOperationController().getSyncDirectory(getSyncTableConfiguration()).getAbsolutePath();
    	
    	return new File(baseDirectory);
    }

	@Override
	public void requestStop() {
	}
}
