package org.openmrs.module.eptssync.changesdetector.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.changesdetector.controller.ChangesDetectorController;
import org.openmrs.module.eptssync.changesdetector.model.ChangesDetectorSearchParams;
import org.openmrs.module.eptssync.changesdetector.model.DetectedRecordInfo;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

import fgh.spi.changedrecordsdetector.DetectedRecordService;

public class ChangesDetectorEngine extends Engine {
	
	public ChangesDetectorEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}

	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	public ChangesDetectorController getRelatedOperationController() {
		return (ChangesDetectorController) super.getRelatedOperationController();
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
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ChangesDetectorSearchParams(this.getSyncTableConfiguration(),  getRelatedOperationController().getConfiguration().getApplicationCode(), limits, conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
}
