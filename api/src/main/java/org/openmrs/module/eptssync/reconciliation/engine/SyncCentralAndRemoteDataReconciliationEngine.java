package org.openmrs.module.eptssync.reconciliation.engine;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.TableOperationProgressInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.reconciliation.controller.SyncCentralAndRemoteDataReconciliationController;
import org.openmrs.module.eptssync.reconciliation.model.CentralAndRemoteDataReconciliationSearchParams;
import org.openmrs.module.eptssync.reconciliation.model.ConciliationReasonType;
import org.openmrs.module.eptssync.reconciliation.model.DataReconciliationRecord;
import org.openmrs.module.eptssync.reconciliation.model.DataReconciliationSearchLimits;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SyncCentralAndRemoteDataReconciliationEngine extends Engine {
		
	public SyncCentralAndRemoteDataReconciliationEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
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
	public SyncCentralAndRemoteDataReconciliationController getRelatedOperationController() {
		return (SyncCentralAndRemoteDataReconciliationController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		if (getSyncTableConfiguration().getTableName().equalsIgnoreCase("users")) return;
		
		this.getMonitor().logInfo("PERFORMING DATA RECONCILIATION ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());

		this.getMonitor().logInfo("RECONCILIATION DONE ON " + syncRecords.size() + " " + getSyncTableConfiguration().getTableName() + "!");
	
		
		if (getRelatedOperationController().isMissingRecordsDetector()) {
			performeMissingRecordsCreation(syncRecords, conn);
		}
		else
		if (getRelatedOperationController().isOutdateRecordsDetector()) {
			performeOutdatedRecordsUpdate(syncRecords, conn);
		}
		else
		if (getRelatedOperationController().isPhantomRecordsDetector()) {
			performePhantomRecordsRemotion(syncRecords, conn);
		}
		
		getLimits().moveNext(getQtyRecordsPerProcessing());
		
		saveCurrentLimits();
		
		if (isMainEngine()) {
			TableOperationProgressInfo progressInfo = this.getRelatedOperationController().getProgressInfo().retrieveProgressInfo(getSyncTableConfiguration());
			
			progressInfo.refreshProgressMeter();
			
			progressInfo.refreshOnDB(conn);
		}
	}
	
	private void performeMissingRecordsCreation(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		for (SyncRecord record: syncRecords) {
			DataReconciliationRecord data = new DataReconciliationRecord(((OpenMRSObject)record).getUuid() , getSyncTableConfiguration(), ConciliationReasonType.MISSING);
			
			data.reloadRelatedRecordDataFromRemote(conn);
			
			data.consolidateAndSaveData(conn);
			
			data.save(conn);
		}
	}
	
	private void performeOutdatedRecordsUpdate(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		for (SyncRecord record: syncRecords) {
			DataReconciliationRecord data = new DataReconciliationRecord(((OpenMRSObject)record).getUuid() , getSyncTableConfiguration(), ConciliationReasonType.OUTDATED);
			
			data.reloadRelatedRecordDataFromRemote(conn);
			
			data.consolidateAndSaveData(conn);
			
			data.save(conn);
		}	
	}
	
	private void performePhantomRecordsRemotion(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		for (SyncRecord record: syncRecords) {
			DataReconciliationRecord data = new DataReconciliationRecord(((OpenMRSObject)record).getUuid() , getSyncTableConfiguration(), ConciliationReasonType.PHANTOM);
			data.reloadRelatedRecordDataFromDestination(conn);
			data.removeRelatedRecord(conn);
			data.save(conn);
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
		SyncSearchParams<? extends SyncRecord> searchParams = new CentralAndRemoteDataReconciliationSearchParams(this.getSyncTableConfiguration(), limits, getRelatedOperationController().getOperationType(), conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
}
