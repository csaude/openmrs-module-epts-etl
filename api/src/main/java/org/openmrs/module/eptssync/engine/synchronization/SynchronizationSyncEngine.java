package org.openmrs.module.eptssync.engine.synchronization;

import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.load.SyncImportInfoVO;
import org.openmrs.module.eptssync.model.synchronization.SynchronizationSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class SynchronizationSyncEngine extends SyncEngine {
	private SynchronizationSearchParams searchParams;

	public SynchronizationSyncEngine(SyncTableInfo syncTableInfo) {
		super(syncTableInfo);

		searchParams = new SynchronizationSearchParams(syncTableInfo);
		searchParams.setQtdRecordPerSelected(100);
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

}
