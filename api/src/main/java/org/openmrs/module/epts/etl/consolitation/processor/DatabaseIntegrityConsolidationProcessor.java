package org.openmrs.module.epts.etl.consolitation.processor;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DatabaseIntegrityConsolidationProcessor extends TaskProcessor<EtlDatabaseObject> {
	
	public DatabaseIntegrityConsolidationProcessor(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public DatabaseIntegrityConsolidationController getRelatedOperationController() {
		return (DatabaseIntegrityConsolidationController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> records, Connection srcConn, Connection dstConn) throws DBException {
		
		throw new ForbiddenOperationException("Rever este metodo!");
		
		/*List<OpenMRSObject> syncRecordsAsOpenMRSObjects = utilities.parseList(syncRecords, OpenMRSObject.class);
		
		this.getMonitor().logInfo("CONSOLIDATING INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName());
		
		for (OpenMRSObject obj : syncRecordsAsOpenMRSObjects) {
			obj.setRelatedSyncInfo(EtlStageRecordVO.retrieveFromSyncRecord(getSyncTableConfiguration(), obj, getRelatedOperationController().getAppOriginLocationCode(), conn));
			
			obj.consolidateData(getSyncTableConfiguration(), conn);
		}
		
		this.getMonitor().logInfo("INTEGRITY DATA FOR '"+syncRecords.size() + "' " + getSyncTableConfiguration().getTableName() + " CONSOLIDATED!");*/
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initReloadRecordsWithDefaultParentsTaskProcessor(IntervalExtremeRecord limits) {
		throw new ForbiddenOperationException("Forbiden Method");
	}
}
