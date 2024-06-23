package org.openmrs.module.epts.etl.etl.re_etl.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.etl.engine.EtlEngine;
import org.openmrs.module.epts.etl.etl.model.LoadRecord;
import org.openmrs.module.epts.etl.etl.re_etl.controller.ReEtlController;
import org.openmrs.module.epts.etl.exceptions.ConflictWithRecordNotYetAvaliableException;
import org.openmrs.module.epts.etl.exceptions.MissingParentException;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceInfo;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;

/**
 * @author jpboane
 * @see DbExtractController
 */
public class ReEtlEngine extends EtlEngine {
	
	public ReEtlEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits, boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public ReEtlController getRelatedOperationController() {
		return (ReEtlController) super.getRelatedOperationController();
	}
	
	@Override
	public EtlOperationResultHeader<EtlDatabaseObject> performeSync(List<EtlDatabaseObject> etlObjects, Connection srcConn,
	        Connection dstConn) throws DBException {
		logInfo("PERFORMING RE ETL OPERATION [" + getEtlConfiguration().getConfigCode() + "] ON " + etlObjects.size()
		        + "' RECORDS");
		
		int i = 1;
		
		EtlOperationResultHeader<EtlDatabaseObject> result = new EtlOperationResultHeader<>(getLimits());
		
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
				
				LoadRecord data = new LoadRecord(destObject, getSrcConf(), mappingInfo, this, wrt);
				
				try {
					process(data, startingStrLog, 0, srcConn, dstConn);
					
					result.addToRecordsWithNoError(rec);
					
					wentWrong = false;
				}
				catch (MissingParentException e) {
					logWarn(
					    startingStrLog + "." + data.getRecord() + " - " + e.getMessage() + " The record will be skipped");
					
					InconsistenceInfo inconsistenceInfo = InconsistenceInfo.generate(rec.generateTableName(),
					    rec.getObjectId(), e.getParentTable(), e.getParentId(), null, e.getOriginAppLocationConde());
					
					inconsistenceInfo.save(mappingInfo, srcConn);
					
					result.addToRecordsWithResolvedErrors(rec, inconsistenceInfo);
					
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
					}
				}
				
				i++;
				
			}
		}
		
		return result;
	}
	
	private void process(LoadRecord etlData, String startingStrLog, int reprocessingCount, Connection srcConn,
	        Connection dstConnn) throws DBException {
		String reprocessingMessage = reprocessingCount == 0 ? "Re Merging Record"
		        : "Re re-merging " + reprocessingCount + " Record";
		
		logDebug(startingStrLog + ": " + reprocessingMessage + ": [" + etlData.getRecord() + "]");
		
		etlData.reLoad(srcConn, dstConnn);
	}
	
}
