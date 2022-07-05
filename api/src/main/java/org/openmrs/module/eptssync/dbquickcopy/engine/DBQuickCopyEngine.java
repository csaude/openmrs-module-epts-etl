package org.openmrs.module.eptssync.dbquickcopy.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.dbquickcopy.controller.DBQuickCopyController;
import org.openmrs.module.eptssync.dbquickcopy.model.DBQuickCopySearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.TableOperationProgressInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DBQuickCopyEngine extends Engine {
		
	public DBQuickCopyEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		if (!getLimits().isLoadedFromFile()) {
			RecordLimits saveLimits = retriveSavedLimits();
		
			if (saveLimits != null) {
				this.searchParams.setLimits(saveLimits);
			}
		}
	
		if (getLimits().canGoNext()) {
			logInfo("SERCHING NEXT RECORDS FOR LIMITS " + getLimits());
		
			OpenConnection srcConn = getRelatedOperationController().openSrcConnection();
			
			try {
				return  utilities.parseList(SearchParamsDAO.search(this.searchParams, srcConn), SyncRecord.class);
			}
			finally {
				srcConn.finalizeConnection();
			}
		}
		else return null;	
	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public DBQuickCopyController getRelatedOperationController() {
		return (DBQuickCopyController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		try {
			List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
			
			logInfo("LOADING  '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " TO DESTINATION DB");
			
			
			List<SyncImportInfoVO> records = SyncImportInfoVO.generateFromSyncRecord(syncRecordsAsOpenMRSObjects, getRelatedOperationController().getAppOriginLocationCode(), false);
		
			SyncImportInfoDAO.insertAllBatch(records, getSyncTableConfiguration(), conn);
			
			
			//int i = 1;
			
			for (OpenMRSObject syncRecord : syncRecordsAsOpenMRSObjects) {
				//String startingStrLog = getEngineId().split("_")[getEngineId().split("_").length - 1] + "_" + utilities.garantirXCaracterOnNumber(i, (""+getSearchParams().getQtdRecordPerSelected()).length());
				
				//logInfo(startingStrLog + " : Generating import info for record [" + syncRecord + "]");
				
				SyncImportInfoVO rec = SyncImportInfoVO.generateFromSyncRecord(syncRecord, getRelatedOperationController().getAppOriginLocationCode(), false);
				
				rec.setConsistent(1);
				
				//logInfo(startingStrLog + " : Saving import info of record [" + syncRecord + "]");
				
				rec.save(getSyncTableConfiguration(), conn);
				
				//logInfo(startingStrLog + " : Saved import info of record [" + syncRecord + "]!");
				
				//i++;
			}
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}	
		
		getLimits().moveNext(getQtyRecordsPerProcessing());
		
		saveCurrentLimits();
		
		if (isMainEngine()) {
			TableOperationProgressInfo progressInfo = this.getRelatedOperationController().getProgressInfo().retrieveProgressInfo(getSyncTableConfiguration());
			
			progressInfo.refreshProgressMeter();
			
			progressInfo.refreshOnDB(conn);
		}
	}
	
	private void saveCurrentLimits() {
		getLimits().save();
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DBQuickCopySearchParams(this.getSyncTableConfiguration(), limits, getRelatedOperationController());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}

}
