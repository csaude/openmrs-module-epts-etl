package org.openmrs.module.eptssync.load.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.load.controller.SyncDataLoadController;
import org.openmrs.module.eptssync.load.model.LoadSyncDataSearchParams;
import org.openmrs.module.eptssync.load.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class LoadSyncDataEngine extends SyncEngine{
	private File currJSONSourceFile;
	
	/*
	 * The current json info which is being processed
	 */
	private SyncJSONInfo currJSONInfo;
	
	public LoadSyncDataEngine(SyncTableInfo syncTableInfo, RecordLimits limits, SyncDataLoadController syncController) {
		super(syncTableInfo, limits, syncController);
	}

	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> migrationRecords) {
		/*if (this.currJSONInfo.getSyncRecords().hashCode() !=  migrationRecords.hashCode()) {
			throw new ForbiddenOperationException("The migration record source differ from the current migration records");
		}*/
		
		OpenConnection conn = openConnection();
		
		try {
			
			List<SyncImportInfoVO> migrationRecordAsSyncInfo = utilities.parseList(migrationRecords, SyncImportInfoVO.class);
			
			//List<SyncImportInfoVO> syncImportInfo = SyncImportInfoVO.generateFromSyncRecord(migrationRecordAsOpenMRSObjects);
		
			this.syncController.logInfo("WRITING  '"+migrationRecords.size() + "' " + getSyncTableInfo().getTableName() + " TO STAGING TABLE");
			
			SyncImportInfoDAO.insertAll(migrationRecordAsSyncInfo, getSyncTableInfo(), conn);
			
			this.syncController.logInfo("'"+migrationRecords.size() + "' " + getSyncTableInfo().getTableName() + " WROTE TO STAGING TABLE");
			
			this.syncController.logInfo("MOVING SOURCE JSON ["+this.currJSONSourceFile.getAbsolutePath()+"] TO BACKUP AREA.");
			
			moveSoureJSONFileToBackup();
			
			this.syncController.logInfo("SOURCE JSON ["+this.currJSONSourceFile.getAbsolutePath()+"] MOVED TO BACKUP AREA.");
			
			conn.markAsSuccessifullyTerminected();
		} catch (DBException e) {
			
			getSyncController().logInfo("Error performing "+ currJSONInfo.getFileName());
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
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
	protected List<SyncRecord> searchNextRecords() {
		/*if (tmpPrintFiles()) {
			return null;
		}*/
		
		this.currJSONSourceFile = getNextJSONFileToLoad();
		
		if (this.currJSONSourceFile == null) return null;
		
		try {
			String json = new String(Files.readAllBytes(Paths.get(currJSONSourceFile.getAbsolutePath())));
			
			this.currJSONInfo = SyncJSONInfo.loadFromJSON(json);
			this.currJSONInfo.setFileName(currJSONSourceFile.getAbsolutePath());
			
			return utilities.parseList(this.currJSONInfo.getSyncInfo(), SyncRecord.class);
			
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	boolean printed; 
	boolean tmpPrintFiles() {
		if (printed) return printed;
		
		File[] files = getSyncDirectory().listFiles(this.getSearchParams());
	    
		System.out.println("---------------------------------------------------------------------------------------------------------------------");
		
		for (File f :files) {
			System.out.println(this.hashCode()+ ">" + f.getName());
		}
		this.printed = true;
		
		return this.printed;
	}
	
    private File getNextJSONFileToLoad(){
    	File[] files = getSyncDirectory().listFiles(this.getSearchParams());
    	
    	if (files != null && files.length >0){
    		return files[0];
    	}
    	
    	return null;
    }
    
	@Override
	public LoadSyncDataSearchParams getSearchParams() {
		return (LoadSyncDataSearchParams) super.getSearchParams();
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits) {
		SyncSearchParams<? extends SyncRecord> searchParams = new LoadSyncDataSearchParams(this.syncTableInfo, limits);
		searchParams.setQtdRecordPerSelected(2500);
		
		return searchParams;
	}
    
    private File getSyncBkpDirectory() throws IOException {
     	return SyncDataLoadController.getSyncBkpDirectory(getSyncTableInfo());
    }
    
    @Override
    public SyncDataLoadController getSyncController() {
    	return (SyncDataLoadController) super.getSyncController();
    }
    
    private File getSyncDirectory() {
    	return SyncDataLoadController.getSyncDirectory(getSyncTableInfo());
    }

	@Override
	public void requestStop() {
		// TODO Auto-generated method stub
		
	}
}
