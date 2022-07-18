package org.openmrs.module.eptssync.synchronization.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.synchronization.controller.DatabaseMergeFromJSONController;
import org.openmrs.module.eptssync.synchronization.model.DataBaseMergeFromJSONSearchParams;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class DataBaseMergeFromJSONEngine extends Engine {
	
	public DataBaseMergeFromJSONEngine(EngineMonitor monitor, RecordLimits limits) {
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
	public DatabaseMergeFromJSONController getRelatedOperationController() {
		return (DatabaseMergeFromJSONController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		throw new ForbiddenOperationException("Rever este metodo!");
		
		/*getRelatedOperationController().logInfo("SYNCHRONIZING '"+syncRecords.size() + "' "+ getSyncTableConfiguration().getTableName().toUpperCase());
		
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
		
		getRelatedOperationController().logInfo("SYNCHRONIZED'"+syncRecords.size() + "' "+ getSyncTableConfiguration().getTableName().toUpperCase());*/
	}
	
	@Override
	public DataBaseMergeFromJSONSearchParams getSearchParams() {
		return (DataBaseMergeFromJSONSearchParams) super.getSearchParams();
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		/*SyncSearchParams<? extends SyncRecord> searchParams = new DataBaseMergeFromJSONSearchParams(this.getSyncTableConfiguration(), limits, this.getRelatedOperationController().getAppOriginLocationCode());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getRelatedOperationController().getProgressInfo().getStartTime());
		
		return searchParams;*/
		
		return null;
	}

	@Override
	public void requestStop() {
	}
}
