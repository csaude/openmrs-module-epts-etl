package org.openmrs.module.eptssync.changedrecordsdetector.engine;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.changedrecordsdetector.controller.ChangedRecordsDetectorController;
import org.openmrs.module.eptssync.changedrecordsdetector.model.ChangedRecordSearchLimits;
import org.openmrs.module.eptssync.changedrecordsdetector.model.ChangedRecordsDetectorSearchParams;
import org.openmrs.module.eptssync.changedrecordsdetector.model.DetectedRecordInfo;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

import fgh.spi.changedrecordsdetector.DetectedRecordService;

public class ChangedRecordsDetectorEngine extends Engine {
	
	public ChangedRecordsDetectorEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		this.limits = new ChangedRecordSearchLimits(limits.getFirstRecordId(), limits.getLastRecordId(), this);
		
		getLimits().setThreadMaxRecord(limits.getLastRecordId());
		getLimits().setThreadMinRecord(limits.getFirstRecordId());
	}

	@Override
	public ChangedRecordSearchLimits getLimits() {
		return (ChangedRecordSearchLimits) super.getLimits();
	}
	
	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		
		if (!getLimits().isLoadedFromFile()) {
			ChangedRecordSearchLimits saveLimits = retriveSavedLimits();
			
			if (saveLimits != null) {
				this.limits = saveLimits;
			}
		}
	
		if (getLimits().canGoNext()) {
			return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
		}
		else return null;	
	}
	
	private ChangedRecordSearchLimits retriveSavedLimits() {
		if (!getLimits().hasThreadCode()) getLimits().setThreadCode(this.getEngineId());
		
		return ChangedRecordSearchLimits.loadFromFile(new File(getLimits().generateFilePath()), this);
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
		
		this.getMonitor().logInfo("PERFORMING CHANGE DETECTED ACTION '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				
				DetectedRecordInfo rec = DetectedRecordInfo.generate(obj, getRelatedOperationController().getConfiguration().getApplicationCode(), getMonitor().getSyncTableInfo().getOriginAppLocationCode());
				
				rec.save(getMonitor().getSyncTableInfo(), conn);
				
				DetectedRecordService.getInstance().performeAction( getRelatedOperationController().getConfiguration().getApplicationCode(), rec);
				
			} catch (Exception e) {
				e.printStackTrace();
				
				logInfo("Any error occurred processing record [uuid: " + obj.getUuid() + ", id: " + obj.getObjectId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		this.getMonitor().logInfo("ACTION PERFORMED FOR CHANGED RECORDS '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + "!");
	
		getLimits().moveNext(syncRecords.size());
		
		saveCurrentLimits();
	}
	
	
	private void saveCurrentLimits() {
		getLimits().save();
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ChangedRecordsDetectorSearchParams(this.getSyncTableConfiguration(),  getRelatedOperationController().getConfiguration().getApplicationCode(), limits, getRelatedOperationController().getOperationType(), conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
}
