package org.openmrs.module.epts.etl.resolveconflictsinstagearea.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.DatabaseObjectSearchParamsDAO;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.resolveconflictsinstagearea.controller.ResolveConflictsInStageAreaController;
import org.openmrs.module.epts.etl.resolveconflictsinstagearea.model.ResolveConflictsInStageAreaSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ResolveConflictsInStageAreaEngine extends Engine {
	
	public ResolveConflictsInStageAreaEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public List<EtlObject> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(DatabaseObjectSearchParamsDAO.search((DatabaseObjectSearchParams) this.searchParams, conn), EtlObject.class);
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
	public void performeSync(List<EtlObject> etlObjects, Connection conn) throws DBException {
		utilities.throwReviewMethodException();

		
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
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(RecordLimits limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new ResolveConflictsInStageAreaSearchParams(
		        this.getEtlConfiguration(), limits, conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
}
