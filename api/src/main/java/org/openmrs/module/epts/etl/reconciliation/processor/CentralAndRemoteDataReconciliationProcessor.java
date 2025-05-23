package org.openmrs.module.epts.etl.reconciliation.processor;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationItemResult;
import org.openmrs.module.epts.etl.reconciliation.controller.CentralAndRemoteDataReconciliationController;
import org.openmrs.module.epts.etl.reconciliation.model.ConciliationReasonType;
import org.openmrs.module.epts.etl.reconciliation.model.DataReconciliationRecord;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class CentralAndRemoteDataReconciliationProcessor extends TaskProcessor<EtlDatabaseObject> {
	
	public CentralAndRemoteDataReconciliationProcessor(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public CentralAndRemoteDataReconciliationController getRelatedOperationController() {
		return (CentralAndRemoteDataReconciliationController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn) throws DBException {
		
		try {
			this.getEngine()
			        .logInfo("PERFORMING DATA RECONCILIATION ON " + etlObjects.size() + "' " + this.getMainSrcTableName());
			
			if (getMainSrcTableName().equalsIgnoreCase("users")) {
				getTaskResultInfo()
				        .addAllToRecordsWithNoError(EtlOperationItemResult.parseFromEtlDatabaseObject(etlObjects));
			} else if (getRelatedOperationController().isMissingRecordsDetector()) {
				performeMissingRecordsCreation(etlObjects, srcConn, dstConn);
			} else if (getRelatedOperationController().isOutdateRecordsDetector()) {
				performeOutdatedRecordsUpdate(etlObjects, srcConn, dstConn);
			} else if (getRelatedOperationController().isPhantomRecordsDetector()) {
				performePhantomRecordsRemotion(etlObjects, srcConn, dstConn);
			}
		}
		finally {
			this.getEngine().logInfo("RECONCILIATION DONE ON " + etlObjects.size() + " " + getMainSrcTableName() + "!");
		}
	}
	
	private void performeMissingRecordsCreation(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn)
	        throws DBException {
		
		int i = 1;
		
		for (EtlDatabaseObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logInfo(startingStrLog + ": Restoring dstRecord: [" + record + "]");
			
			DataReconciliationRecord data = new DataReconciliationRecord((EtlDatabaseObject) record, getSrcConf(),
			        ConciliationReasonType.MISSING);
			
			data.reloadRelatedRecordDataFromRemote(srcConn);
			
			data.consolidateAndSaveData(srcConn);
			
			data.save(srcConn);
			
			getTaskResultInfo().addToRecordsWithNoError(record);
			
			i++;
		}
		
	}
	
	private void performeOutdatedRecordsUpdate(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn)
	        throws DBException {
		
		int i = 1;
		
		for (EtlDatabaseObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logInfo(startingStrLog + ": Updating dstRecord: [" + record + "]");
			
			DataReconciliationRecord.tryToReconciliate((EtlDatabaseObject) record, getSrcConf(), srcConn);
			
			getTaskResultInfo().addToRecordsWithNoError(record);
			i++;
		}
		
	}
	
	private void performePhantomRecordsRemotion(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn)
	        throws DBException {
		
		int i = 1;
		
		for (EtlDatabaseObject record : etlObjects) {
			String startingStrLog = utilities.garantirXCaracterOnNumber(i,
			    ("" + getSearchParams().getQtdRecordPerSelected()).length()) + "/" + etlObjects.size();
			
			logInfo(startingStrLog + ": Removing dstRecord: [" + record + "]");
			
			DataReconciliationRecord data = new DataReconciliationRecord(((EtlDatabaseObject) record).getUuid(),
			        getSrcConf(), ConciliationReasonType.PHANTOM);
			data.reloadRelatedRecordDataFromDestination(srcConn);
			data.removeRelatedRecord(srcConn);
			data.save(srcConn);
			
			getTaskResultInfo().addToRecordsWithNoError(record);
			i++;
		}
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initReloadRecordsWithDefaultParentsTaskProcessor(IntervalExtremeRecord limits) {
		throw new ForbiddenOperationException("Forbiden Method");
	}
}
