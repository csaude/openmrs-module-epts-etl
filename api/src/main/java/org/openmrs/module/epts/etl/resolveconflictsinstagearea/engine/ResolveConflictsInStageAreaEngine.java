package org.openmrs.module.epts.etl.resolveconflictsinstagearea.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.resolveconflictsinstagearea.controller.ResolveConflictsInStageAreaController;
import org.openmrs.module.epts.etl.resolveconflictsinstagearea.model.ResolveConflictsInStageAreaSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ResolveConflictsInStageAreaEngine extends Engine {
	
	public ResolveConflictsInStageAreaEngine(EngineMonitor monitor, RecordLimits limits) {
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
	public ResolveConflictsInStageAreaController getRelatedOperationController() {
		return (ResolveConflictsInStageAreaController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		throw new ForbiddenOperationException("Review this method");
		
		/*List<SyncImportInfoVO> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, SyncImportInfoVO.class);
		
		this.getMonitor().logInfo("PERFORMING CONFLICTS RESOLUTION ACTION '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		for (SyncImportInfoVO obj : syncRecordsAsOpenMRSObjects) {
			try {
				List<SyncImportInfoVO> recordsInConflict = SyncImportInfoDAO.getAllByUuid(getSyncTableConfiguration(), obj.getRecordUuid(), conn);
				
				SyncImportInfoVO mostRecent = SyncImportInfoVO.chooseMostRecent(recordsInConflict);
				
				recordsInConflict.remove(mostRecent);
				
				String loosers = "";
				
				for (SyncImportInfoVO recInConflict : recordsInConflict) {
					
					loosers += (!loosers.isEmpty() ? "," : "") + "{" + generateRecInfo(recInConflict) + "}";
					
					recInConflict.markAsInconsistent(getSyncTableConfiguration(), conn);
				}
				
				logDebug("Done Processing record: " + obj.getRecordUuid() + "! Win: {" + generateRecInfo(mostRecent) + "} loosers: [" + loosers + "]");
			} catch (Exception e) {
				e.printStackTrace();
				
				logError("Any error occurred processing record [uuid: " + obj.getRecordUuid() + ", id: " + obj.getId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		this.getMonitor().logInfo("CONFLICTS RESOLVED FOR RECORDS '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + "!");
		*/
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ResolveConflictsInStageAreaSearchParams(
		        this.getEtlConfiguration(), limits, conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
}
