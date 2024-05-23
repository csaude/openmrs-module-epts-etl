package org.openmrs.module.epts.etl.changedrecordsdetector.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.changedrecordsdetector.controller.ChangedRecordsDetectorController;
import org.openmrs.module.epts.etl.changedrecordsdetector.model.ChangedRecordsDetectorSearchParams;
import org.openmrs.module.epts.etl.changedrecordsdetector.model.DetectedRecordInfo;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.EtlObject;
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
	public List<EtlObject> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), EtlObject.class);
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
	public void performeSync(List<EtlObject> etlObjects, Connection conn) throws DBException {
		List<EtlDatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(etlObjects, EtlDatabaseObject.class);
		List<ChangedRecord> processedRecords = new ArrayList<ChangedRecord>(etlObjects.size());
		
		this.getMonitor().logInfo(
		    "PERFORMING CHANGE DETECTED ACTION '" + etlObjects.size() + "' " + getSrcConf().getTableName());
		
		for (EtlDatabaseObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				((GenericDatabaseObject) obj).setRelatedConfiguration(getSrcConf());
				
				processedRecords.add(DetectedRecordInfo.generate(obj,
				    getRelatedOperationController().getActionPerformeApp().getApplicationCode(),
				    getMonitor().getSrcMainTableConf().getOriginAppLocationCode()));
				
				if (getRelatedOperationController().getActionPerformeApp().isSinglePerformingMode()) {
					DetectedRecordService.getInstance().performeAction(
					    getRelatedOperationController().getActionPerformeApp().getApplicationCode(),
					    processedRecords.get(processedRecords.size() - 1), getSrcConf());
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
				
				logError(
				    "Any error occurred processing record [uuid: " + obj.getUuid() + ", id: " + obj.getObjectId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		if (getRelatedOperationController().getActionPerformeApp().isBatchPerformingMode()) {
			DetectedRecordService.getInstance().performeAction(
			    getRelatedOperationController().getActionPerformeApp().getApplicationCode(), processedRecords,
			    getSrcConf());
		}
		
		this.getMonitor().logInfo("ACTION PERFORMED FOR CHANGED RECORDS '" + etlObjects.size() + "' "
		        + getSrcConf().getTableName() + "!");
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(RecordLimits limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new ChangedRecordsDetectorSearchParams(
		        this.getEtlConfiguration(), getRelatedOperationController().getActionPerformeApp().getApplicationCode(),
		        limits, getRelatedOperationController().getOperationType(), conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		
		
		
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
}
