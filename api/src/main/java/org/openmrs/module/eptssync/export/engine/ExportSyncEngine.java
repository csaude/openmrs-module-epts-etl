package org.openmrs.module.eptssync.export.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.export.controller.SyncExportController;
import org.openmrs.module.eptssync.export.model.SyncExportSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.monitor.EnginActivityMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class ExportSyncEngine extends Engine {
	
	public ExportSyncEngine(EnginActivityMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}

	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		return utilities.parseList(SearchParamsDAO.search(this, conn), SyncRecord.class);
	}
	
	@Override
	public SyncExportController getRelatedOperationController() {
		return (SyncExportController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) {
		try {
			List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
			
			for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
				obj.setOriginAppLocationCode(getSyncTableConfiguration().getOriginAppLocationCode());
			}
			
			this.getMonitor().logInfo("GENERATING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " TO JSON FILE");
			
			SyncJSONInfo jsonInfo = SyncJSONInfo.generate(syncRecordsAsOpenMRSObjects);
			jsonInfo.setOriginAppLocationCode(getSyncTableConfiguration().getOriginAppLocationCode());

			File jsonFIle = generateJSONTempFile(jsonInfo, syncRecords.get(0).getObjectId(), syncRecords.get(syncRecords.size() - 1).getObjectId());
			
			this.getMonitor().logInfo("WRITING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " TO JSON FILE [" + jsonFIle.getAbsolutePath() + ".json]");
			
			FileUtilities.write(jsonFIle.getAbsolutePath(), jsonInfo.parseToJSON());
			
			FileUtilities.write(generateTmpMinimalJSONInfoFileName(jsonFIle), jsonInfo.generateMinimalInfo().parseToJSON());
			
			this.getMonitor().logInfo("JSON [" + jsonFIle + ".json] CREATED!");
			
			this.getMonitor().logInfo("MARKING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " AS SYNCHRONIZED");
				
			markAllAsSynchronized(utilities.parseList(syncRecords, OpenMRSObject.class));
			
			this.getMonitor().logInfo("MARKING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " AS SYNCHRONIZED FINISHED");
			
			this.getMonitor().logInfo("MAKING FILES AVALIABLE");
			
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
		return getRelatedOperationController().generateJSONTempFile(jsonInfo, getSyncTableConfiguration(), startRecord, lastRecord);
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new SyncExportSearchParams(this.getSyncTableConfiguration(), limits, conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		
		return searchParams;
	}
}
