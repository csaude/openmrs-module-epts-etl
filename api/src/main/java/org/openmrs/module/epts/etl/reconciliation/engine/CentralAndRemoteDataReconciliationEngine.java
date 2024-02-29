package org.openmrs.module.epts.etl.reconciliation.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.reconciliation.controller.CentralAndRemoteDataReconciliationController;
import org.openmrs.module.epts.etl.reconciliation.model.CentralAndRemoteDataReconciliationSearchParams;
import org.openmrs.module.epts.etl.reconciliation.model.ConciliationReasonType;
import org.openmrs.module.epts.etl.reconciliation.model.DataReconciliationRecord;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class CentralAndRemoteDataReconciliationEngine extends Engine {
	
	public CentralAndRemoteDataReconciliationEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
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
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (getSrcTableName().equalsIgnoreCase("users"))
			return;
		
		this.getMonitor().logInfo("PERFORMING DATA RECONCILIATION ON " + syncRecords.size() + "' " + this.getSrcTableName());
		
		if (getRelatedOperationController().isMissingRecordsDetector()) {
			performeMissingRecordsCreation(syncRecords, conn);
		} else if (getRelatedOperationController().isOutdateRecordsDetector()) {
			performeOutdatedRecordsUpdate(syncRecords, conn);
		} else if (getRelatedOperationController().isPhantomRecordsDetector()) {
			performePhantomRecordsRemotion(syncRecords, conn);
		}
		
		this.getMonitor().logInfo("RECONCILIATION DONE ON " + syncRecords.size() + " " + getSrcTableName() + "!");
	}
	
	private void performeMissingRecordsCreation(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		int i = 1;
		
		for (SyncRecord record : syncRecords) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
			
			logInfo(startingStrLog + ": Restoring record: [" + record + "]");
			
			DataReconciliationRecord data = new DataReconciliationRecord((DatabaseObject) record, getSrcTableConfiguration(),
			        ConciliationReasonType.MISSING);
			
			data.reloadRelatedRecordDataFromRemote(conn);
			
			data.consolidateAndSaveData(conn);
			
			data.save(conn);
			
			i++;
		}
	}
	
	private void performeOutdatedRecordsUpdate(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		int i = 1;
		
		for (SyncRecord record : syncRecords) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
			
			logInfo(startingStrLog + ": Updating record: [" + record + "]");
			
			DataReconciliationRecord.tryToReconciliate((DatabaseObject) record, getSrcTableConfiguration(), conn);
			
			i++;
		}
	}
	
	private void performePhantomRecordsRemotion(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		int i = 1;
		
		for (SyncRecord record : syncRecords) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
			
			logInfo(startingStrLog + ": Removing record: [" + record + "]");
			
			DataReconciliationRecord data = new DataReconciliationRecord(((DatabaseObject) record).getUuid(),
			        getSrcTableConfiguration(), ConciliationReasonType.PHANTOM);
			data.reloadRelatedRecordDataFromDestination(conn);
			data.removeRelatedRecord(conn);
			data.save(conn);
			
			i++;
		}
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new CentralAndRemoteDataReconciliationSearchParams(
		        this.getEtlConfiguration(), limits, getRelatedOperationController().getOperationType(), conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
}
