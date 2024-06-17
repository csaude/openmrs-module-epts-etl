package org.openmrs.module.epts.etl.changedrecordsdetector.engine;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.changedrecordsdetector.controller.ChangedRecordsDetectorController;
import org.openmrs.module.epts.etl.changedrecordsdetector.model.DetectedRecordInfo;
import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.model.pojo.generic.GenericDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import fgh.spi.changedrecordsdetector.ChangedRecord;
import fgh.spi.changedrecordsdetector.DetectedRecordService;

public class ChangedRecordsDetectorEngine extends TaskProcessor<EtlDatabaseObject> {
	
	public ChangedRecordsDetectorEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits) {
		super(monitor, limits);
		
		DetectedRecordService action = DetectedRecordService.getInstance();
		
		DBConnectionInfo connInfo = getRelatedOperationController().getActionPerformeApp().getConnInfo();
		
		action.configureDBService(getRelatedOperationController().getActionPerformeApp().getApplicationCode(), connInfo);
	}
	
	@Override
	public ChangedRecordsDetectorController getRelatedOperationController() {
		return (ChangedRecordsDetectorController) super.getRelatedOperationController();
	}
	
	@Override
	protected EtlOperationResultHeader<EtlDatabaseObject> performeSync(List<EtlDatabaseObject> records, Connection srcConn,
	        Connection dstConn) throws DBException {
		
		List<EtlDatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(records, EtlDatabaseObject.class);
		List<ChangedRecord> processedRecords = new ArrayList<ChangedRecord>(records.size());
		
		this.getMonitor()
		        .logInfo("PERFORMING CHANGE DETECTED ACTION '" + records.size() + "' " + getSrcConf().getTableName());
		
		for (EtlDatabaseObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				((GenericDatabaseObject) obj).setRelatedConfiguration(getSrcConf());
				
				processedRecords.add(DetectedRecordInfo.generate(obj,
				    getRelatedOperationController().getActionPerformeApp().getApplicationCode(),
				    getMonitor().getSrcConf().getOriginAppLocationCode()));
				
				if (getRelatedOperationController().getActionPerformeApp().isSinglePerformingMode()) {
					DetectedRecordService.getInstance().performeAction(
					    getRelatedOperationController().getActionPerformeApp().getApplicationCode(),
					    processedRecords.get(processedRecords.size() - 1), getSrcConf());
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
				
				logError(
				    "Any error occurred processing record [uuid: " + obj.getUuid() + ", id: " + obj.getObjectId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		if (getRelatedOperationController().getActionPerformeApp().isBatchPerformingMode()) {
			DetectedRecordService.getInstance().performeAction(
			    getRelatedOperationController().getActionPerformeApp().getApplicationCode(), processedRecords, getSrcConf());
		}
		
		EtlOperationResultHeader<EtlDatabaseObject> result = new EtlOperationResultHeader<>();
		
		result.addAllToRecordsWithNoError(utilities.parseList(processedRecords, EtlDatabaseObject.class));
		
		this.getMonitor().logInfo(
		    "ACTION PERFORMED FOR CHANGED RECORDS '" + records.size() + "' " + getSrcConf().getTableName() + "!");
		
		return result;
		
	}
	
}
