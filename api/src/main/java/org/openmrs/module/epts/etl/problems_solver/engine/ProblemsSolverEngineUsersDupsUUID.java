package org.openmrs.module.epts.etl.problems_solver.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.dbextract.controller.DbExtractController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.problems_solver.model.ProblemsSolverSearchParamsUsersDupsUUID;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * @author jpboane
 * @see DbExtractController
 */
public class ProblemsSolverEngineUsersDupsUUID extends GenericEngine {
	
	public ProblemsSolverEngineUsersDupsUUID(EngineMonitor monitor, RecordLimits limits) {
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
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException {
		utilities.throwReviewMethodException();
		
		/*		logDebug("RESOLVING PROBLEM ON " + syncRecords.size() + "' " + getMainSrcTableName());
				
				int i = 1;
				
				for (EtlObject record : syncRecords) {
					String startingStrLog = utilities.garantirXCaracterOnNumber(i,
					    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
					
					EtlDatabaseObject rec = (EtlDatabaseObject) record;
					
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
	
	protected void resolveDuplicatedUuidOnUserTable(List<EtlObject> etlObjects, Connection conn)
	        throws DBException, ForbiddenOperationException {
		logDebug("RESOLVING PROBLEM MERGE ON " + etlObjects.size() + "' " + this.getMainSrcTableName());
		
		int i = 1;
		
		List<EtlObject> recordsToIgnoreOnStatistics = new ArrayList<EtlObject>();
		
		for (EtlObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			EtlDatabaseObject rec = (EtlDatabaseObject) record;
			
			List<EtlDatabaseObject> dups = new ArrayList<>();
			
			logDebug(startingStrLog + " RESOLVING..." + rec);
			
			for (int j = 1; j < dups.size(); j++) {
				EtlDatabaseObject dup = dups.get(j);
				
				dup.setUuid(dup.getUuid() + "_" + j);
				
				dup.save(getSrcConf(), conn);
			}
			
			i++;
		}
		
		if (utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
			logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
			etlObjects.removeAll(recordsToIgnoreOnStatistics);
		}
		
		logDebug("MERGE DONE ON " + etlObjects.size() + " " + this.getMainSrcTableName() + "!");
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(RecordLimits limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new ProblemsSolverSearchParamsUsersDupsUUID(
		        this.getEtlConfiguration(), null, this);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
