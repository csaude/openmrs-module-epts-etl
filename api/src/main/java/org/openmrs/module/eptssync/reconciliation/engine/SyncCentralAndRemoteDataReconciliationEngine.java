package org.openmrs.module.eptssync.reconciliation.engine;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.changedrecordsdetector.controller.ChangedRecordsDetectorController;
import org.openmrs.module.eptssync.changedrecordsdetector.model.ChangedRecordsDetectorSearchParams;
import org.openmrs.module.eptssync.changedrecordsdetector.model.DetectedRecordInfo;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.TableOperationProgressInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.reconciliation.model.DataReconciliationSearchLimits;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

import fgh.spi.changedrecordsdetector.ChangedRecord;
import fgh.spi.changedrecordsdetector.DetectedRecordService;

public class SyncCentralAndRemoteDataReconciliationEngine extends Engine {
		
	public SyncCentralAndRemoteDataReconciliationEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		DetectedRecordService action = DetectedRecordService.getInstance();
		
		DBConnectionInfo connInfo = getRelatedOperationController().getActionPerformeApp().getConnInfo();
			
		action.configureDBService(getRelatedOperationController().getActionPerformeApp().getApplicationCode(), connInfo);
	}
	
	@Override
	public void resetLimits(RecordLimits limits) {
		getSearchParams().setLimits(new DataReconciliationSearchLimits(limits.getFirstRecordId(), limits.getLastRecordId(), this));
		getLimits().setThreadMaxRecord(limits.getLastRecordId());
		getLimits().setThreadMinRecord(limits.getFirstRecordId());
	}
	
	public DataReconciliationSearchLimits getLimits() {
		return (DataReconciliationSearchLimits) getSearchParams().getLimits();
	}
	
	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		if (!getLimits().isLoadedFromFile()) {
			DataReconciliationSearchLimits saveLimits = retriveSavedLimits();
			
			if (saveLimits != null) {
				this.searchParams.setLimits(saveLimits);
			}
		}
	
		logInfo("SERCHING NEXT RECORDS FOR LIMITS " + getLimits());
		
		if (getLimits().canGoNext()) {
			return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
		}
		else return null;	
	}
	
	private DataReconciliationSearchLimits retriveSavedLimits() {
		if (!getLimits().hasThreadCode()) getLimits().setThreadCode(this.getEngineId());
		
		return DataReconciliationSearchLimits.loadFromFile(new File(getLimits().generateFilePath()), this);
	}

	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public ChangedRecordsDetectorController getRelatedOperationController() {
		return (ChangedRecordsDetectorController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
		List<ChangedRecord> processedRecords = new ArrayList<ChangedRecord>(syncRecords.size());
		
		this.getMonitor().logInfo("PERFORMING CHANGE DETECTED ACTION '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());

		for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				processedRecords.add(DetectedRecordInfo.generate(obj, getRelatedOperationController().getActionPerformeApp().getApplicationCode(), getMonitor().getSyncTableInfo().getOriginAppLocationCode()));
				
				if (getRelatedOperationController().getActionPerformeApp().isSinglePerformingMode()) {
					DetectedRecordService.getInstance().performeAction(getRelatedOperationController().getActionPerformeApp().getApplicationCode(), processedRecords.get(processedRecords.size() - 1), getSyncTableConfiguration());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				
				logInfo("Any error occurred processing record [uuid: " + obj.getUuid() + ", id: " + obj.getObjectId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		if (getRelatedOperationController().getActionPerformeApp().isBatchPerformingMode()) {
			DetectedRecordService.getInstance().performeAction(getRelatedOperationController().getActionPerformeApp().getApplicationCode(), processedRecords, getSyncTableConfiguration());
		}
		
		this.getMonitor().logInfo("ACTION PERFORMED FOR CHANGED RECORDS '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + "!");
	
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
		SyncSearchParams<? extends SyncRecord> searchParams = new ChangedRecordsDetectorSearchParams(this.getSyncTableConfiguration(),  getRelatedOperationController().getActionPerformeApp().getApplicationCode(), limits, getRelatedOperationController().getOperationType(), conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
}
