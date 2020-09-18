package org.openmrs.module.eptssync.controller.export_;

import java.io.File;

import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.export.ExportSyncEngine;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
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
	
	public SyncExportController() {
		
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
	
	public synchronized String generateJSONFileName(SyncJSONInfo jsonInfo, SyncTableInfo tableInfo) {
		String fileName = "";
		String fileSufix = "00";
		
		fileName += tableInfo.getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += tableInfo.getTableName();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += tableInfo.getTableName();
		fileName += "_" + DateAndTimeUtilities.parseFullDateToTimeLongIncludeSeconds(jsonInfo.getDateGenerated());

		if(new File(fileName + fileSufix + ".json").exists()) {
			int count = 1;
			
			while(new File(fileName + count + ".json").exists()) {
				count++;
			}
			
			fileSufix = utilities().garantirXCaracterOnNumber(count, 2) ;
		}
		
		return fileName + fileSufix + ".json";
	}

}
