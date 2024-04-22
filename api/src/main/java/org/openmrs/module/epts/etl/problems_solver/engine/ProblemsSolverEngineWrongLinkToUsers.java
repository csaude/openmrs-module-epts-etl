package org.openmrs.module.epts.etl.problems_solver.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.RefInfo;
import org.openmrs.module.epts.etl.controller.conf.RefMapping;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.DatabaseObjectSearchParamsDAO;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.problems_solver.model.ProblemsSolverSearchParams;
import org.openmrs.module.epts.etl.problems_solver.model.TmpUserVO;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see EtlController
 */
public class ProblemsSolverEngineWrongLinkToUsers extends GenericEngine {
	
	public static String[] DB_NAMES = DatabasesInfo.ARIEL_DB_NAMES_MAPUTO;
	
	private AppInfo remoteApp;
	
	public ProblemsSolverEngineWrongLinkToUsers(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		this.remoteApp = getRelatedOperationController().getConfiguration().find(AppInfo.init("remote"));
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(DatabaseObjectSearchParamsDAO.search((DatabaseObjectSearchParams) this.searchParams, conn), SyncRecord.class);
	}
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@SuppressWarnings({ "null", "unused" })
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		logDebug("RESOLVING PROBLEM MERGE ON " + syncRecords.size() + "' " + this.getMainSrcTableName());
		
		OpenConnection srcConn = remoteApp.openConnection();
		
		try {
			int i = 1;
			for (SyncRecord record : syncRecords) {
				try {
					
					String startingStrLog = utilities.garantirXCaracterOnNumber(i,
					    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
					
					logDebug(startingStrLog + " STARTING RESOLVE PROBLEMS OF RECORD [" + record + "]");
					
					AbstractTableConfiguration personTabConf = AbstractTableConfiguration
					        .initGenericTabConf("person", getEtlConfiguration().getSrcConf());
					
					DatabaseObject userOnDestDB = DatabaseObjectDAO.getByOid(getMainSrcTableConf(),
					    ((DatabaseObject) record).getObjectId(), conn);
					
					if ((Integer) userOnDestDB.getParentValue("personId") != 1) {
						logDebug("SKIPPING THE RECORD BECAUSE IT HAS THE CORRECT PERSON ["
						        + userOnDestDB.getParentValue("personId") + "]");
						continue;
					}
					
					if (userOnDestDB.getObjectId().getSimpleValueAsInt() == 1) {
						logDebug("SKIPPING THE RECORD BECAUSE IT IS THE DEFAULT USER");
						continue;
					}
					
					boolean found = false;
					
					for (String dbName : DB_NAMES) {
						DatabaseObject userOnSrcDB = new GenericDatabaseObject();//DatabaseObjectDAO.getByUuidOnSpecificSchema(syncRecordClass, userOnDestDB.getUuid(), dbName, srcConn);
						
						if (userOnSrcDB != null) {
							
							logDebug("RESOLVING USER PROBLEM USING DATA FROM [" + dbName + "]");
							
							DatabaseObject relatedPersonOnSrcDB = DatabaseObjectDAO.getByIdOnSpecificSchema(personTabConf,
							    Oid.fastCreate("", userOnSrcDB.getParentValue("personId")), dbName, srcConn);
							
							List<DatabaseObject> relatedPersonOnDestDB = null;//DatabaseObjectDAO.getByUuid(prsonRecordClass, relatedPersonOnSrcDB.getUuid(), conn);
							
							RefInfo r = new RefInfo();
							
							r.addMapping(RefMapping.fastCreate("person_id", "person_id"));
							
							userOnDestDB.changeParentValue(r, relatedPersonOnDestDB.get(0));
							userOnDestDB.save(getMainSrcTableConf(), conn);
							
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
	
	@SuppressWarnings("null")
	protected void resolveDuplicatedUuidOnUserTable(List<SyncRecord> syncRecords, Connection conn)
	        throws DBException, ForbiddenOperationException {
		logDebug("RESOLVING PROBLEM MERGE ON " + syncRecords.size() + "' " + getMainSrcTableName());
		
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
				
				dup.save(getMainSrcTableConf(), conn);
			}
			
			i++;
		}
		
		if (utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
			logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
			syncRecords.removeAll(recordsToIgnoreOnStatistics);
		}
		
		logDebug("MERGE DONE ON " + syncRecords.size() + " " + getMainSrcTableName() + "!");
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ProblemsSolverSearchParams(this.getEtlConfiguration(),
		        null);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
