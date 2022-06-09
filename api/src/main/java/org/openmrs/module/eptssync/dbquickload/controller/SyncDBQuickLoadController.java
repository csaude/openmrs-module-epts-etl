package org.openmrs.module.eptssync.dbquickload.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openmrs.module.eptssync.controller.DestinationOperationController;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickload.engine.SyncDBQuickLoadEngine;
import org.openmrs.module.eptssync.dbquickload.model.DBQuickLoadSearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * This class is responsible for control the loading of sync data to stage area.
 * <p>
 * This load consist on readding the JSON content from the sync directory and load them to temp tables on sync stage.
 * 
 * @author jpboane
 *
 */
public class SyncDBQuickLoadController extends OperationController implements DestinationOperationController{
	private String appOriginLocationCode;

	public SyncDBQuickLoadController(ProcessController processController, SyncOperationConfig operationConfig, String appOriginLocationCode) {
		super(processController, operationConfig);
		
		this.appOriginLocationCode = appOriginLocationCode;
		
		this.controllerId = processController.getControllerId() + "_" + getOperationType() + "_from_" + appOriginLocationCode;	
		
		this.progressInfo = this.processController.initOperationProgressMeter(this);
	}
	
	@Override
	public String getAppOriginLocationCode() {
		return appOriginLocationCode;
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new SyncDBQuickLoadEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		DBQuickLoadSearchParams searchParams = new DBQuickLoadSearchParams(this, tableInfo, null);
		
		File[] files = getSyncDirectory(tableInfo).listFiles(searchParams);
	    
		if (files == null || files.length == 0) return 0;
		
		Arrays.sort(files);
		
		File firstFile = files[0];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_FIRSTRECORDID_LASTRECORDID.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(firstFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 2]);
	}

	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		DBQuickLoadSearchParams searchParams = new DBQuickLoadSearchParams(this, tableInfo, null);
		
		File[] files = getSyncDirectory(tableInfo).listFiles(searchParams);
	    
		if (files == null || files.length == 0) return 0;
		
		Arrays.sort(files);
		
		File lastFile = files[files.length -1];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_FIRSTRECORDID_LASTRECORDID.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(lastFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 1]);
	}
	
    public File getSyncDirectory(SyncTableConfiguration syncInfo) {
    	String fileName = "";

		fileName += syncInfo.getRelatedSynconfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += this.appOriginLocationCode;
		fileName += FileUtilities.getPathSeparator();
		
		fileName += syncInfo.getTableName();
 
		return new File(fileName);
    }
    
    public File getSyncBkpDirectory(SyncTableConfiguration syncInfo) throws IOException {
     	String fileName = "";

		fileName += syncInfo.getRelatedSynconfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import_bkp";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += this.appOriginLocationCode;
		fileName += FileUtilities.getPathSeparator();
		
		fileName += syncInfo.getTableName();
 
		File bkpDirectory = new File(fileName);
    	
		if (!bkpDirectory.exists()) {
			FileUtilities.tryToCreateDirectoryStructure(bkpDirectory.getAbsolutePath());
		}
		
		return bkpDirectory;
    }

	@Override
	public boolean mustRestartInTheEnd() {
		return hasNestedController() ? false : true;
	}

	@Override
	public String getOperationType() {
		return SyncOperationConfig.SYNC_OPERATION_LOAD;
	}
}
