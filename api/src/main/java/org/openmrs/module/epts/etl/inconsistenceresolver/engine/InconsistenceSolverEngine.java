package org.openmrs.module.epts.etl.inconsistenceresolver.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.inconsistenceresolver.controller.InconsistenceSolverController;
import org.openmrs.module.epts.etl.inconsistenceresolver.model.InconsistenceSolverSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class InconsistenceSolverEngine extends Engine {
	
	public InconsistenceSolverEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		super(monitor, limits);
	}
	
	@Override
	public InconsistenceSolverController getRelatedOperationController() {
		return (InconsistenceSolverController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<? extends EtlObject> etlObjects, Connection conn) throws DBException {
		List<EtlDatabaseObject> syncRecordsAsOpenMRSObjects = utilities.parseList(etlObjects, EtlDatabaseObject.class);
		
		logInfo("DOING INCONSISTENCE SOLVER FOR '" + etlObjects.size() + "' " + getMainSrcTableName());
		
		for (EtlDatabaseObject obj : syncRecordsAsOpenMRSObjects) {
			try {
				obj.resolveInconsistence(getSrcConf(), conn);
			}
			catch (Exception e) {
				logError(
				    "Any error occurred processing record [uuid: " + obj.getUuid() + ", id: " + obj.getObjectId() + "]");
				
				throw new RuntimeException(e);
			}
		}
		
		logInfo("INCONSISTENCE SOLVED FOR '" + etlObjects.size() + "' " + getMainSrcTableName() + "!");
	}
	
	@Override
	protected AbstractEtlSearchParams<? extends EtlObject> initSearchParams(ThreadLimitsManager limits, Connection conn) {
		AbstractEtlSearchParams<? extends EtlObject> searchParams = new InconsistenceSolverSearchParams(this.getEtlConfiguration(),
		        limits, conn);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getRelatedOperationController().getProgressInfo().getStartTime());
		
		return searchParams;
	}
}
