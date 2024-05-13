package org.openmrs.module.epts.etl.problems_solver.engine.mozart;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.problems_solver.engine.DatabasesInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see MozartProblemSolverEngine
 */
public class CorrectLabTestResult extends MozartProblemSolverEngine {
	
	private AppInfo dstApp;
	
	public CorrectLabTestResult(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		try {
			this.dstApp = getRelatedSyncConfiguration().find(AppInfo.init("destination"));
		}
		catch (ForbiddenOperationException e) {
			throw new ForbiddenOperationException("You must configure 'destination' app for destination database!");
		}
	}
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeSync(List<EtlObject> etlObjects, Connection conn) throws DBException {
		if (done)
			return;
		
		logInfo("STARTING PROBLEMS RESOLUTION...'");
		
		performeOnServer(this.dbsInfo, conn);
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		OpenConnection dstConn = dstApp.openConnection();
		
		int i = 0;
		
		try {
			
			for (String dbName : dbInfo.getDbNames()) {
				logDebug(
				    "Trying to update 'selected' field " + ++i + "/" + dbInfo.getDbNames().size() + " [" + dbName + "]");
				
				for (EtlItemConfiguration config : getRelatedSyncConfiguration().getEtlItemConfiguration()) {
					AbstractTableConfiguration configuredTable = config.getSrcConf();
					
					if (!configuredTable.getTableName().equals("location"))
						continue;
					
					if (!configuredTable.isFullLoaded()) {
						configuredTable.fullLoad();
					}
					
					dstConn.markAsSuccessifullyTerminated();
				}
			}
		}
		finally {
			dstConn.finalizeConnection();
			srcConn.finalizeConnection();
		}
	}
}
