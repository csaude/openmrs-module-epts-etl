package org.openmrs.module.eptssync.export.controller;

import java.io.File;
import java.io.IOException;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.export.engine.SyncExportEngine;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * This class is responsible for control the data export in the synchronization processs
 * 
 * @author jpboane
 *
 */
public class SyncExportController extends OperationController {
	
	public SyncExportController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}

	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new SyncExportEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			OpenMRSObject obj = OpenMRSObjectDAO.getFirstRecord(tableInfo, null, conn);
		
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
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openConnection();
		
		try {
			OpenMRSObject obj = OpenMRSObjectDAO.getLastRecord(tableInfo, null, conn);
		
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
	
	public synchronized File generateJSONTempFile(SyncJSONInfo jsonInfo, SyncTableConfiguration tableInfo, int startRecord, int lastRecord) throws IOException {
		String fileName = "";
		//String fileSufix = "00";
		
		fileName += tableInfo.getRelatedSynconfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getRelatedSynconfiguration().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getTableName();
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getTableName();
		
		fileName += "_" + utilities().garantirXCaracterOnNumber(startRecord, 10);
		fileName += "_" + utilities().garantirXCaracterOnNumber(lastRecord, 10);
	
		if(new File(fileName).exists() ) {
			logInfo("The file '" + fileName + "' is already exists!!! Removing it...");
			new File(fileName).delete();
		}
		
		if(new File(fileName+".json").exists() ) {
			logInfo("The file '" + fileName  + ".json' is already exists!!! Removing it...");
			new File(fileName+".json").delete();
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
