package org.openmrs.module.epts.etl.etl.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.etl.model.LoadRecord;
import org.openmrs.module.epts.etl.exceptions.ConflictWithRecordNotYetAvaliableException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseOperationHeaderResult;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DbExtractController
 */
public class EtlEngine extends TaskProcessor {
	
	public EtlEngine(Engine monitor, ThreadRecordIntervalsManager limits) {
		super(monitor, limits);
	}
	
	@Override
	public AbstractEtlSearchParams<? extends EtlObject> getSearchParams() {
		return (EtlDatabaseObjectSearchParams) super.getSearchParams();
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
			OpenConnection srcConn = null;
			OpenConnection dstConn = null;
			
			try {
				srcConn = openConnection();
				dstConn = this.getDstApp().openConnection();
				
				if (DBUtilities.isSameDatabaseServer(srcConn, dstConn)) {
					return utilities.stringHasValue(getSearchParams().generateDestinationExclusionClause(srcConn, dstConn));
				} else {
					return false;
				}
			}
			catch (DBException e) {
				throw new RuntimeException(e);
			}
			finally {
				if (srcConn != null)
					srcConn.finalizeConnection();
				if (dstConn != null)
					dstConn.finalizeConnection();
			}
		}
	}
	
	@Override
	public EtlController getRelatedOperationController() {
		return (EtlController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException {
		if (getRelatedEtlOperationConfig().writeOperationHistory()
		        || getEtlConfiguration().getSrcConf().hasWinningRecordsInfo()) {
			performeSyncOneByOne(etlObjects, conn);
		} else {
			performeBatchSync(etlObjects, conn);
		}
	}
	
	public void performeBatchSync(List<? extends EtlObject> etlObjects, Connection srcConn) throws DBException {
		logInfo("PERFORMING ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] ON " + etlObjects.size()
		        + "' RECORDS");
		
		OpenConnection dstConn = getRelatedOperationController().openDstConnection();
		
		List<EtlObject> recordsToIgnoreOnStatistics = new ArrayList<EtlObject>();
		
		Map<String, List<LoadRecord>> mergingRecs = new HashMap<>();
		
		try {
			
			for (EtlObject record : etlObjects) {
				EtlDatabaseObject rec = (EtlDatabaseObject) record;
				
				for (DstConf mappingInfo : getEtlConfiguration().getDstConf()) {
					
					EtlDatabaseObject destObject = transform(rec, mappingInfo, etlObjects, srcConn, dstConn);
					
					if (destObject != null) {
						LoadRecord etlRec = initEtlRecord(destObject, mappingInfo, false);
						
						if (mergingRecs.get(mappingInfo.getTableName()) == null) {
							mergingRecs.put(mappingInfo.getTableName(), new ArrayList<>(etlObjects.size()));
						}
						
						mergingRecs.get(mappingInfo.getTableName()).add(etlRec);
					}
				}
			}
			
			if (finalCheckStatus.notInitialized() && utilities.arrayHasElement(recordsToIgnoreOnStatistics)) {
				logWarn(recordsToIgnoreOnStatistics.size() + " not successifuly processed. Removing them on statistics");
				etlObjects.removeAll(recordsToIgnoreOnStatistics);
			}
			
			DatabaseOperationHeaderResult result = LoadRecord.loadAll(mergingRecs, srcConn, dstConn);
			
			if (!result.hasFatalError()) {
				afterEtl(result.getRecordsWithNoError(), srcConn, dstConn);
				
				if (result.hasRecordsWithUnresolvedErrors()) {
					logWarn("Some errors where found loading '" + result.getRecordsWithUnresolvedErrors().size()
					        + "! The errors will be documented");
					
					result.documentErrors(srcConn, dstConn);
				}
				
			} else {
				if (result.hasRecordsWithUnresolvedErrors()) {
					logError("Fatal error found loading some records. The process will be aborted");
					
					result.printStackErrorOfFatalErrors();
					
				}
			}
			
			logInfo(
			    "ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] DONE ON " + etlObjects.size() + "' RECORDS");
			
			dstConn.markAsSuccessifullyTerminated();
		}
		catch (Exception e) {
			logWarn("Error ocurred on thread " + getEngineId() + " On Records [" + getLimits() + "]... \n");
			
			e.printStackTrace();
			
			throw e;
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	private void performeSyncOneByOne(List<? extends EtlObject> etlObjects, Connection srcConn) throws DBException {
		logInfo("PERFORMING ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] ON " + etlObjects.size()
		        + "' RECORDS");
		
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
					
					EtlDatabaseObject destObject = transform(rec, mappingInfo, etlObjects, srcConn, dstConn);
					
					if (destObject == null) {
						continue;
					}
					
					boolean wrt = writeOperationHistory();
					
					LoadRecord data = initEtlRecord(destObject, mappingInfo, wrt);
					
					try {
						process(data, startingStrLog, 0, srcConn, dstConn);
						afterEtl(utilities.parseObjectToList_(record, rec.getClass()), srcConn, dstConn);
						
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
			
			logInfo(
			    "ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] DONE ON " + etlObjects.size() + "' RECORDS");
			
			dstConn.markAsSuccessifullyTerminated();
		}
		finally {
			dstConn.finalizeConnection();
		}
	}
	
	/**
	 * @param srcConn
	 * @param rec
	 * @param mappingInfo
	 * @return
	 * @throws DBException
	 * @throws ForbiddenOperationException
	 */
	public EtlDatabaseObject transform(EtlDatabaseObject rec, DstConf mappingInfo, List<? extends EtlObject> etlObjects,
	        Connection srcConn, Connection dstConn) throws DBException, ForbiddenOperationException {
		
		EtlDatabaseObject transformed = mappingInfo.transform(rec, srcConn, this.getSrcApp(), this.getDstApp());
		
		if (transformed != null) {
			
			if (!mappingInfo.isAutoIncrementId() && mappingInfo.useSimpleNumericPk()) {
				
				int currObjectId = mappingInfo.generateNextStartIdForThread(etlObjects, dstConn);
				
				transformed.setObjectId(
				    Oid.fastCreate(mappingInfo.getPrimaryKey().retrieveSimpleKeyColumnNameAsClassAtt(), currObjectId++));
			} else {
				transformed.loadObjectIdData(mappingInfo);
			}
		}
		return transformed;
	}
	
	private void process(LoadRecord etlData, String startingStrLog, int reprocessingCount, Connection srcConn,
	        Connection destConn) throws DBException {
		String reprocessingMessage = reprocessingCount == 0 ? "Merging Record"
		        : "Re-merging " + reprocessingCount + " Record";
		
		logDebug(startingStrLog + ": " + reprocessingMessage + ": [" + etlData.getRecord() + "]");
		
		etlData.load(srcConn, destConn);
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
	
	public LoadRecord initEtlRecord(EtlDatabaseObject destObject, DstConf mappingInfo, boolean writeOperationHistory) {
		return new LoadRecord(destObject, getSrcConf(), mappingInfo, this, writeOperationHistory);
	}
	
	public void afterEtl(List<? extends EtlObject> objs, Connection srcConn, Connection dstConn) throws DBException {
	}
}
