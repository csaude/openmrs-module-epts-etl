package org.openmrs.module.epts.etl.inconsistenceresolver.processor;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.inconsistenceresolver.controller.InconsistenceSolverController;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class InconsistenceSolverProcessor extends TaskProcessor<EtlDatabaseObject> {
	
	public InconsistenceSolverProcessor(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits,
	    boolean runningInConcurrency) {
		super(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public InconsistenceSolverController getRelatedOperationController() {
		return (InconsistenceSolverController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeEtl(List<EtlDatabaseObject> etlObjects, Connection srcConn, Connection dstConn) throws DBException {
		List<EtlDatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(etlObjects, EtlDatabaseObject.class);
		
		logInfo("DOING INCONSISTENCE SOLVER FOR '" + etlObjects.size() + "' " + getMainSrcTableName());
		
		for (EtlDatabaseObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				obj.resolveInconsistence(getSrcConf(), srcConn);
				
				getTaskResultInfo().addToRecordsWithNoError(obj);
			}
			catch (Exception e) {
				logError(
				    "Any error occurred processing dstRecord [uuid: " + obj.getUuid() + ", id: " + obj.getObjectId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		logInfo("INCONSISTENCE SOLVED FOR '" + etlObjects.size() + "' " + getMainSrcTableName() + "!");
		
	}
}
