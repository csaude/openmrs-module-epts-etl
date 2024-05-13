package org.openmrs.module.epts.etl.reconciliation.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.DatabaseObjectSearchParamsDAO;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
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
	public List<EtlObject> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(
		    DatabaseObjectSearchParamsDAO.search((DatabaseObjectSearchParams) this.searchParams, conn), EtlObject.class);
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
	public void performeSync(List<EtlObject> etlObjects, Connection conn) throws DBException {
		if (getMainSrcTableName().equalsIgnoreCase("users"))
			return;
		
		this.getMonitor()
		        .logInfo("PERFORMING DATA RECONCILIATION ON " + etlObjects.size() + "' " + this.getMainSrcTableName());
		
		if (getRelatedOperationController().isMissingRecordsDetector()) {
			performeMissingRecordsCreation(etlObjects, conn);
		} else if (getRelatedOperationController().isOutdateRecordsDetector()) {
			performeOutdatedRecordsUpdate(etlObjects, conn);
		} else if (getRelatedOperationController().isPhantomRecordsDetector()) {
			performePhantomRecordsRemotion(etlObjects, conn);
		}
		
		this.getMonitor().logInfo("RECONCILIATION DONE ON " + etlObjects.size() + " " + getMainSrcTableName() + "!");
	}
	
	private void performeMissingRecordsCreation(List<EtlObject> etlObjects, Connection conn) throws DBException {
		int i = 1;
		
		for (EtlObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logInfo(startingStrLog + ": Restoring record: [" + record + "]");
			
			DataReconciliationRecord data = new DataReconciliationRecord((EtlDatabaseObject) record, getMainSrcTableConf(),
			        ConciliationReasonType.MISSING);
			
			data.reloadRelatedRecordDataFromRemote(conn);
			
			data.consolidateAndSaveData(conn);
			
			data.save(conn);
			
			i++;
		}
	}
	
	private void performeOutdatedRecordsUpdate(List<EtlObject> etlObjects, Connection conn) throws DBException {
		int i = 1;
		
		for (EtlObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logInfo(startingStrLog + ": Updating record: [" + record + "]");
			
			DataReconciliationRecord.tryToReconciliate((EtlDatabaseObject) record, getMainSrcTableConf(), conn);
			
			i++;
		}
	}
	
	private void performePhantomRecordsRemotion(List<EtlObject> etlObjects, Connection conn) throws DBException {
		int i = 1;
		
		for (EtlObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logInfo(startingStrLog + ": Removing record: [" + record + "]");
			
			DataReconciliationRecord data = new DataReconciliationRecord(((EtlDatabaseObject) record).getUuid(),
			        getMainSrcTableConf(), ConciliationReasonType.PHANTOM);
			data.reloadRelatedRecordDataFromDestination(conn);
			data.removeRelatedRecord(conn);
			data.save(conn);
			
			i++;
		}
	}
	
	@Override
	protected SyncSearchParams<? extends EtlObject> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends EtlObject> searchParams = new CentralAndRemoteDataReconciliationSearchParams(
		        this.getEtlConfiguration(), limits, getRelatedOperationController().getOperationType(), conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
}
