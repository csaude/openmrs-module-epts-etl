package org.openmrs.module.epts.etl.dbquickmerge.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.dbextract.controller.DbExtractController;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.dbquickmerge.model.DBQuickMergeSearchParams;
import org.openmrs.module.epts.etl.dbquickmerge.model.QuickMergeRecord;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.exceptions.ConflictWithRecordNotYetAvaliableException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DbExtractController
 */
public class DBQuickMergeEngine extends EtlEngine {
	
	public DBQuickMergeEngine(EngineMonitor monitor, ThreadRecordIntervalsManager limits) {
		super(monitor, limits);
	}
	
	@Override
	public DBQuickMergeController getRelatedOperationController() {
		return (DBQuickMergeController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException {
		if (getRelatedSyncOperationConfig().writeOperationHistory()
		        || getEtlConfiguration().getSrcConf().hasWinningRecordsInfo()) {
			performeSyncOneByOne(etlObjects, conn);
		} else {
			performeBatchSync(etlObjects, conn);
		}
	}
	
	public void performeBatchSync(List<? extends EtlObject> etlObjects, Connection srcConn) throws DBException {
		logInfo("PERFORMING MERGE ON " + etlObjects.size() + "' " + getMainSrcTableName());
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		
		List<EtlObject> recordsToIgnoreOnStatistics = new ArrayList<EtlObject>();
		
		Map<String, List<QuickMergeRecord>> mergingRecs = new HashMap<>();
		List<String> mapOrder = new ArrayList<>();
		
		try {
			
			for (EtlObject record : etlObjects) {
				EtlDatabaseObject rec = (EtlDatabaseObject) record;
				
				for (DstConf mappingInfo : getEtlConfiguration().getDstConf()) {
					
					EtlDatabaseObject destObject = null;
					
					destObject = mappingInfo.transform(rec, srcConn, this.getSrcApp(), this.getDstApp());
					
					if (destObject != null) {
						if (!mappingInfo.isAutoIncrementId() && mappingInfo.useSimpleNumericPk()) {
							
							int currObjectId = mappingInfo.generateNextStartIdForThread(etlObjects, dstConn);
							
							destObject.setObjectId(Oid.fastCreate(
							    mappingInfo.getPrimaryKey().retrieveSimpleKeyColumnNameAsClassAtt(), currObjectId++));
						} else {
							destObject.loadObjectIdData(mappingInfo);
						}
						
						QuickMergeRecord mr = new QuickMergeRecord(destObject, getSrcConf(), mappingInfo, this.getSrcApp(),
						        this.getDstApp(), false);
						
						if (mergingRecs.get(mappingInfo.getTableName()) == null) {
							mapOrder.add(mappingInfo.getTableName());
							
							mergingRecs.put(mappingInfo.getTableName(), new ArrayList<>(etlObjects.size()));
						}
						
						mergingRecs.get(mappingInfo.getTableName()).add(mr);
					}
				}
			}
			
			if (finalCheckStatus.notInitialized() && utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
				logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
				etlObjects.removeAll(recordsToIgnoreOnStatistics);
			}
			
			QuickMergeRecord.mergeAll(mapOrder, mergingRecs, srcConn, dstConn);
			
			logInfo("MERGE DONE ON " + etlObjects.size() + " " + getMainSrcTableName());
			
			dstConn.markAsSuccessifullyTerminated();
		}
		catch (Exception e) {
			e.printStackTrace();
			
			logWarn("Error ocurred on thread " + getEngineId() + " On Records [" + getLimits()
			        + "]... \n Try to performe merge record by record...");
			
			performeSyncOneByOne(etlObjects, srcConn);
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	private void performeSyncOneByOne(List<? extends EtlObject> etlObjects, Connection srcConn) throws DBException {
		logInfo("PERFORMING MERGE ON " + etlObjects.size() + "' " + getMainSrcTableName() + "' ONE-BY-ONE");
		
		int i = 1;
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		
		List<EtlObject> recordsToIgnoreOnStatistics = new ArrayList<EtlObject>();
		
		try {
			for (EtlObject record : etlObjects) {
				String startingStrLog = utilities.garantirXCaracterOnNumber(i,
				    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
				
				boolean wentWrong = true;
				
				EtlDatabaseObject rec = (EtlDatabaseObject) record;
				
				for (DstConf mappingInfo : getEtlConfiguration().getDstConf()) {
					
					EtlDatabaseObject destObject = null;
					
					destObject = mappingInfo.transform(rec, srcConn, this.getSrcApp(), this.getDstApp());
					
					if (destObject == null) {
						continue;
					}
					
					if (!mappingInfo.isAutoIncrementId() && mappingInfo.useSimpleNumericPk()) {
						int currObjectId = mappingInfo.generateNextStartIdForThread(etlObjects, dstConn);
						
						destObject.setObjectId(Oid.fastCreate(
						    mappingInfo.getPrimaryKey().retrieveSimpleKeyColumnNameAsClassAtt(), currObjectId++));
					} else {
						destObject.loadObjectIdData(mappingInfo);
					}
					
					boolean wrt = writeOperationHistory();
					
					QuickMergeRecord data = new QuickMergeRecord(destObject, getSrcConf(), mappingInfo, this.getSrcApp(),
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
				etlObjects.removeAll(recordsToIgnoreOnStatistics);
			}
			
			logInfo("MERGE DONE ON " + etlObjects.size() + " " + getMainSrcTableName() + "!");
			
			dstConn.markAsSuccessifullyTerminated();
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	private void process(QuickMergeRecord mergingData, String startingStrLog, int reprocessingCount, Connection srcConn,
	        Connection destConn) throws DBException {
		String reprocessingMessage = reprocessingCount == 0 ? "Merging Record"
		        : "Re-merging " + reprocessingCount + " Record";
		
		logDebug(startingStrLog + ": " + reprocessingMessage + ": [" + mergingData.getRecord() + "]");
		
		mergingData.merge(srcConn, destConn);
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadRecordIntervalsManager limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new DBQuickMergeSearchParams(this.getEtlConfiguration(),
		        limits, this);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
