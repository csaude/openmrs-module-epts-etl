package org.openmrs.module.eptssync.engine.export;

import java.io.File;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.export.SyncExportSearchParams;
import org.openmrs.module.eptssync.model.openmrs.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class ExportSyncEngine extends SyncEngine {
	private SyncExportSearchParams searchParams;

	public ExportSyncEngine(SyncTableInfo syncTableInfo) {
		super(syncTableInfo);

		searchParams = new SyncExportSearchParams(syncTableInfo);
		searchParams.setQtdRecordPerSelected(100);
	}

	@Override	
	public List<SyncRecord> searchNextRecords(){
		OpenConnection conn = openConnection();
		
		try {
			return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
			
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords) {
		List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
		
		for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
			obj.setOriginAppLocationCode(getSyncTableInfo().getOriginAppLocationCode());
		}
		
		SyncJSONInfo jsonInfo = SyncJSONInfo.generate(syncRecordsAsOpenMRSObjects);
		jsonInfo.setOriginAppLocationCode(getSyncTableInfo().getOriginAppLocationCode());
		
		FileUtilities.write(generateJSONFileName(jsonInfo), jsonInfo.parseToJSON());
		
		markAllAsSynchronized(utilities.parseList(syncRecords, OpenMRSObject.class));
	}

	private void markAllAsSynchronized(List<OpenMRSObject> syncRecords) {
		OpenConnection conn = DBConnectionService.getInstance().openConnection();
		
		try {
			for (OpenMRSObject syncRecord : syncRecords) {
				syncRecord.refreshLastSyncDate(conn);
			}
			
			conn.markAsSuccessifullyTerminected();
		} 
		finally {
			conn.finalizeConnection();
		}
		
		
	}

	private String generateJSONFileName(SyncJSONInfo jsonInfo) {
		String fileName = "";

		fileName += this.getSyncTableInfo().getRelatedSyncTableInfoSource().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += this.getSyncTableInfo().getTableName();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += this.getSyncTableInfo().getTableName();
		fileName += "_" + DateAndTimeUtilities.parseFullDateToTimeLongIncludeSeconds(jsonInfo.getDateGenerated());

		if(new File(fileName + ".json").exists()) {
			int count = 1;
			
			while(new File(fileName + count + ".json").exists()) {
				count++;
			}
			
			fileName += count;
		}
		
		return fileName + ".json";
	}
}
