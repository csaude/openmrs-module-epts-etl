package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.controller.ProblemsSolverController;
import org.openmrs.module.eptssync.problems_solver.model.ProblemsSolverSearchParams;
import org.openmrs.module.eptssync.problems_solver.model.TmpUserVO;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DBQuickMergeController
 */
public class ProblemsSolverEngineWrongLinkToUsers extends ProblemsSolverEngine {
	
	public static String[] DB_NAMES = DatabasesInfo.ARIEL_DB_NAMES_MAPUTO;
	
	private AppInfo mainApp;
	
	private AppInfo remoteApp;
	
	public ProblemsSolverEngineWrongLinkToUsers(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		this.remoteApp = getRelatedOperationController().getConfiguration().find(AppInfo.init("remote"));
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	public ProblemsSolverController getRelatedOperationController() {
		return (ProblemsSolverController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		logDebug("RESOLVING PROBLEM MERGE ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		OpenConnection srcConn = remoteApp.openConnection();
		
		try {
			int i = 1;
			for (SyncRecord record : syncRecords) {
				try {
					
					String startingStrLog = utilities.garantirXCaracterOnNumber(i,
					    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
					
					logDebug(startingStrLog + " STARTING RESOLVE PROBLEMS OF RECORD [" + record + "]");
					
					Class<DatabaseObject> syncRecordClass = getSyncTableConfiguration().getSyncRecordClass(getDefaultApp());
					Class<DatabaseObject> prsonRecordClass = SyncTableConfiguration
					        .init("person", getSyncTableConfiguration().getRelatedSynconfiguration())
					        .getSyncRecordClass(getDefaultApp());
					
					DatabaseObject userOnDestDB = DatabaseObjectDAO.getById(syncRecordClass,
					    ((DatabaseObject) record).getObjectId(), conn);
					
					if (userOnDestDB.getParentValue("personId") != 1) {
						logDebug("SKIPPING THE RECORD BECAUSE IT HAS THE CORRECT PERSON ["
						        + userOnDestDB.getParentValue("personId") + "]");
						continue;
					}
					
					if (userOnDestDB.getObjectId() == 1) {
						logDebug("SKIPPING THE RECORD BECAUSE IT IS THE DEFAULT USER");
						continue;
					}
					
					boolean found = false;
					
					for (String dbName : DB_NAMES) {
						DatabaseObject userOnSrcDB = new GenericDatabaseObject();//DatabaseObjectDAO.getByUuidOnSpecificSchema(syncRecordClass, userOnDestDB.getUuid(), dbName, srcConn);
						
						if (userOnSrcDB != null) {
							
							logDebug("RESOLVING USER PROBLEM USING DATA FROM [" + dbName + "]");
							
							DatabaseObject relatedPersonOnSrcDB = DatabaseObjectDAO.getByIdOnSpecificSchema(prsonRecordClass,
							    userOnSrcDB.getParentValue("personId"), dbName, srcConn);
							
							/*if (relatedPersonOnSrcDB == null) {
								logDebug("RELATED PERSON NOT FOUND ON ON [" + dbName + "]");
								continue;
							}
							
							*/
							
							List<DatabaseObject> relatedPersonOnDestDB = null;//DatabaseObjectDAO.getByUuid(prsonRecordClass, relatedPersonOnSrcDB.getUuid(), conn);
							
							userOnDestDB.changeParentValue("personId", relatedPersonOnDestDB.get(0));
							userOnDestDB.save(getSyncTableConfiguration(), conn);
							
							found = true;
							
							break;
						} else {
							logDebug("USER NOT FOUND ON [" + dbName + "]");
						}
					}
					
					if (!found) {
						//throw new ForbiddenOperationException("THE RECORD [" + record + "] WERE NOT FOUND IN ANY SRC!");
					}
				}
				finally {
					i++;
					((TmpUserVO) record).markAsHarmonized(conn);
				}
			}
		}
		finally {
			srcConn.finalizeConnection();
		}
	}
	
	protected void resolveDuplicatedUuidOnUserTable(List<SyncRecord> syncRecords, Connection conn)
	        throws DBException, ForbiddenOperationException {
		logDebug("RESOLVING PROBLEM MERGE ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		int i = 1;
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		for (SyncRecord record : syncRecords) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
			
			DatabaseObject rec = (DatabaseObject) record;
			
			List<DatabaseObject> dups = null;//DatabaseObjectDAO.getByUuid(getSyncTableConfiguration().getSyncRecordClass(getDefaultApp()), rec.getUuid(), conn);
			
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
		SyncSearchParams<? extends SyncRecord> searchParams = new ProblemsSolverSearchParams(
		        this.getSyncTableConfiguration(), null);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
	
}
