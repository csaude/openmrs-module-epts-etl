package org.openmrs.module.epts.etl.problems_solver.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.problems_solver.model.ProblemsSolverSearchParamsUsersDupsUUID;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * @author jpboane
 * @see EtlController
 */
public class ProblemsSolverEngineUsersDupsUUID extends GenericEngine {
	
	public ProblemsSolverEngineUsersDupsUUID(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
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
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		throw new ForbiddenOperationException("Review this method");
		
		/*		logDebug("RESOLVING PROBLEM ON " + syncRecords.size() + "' " + getMainSrcTableName());
				
				int i = 1;
				
				for (SyncRecord record : syncRecords) {
					String startingStrLog = utilities.garantirXCaracterOnNumber(i,
					    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
					
					DatabaseObject rec = (DatabaseObject) record;
					
					AbstractTableConfiguration syncTableInfo = AbstractTableConfiguration.init("tmp_user",
					    getEtlConfiguration().getSrcConf());
					
					List<TmpUserVO> allDuplicatedByUuid = DatabaseObjectDAO.getByField(TmpUserVO.class, "user_uuid", rec.getUuid(),
					    conn);
					
					logDebug(startingStrLog + " RESOLVING..." + rec);
					
					TmpUserVO preservedUser = TmpUserVO.getWinningRecord(allDuplicatedByUuid, conn);
					preservedUser.setUsersSyncTableConfiguration(this.getMainSrcTableConf());
					
					for (int j = 0; j < allDuplicatedByUuid.size(); j++) {
						TmpUserVO dup = allDuplicatedByUuid.get(j);
						
						if (dup.isWinning()) {
							continue;
						}
						
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
							logWarn(e.getLocalizedMessage());
							
							dup.markAsUndeletable();
							preservedUser.harmonize(conn);
						}
						
						finally {
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
				}*/
	}
	
	protected void resolveDuplicatedUuidOnUserTable(List<SyncRecord> syncRecords, Connection conn)
	        throws DBException, ForbiddenOperationException {
		logDebug("RESOLVING PROBLEM MERGE ON " + syncRecords.size() + "' " + this.getMainSrcTableName());
		
		int i = 1;
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		for (SyncRecord record : syncRecords) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
			
			DatabaseObject rec = (DatabaseObject) record;
			
			List<DatabaseObject> dups = new ArrayList<>();
			
			logDebug(startingStrLog + " RESOLVING..." + rec);
			
			for (int j = 1; j < dups.size(); j++) {
				DatabaseObject dup = dups.get(j);
				
				dup.setUuid(dup.getUuid() + "_" + j);
				
				dup.save(getMainSrcTableConf(), conn);
			}
			
			i++;
		}
		
		if (utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
			logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
			syncRecords.removeAll(recordsToIgnoreOnStatistics);
		}
		
		logDebug("MERGE DONE ON " + syncRecords.size() + " " + this.getMainSrcTableName() + "!");
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ProblemsSolverSearchParamsUsersDupsUUID(
		        this.getEtlConfiguration(), null);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
