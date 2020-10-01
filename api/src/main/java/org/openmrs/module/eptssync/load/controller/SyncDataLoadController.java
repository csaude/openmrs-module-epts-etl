package org.openmrs.module.eptssync.load.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.load.engine.LoadSyncDataEngine;
import org.openmrs.module.eptssync.load.model.LoadSyncDataSearchParams;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * This class is responsible for control the loading of sync data to stage area.
 * <p>
 * This load concist on readding the JSON content from the sync directory and load them to temp tables on sync stage.
 * 
 * @author jpboane
 *
 */
public class SyncDataLoadController extends AbstractSyncController {
	
	public SyncDataLoadController() {
		super();
	}

	@Override
	public SyncEngine initRelatedEngine(SyncTableInfo syncInfo, RecordLimits limits) {
		return new LoadSyncDataEngine(syncInfo, limits, this);
	}

	@Override
	protected long getMinRecordId(SyncTableInfo tableInfo) {
		LoadSyncDataSearchParams searchParams = new LoadSyncDataSearchParams(tableInfo, null);
		
		File[] files = getSyncDirectory(tableInfo).listFiles(searchParams);
	    
		if (files == null || files.length == 0) return 0;
		
		Arrays.sort(files);
		
		File firstFile = files[0];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_SEQNAME.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(firstFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 1]);
	}

	@Override
	protected long getMaxRecordId(SyncTableInfo tableInfo) {
		LoadSyncDataSearchParams searchParams = new LoadSyncDataSearchParams(tableInfo, null);
		
		File[] files = getSyncDirectory(tableInfo).listFiles(searchParams);
	    
		if (files == null || files.length == 0) return 0;
		
		Arrays.sort(files);
		
		File firstFile = files[files.length -1];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_SEQNAME.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(firstFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 1]);
	}
	
    public static File getSyncDirectory(SyncTableInfo syncInfo) {
    	String fileName = "";

		fileName += syncInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += syncInfo.getTableName();
 
		return new File(fileName);
    }
    
    public static File getSyncBkpDirectory(SyncTableInfo syncInfo) throws IOException {
     	String fileName = "";

		fileName += syncInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import_bkp";
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
		return SyncOperationConfig.SYNC_OPERATION_LOAD;
	}
}
