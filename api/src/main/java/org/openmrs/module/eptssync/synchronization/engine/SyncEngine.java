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
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.monitor.EnginActivityMonitor;
import org.openmrs.module.eptssync.synchronization.model.SynchronizationSearchParams;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class SyncEngine extends Engine {
	
	public SyncEngine(EnginActivityMonitor monitor, RecordLimits limits) {
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
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		getRelatedOperationController().logInfo("SYNCHRONIZING '"+syncRecords.size() + "' "+ getSyncTableInfo().getTableName().toUpperCase());
		
		if (getSyncTableInfo().isDoIntegrityCheckInTheEnd(getRelatedOperationController().getOperationType()) && !getSyncTableInfo().useSharedPKKey()) {
			List<OpenMRSObject> objects = SyncImportInfoVO.convertAllToOpenMRSObject(getSyncTableInfo(), utilities.parseList(syncRecords, SyncImportInfoVO.class), conn);
			
			OpenMRSObjectDAO.insertAll(objects, conn);
			
			SyncImportInfoDAO.refreshLastMigrationTrySync(getSyncTableInfo(), utilities.parseList(syncRecords, SyncImportInfoVO.class), conn);
		}
		else{
			for (SyncRecord record : syncRecords) {
				((SyncImportInfoVO)record).sync(this.getSyncTableInfo(), conn);
			}
		}
		
		getRelatedOperationController().logInfo("SYNCHRONIZED'"+syncRecords.size() + "' "+ getSyncTableInfo().getTableName().toUpperCase());
	}
	
	@Override
	public SynchronizationSearchParams getSearchParams() {
		return (SynchronizationSearchParams) super.getSearchParams();
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new SynchronizationSearchParams(this.getSyncTableInfo(), limits);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		//searchParams.setExtraCondition("json like \"%1f485395-b5ea-4889-8c1f-2b4f85a44776%\"");
		
		return searchParams;
	}

	@Override
	public void requestStop() {
	}
}
