package org.openmrs.module.eptssync.inconsistenceresolver.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.inconsistenceresolver.controller.InconsistenceSolverController;
import org.openmrs.module.eptssync.inconsistenceresolver.model.InconsistenceSolverSearchParams;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

public class InconsistenceSolverEngine extends Engine {
	
	public InconsistenceSolverEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}

	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		return  utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	public InconsistenceSolverController getRelatedOperationController() {
		return (InconsistenceSolverController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		List<DatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, DatabaseObject.class);
		
		logInfo("DOING INCONSISTENCE SOLVER FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		for (DatabaseObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				obj.resolveInconsistence(getSyncTableConfiguration(), conn);
			} catch (Exception e) {
				logError("Any error occurred processing record [uuid: " + obj.getUuid() + ", id: " + obj.getObjectId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		logInfo("INCONSISTENCE SOLVED FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + "!");
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new InconsistenceSolverSearchParams(this.getSyncTableConfiguration(), limits, conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getRelatedOperationController().getProgressInfo().getStartTime());
		
		return searchParams;
	}
}
