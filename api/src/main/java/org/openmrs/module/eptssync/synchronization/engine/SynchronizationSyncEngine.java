package org.openmrs.module.eptssync.synchronization.engine;

import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.load.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.load.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.synchronization.controller.SynchronizationController;
import org.openmrs.module.eptssync.synchronization.model.SynchronizationSearchParams;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class SynchronizationSyncEngine extends SyncEngine {
	public SynchronizationSyncEngine(SyncTableInfo syncTableInfo, RecordLimits limits, SynchronizationController syncController) {
		super(syncTableInfo, limits, syncController);
	}

	@Override	
	public List<SyncRecord> searchNextRecords(){
		OpenConnection conn = openConnection();
		
		try {
			return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
			
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	protected void restart() {
		this.getSearchParams().setSyncStartDate(DateAndTimeUtilities.getCurrentDate());
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords) {
		OpenConnection conn = openConnection();
		
		try {
			getSyncController().logInfo("SYNCHRONIZING '"+syncRecords.size() + "' "+ getSyncTableInfo().getTableName().toUpperCase());
			
			
			if (getSyncTableInfo().isDoIntegrityCheckInTheEnd(getSyncController().getOperationType()) && !getSyncTableInfo().useSharedPKKey()) {
				List<OpenMRSObject> objects = SyncImportInfoVO.convertAllToOpenMRSObject(getSyncTableInfo(), utilities.parseList(syncRecords, SyncImportInfoVO.class));
				
				OpenMRSObjectDAO.insertAll(objects, conn);
				
				SyncImportInfoDAO.refreshLastMigrationTrySync(getSyncTableInfo(), utilities.parseList(syncRecords, SyncImportInfoVO.class), conn);
			}
			else{
				for (SyncRecord record : syncRecords) {
					((SyncImportInfoVO)record).sync(this.getSyncTableInfo(), conn);
				}
			}
			
			getSyncController().logInfo("SYNCHRONIZED'"+syncRecords.size() + "' "+ getSyncTableInfo().getTableName().toUpperCase());
			
			conn.markAsSuccessifullyTerminected();
		} catch (DBException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public SynchronizationSearchParams getSearchParams() {
		return (SynchronizationSearchParams) super.getSearchParams();
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits) {
		SyncSearchParams<? extends SyncRecord> searchParams = new SynchronizationSearchParams(this.syncTableInfo, limits);
		searchParams.setQtdRecordPerSelected(getSyncTableInfo().getQtyRecordsPerSelect(getSyncController().getOperationType()));
		//searchParams.setExtraCondition("json like \"%1f485395-b5ea-4889-8c1f-2b4f85a44776%\"");
		
		return searchParams;
	}

	@Override
	public void requestStop() {
	}
}
