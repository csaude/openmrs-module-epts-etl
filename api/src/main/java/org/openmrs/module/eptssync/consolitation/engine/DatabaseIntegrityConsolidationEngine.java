package org.openmrs.module.eptssync.consolitation.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.consolitation.model.DatabaseIntegrityConsolidationSearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.monitor.EnginActivityMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class DatabaseIntegrityConsolidationEngine extends Engine {
	
	public DatabaseIntegrityConsolidationEngine(EnginActivityMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}

	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	public DatabaseIntegrityConsolidationController getRelatedOperationController() {
		return (DatabaseIntegrityConsolidationController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
		
		this.getMonitor().logInfo("CONSOLIDATING INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableInfo().getTableName());
		
		for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
			obj.consolidateData(getSyncTableInfo(), conn);
		}
			
		this.getMonitor().logInfo("INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableInfo().getTableName() + " CONSOLIDATED!");
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DatabaseIntegrityConsolidationSearchParams(this.getSyncTableInfo(), limits, conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
	
		return searchParams;
	}
}
