package org.openmrs.module.epts.etl.etl.re_etl.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.etl.model.EtlRecord;
import org.openmrs.module.epts.etl.etl.re_etl.controller.ReEtlController;
import org.openmrs.module.epts.etl.exceptions.ConflictWithRecordNotYetAvaliableException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DbExtractController
 */
public class ReEtlEngine extends EtlEngine {
	
	public ReEtlEngine(EngineMonitor monitor, ThreadRecordIntervalsManager limits) {
		super(monitor, limits);
	}
	
	@Override
	protected boolean mustDoFinalCheck() {
		return false;
	}
	
	@Override
	public ReEtlController getRelatedOperationController() {
		return (ReEtlController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException {
		logInfo("PERFORMING RE ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] ON " + etlObjects.size()
		        + "' RECORDS");
		
		int i = 1;
		
		Connection srcConn = conn;
		
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
					
					destObject.loadObjectIdData(mappingInfo);
					
					boolean wrt = writeOperationHistory();
					
					EtlRecord data = new EtlRecord(destObject, getSrcConf(), mappingInfo, this, wrt);
					
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
			
			logInfo("RE ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] DONE ON " + etlObjects.size()
			        + "' RECORDS");
			
			dstConn.markAsSuccessifullyTerminated();
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	private void process(EtlRecord etlData, String startingStrLog, int reprocessingCount, Connection srcConn,
	        Connection destConn) throws DBException {
		String reprocessingMessage = reprocessingCount == 0 ? "Re Merging Record"
		        : "Re re-merging " + reprocessingCount + " Record";
		
		logDebug(startingStrLog + ": " + reprocessingMessage + ": [" + etlData.getRecord() + "]");
		
		etlData.reLoad(srcConn, destConn);
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadRecordIntervalsManager limits,
	        Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new EtlDatabaseObjectSearchParams(
		        this.getEtlConfiguration(), limits, this);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getRelatedSyncConfiguration().getStartDate());
		
		return searchParams;
	}
	
}
