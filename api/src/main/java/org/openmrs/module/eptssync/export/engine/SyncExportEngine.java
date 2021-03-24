package org.openmrs.module.eptssync.export.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.export.controller.SyncExportController;
import org.openmrs.module.eptssync.export.model.SyncExportSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.SyncJSONInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

public class SyncExportEngine extends Engine {
	
	public SyncExportEngine(EngineMonitor monitor, RecordLimits limits) {
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
			
			this.getMonitor().logInfo("GENERATING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " TO JSON FILE");
			
			SyncJSONInfo jsonInfo = SyncJSONInfo.generate(getSyncTableConfiguration(), syncRecordsAsOpenMRSObjects, getSyncTableConfiguration().getOriginAppLocationCode(), conn);
		
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
			
			this.getMonitor().logInfo("JSON [" + jsonFIle + ".json] CREATED!");
			
			this.getMonitor().logInfo("MARKING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " AS SYNCHRONIZED");
			
			this.getMonitor().logInfo("MARKING '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " AS SYNCHRONIZED FINISHED");
			
			this.getMonitor().logInfo("MAKING FILES AVALIABLE");
			
			
			FileUtilities.renameTo(generateTmpMinimalJSONInfoFileName(jsonFIle), generateTmpMinimalJSONInfoFileName(jsonFIle) + ".json");
			FileUtilities.renameTo(jsonFIle.getAbsolutePath(), jsonFIle.getAbsolutePath() + ".json");
			
			logInfo("WRITEN FILE " + jsonFIle.getPath() + ".json" + " WITH SIZE " + new File(jsonFIle.getAbsolutePath() + ".json").length());
			
			if (new File(jsonFIle.getAbsolutePath() + ".json").length() == 0) {
				new File(jsonFIle.getAbsolutePath() + ".json").delete();
				new File(generateTmpMinimalJSONInfoFileName(jsonFIle) + ".json").delete();
				
				throw new ForbiddenOperationException("EMPTY FILE WAS WROTE!!!!!");
			}
			
			markAllAsSynchronized(utilities.parseList(syncRecords, OpenMRSObject.class));		
		} catch (IOException e) {
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

	private void markAllAsSynchronized(List<OpenMRSObject> syncRecords) {
		OpenConnection conn = openConnection();
		
		try {
			OpenMRSObjectDAO.refreshLastSyncDateOnOrigin(syncRecords, getSyncTableConfiguration(), getSyncTableConfiguration().getOriginAppLocationCode(), conn);
			
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
		searchParams.setSyncStartDate(this.getRelatedOperationController().getSyncOperationStatus().getStartTime());
		
		return searchParams;
	}
}
