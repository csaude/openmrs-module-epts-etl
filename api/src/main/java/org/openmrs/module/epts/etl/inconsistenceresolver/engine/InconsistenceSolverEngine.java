package org.openmrs.module.epts.etl.inconsistenceresolver.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.inconsistenceresolver.controller.InconsistenceSolverController;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.EtlOperationResultHeader;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class InconsistenceSolverEngine extends TaskProcessor<EtlDatabaseObject> {
	
	public InconsistenceSolverEngine(Engine<EtlDatabaseObject> monitor, IntervalExtremeRecord limits) {
		super(monitor, limits);
	}
	
	@Override
	public InconsistenceSolverController getRelatedOperationController() {
		return (InconsistenceSolverController) super.getRelatedOperationController();
	}
	
	@Override
	protected EtlOperationResultHeader<EtlDatabaseObject> performeSync(List<EtlDatabaseObject> etlObjects,
	        Connection srcConn, Connection dstConn) throws DBException {
		List<EtlDatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(etlObjects, EtlDatabaseObject.class);
		
		logInfo("DOING INCONSISTENCE SOLVER FOR '" + etlObjects.size() + "' " + getMainSrcTableName());
		
		EtlOperationResultHeader<EtlDatabaseObject> result = new EtlOperationResultHeader<>();
		
		for (EtlDatabaseObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				obj.resolveInconsistence(getSrcConf(), srcConn);
				
				result.addToRecordsWithNoError(obj);
			}
			catch (Exception e) {
				logError(
				    "Any error occurred processing record [uuid: " + obj.getUuid() + ", id: " + obj.getObjectId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		logInfo("INCONSISTENCE SOLVED FOR '" + etlObjects.size() + "' " + getMainSrcTableName() + "!");
		
		return result;
	}
}
