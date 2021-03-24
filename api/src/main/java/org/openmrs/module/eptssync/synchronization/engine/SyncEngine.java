package org.openmrs.module.eptssync.synchronization.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.load.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.synchronization.controller.SyncController;
import org.openmrs.module.eptssync.synchronization.model.SynchronizationSearchParams;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SyncEngine extends Engine {
	
	public SyncEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	protected void restart() {
		this.getSearchParams().setSyncStartDate(DateAndTimeUtilities.getCurrentDate());
	}
	
	
	@Override
	public SyncController getRelatedOperationController() {
		return (SyncController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		getRelatedOperationController().logInfo("SYNCHRONIZING '"+syncRecords.size() + "' "+ getSyncTableConfiguration().getTableName().toUpperCase());
		
		if (getSyncTableConfiguration().isDoIntegrityCheckInTheEnd(getRelatedOperationController().getOperationType()) && !getSyncTableConfiguration().useSharedPKKey()) {
			List<OpenMRSObject> objects = SyncImportInfoVO.convertAllToOpenMRSObject(getSyncTableConfiguration(), utilities.parseList(syncRecords, SyncImportInfoVO.class), conn);
			
			OpenMRSObjectDAO.insertAll(objects, getSyncTableConfiguration(), getRelatedOperationController().getAppOriginLocationCode(), conn);
			
			SyncImportInfoDAO.markAsToBeCompletedInFuture(getSyncTableConfiguration(), utilities.parseList(syncRecords, SyncImportInfoVO.class), conn);
		}
		else{
			for (SyncRecord record : syncRecords) {
				((SyncImportInfoVO)record).sync(this.getSyncTableConfiguration(), conn);
			}
		}
		
		getRelatedOperationController().logInfo("SYNCHRONIZED'"+syncRecords.size() + "' "+ getSyncTableConfiguration().getTableName().toUpperCase());
	}
	
	@Override
	public SynchronizationSearchParams getSearchParams() {
		return (SynchronizationSearchParams) super.getSearchParams();
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new SynchronizationSearchParams(this.getSyncTableConfiguration(), limits, this.getRelatedOperationController().getAppOriginLocationCode());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getRelatedOperationController().getSyncOperationStatus().getStartTime());
		
		return searchParams;
	}

	@Override
	public void requestStop() {
	}
}
