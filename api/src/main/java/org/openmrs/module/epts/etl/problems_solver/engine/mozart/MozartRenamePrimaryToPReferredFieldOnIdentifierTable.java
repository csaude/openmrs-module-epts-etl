package org.openmrs.module.epts.etl.problems_solver.engine.mozart;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.problems_solver.engine.DatabasesInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.ResolvedProblem;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DBQuickMergeController
 */
public class MozartRenamePrimaryToPReferredFieldOnIdentifierTable extends MozartProblemSolverEngine {
	
	public MozartRenamePrimaryToPReferredFieldOnIdentifierTable(EngineMonitor monitor, RecordLimits limits) {
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
		
		logInfo("STARTING PROBLEMS RESOLUTION...'");
		
		performeOnServer(this.dbsInfo, conn);
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		
		List<SyncTableConfiguration> configuredTables = getRelatedOperationController().getConfiguration()
		        .getTablesConfigurations();
		
		int i = 0;
		for (String dbName : dbInfo.getDbNames()) {
			logDebug("Trying to rename Prefered Field on Identifier table on  " + ++i + "/" + dbInfo.getDbNames().size() + " [" + dbName + "]");
			
			DBValidateInfo report = this.reportOfResolvedProblems.initDBValidatedInfo(dbName);
					
			if (!DBUtilities.isResourceExist(dbName, null, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logWarn("DB '" + dbName + "' is missing!");
				
				this.reportOfProblematics.addMissingDb(dbName);
				
				continue;
			}
			
			for (SyncTableConfiguration configuredTable : configuredTables) {
				
				if (!configuredTable.getTableName().equals("identifier"))
					continue;
				
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				List<String> missingField = generateMissingFields(dbName, configuredTable, srcConn);
				
				tryToRenamePrimaryFieldToPreferred(report, dbName, missingField, srcConn);
			}
			
		}
	}
	
	private void tryToRenamePrimaryFieldToPreferred(DBValidateInfo report, String dbName, List<String> missingField,
	        OpenConnection conn) throws DBException {
		
		if (utilities.arrayHasElement(missingField) && utilities.existOnArray(missingField, "preferred")) {
			missingField.remove("preferred");
			
			String table = dbName + ".identifier";
			
			logWarn("Renaming field 'primary' to 'preferred' on " + table);
			
			String sql = "";
			
			sql += "ALTER TABLE " + table + " CHANGE `primary` `preferred` tinyint(4) DEFAULT NULL";
			
			ResolvedProblem resolvedProblem = ResolvedProblem.init("identifier");
			resolvedProblem.setProblemType(MozartProblemType.WRONG_FIELD_NAME);
			resolvedProblem.setOriginalColumnName("primary");
			resolvedProblem.setColumnName("preferred");
			
			report.addResolvedProblem(resolvedProblem);
			
			report.getReport().saveOnFile();
			
			DBUtilities.executeBatch(conn, sql);
		}
	}
}
