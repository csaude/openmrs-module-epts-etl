package org.openmrs.module.epts.etl.export.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.export.controller.DBExportController;
import org.openmrs.module.epts.etl.export.model.ExportSearchParams;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public class DBExportEngine extends Engine {
	
	public DBExportEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(SearchParamsDAO.search(this, conn), SyncRecord.class);
	}
	
	@Override
	public DBExportController getRelatedOperationController() {
		return (DBExportController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) {
		try {
			List<DatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, DatabaseObject.class);
			
			logDebug("GENERATING '" + syncRecords.size() + "' " + getMainSrcTableName() + " TO JSON FILE");
			
			SyncJSONInfo jsonInfo = SyncJSONInfo.generate(getMainSrcTableName(), syncRecordsAsOpenMRSObjects,
			    getEtlConfiguration().getOriginAppLocationCode(), true);
			
			File jsonFIle = generateJSONTempFile(jsonInfo, syncRecordsAsOpenMRSObjects.get(0).getObjectId(),
			    syncRecordsAsOpenMRSObjects.get(syncRecords.size() - 1).getObjectId());
			
			logInfo("WRITING '" + syncRecords.size() + "' " + getMainSrcTableName() + " TO JSON FILE ["
			        + jsonFIle.getAbsolutePath() + ".json]");
			
			//Try to remove not terminate files
			{
				FileUtilities.removeFile(jsonFIle.getAbsolutePath());
				FileUtilities.removeFile(generateTmpMinimalJSONInfoFileName(jsonFIle));
				FileUtilities.removeFile(jsonFIle.getAbsolutePath() + ".json");
				FileUtilities.removeFile(generateTmpMinimalJSONInfoFileName(jsonFIle) + ".json");
			}
			
			FileUtilities.write(jsonFIle.getAbsolutePath(), jsonInfo.parseToJSON());
			
			FileUtilities.write(generateTmpMinimalJSONInfoFileName(jsonFIle), jsonInfo.generateMinimalInfo().parseToJSON());
			
			this.getMonitor().logInfo("JSON [" + jsonFIle + ".json] CREATED!");
			
			logDebug("MARKING '" + syncRecords.size() + "' " + getMainSrcTableName() + " AS SYNCHRONIZED");
			
			logDebug("MARKING '" + syncRecords.size() + "' " + getMainSrcTableName() + " AS SYNCHRONIZED FINISHED");
			
			logDebug("MAKING FILES AVALIABLE");
			
			FileUtilities.renameTo(generateTmpMinimalJSONInfoFileName(jsonFIle),
			    generateTmpMinimalJSONInfoFileName(jsonFIle) + ".json");
			FileUtilities.renameTo(jsonFIle.getAbsolutePath(), jsonFIle.getAbsolutePath() + ".json");
			
			logInfo("WRITEN FILE " + jsonFIle.getPath() + ".json" + " WITH SIZE "
			        + new File(jsonFIle.getAbsolutePath() + ".json").length());
			
			if (new File(jsonFIle.getAbsolutePath() + ".json").length() == 0) {
				new File(jsonFIle.getAbsolutePath() + ".json").delete();
				new File(generateTmpMinimalJSONInfoFileName(jsonFIle) + ".json").delete();
				
				throw new ForbiddenOperationException("EMPTY FILE WAS WROTE!!!!!");
			}
			
			markAllAsSynchronized(utilities.parseList(syncRecords, DatabaseObject.class));
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private String generateTmpMinimalJSONInfoFileName(File mainTempJSONInfoFile) {
		return mainTempJSONInfoFile.getAbsolutePath() + "_minimal";
	}
	
	private void markAllAsSynchronized(List<DatabaseObject> syncRecords) {
		OpenConnection conn = openConnection();
		
		try {
			DatabaseObjectDAO.refreshLastSyncDateOnOrigin(syncRecords, getMainSrcTableConf(),
			    getEtlConfiguration().getOriginAppLocationCode(), conn);
			
			conn.markAsSuccessifullyTerminated();
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	private File generateJSONTempFile(SyncJSONInfo jsonInfo, Integer startRecord, Integer lastRecord) throws IOException {
		return getRelatedOperationController().generateJSONTempFile(jsonInfo, getMainSrcTableConf(), startRecord,
		    lastRecord);
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ExportSearchParams(this.getEtlConfiguration(), limits,
		        conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getRelatedOperationController().getProgressInfo().getStartTime());
		
		return searchParams;
	}
}
