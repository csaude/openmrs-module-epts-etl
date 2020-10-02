package org.openmrs.module.eptssync.export.engine;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.export.controller.SyncExportController;
import org.openmrs.module.eptssync.export.model.SyncExportSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class ExportSyncEngine extends SyncEngine {
	
	public ExportSyncEngine(SyncTableInfo syncTableInfo, RecordLimits limits, SyncExportController syncController) {
		super(syncTableInfo, limits, syncController);
	}

	@Override	
	public List<SyncRecord> searchNextRecords(){
		OpenConnection conn = openConnection();
		
		try {
			//List<SyncRecord> records = utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
			
			List<SyncRecord> records = utilities.parseList(SearchParamsDAO.search(this, conn), SyncRecord.class);
				
			return records;
			
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public SyncExportController getSyncController() {
		return (SyncExportController) super.getSyncController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords) {
		try {
			List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
			
			for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
				obj.setOriginAppLocationCode(getSyncTableInfo().getOriginAppLocationCode());
			}
			
			this.syncController.logInfo("GENERATING '"+syncRecords.size() + "' " + getSyncTableInfo().getTableName() + " TO JSON FILE");
			
			SyncJSONInfo jsonInfo = SyncJSONInfo.generate(syncRecordsAsOpenMRSObjects);
			jsonInfo.setOriginAppLocationCode(getSyncTableInfo().getOriginAppLocationCode());

			File jsonFIle = generateJSONTempFile(jsonInfo, syncRecords.get(0).getObjectId(), syncRecords.get(syncRecords.size() - 1).getObjectId());
			
			this.syncController.logInfo("WRITING '"+syncRecords.size() + "' " + getSyncTableInfo().getTableName() + " TO JSON FILE [" + jsonFIle.getAbsolutePath() + ".json]");
			
			FileUtilities.write(jsonFIle.getAbsolutePath(), jsonInfo.parseToJSON());
			
			FileUtilities.write(generateTmpMinimalJSONInfoFileName(jsonFIle), jsonInfo.generateMinimalInfo().parseToJSON());
			
			this.syncController.logInfo("JSON [" + jsonFIle + ".json] CREATED!");
			
			this.syncController.logInfo("MARKING '"+syncRecords.size() + "' " + getSyncTableInfo().getTableName() + " AS SYNCHRONIZED");
				
			markAllAsSynchronized(utilities.parseList(syncRecords, OpenMRSObject.class));
			
			this.syncController.logInfo("MARKING '"+syncRecords.size() + "' " + getSyncTableInfo().getTableName() + " AS SYNCHRONIZED FINISHED");
			
			this.syncController.logInfo("MAKING FILES AVALIABLE");
			
			FileUtilities.renameTo(generateTmpMinimalJSONInfoFileName(jsonFIle), generateTmpMinimalJSONInfoFileName(jsonFIle) + ".json");
			FileUtilities.renameTo(jsonFIle.getAbsolutePath(), jsonFIle.getAbsolutePath() + ".json");
			
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}

	private String generateTmpMinimalJSONInfoFileName(File mainTempJSONInfoFile) {
		return mainTempJSONInfoFile.getAbsolutePath() + "_minimal";
	}

	private void markAllAsSynchronized(List<OpenMRSObject> syncRecords) {
		OpenConnection conn = openConnection();
		
		try {
			OpenMRSObjectDAO.refreshLastSyncDate(syncRecords, conn);
			
			conn.markAsSuccessifullyTerminected();
		} 
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	private File generateJSONTempFile(SyncJSONInfo jsonInfo, int startRecord, int lastRecord) throws IOException {
		return getSyncController().generateJSONTempFile(jsonInfo, getSyncTableInfo(), startRecord, lastRecord);
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits) {
		SyncSearchParams<? extends SyncRecord> searchParams = new SyncExportSearchParams(this.syncTableInfo, limits);
		searchParams.setQtdRecordPerSelected(getSyncTableInfo().getQtyRecordsPerSelect(getSyncController().getOperationType()));
	
		return searchParams;
	}
}
