package org.openmrs.module.epts.etl.reconciliation.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.reconciliation.controller.CentralAndRemoteDataReconciliationController;
import org.openmrs.module.epts.etl.reconciliation.model.ConciliationReasonType;
import org.openmrs.module.epts.etl.reconciliation.model.DataReconciliationRecord;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class CentralAndRemoteDataReconciliationEngine extends TaskProcessor<EtlDatabaseObject> {
	
	public CentralAndRemoteDataReconciliationEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits) {
		super(monitor, limits);
	}
	
	@Override
	public CentralAndRemoteDataReconciliationController getRelatedOperationController() {
		return (CentralAndRemoteDataReconciliationController) super.getRelatedOperationController();
	}
	
	@Override
	public EtlOperationResultHeader<EtlDatabaseObject> performeSync(List<EtlDatabaseObject> etlObjects,
	        Connection srcConn, Connection dstConn) throws DBException {
		
		EtlOperationResultHeader<EtlDatabaseObject> result = new EtlOperationResultHeader<>(etlObjects);
		
		try {
			this.getMonitor()
			        .logInfo("PERFORMING DATA RECONCILIATION ON " + etlObjects.size() + "' " + this.getMainSrcTableName());
			
			if (getMainSrcTableName().equalsIgnoreCase("users")) {
				result.addAllToRecordsWithNoError(etlObjects);
			} else if (getRelatedOperationController().isMissingRecordsDetector()) {
				result = performeMissingRecordsCreation(etlObjects, srcConn, dstConn);
			} else if (getRelatedOperationController().isOutdateRecordsDetector()) {
				result = performeOutdatedRecordsUpdate(etlObjects, srcConn, dstConn);
			} else if (getRelatedOperationController().isPhantomRecordsDetector()) {
				result = performePhantomRecordsRemotion(etlObjects, srcConn, dstConn);
			}
		}
		finally {
			this.getMonitor().logInfo("RECONCILIATION DONE ON " + etlObjects.size() + " " + getMainSrcTableName() + "!");
		}
		
		return result;
	}
	
	private EtlOperationResultHeader<EtlDatabaseObject> performeMissingRecordsCreation(List<EtlDatabaseObject> etlObjects,
	        Connection srcConn, Connection dstConn) throws DBException {
		
		EtlOperationResultHeader<EtlDatabaseObject> result = new EtlOperationResultHeader<>(getLimits());
		
		int i = 1;
		
		for (EtlDatabaseObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logInfo(startingStrLog + ": Restoring record: [" + record + "]");
			
			DataReconciliationRecord data = new DataReconciliationRecord((EtlDatabaseObject) record, getSrcConf(),
			        ConciliationReasonType.MISSING);
			
			data.reloadRelatedRecordDataFromRemote(srcConn);
			
			data.consolidateAndSaveData(srcConn);
			
			data.save(srcConn);
			
			result.addToRecordsWithNoError(record);
			
			i++;
		}
		
		return result;
	}
	
	private EtlOperationResultHeader<EtlDatabaseObject> performeOutdatedRecordsUpdate(List<EtlDatabaseObject> etlObjects,
	        Connection srcConn, Connection dstConn) throws DBException {
		
		EtlOperationResultHeader<EtlDatabaseObject> result = new EtlOperationResultHeader<>(getLimits());
		
		int i = 1;
		
		for (EtlDatabaseObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logInfo(startingStrLog + ": Updating record: [" + record + "]");
			
			DataReconciliationRecord.tryToReconciliate((EtlDatabaseObject) record, getSrcConf(), srcConn);
			
			result.addToRecordsWithNoError(record);
			i++;
		}
		
		return result;
	}
	
	private EtlOperationResultHeader<EtlDatabaseObject> performePhantomRecordsRemotion(List<EtlDatabaseObject> etlObjects,
	        Connection srcConn, Connection dstConn) throws DBException {
		
		EtlOperationResultHeader<EtlDatabaseObject> result = new EtlOperationResultHeader<>(getLimits());
		
		int i = 1;
		
		for (EtlDatabaseObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logInfo(startingStrLog + ": Removing record: [" + record + "]");
			
			DataReconciliationRecord data = new DataReconciliationRecord(((EtlDatabaseObject) record).getUuid(),
			        getSrcConf(), ConciliationReasonType.PHANTOM);
			data.reloadRelatedRecordDataFromDestination(srcConn);
			data.removeRelatedRecord(srcConn);
			data.save(srcConn);
			
			result.addToRecordsWithNoError(record);
			i++;
		}
		
		return result;
	}
	
}
