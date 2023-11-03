package org.openmrs.module.epts.etl.problems_solver.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.openmrs._default.UsersVO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.problems_solver.model.ProblemsSolverSearchParams;
import org.openmrs.module.epts.etl.problems_solver.model.TmpUserVO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * 
 * @author jpboane
 *
 * @see DBQuickMergeController
 */
public class ProblemsSolverEngineUsersDupsUUID extends GenericEngine {
	public ProblemsSolverEngineUsersDupsUUID(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}

	
	@Override	
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException{
		return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException{
		logDebug("RESOLVING PROBLEM MERGE ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		int i = 1;
		
		for (SyncRecord record: syncRecords) {
			if ( ((DatabaseObject)record).getUuid().isEmpty()) continue;
			
			String startingStrLog = utilities.garantirXCaracterOnNumber(i, (""+getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
			
			DatabaseObject rec = (DatabaseObject)record;
			
			SyncTableConfiguration syncTableInfo = SyncTableConfiguration.init("tmp_user", getSyncTableConfiguration().getRelatedSyncConfiguration());
			
			List<TmpUserVO> dups = DatabaseObjectDAO.getByField(TmpUserVO.class, "user_uuid", rec.getUuid(), conn);
				
			logDebug(startingStrLog + " RESOLVING..." + rec);
			
		
				
			TmpUserVO preservedUser = TmpUserVO.getWinningRecord(dups, conn);
			preservedUser.setUsersSyncTableConfiguration(getSyncTableConfiguration());
			
			for (int j = 1; j < dups.size(); j++) {
				TmpUserVO dup = dups.get(j);
				
				dup.setSyncTableConfiguration(syncTableInfo);
				
				
				UsersVO user = new UsersVO();
				
				user.setUserId(dup.getObjectId());
				user.setUuid(dup.getUuid());
			
				try {
					logDebug("REMOVING USER [" + dup + "]");
					
					user.remove(conn);
					dup.markAsDeletable();
				}
				catch (DBException e) {
					logWarn("THE USER HAS RECORDS ASSOCIETED... HARMONIZING...");
					dup.markAsUndeletable();
					preservedUser.harmonize(dup, conn);
					logWarn(e.getLocalizedMessage());
				}
				
				finally {
					/*try {
						conn.rollback();
					}
					catch (SQLException e) {
					}*/
					
					dup.save(syncTableInfo, conn);
					
					try {
						conn.commit();
					}
					catch (SQLException e) {
						logWarn(e.getLocalizedMessage());
					}
				}
			}
			
			
			
			i++;
		}
	}

	
	
	protected void resolveDuplicatedUuidOnUserTable(List<SyncRecord> syncRecords, Connection conn) throws DBException, ForbiddenOperationException {
		logDebug("RESOLVING PROBLEM MERGE ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		int i = 1;
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		for (SyncRecord record: syncRecords) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i, (""+getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
			
			DatabaseObject rec = (DatabaseObject)record;
			
			List<DatabaseObject> dups = new ArrayList<>();//DatabaseObjectDAO.getByUuid(getSyncTableConfiguration().getSyncRecordClass(getDefaultApp()), rec.getUuid(), conn);
				
			logDebug(startingStrLog + " RESOLVING..." + rec);
			
			for (int j = 1; j < dups.size(); j++) {
				DatabaseObject dup = dups.get(j);
				
				dup.setUuid(dup.getUuid() + "_" + j);
				
				dup.save(getSyncTableConfiguration(), conn);
			}
			
			
			i++;
		}
		
		if (utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
			logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
			syncRecords.removeAll(recordsToIgnoreOnStatistics);
		}
		
		logDebug("MERGE DONE ON " + syncRecords.size() + " " + getSyncTableConfiguration().getTableName() + "!");		
	}
	
	@Override
	public void requestStop() {
	}

	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ProblemsSolverSearchParams(this.getSyncTableConfiguration(), null);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSyncConfiguration().getObservationDate());
		
		return searchParams;
	}

}
