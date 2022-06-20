package org.openmrs.module.eptssync.reconciliation.engine;

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
import org.openmrs.module.eptssync.reconciliation.controller.CentralAndRemoteDataReconciliationController;
import org.openmrs.module.eptssync.reconciliation.model.CentralAndRemoteDataReconciliationSearchParams;
import org.openmrs.module.eptssync.reconciliation.model.ConciliationReasonType;
import org.openmrs.module.eptssync.reconciliation.model.DataReconciliationRecord;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class CentralAndRemoteDataReconciliationEngine extends Engine {
		
	public CentralAndRemoteDataReconciliationEngine(EngineMonitor monitor, RecordLimits limits) {
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
	
		logInfo("SERCHING NEXT RECORDS FOR LIMITS " + getLimits());
		
		if (getLimits().canGoNext()) {
			return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
		}
		else return null;	
	}

	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public CentralAndRemoteDataReconciliationController getRelatedOperationController() {
		return (CentralAndRemoteDataReconciliationController) super.getRelatedOperationController();
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
			DataReconciliationRecord.tryToReconciliate((OpenMRSObject) record, getSyncTableConfiguration(), conn);
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
