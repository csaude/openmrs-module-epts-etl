package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.controller.GenericOperationController;
import org.openmrs.module.eptssync.problems_solver.model.ProblemsSolverSearchParams;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;

/**
 * @author jpboane
 */
public abstract class GenericEngine extends Engine {
	
	public static boolean done;
	
	public GenericEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		SyncRecord rec = new SyncRecord() {
			
			@Override
			public void setExcluded(boolean excluded) {
			}
			
			@Override
			public void load(ResultSet rs) throws SQLException {
			}
			
			@Override
			public boolean isExcluded() {
				return false;
			}
			
			@Override
			public String generateTableName() {
				return null;
			}
		};
		
		if (!GenericEngine.done) {
			return utilities.parseToList(rec);
		} else {
			return null;
		}
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ProblemsSolverSearchParams(
		        this.getSyncTableConfiguration(), null);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSyncConfiguration().getObservationDate());
		
		return searchParams;
	}
}
