package org.openmrs.module.eptssync.engine.synchronization;

import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.controller.synchronization.SynchronizationController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.load.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.synchronization.SynchronizationSearchParams;
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
			for (SyncRecord record : syncRecords) {
				((SyncImportInfoVO)record).sync(this.getSyncTableInfo(), conn);
			}
			
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
		searchParams.setQtdRecordPerSelected(2500);
		
		return searchParams;
	}

	@Override
	public void requestStop() {
	}
}
