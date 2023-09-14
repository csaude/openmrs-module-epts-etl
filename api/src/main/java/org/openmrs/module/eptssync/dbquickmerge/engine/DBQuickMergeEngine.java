package org.openmrs.module.eptssync.dbquickmerge.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.MappedTableInfo;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.dbquickmerge.model.DBQuickMergeSearchParams;
import org.openmrs.module.eptssync.dbquickmerge.model.MergingRecord;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.exceptions.ConflictWithRecordNotYetAvaliableException;
import org.openmrs.module.eptssync.exceptions.MissingParentException;
import org.openmrs.module.eptssync.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DBQuickMergeController
 */
public class DBQuickMergeEngine extends Engine {
	
	private AppInfo srcApp;
	
	private AppInfo dstApp;
	
	public DBQuickMergeEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		this.srcApp = getRelatedOperationController().getConfiguration().find(AppInfo.init("main"));
		this.dstApp = getRelatedOperationController().getConfiguration().find(AppInfo.init("destination"));
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		List<SyncRecord> records = new ArrayList<SyncRecord>();
		
		try {
			DBQuickMergeSearchParams searchParams = (DBQuickMergeSearchParams) getSearchParams();
			
			if (getFinalCheckStatus().onGoing()) {
				OpenConnection dstConn = this.dstApp.openConnection();
				
				try {
					if (DBUtilities.isSameDatabaseServer(conn, dstConn)) {
						this.searchParams.setExtraCondition(searchParams.generateDestinationExclusionClause(conn, dstConn));
					}
				}
				finally {
					dstConn.finalizeConnection();
				}
			}
			
			records = utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
		}
		catch (DBException e) {
			logError("Error Searching on limits: " + getLimits());
			
			throw e;
		}
		
		return records;
	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		if (getRelatedOperationController().getOperationConfig().skipFinalDataVerification()) {
			return false;
		} else {
			
			OpenConnection srcConn = openConnection();
			OpenConnection dstConn = this.dstApp.openConnection();
			
			boolean sameDBSDerver;
			
			try {
				sameDBSDerver = DBUtilities.isSameDatabaseServer(srcConn, dstConn);
			}
			catch (DBException e) {
				throw new RuntimeException(e);
			}
			finally {
				srcConn.finalizeConnection();
				dstConn.finalizeConnection();
			}
			
			if (sameDBSDerver) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public DBQuickMergeController getRelatedOperationController() {
		return (DBQuickMergeController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (getRelatedSyncOperationConfig().writeOperationHistory()) {
			performeSyncOneByOne(syncRecords, conn);
		} else {
			performeBatchSync(syncRecords, conn);
		}
	}
	
	public void performeBatchSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		logInfo("PERFORMING BATCH MERGE ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		Connection srcConn = conn;
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		List<MergingRecord> mergingRecs = new ArrayList<MergingRecord>(syncRecords.size());
		
		try {
			int currObjectId = 0;
			
			if (getSyncTableConfiguration().isManualIdGeneration()) {
				currObjectId = getRelatedOperationController().generateNextStartIdForThread(this, syncRecords);
			}
			
			for (SyncRecord record : syncRecords) {
				DatabaseObject rec = (DatabaseObject) record;
				
				DatabaseObject destObject = null;
				
				MappedTableInfo mappingInfo = getSyncTableConfiguration().getMappedTableInfo();
				
				destObject = mappingInfo.generateMappedObject(rec, this.dstApp);
				
				if (getSyncTableConfiguration().isManualIdGeneration()) {
					destObject.setObjectId(currObjectId++);
				}
				
				mergingRecs.add(new MergingRecord(destObject, getSyncTableConfiguration(), this.srcApp, this.dstApp));
			}
			
			if (finalCheckStatus.notInitialized() && utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
				logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
				syncRecords.removeAll(recordsToIgnoreOnStatistics);
			}
			
			MergingRecord.mergeAll(mergingRecs, srcConn, dstConn);
			
			logInfo("MERGE DONE ON " + syncRecords.size() + " " + getSyncTableConfiguration().getTableName() + "!");
			
			dstConn.markAsSuccessifullyTerminected();
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	private void performeSyncOneByOne(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		logInfo("PERFORMING MERGE ON " + syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + "' ONE-BY-ONE");
		
		int i = 1;
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		int currObjectId = 0;
		
		if (getSyncTableConfiguration().isManualIdGeneration()) {
			currObjectId = getRelatedOperationController().generateNextStartIdForThread(this, syncRecords);
		}
		
		try {
			for (SyncRecord record : syncRecords) {
				String startingStrLog = utilities.garantirXCaracterOnNumber(i,
				    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
				
				boolean wentWrong = true;
				
				DatabaseObject rec = (DatabaseObject) record;
				
				DatabaseObject destObject = null;
				
				MappedTableInfo mappingInfo = getSyncTableConfiguration().getMappedTableInfo();
				
				destObject = mappingInfo.generateMappedObject(rec, this.dstApp);
				
				if (getSyncTableConfiguration().isManualIdGeneration()) {
					destObject.setObjectId(currObjectId++);
				}
				
				MergingRecord data = new MergingRecord(destObject, getSyncTableConfiguration(), this.srcApp, this.dstApp);
				
				try {
					process(data, startingStrLog, 0, conn, dstConn);
					wentWrong = false;
				}
				catch (MissingParentException e) {
					logWarn(
					    startingStrLog + "." + data.getRecord() + " - " + e.getMessage() + " The record will be skipped");
					
					InconsistenceInfo inconsistenceInfo = InconsistenceInfo.generate(rec.generateTableName(),
					    rec.getObjectId(), e.getParentTable(), e.getParentId(), null, e.getOriginAppLocationConde());
					
					inconsistenceInfo.save(getSyncTableConfiguration(), conn);
					
					wentWrong = false;
				}
				catch (ConflictWithRecordNotYetAvaliableException e) {
					logWarn(startingStrLog + ".  Problem while merging record: [" + data.getRecord() + "]! "
					        + e.getLocalizedMessage() + ". Skipping... ");
				}
				catch (DBException e) {
					if (e.isDuplicatePrimaryOrUniqueKeyException()) {
						logWarn(startingStrLog + ".  Problem while merging record: [" + data.getRecord() + "]! "
						        + e.getLocalizedMessage() + ". Skipping... ");
					} else if (e.isIntegrityConstraintViolationException()) {
						logWarn(startingStrLog + ".  Problem while merging record: [" + data.getRecord() + "]! "
						        + e.getLocalizedMessage() + ". Skipping... ");
					} else {
						logWarn(startingStrLog + ".  Problem while merging record: [" + data.getRecord() + "]! "
						        + e.getLocalizedMessage() + ". Skipping... ");
						
						throw e;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
					logWarn(startingStrLog + ".  Problem while merging record: [" + data.getRecord() + "]! "
					        + e.getLocalizedMessage());
					
					throw e;
				}
				finally {
					
					if (wentWrong) {
						if (DBUtilities.isPostgresDB(dstConn)) {
							/*
							 * PosgresSql fails when you continue to use a connection which previously encountered an exception
							 * So we are committing before try to use the connection again
							 * 
							 * NOTE that we are taking risk if some other bug happen and the transaction need to be aborted
							 */
							try {
								dstConn.commit();
							}
							catch (SQLException e) {
								throw new DBException(e);
							}
						}
						
						if (this.finalCheckStatus.notInitialized()) {
							recordsToIgnoreOnStatistics.add(record);
						}
					}
				}
				
				i++;
			}
			
			if (finalCheckStatus.notInitialized() && utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
				logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
				syncRecords.removeAll(recordsToIgnoreOnStatistics);
			}
			
			logInfo("MERGE DONE ON " + syncRecords.size() + " " + getSyncTableConfiguration().getTableName() + "!");
			
			dstConn.markAsSuccessifullyTerminected();
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	private void process(MergingRecord mergingData, String startingStrLog, int reprocessingCount, Connection srcConn,
	        Connection destConn) throws DBException {
		String reprocessingMessage = reprocessingCount == 0 ? "Merging Record"
		        : "Re-merging " + reprocessingCount + " Record";
		
		logDebug(startingStrLog + ": " + reprocessingMessage + ": [" + mergingData.getRecord() + "]");
		
		mergingData.merge(srcConn, destConn);
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DBQuickMergeSearchParams(this.getSyncTableConfiguration(),
		        limits, getRelatedOperationController());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
	
}
