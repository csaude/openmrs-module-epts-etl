package org.openmrs.module.eptssync.engine.load;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.controller.load.SyncDataLoadController;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.load.LoadSyncDataSearchParams;
import org.openmrs.module.eptssync.model.load.SyncImportInfoDAO;
import org.openmrs.module.eptssync.model.load.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.openmrs.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class LoadSyncDataEngine extends SyncEngine{
	private LoadSyncDataSearchParams searchParams;
	private File currJSONSourceFile;
	
	/*
	 * The current json info which is being processed
	 */
	private SyncJSONInfo currJSONInfo;
	
	public LoadSyncDataEngine(SyncTableInfo syncTableInfo, SyncDataLoadController syncController) {
		super(syncTableInfo, syncController);
		
		this.searchParams = new LoadSyncDataSearchParams(syncTableInfo);
	}

	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> migrationRecords) {
		if (this.currJSONInfo.getSyncRecords().hashCode() !=  migrationRecords.hashCode()) {
			throw new ForbiddenOperationException("The migration record source differ from the current migration records");
		}
		
		OpenConnection conn = DBConnectionService.getInstance().openConnection();
		
		try {
			
			List<OpenMRSObject> migrationRecordAsOpenMRSObjects = utilities.parseList(migrationRecords, OpenMRSObject.class);
			
			List<SyncImportInfoVO> syncImportInfo = SyncImportInfoVO.generateFromSyncRecord(migrationRecordAsOpenMRSObjects);
			
			SyncImportInfoDAO.insertAll(syncImportInfo, getSyncTableInfo(), conn);
			
			moveSoureJSONFileToBackup();

			conn.markAsSuccessifullyTerminected();
		} catch (DBException e) {
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
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}

	
	@Override
	protected List<SyncRecord> searchNextRecords() {
		this.currJSONSourceFile = getNextJSONFileToLoad();
		
		if (this.currJSONSourceFile == null) return null;
		
		try {
			String json = new String(Files.readAllBytes(Paths.get(currJSONSourceFile.getAbsolutePath())));
			
			this.currJSONInfo = SyncJSONInfo.loadFromJSON(json);
			
			return utilities.parseList(this.currJSONInfo.getSyncRecords(), SyncRecord.class);
			
		} catch (Exception e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
    private File getNextJSONFileToLoad(){
    	File[] files = getSyncDirectory().listFiles(this.searchParams);
    	
    	if (files != null && files.length >0){
    		return files[0];
    	}
    	
    	return null;
    }
    
    private File getSyncBkpDirectory() throws IOException {
     	String fileName = "";

		fileName += this.getSyncTableInfo().getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import_bkp";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += this.getSyncTableInfo().getTableName();
 
		File bkpDirectory = new File(fileName);
    	
		
		if (!bkpDirectory.exists()) {
			FileUtilities.tryToCreateDirectoryStructure(bkpDirectory.getAbsolutePath());
		}
		
		return bkpDirectory;
    }
    
    private File getSyncDirectory() {
    	String fileName = "";

		fileName += this.getSyncTableInfo().getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += this.getSyncTableInfo().getTableName();
 
		return new File(fileName);
    }
}
