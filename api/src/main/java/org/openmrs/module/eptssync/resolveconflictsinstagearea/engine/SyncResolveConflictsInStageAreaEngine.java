package org.openmrs.module.eptssync.resolveconflictsinstagearea.engine;

import java.io.File;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.TableOperationProgressInfo;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.resolveconflictsinstagearea.controller.SyncResolveConflictsInStageAreaController;
import org.openmrs.module.eptssync.resolveconflictsinstagearea.model.ResolveConflictsInStageAreaSearchLimits;
import org.openmrs.module.eptssync.resolveconflictsinstagearea.model.ResolveConflictsInStageAreaSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SyncResolveConflictsInStageAreaEngine extends Engine {
		
	public SyncResolveConflictsInStageAreaEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public void resetLimits(RecordLimits limits) {
		getSearchParams().setLimits(new ResolveConflictsInStageAreaSearchLimits(limits.getFirstRecordId(), limits.getLastRecordId(), this));
		getLimits().setThreadMaxRecord(limits.getLastRecordId());
		getLimits().setThreadMinRecord(limits.getFirstRecordId());
	}
	
	public ResolveConflictsInStageAreaSearchLimits getLimits() {
		return (ResolveConflictsInStageAreaSearchLimits) getSearchParams().getLimits();
	}
	
	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		if (!getLimits().isLoadedFromFile()) {
			ResolveConflictsInStageAreaSearchLimits saveLimits = retriveSavedLimits();
			
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
	
	private ResolveConflictsInStageAreaSearchLimits retriveSavedLimits() {
		if (!getLimits().hasThreadCode()) getLimits().setThreadCode(this.getEngineId());
		
		return ResolveConflictsInStageAreaSearchLimits.loadFromFile(new File(getLimits().generateFilePath()), this);
	}

	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public SyncResolveConflictsInStageAreaController getRelatedOperationController() {
		return (SyncResolveConflictsInStageAreaController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		List<SyncImportInfoVO> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, SyncImportInfoVO.class);
		
		this.getMonitor().logInfo("PERFORMING CONFLICTS RESOLUTION ACTION '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());

		for (SyncImportInfoVO obj : syncRecordsAsOpenMRSObjects) {
			try {
				List<SyncImportInfoVO> recordsInConflict = SyncImportInfoDAO.getAllByUuid(getSyncTableConfiguration(), obj.getRecordUuid(), conn);
				
				recordsInConflict.remove(SyncImportInfoVO.chooseMostRecent(recordsInConflict));
				
				for (SyncImportInfoVO recInConflict : recordsInConflict) {
					recInConflict.markAsInconsistent(getSyncTableConfiguration(), conn);
				}
				
				conn.commit();
			} catch (Exception e) {
				e.printStackTrace();
				
				logInfo("Any error occurred processing record [uuid: " + obj.getRecordUuid() + ", id: " + obj.getId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		this.getMonitor().logInfo("CONFLICTS RESOLVED FOR RECORDS '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + "!");
	
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
		SyncSearchParams<? extends SyncRecord> searchParams = new ResolveConflictsInStageAreaSearchParams(this.getSyncTableConfiguration(), limits, conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
}
