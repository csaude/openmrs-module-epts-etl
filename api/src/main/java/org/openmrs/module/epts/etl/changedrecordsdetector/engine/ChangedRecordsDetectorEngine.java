package org.openmrs.module.epts.etl.changedrecordsdetector.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.changedrecordsdetector.controller.ChangedRecordsDetectorController;
import org.openmrs.module.epts.etl.changedrecordsdetector.model.ChangedRecordsDetectorSearchParams;
import org.openmrs.module.epts.etl.changedrecordsdetector.model.DetectedRecordInfo;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import fgh.spi.changedrecordsdetector.ChangedRecord;
import fgh.spi.changedrecordsdetector.DetectedRecordService;

public class ChangedRecordsDetectorEngine extends Engine {
		
	public ChangedRecordsDetectorEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		DetectedRecordService action = DetectedRecordService.getInstance();
		
		DBConnectionInfo connInfo = getRelatedOperationController().getActionPerformeApp().getConnInfo();
			
		action.configureDBService(getRelatedOperationController().getActionPerformeApp().getApplicationCode(), connInfo);
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
	public ChangedRecordsDetectorController getRelatedOperationController() {
		return (ChangedRecordsDetectorController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		List<DatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, DatabaseObject.class);
		List<ChangedRecord> processedRecords = new ArrayList<ChangedRecord>(syncRecords.size());
		
		this.getMonitor().logInfo("PERFORMING CHANGE DETECTED ACTION '"+syncRecords.size() + "' " + getSrcTableConfiguration().getTableName());

		for (DatabaseObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				((GenericDatabaseObject)obj).setSyncTableConfiguration(getSrcTableConfiguration());
				
				processedRecords.add(DetectedRecordInfo.generate(obj, getRelatedOperationController().getActionPerformeApp().getApplicationCode(), getMonitor().getSrcTableConfiguration().getOriginAppLocationCode()));
				
				if (getRelatedOperationController().getActionPerformeApp().isSinglePerformingMode()) {
					DetectedRecordService.getInstance().performeAction(getRelatedOperationController().getActionPerformeApp().getApplicationCode(), processedRecords.get(processedRecords.size() - 1), getSrcTableConfiguration());
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				
				logError("Any error occurred processing record [uuid: " + obj.getUuid() + ", id: " + obj.getObjectId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		if (getRelatedOperationController().getActionPerformeApp().isBatchPerformingMode()) {
			DetectedRecordService.getInstance().performeAction(getRelatedOperationController().getActionPerformeApp().getApplicationCode(), processedRecords, getSrcTableConfiguration());
		}
		
		this.getMonitor().logInfo("ACTION PERFORMED FOR CHANGED RECORDS '"+syncRecords.size() + "' " + getSrcTableConfiguration().getTableName() + "!");
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ChangedRecordsDetectorSearchParams(this.getEtlConfiguration(),  getRelatedOperationController().getActionPerformeApp().getApplicationCode(), limits, getRelatedOperationController().getOperationType(), conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
}
