package org.openmrs.module.eptssync.export.controller;

import java.io.File;
import java.io.IOException;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncConfig;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.export.engine.ExportSyncEngine;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * This class is responsible for control the data export in the synchronization processs
 * 
 * @author jpboane
 *
 */
public class SyncExportController extends AbstractSyncController {
	public SyncExportController(SyncConfig syncConfig) {
		super(syncConfig);
	}

	@Override
	public SyncEngine initRelatedEngine(SyncTableInfo syncInfo, RecordLimits limits) {
		return new ExportSyncEngine(syncInfo, limits, this);
	}

	@Override
	protected long getMinRecordId(SyncTableInfo tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			OpenMRSObject obj = OpenMRSObjectDAO.getFirstRecord(tableInfo, conn);
		
			if (obj != null) return obj.getObjectId();
			
			return 0;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	protected long getMaxRecordId(SyncTableInfo tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			OpenMRSObject obj = OpenMRSObjectDAO.getLastRecord(tableInfo, conn);
		
			if (obj != null) return obj.getObjectId();
			
			return 0;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	public synchronized File generateJSONTempFile(SyncJSONInfo jsonInfo, SyncTableInfo tableInfo, int startRecord, int lastRecord) throws IOException {
		String fileName = "";
		//String fileSufix = "00";
		
		fileName += tableInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getRelatedSyncTableInfoSource().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getTableName();
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getTableName();
		
		fileName += "_" + utilities().garantirXCaracterOnNumber(startRecord, 10);
		fileName += "_" + utilities().garantirXCaracterOnNumber(lastRecord, 10);
	
		if(new File(fileName).exists() || new File(fileName+".json").exists()) {
			throw new ForbiddenOperationException("The file '" + fileName + "' is already exists!!!");
		}
		
		/*
		String fileNameWithoutExtension = fileName; //+ fileSufix;
		
		String fileNameWithExtension = fileNameWithoutExtension + ".json";
		
		if(new File(fileNameWithExtension).exists() || new File(fileNameWithoutExtension).exists()) {
			int count = 1;
			
			fileSufix = utilities().garantirXCaracterOnNumber(count, 2) ;
			
			fileNameWithoutExtension = fileName + fileSufix;
			fileNameWithExtension = fileNameWithoutExtension + ".json";
			
			while(new File(fileNameWithoutExtension).exists() || new File(fileNameWithExtension).exists()) {
				count++;
				
				fileSufix = utilities().garantirXCaracterOnNumber(count, 2) ;
				
				fileNameWithoutExtension = fileName + fileSufix;
				fileNameWithExtension = fileNameWithoutExtension + ".json";
			}
			
			fileSufix = utilities().garantirXCaracterOnNumber(count, 2) ;
		}
		
		fileName += fileSufix;
		*/
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		File file = new File(fileName);
		file.createNewFile();
		
		return file;
	}

	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}

	@Override
	public String getOperationType() {
		return SyncOperationConfig.SYNC_OPERATION_EXPORT;
	}

}
