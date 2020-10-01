package org.openmrs.module.eptssync.transport.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.transport.engine.TransportSyncFilesEngine;
import org.openmrs.module.eptssync.transport.model.TransportSyncFilesSearchParams;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * This class is responsible for control the transpor of sync files from origin to destination site
 * 
 * @author jpboane
 *
 */
public class SyncTransportController extends AbstractSyncController {
	
	public SyncTransportController() {
		super();
	}

	@Override
	public SyncEngine initRelatedEngine(SyncTableInfo syncInfo, RecordLimits limits) {
		return new TransportSyncFilesEngine(syncInfo, limits, this);
	}

	@Override
	protected long getMinRecordId(SyncTableInfo tableInfo) {
		File[] files = getSyncDirectory(tableInfo).listFiles(new TransportSyncFilesSearchParams(tableInfo));
	    
		if (files == null || files.length == 0) return 0;
		
		Arrays.sort(files);
		
		File firstFile = files[0];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_SEQNAME.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(firstFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length -1]);
	}

	@Override
	protected long getMaxRecordId(SyncTableInfo tableInfo) {
		File[] files = getSyncDirectory(tableInfo).listFiles(new TransportSyncFilesSearchParams(tableInfo));
	    
		if (files == null || files.length == 0) return 0;
		
		Arrays.sort(files);
		
		File firstFile = files[files.length -1];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_SEQNAME.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(firstFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length -1]);
	}
	
    public static File getSyncDirectory(SyncTableInfo syncInfo) {
    	String fileName = "";

		fileName += syncInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += syncInfo.getTableName();
 
		return new File(fileName);
    }
    
    public static File getSyncBkpDirectory(SyncTableInfo syncInfo) throws IOException {
     	String fileName = "";

		fileName += syncInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
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
    
    public static File getSyncDestinationDirectory(SyncTableInfo syncInfo) throws IOException {
     	String fileName = "";

		fileName += syncInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import";
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
		return true;
	}

	@Override
	public String getOperationType() {
		return SyncOperationConfig.SYNC_OPERATION_TRANSPORT;
	}	
}
