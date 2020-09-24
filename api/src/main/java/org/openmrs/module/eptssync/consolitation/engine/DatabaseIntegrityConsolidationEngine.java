package org.openmrs.module.eptssync.consolitation.engine;

import java.util.List;

import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.consolitation.model.DatabaseIntegrityConsolidationSearchParams;
import org.openmrs.module.eptssync.controller.conf.SyncTableInfo;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncEngine;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

public class DatabaseIntegrityConsolidationEngine extends SyncEngine {
	
	public DatabaseIntegrityConsolidationEngine(SyncTableInfo syncTableInfo, RecordLimits limits, DatabaseIntegrityConsolidationController syncController) {
		super(syncTableInfo, limits, syncController);
	}

	@Override	
	public List<SyncRecord> searchNextRecords(){
		OpenConnection conn = openConnection();
		
		try {
			return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public DatabaseIntegrityConsolidationController getSyncController() {
		return (DatabaseIntegrityConsolidationController) super.getSyncController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords) {
		List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
		
		OpenConnection conn = openConnection();
	
		this.syncController.logInfo("CONSOLIDATING INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableInfo().getTableName());
		
		for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				obj.consolidateData(getSyncTableInfo(), conn);
			} 
			catch (DBException e) {
				e.printStackTrace();
			
				throw new RuntimeException(e);
			}
		}
			
		this.syncController.logInfo("INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableInfo().getTableName() + " CONSOLIDATED!");
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DatabaseIntegrityConsolidationSearchParams(this.syncTableInfo, limits);
		searchParams.setQtdRecordPerSelected(getSyncTableInfo().getQtyRecordsPerSelect());
	
		return searchParams;
	}
}
