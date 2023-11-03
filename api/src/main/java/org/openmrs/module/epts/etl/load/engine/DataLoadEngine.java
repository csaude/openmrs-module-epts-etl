package org.openmrs.module.epts.etl.load.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.load.controller.DataLoadController;
import org.openmrs.module.epts.etl.load.model.LoadSyncDataSearchParams;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DataLoadEngine extends Engine{
	private File currJSONSourceFile;
	
	/*
	 * The current json info which is being processed
	 */
	private SyncJSONInfo currJSONInfo;
	
	
	public DataLoadEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> migrationRecords, Connection conn) throws DBException {
		List<SyncImportInfoVO> migrationRecordAsSyncInfo = utilities.parseList(migrationRecords, SyncImportInfoVO.class);
		
		logInfo("WRITING  '"+migrationRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " TO STAGING TABLE");
		
		SyncImportInfoDAO.insertAll(migrationRecordAsSyncInfo, getSyncTableConfiguration(), conn);
		
		logInfo("'"+migrationRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " WROTE TO STAGING TABLE");
		
		logDebug("MOVING SOURCE JSON ["+this.currJSONSourceFile.getAbsolutePath()+"] TO BACKUP AREA.");
		
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
		/*if (tmpPrintFiles()) {
			return null;
		}*/
		
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
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new LoadSyncDataSearchParams(getRelatedOperationController(), this.getSyncTableConfiguration(), limits);
		searchParams.setQtdRecordPerSelected(2500);
		//searchParams.setExtraCondition("obs_2020093011233502.json");
		
		return searchParams;
	}
    
    private File getSyncBkpDirectory() throws IOException {
    	String baseDirectory = getRelatedOperationController().getSyncBkpDirectory(getSyncTableConfiguration()).getAbsolutePath();
    	
    	return new File(baseDirectory);
    }
    
    @Override
    public DataLoadController getRelatedOperationController() {
    	return (DataLoadController) super.getRelatedOperationController();
    }
    
    private File getSyncDirectory() {
    	String baseDirectory = getRelatedOperationController().getSyncDirectory(getSyncTableConfiguration()).getAbsolutePath();
    	
    	return new File(baseDirectory);
    }

	@Override
	public void requestStop() {
		// TODO Auto-generated method stub
		
	}
}
