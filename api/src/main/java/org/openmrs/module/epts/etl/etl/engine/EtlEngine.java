package org.openmrs.module.epts.etl.etl.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.dbextract.controller.DbExtractController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.model.EtlRecord;
import org.openmrs.module.epts.etl.etl.model.EtlSearchParams;
import org.openmrs.module.epts.etl.exceptions.ConflictWithRecordNotYetAvaliableException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.DatabaseObjectSearchParamsDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DbExtractController
 */
public class EtlEngine extends Engine {
	
	public EtlEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public List<SyncRecord> searchNextRecords(Connection conn) throws DBException {
		return utilities.parseList(
		    DatabaseObjectSearchParamsDAO.search((DatabaseObjectSearchParams) this.searchParams, conn), SyncRecord.class);
	}
	
	public AppInfo getDstApp() {
		return this.getRelatedOperationController().getDstApp();
	}
	
	public AppInfo getSrcApp() {
		return this.getRelatedOperationController().getSrcApp();
	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public EtlController getRelatedOperationController() {
		return (EtlController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (getRelatedSyncOperationConfig().writeOperationHistory()
		        || getEtlConfiguration().getSrcConf().hasWinningRecordsInfo()) {
			performeSyncOneByOne(syncRecords, conn);
		} else {
			performeBatchSync(syncRecords, conn);
		}
	}
	
	public void performeBatchSync(List<SyncRecord> syncRecords, Connection srcConn) throws DBException {
		logInfo("PERFORMING ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] ON " + syncRecords.size()
		        + "' RECORDS");
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		Map<String, List<EtlRecord>> mergingRecs = new HashMap<>();
		
		try {
			
			for (SyncRecord record : syncRecords) {
				DatabaseObject rec = (DatabaseObject) record;
				
				for (DstConf mappingInfo : getEtlConfiguration().getDstConf()) {
					
					DatabaseObject destObject = null;
					
					destObject = mappingInfo.generateDstObject(rec, srcConn, this.getSrcApp(), this.getDstApp());
					
					if (destObject != null) {
						if (!mappingInfo.isAutoIncrementId() && mappingInfo.useSimpleNumericPk()) {
							
							int currObjectId = mappingInfo.generateNextStartIdForThread(syncRecords, dstConn);
							
							destObject.setObjectId(Oid.fastCreate(
							    mappingInfo.getPrimaryKey().retrieveSimpleKeyColumnNameAsClassAtt(), currObjectId++));
						} else {
							destObject.loadObjectIdData(mappingInfo);
						}
						
						EtlRecord etlRec = new EtlRecord(destObject, mappingInfo, false);
						
						if (mergingRecs.get(mappingInfo.getTableName()) == null) {
							mergingRecs.put(mappingInfo.getTableName(), new ArrayList<>(syncRecords.size()));
						}
						
						mergingRecs.get(mappingInfo.getTableName()).add(etlRec);
					}
				}
			}
			
			if (finalCheckStatus.notInitialized() && utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
				logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
				syncRecords.removeAll(recordsToIgnoreOnStatistics);
			}
			
			EtlRecord.mergeAll(mergingRecs, srcConn, dstConn);
			
			logInfo(
			    "ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] DONE ON " + syncRecords.size() + "' RECORDS");
			
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
		logInfo("PERFORMING ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] ON " + syncRecords.size()
		        + "' RECORDS");
		
		int i = 1;
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		
		List<SyncRecord> recordsToIgnoreOnStatistics = new ArrayList<SyncRecord>();
		
		try {
			for (SyncRecord record : syncRecords) {
				String startingStrLog = utilities.garantirXCaracterOnNumber(i,
				    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + syncRecords.size();
				
				boolean wentWrong = true;
				
				DatabaseObject rec = (DatabaseObject) record;
				
				for (DstConf mappingInfo : getEtlConfiguration().getDstConf()) {
					
					DatabaseObject destObject = null;
					
					destObject = mappingInfo.generateDstObject(rec, srcConn, this.getSrcApp(), this.getDstApp());
					
					if (destObject == null) {
						continue;
					}
					
					if (!mappingInfo.isAutoIncrementId() && mappingInfo.useSimpleNumericPk()) {
						
						int currObjectId = mappingInfo.generateNextStartIdForThread(syncRecords, dstConn);
						
						destObject.setObjectId(Oid.fastCreate(
						    mappingInfo.getPrimaryKey().retrieveSimpleKeyColumnNameAsClassAtt(), currObjectId++));
					} else {
						destObject.loadObjectIdData(mappingInfo);
					}
					
					boolean wrt = writeOperationHistory();
					
					EtlRecord data = new EtlRecord(destObject, mappingInfo, wrt);
					
					try {
						process(data, startingStrLog, 0, srcConn, dstConn);
						wentWrong = false;
					}
					catch (MissingParentException e) {
						logWarn(startingStrLog + "." + data.getRecord() + " - " + e.getMessage()
						        + " The record will be skipped");
						
						InconsistenceInfo inconsistenceInfo = InconsistenceInfo.generate(rec.generateTableName(),
						    rec.getObjectId(), e.getParentTable(), e.getParentId(), null, e.getOriginAppLocationConde());
						
						inconsistenceInfo.save(mappingInfo, srcConn);
						
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
			
			logInfo(
			    "ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] DONE ON " + syncRecords.size() + "' RECORDS");
			
			dstConn.markAsSuccessifullyTerminated();
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	private void process(EtlRecord etlData, String startingStrLog, int reprocessingCount, Connection srcConn,
	        Connection destConn) throws DBException {
		String reprocessingMessage = reprocessingCount == 0 ? "Merging Record"
		        : "Re-merging " + reprocessingCount + " Record";
		
		logDebug(startingStrLog + ": " + reprocessingMessage + ": [" + etlData.getRecord() + "]");
		
		etlData.merge(srcConn, destConn);
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new EtlSearchParams(this.getEtlConfiguration(), limits,
		        getRelatedOperationController());
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
