package org.openmrs.module.epts.etl.dbquickmerge.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.DstConf;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.dbquickmerge.model.DBQuickMergeSearchParams;
import org.openmrs.module.epts.etl.dbquickmerge.model.MergingRecord;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ConflictWithRecordNotYetAvaliableException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.SearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see EtlController
 */
public class DBQuickMergeEngine extends Engine {
	
	public DBQuickMergeEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		
		DBQuickMergeSearchParams searchParams = (DBQuickMergeSearchParams) getSearchParams();
		
		if (getFinalCheckStatus().onGoing()) {
			OpenConnection dstConn = this.getDstApp().openConnection();
			
			try {
				if (DBUtilities.isSameDatabaseServer(conn, dstConn)) {
					this.searchParams.setExtraCondition(searchParams.generateDestinationExclusionClause(conn, dstConn));
				}
			}
			finally {
				dstConn.finalizeConnection();
			}
		}
		
		return utilities.parseList(SearchParamsDAO.search(this.searchParams, conn), SyncRecord.class);
	}
	
	public AppInfo getDstApp() {
		return this.getRelatedOperationController().getDstApp();
	}
	
	public AppInfo getSrcApp() {
		return this.getRelatedOperationController().getSrcApp();
	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		if (getRelatedOperationController().getOperationConfig().skipFinalDataVerification()) {
			return false;
		} else {
			
			OpenConnection srcConn = openConnection();
			OpenConnection dstConn = this.getDstApp().openConnection();
			
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
		if (getRelatedSyncOperationConfig().writeOperationHistory()
		        || getEtlConfiguration().getMainSrcTableConf().hasWinningRecordsInfo()) {
			performeSyncOneByOne(syncRecords, conn);
		} else {
			performeBatchSync(syncRecords, conn);
		}
	}
	
	public void performeBatchSync(List<SyncRecord> syncRecords, Connection srcConn) throws DBException {
		logInfo("PERFORMING MERGE ON " + syncRecords.size() + "' " + getMainSrcTableName());
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		Map<String, List<MergingRecord>> mergingRecs = new HashMap<>();
		
		try {
			int currObjectId = 0;
			
			if (getMainSrcTableConf().isManualIdGeneration()) {
				currObjectId = getRelatedOperationController().generateNextStartIdForThread(this, syncRecords);
			}
			
			for (SyncRecord record : syncRecords) {
				DatabaseObject rec = (DatabaseObject) record;
				
				for (DstConf mappingInfo : getEtlConfiguration().getDstConf()) {
					
					DatabaseObject destObject = null;
					
					destObject = mappingInfo.generateMappedObject(rec, srcConn, this.getSrcApp(), this.getDstApp());
					
					if (destObject != null) {
						if (getMainSrcTableConf().isManualIdGeneration()) {
							destObject.setObjectId(currObjectId++);
						}
						
						MergingRecord mr = new MergingRecord(destObject, mappingInfo.getDstTableConf(), this.getSrcApp(),
						        this.getDstApp(), false);
						
						if (mergingRecs.get(mappingInfo.getDstTableConf().getTableName()) == null) {
							mergingRecs.put(mappingInfo.getDstTableConf().getTableName(),
							    new ArrayList<>(syncRecords.size()));
						}
						
						mergingRecs.get(mappingInfo.getDstTableConf().getTableName()).add(mr);
					}
				}
			}
			
			if (finalCheckStatus.notInitialized() && utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
				logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
				syncRecords.removeAll(recordsToIgnoreOnStatistics);
			}
			
			MergingRecord.mergeAll(mergingRecs, srcConn, dstConn);
			
			logInfo("MERGE DONE ON " + syncRecords.size() + " " + getMainSrcTableName());
			
			dstConn.markAsSuccessifullyTerminated();
		}
		catch (Exception e) {
			logWarn("Error ocurred on thread " + getEngineId() + " On Records [" + getLimits()
			        + "]... \n Try to performe merge record by record...");
			
			performeSyncOneByOne(syncRecords, srcConn);
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	private void performeSyncOneByOne(List<SyncRecord> syncRecords, Connection srcConn) throws DBException {
		logInfo("PERFORMING MERGE ON " + syncRecords.size() + "' " + getMainSrcTableName() + "' ONE-BY-ONE");
		
		int i = 1;
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		int currObjectId = 0;
		
		if (getMainSrcTableConf().isManualIdGeneration()) {
			currObjectId = getRelatedOperationController().generateNextStartIdForThread(this, syncRecords);
		}
		
		try {
			for (SyncRecord record : syncRecords) {
				String startingStrLog = utilities.garantirXCaracterOnNumber(i,
				    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
				
				boolean wentWrong = true;
				
				DatabaseObject rec = (DatabaseObject) record;
				
				for (DstConf dstConf : getEtlConfiguration().getDstConf()) {
					
					DatabaseObject destObject = null;
					
					destObject = dstConf.generateMappedObject(rec, srcConn, this.getSrcApp(), this.getDstApp());
					
					if (destObject == null) {
						continue;
					}
					
					if (getMainSrcTableConf().isManualIdGeneration()) {
						destObject.setObjectId(currObjectId++);
					}
					
					boolean wrt = writeOperationHistory();
					
					MergingRecord data = new MergingRecord(destObject, dstConf.getDstTableConf(), this.getSrcApp(),
					        this.getDstApp(), wrt);
					
					try {
						process(data, startingStrLog, 0, srcConn, dstConn);
						wentWrong = false;
					}
					catch (MissingParentException e) {
						logWarn(startingStrLog + "." + data.getRecord() + " - " + e.getMessage()
						        + " The record will be skipped");
						
						InconsistenceInfo inconsistenceInfo = InconsistenceInfo.generate(rec.generateTableName(),
						    rec.getObjectId(), e.getParentTable(), e.getParentId(), null, e.getOriginAppLocationConde());
						
						inconsistenceInfo.save(dstConf.getDstTableConf(), srcConn);
						
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
			}
			
			if (finalCheckStatus.notInitialized() && utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
				logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
				syncRecords.removeAll(recordsToIgnoreOnStatistics);
			}
			
			logInfo("MERGE DONE ON " + syncRecords.size() + " " + getMainSrcTableName() + "!");
			
			dstConn.markAsSuccessifullyTerminated();
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
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new DBQuickMergeSearchParams(this.getEtlConfiguration(),
		        limits, getRelatedOperationController());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
