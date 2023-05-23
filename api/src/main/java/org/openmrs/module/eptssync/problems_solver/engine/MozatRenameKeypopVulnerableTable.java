package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.controller.GenericOperationController;
import org.openmrs.module.eptssync.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.eptssync.problems_solver.model.mozart.ResolvedProblem;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DBQuickMergeController
 */
public class MozatRenameKeypopVulnerableTable extends MozartProblemSolver {
		
	public MozatRenameKeypopVulnerableTable(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
	

	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (done) return;
		
		logInfo("DETECTING AND RESOLVING PROBLEMS '");
		
		performeOnServer(this.dbsInfo, conn);
	
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		
		for (String dbName : dbInfo.getDbNames()) {
			logDebug("Validating DB '[" + dbName + "]");
			
			DBValidateInfo report = new DBValidateInfo(this.reportOfResolvedProblems, dbName);
			
			if (!DBUtilities.isResourceExist(dbName, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logWarn("DB '" + dbName + "' is missing!");
				
				this.reportOfProblematics.addMissingDb(dbName);
				
				continue;
			}
				
			if (checkIfTableExists("key_vulnerable_pop", dbName, srcConn)){
				if (checkIfTableExists("keypop_vulnerable", dbName, srcConn)) {
					logWarn("DB '" + dbName + "' contains 2 versions of key_vulnerable_pop table");
				}
			}
			else if (checkIfTableExists("keypop_vulnerable", dbName, srcConn)){
				logInfo("Renaming table 'keypop_vulnerable' to 'key_vulnerable_pop'");
				
				ResolvedProblem resolvedProblem = ResolvedProblem.init("key_vulnerable_pop");
				resolvedProblem.setProblemType(MozartProblemType.WRONG_TABLE_NAME);
				resolvedProblem.setOriginalTableName("keypop_vulnerable");
				
				report.addResolvedProblem(resolvedProblem);
				
				report.getReport().saveOnFile();
				
				DBUtilities.renameTable(dbName, "keypop_vulnerable", "key_vulnerable_pop", srcConn);
			}
		}
	}
	
}
