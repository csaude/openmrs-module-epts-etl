package org.openmrs.module.epts.etl.etl.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.etl.model.LoadRecord;
import org.openmrs.module.epts.etl.exceptions.ConflictWithRecordNotYetAvaliableException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.model.pojo.generic.Oid;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * @author jpboane
 * @see DbExtractController
 */
public class EtlEngine extends TaskProcessor<EtlDatabaseObject> {
	
	public EtlEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits, boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> getSearchParams() {
		return (EtlDatabaseObjectSearchParams) super.getSearchParams();
	}
	
	public DBConnectionInfo getDstConnInfo() {
		return this.getRelatedOperationController().getDstConnInfo();
	}
	
	public DBConnectionInfo getSrcConnInfo() {
		return this.getRelatedOperationController().getSrcConnInfo();
	}
	
	@Override
	public EtlController getRelatedOperationController() {
		return (EtlController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn) throws DBException {
		if (getRelatedEtlOperationConfig().writeOperationHistory()
		        || getEtlConfiguration().getSrcConf().hasWinningRecordsInfo()) {
			performeSyncOneByOne(etlObjects, srcConn, dstConn);
		} else {
			performeBatchSync(etlObjects, srcConn, dstConn);
		}
	}
	
	public void performeBatchSync(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn)
	        throws DBException {
		logInfo("PERFORMING ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] ON " + etlObjects.size()
		        + "' RECORDS");
		
		Map<String, List<LoadRecord>> mergingRecs = new HashMap<>();
		
		try {
			
			for (EtlObject record : etlObjects) {
				EtlDatabaseObject rec = (EtlDatabaseObject) record;
				
				for (DstConf mappingInfo : getEtlConfiguration().getDstConf()) {
					
					logTrace("Transforming record " + rec);
					
					EtlDatabaseObject destObject = transform(rec, mappingInfo, etlObjects, srcConn, dstConn);
					
					if (destObject != null) {
						logTrace("record " + rec + " transforming to " + dstConn);
						
						LoadRecord etlRec = initEtlRecord(destObject, mappingInfo, false);
						
						if (mergingRecs.get(mappingInfo.getTableName()) == null) {
							mergingRecs.put(mappingInfo.getTableName(), new ArrayList<>(etlObjects.size()));
						}
						
						mergingRecs.get(mappingInfo.getTableName()).add(etlRec);
					} else {
						logTrace("The record " + rec + " could not be transformed");
					}
				}
			}
			
			logDebug("Initializing the loading of " + etlObjects.size() + " records...");
			
			LoadRecord.loadAll(mergingRecs, srcConn, dstConn);
			
			logDebug("Performing after etl on " + etlObjects.size() + " records!");
			
			afterEtl(etlObjects, srcConn, dstConn);
			
			logInfo(
			    "ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] DONE ON " + etlObjects.size() + "' RECORDS");
		}
		catch (Exception e) {
			logWarn("Error ocurred on thread " + getEngineId() + " On Records [" + getLimits() + "]... \n");
			
			getTaskResultInfo().setFatalException(e);
		}
		
	}
	
	private void performeSyncOneByOne(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn)
	        throws DBException {
		logInfo("PERFORMING ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] ON " + etlObjects.size()
		        + "' RECORDS");
		
		int i = 1;
		
		for (EtlDatabaseObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
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
					afterEtl(utilities.parseObjectToList_(record, EtlDatabaseObject.class), srcConn, dstConn);
					
					getTaskResultInfo().addToRecordsWithNoError(rec);
				}
				catch (MissingParentException e) {
					logWarn(
					    startingStrLog + "." + data.getRecord() + " - " + e.getMessage() + " The record will be skipped");
					
					InconsistenceInfo inconsistenceInfo = InconsistenceInfo.generate(rec.generateTableName(),
					    rec.getObjectId(), e.getParentTable(), e.getParentId(), null, e.getOriginAppLocationConde());
					
					getTaskResultInfo().add(new EtlOperationItemResult<>(rec, inconsistenceInfo));
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
				
				i++;
				
			}
		}
		
		logInfo("ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] DONE ON " + etlObjects.size() + "' RECORDS");
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
		
		EtlDatabaseObject transformed = mappingInfo.transform(rec, srcConn, this.getSrcConnInfo(), this.getDstConnInfo());
		
		if (transformed != null) {
			transformed.setSrcRelatedObject(rec);
			
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
	        Connection destConnn) throws DBException {
		String reprocessingMessage = reprocessingCount == 0 ? "Merging Record"
		        : "Re-merging " + reprocessingCount + " Record";
		
		logDebug(startingStrLog + ": " + reprocessingMessage + ": [" + etlData.getRecord() + "]");
		
		etlData.load(srcConn, destConnn);
	}
	
	public LoadRecord initEtlRecord(EtlDatabaseObject destObject, DstConf mappingInfo, boolean writeOperationHistory) {
		return new LoadRecord(destObject, getSrcConf(), mappingInfo, this, writeOperationHistory);
	}
	
	public void afterEtl(List<EtlDatabaseObject> objs, Connection srcConn, Connection dstConn) throws DBException {
	}
}
