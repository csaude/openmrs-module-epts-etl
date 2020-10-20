package org.openmrs.module.eptssync.transport.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.monitor.EnginActivityMonitor;
import org.openmrs.module.eptssync.transport.engine.TransportSyncFilesEngine;
import org.openmrs.module.eptssync.transport.model.TransportSyncFilesSearchParams;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * This class is responsible for control the transpor of sync files from origin to destination site
 * 
 * @author jpboane
 *
 */
public class SyncTransportController extends OperationController {
	
	public SyncTransportController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}

	@Override
	public Engine initRelatedEngine(EnginActivityMonitor monitor, RecordLimits limits) {
		return new TransportSyncFilesEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		File[] files = getSyncDirectory(tableInfo).listFiles(new TransportSyncFilesSearchParams(this, tableInfo, null));
	    
		if (files == null || files.length == 0) return 0;
		
		Arrays.sort(files);
		
		File firstFile = files[0];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_MINRECORD_MAXRECORD.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(firstFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length-2]);
	}

	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		File[] files = getSyncDirectory(tableInfo).listFiles(new TransportSyncFilesSearchParams(this, tableInfo, null));
	    
		if (files == null || files.length == 0) return 0;
		
		Arrays.sort(files);
		
		File lastFile = files[files.length -1];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_MINRECORD_MAXRECORD.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(lastFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length -1]);
	}
	
    public File getSyncDirectory(SyncTableConfiguration syncInfo) {
    	String fileName = "";

		fileName += syncInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getRelatedSyncTableInfoSource().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getTableName();
 
		return new File(fileName);
    }
    
    public File getSyncBkpDirectory(SyncTableConfiguration syncInfo) throws IOException {
     	String fileName = "";

		fileName += syncInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getRelatedSyncTableInfoSource().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export_bkp";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += syncInfo.getTableName();
 
		File bkpDirectory = new File(fileName);
    	
		
		if (!bkpDirectory.exists()) {
			FileUtilities.tryToCreateDirectoryStructure(bkpDirectory.getAbsolutePath());
		}
		
		return bkpDirectory;
    }
    
    public File getSyncDestinationDirectory(SyncTableConfiguration syncInfo) throws IOException {
     	String fileName = "";

		fileName += syncInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += "import";
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getRelatedSyncTableInfoSource().getOriginAppLocationCode().toLowerCase();
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
		return SyncOperationConfig.SYNC_OPERATION_TRANSPORT;
	}	
}
