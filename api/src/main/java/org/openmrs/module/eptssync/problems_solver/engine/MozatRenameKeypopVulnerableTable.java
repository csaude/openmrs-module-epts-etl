package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.controller.conf.UniqueKeyInfo;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.controller.GenericOperationController;
import org.openmrs.module.eptssync.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartRuntaskWithTimeCheck;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartTaskType;
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
		if (done)
			return;
		
		logInfo("DETECTING AND RESOLVING PROBLEMS '");
		
		performeOnServer(this.dbsInfo, conn);
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		
		int i = 0;
		for (String dbName : dbInfo.getDbNames()) {
			logDebug("Trying to rename KeypopVulnerable Table " + ++i + "/" + dbInfo.getDbNames().size() + " [" + dbName + "]");
				
			DBValidateInfo report = this.reportOfResolvedProblems.initDBValidatedInfo(dbName);
			
			if (!DBUtilities.isResourceExist(dbName, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logWarn("DB '" + dbName + "' is missing!");
				
				this.reportOfProblematics.addMissingDb(dbName);
				
				continue;
			}
			
			if (checkIfTableExists("key_vulnerable_pop", dbName, srcConn)) {
				if (checkIfTableExists("keypop_vulnerable", dbName, srcConn)) {
					logWarn("DB '" + dbName + "' contains 2 versions of key_vulnerable_pop table");
				}
			} else if (checkIfTableExists("keypop_vulnerable", dbName, srcConn)) {
				logInfo("Renaming table 'keypop_vulnerable' to 'key_vulnerable_pop'");
				
				ResolvedProblem resolvedProblem = ResolvedProblem.init("key_vulnerable_pop");
				resolvedProblem.setProblemType(MozartProblemType.WRONG_TABLE_NAME);
				resolvedProblem.setOriginalTableName("keypop_vulnerable");
				
				report.addResolvedProblem(resolvedProblem);
				
				report.getReport().saveOnFile();
				
				DBUtilities.renameTable(dbName, "keypop_vulnerable", "key_vulnerable_pop", srcConn);
			}
			
			SyncTableConfiguration configuredTable = getSyncTableConfiguration().getRelatedSynconfiguration().findSyncTableConfiguration("key_vulnerable_pop");
			
			List<UniqueKeyInfo> missingKeys = generateMissingUniqueKeys(dbName, configuredTable , srcConn);
			
			if (missingKeys.size() > 0) {
				logDebug("There are some missing UKs " + missingKeys);
				
				addUniqueKey(dbName, configuredTable, srcConn);
			}
		}
	}
	
	
	private void addUniqueKey(String dbName, SyncTableConfiguration configuredTable, Connection conn)
	        throws DBException {
		
		String table = dbName + ".key_vulnerable_pop";
		
		String query = "ALTER TABLE " + table + " ADD UNIQUE KEY `clinical_consultation_encounter_uuid` (`encounter_uuid`);";
		
		MozartRuntaskWithTimeCheck.executeWithTimeCheck(MozartTaskType.BATCH, query, 60, conn);
	}
}
