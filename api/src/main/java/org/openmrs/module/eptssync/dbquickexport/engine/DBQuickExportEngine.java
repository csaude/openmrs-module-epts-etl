package org.openmrs.module.eptssync.dbquickexport.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.dbquickexport.controller.DBQuickExportController;
import org.openmrs.module.eptssync.dbquickexport.model.DBQuickExportSearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class DBQuickExportEngine extends Engine {
		
	public DBQuickExportEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}

	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public DBQuickExportController getRelatedOperationController() {
		return (DBQuickExportController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		try {
			List<DatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, DatabaseObject.class);
			
			this.getMonitor().logInfo("GENERATING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " TO JSON FILE");
			
			SyncJSONInfo jsonInfo = SyncJSONInfo.generate(syncRecordsAsOpenMRSObjects, getSyncTableConfiguration().getOriginAppLocationCode(), false);
		
			File jsonFIle = generateJSONTempFile(jsonInfo, syncRecordsAsOpenMRSObjects.get(0).getObjectId(), syncRecordsAsOpenMRSObjects.get(syncRecords.size() - 1).getObjectId());
			
			this.getMonitor().logInfo("WRITING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " TO JSON FILE [" + jsonFIle.getAbsolutePath() + ".json]");
			
			
			//Try to remove not terminate files
			{
				FileUtilities.removeFile(jsonFIle.getAbsolutePath());
				FileUtilities.removeFile(generateTmpMinimalJSONInfoFileName(jsonFIle));
				FileUtilities.removeFile(jsonFIle.getAbsolutePath() + ".json");
				FileUtilities.removeFile(generateTmpMinimalJSONInfoFileName(jsonFIle) + ".json");
			}
			
			FileUtilities.write(jsonFIle.getAbsolutePath(), jsonInfo.parseToJSON());
			
			FileUtilities.write(generateTmpMinimalJSONInfoFileName(jsonFIle), jsonInfo.generateMinimalInfo().parseToJSON());
			
			this.logDebug("JSON [" + jsonFIle + ".json] CREATED!");
			
			this.logDebug("MARKING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " AS SYNCHRONIZED");
			
			this.logDebug("MARKING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " AS SYNCHRONIZED FINISHED");
			
			this.logDebug("MAKING FILES AVALIABLE");
			
			
			FileUtilities.renameTo(generateTmpMinimalJSONInfoFileName(jsonFIle), generateTmpMinimalJSONInfoFileName(jsonFIle) + ".json");
			FileUtilities.renameTo(jsonFIle.getAbsolutePath(), jsonFIle.getAbsolutePath() + ".json");
			
			logInfo("WRITEN FILE " + jsonFIle.getPath() + ".json" + " WITH SIZE " + new File(jsonFIle.getAbsolutePath() + ".json").length());
			
			if (new File(jsonFIle.getAbsolutePath() + ".json").length() == 0) {
				new File(jsonFIle.getAbsolutePath() + ".json").delete();
				new File(generateTmpMinimalJSONInfoFileName(jsonFIle) + ".json").delete();
				
				throw new ForbiddenOperationException("EMPTY FILE WAS WROTE!!!!!");
			}
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}	
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DBQuickExportSearchParams(this.getSyncTableConfiguration(), limits);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSyncConfiguration().getObservationDate());
		
		return searchParams;
	}
	
	private String generateTmpMinimalJSONInfoFileName(File mainTempJSONInfoFile) {
		return mainTempJSONInfoFile.getAbsolutePath() + "_minimal";
	}

	private File generateJSONTempFile(SyncJSONInfo jsonInfo, Integer startRecord, Integer lastRecord) throws IOException {
		return getRelatedOperationController().generateJSONTempFile(jsonInfo, getSyncTableConfiguration(), startRecord, lastRecord);
	}
}
