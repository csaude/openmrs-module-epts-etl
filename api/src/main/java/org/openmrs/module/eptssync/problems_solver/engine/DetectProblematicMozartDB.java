package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.controller.GenericOperationController;
import org.openmrs.module.eptssync.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see MozartProblemSolver
 */
public class DetectProblematicMozartDB extends MozartProblemSolver {
	
	public DetectProblematicMozartDB(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (done)
			return;
		
		logInfo("DETECTING PROBLEMS ON TABLE '" + getSyncTableConfiguration().getTableName() + "'");
		
		performeOnServer(this.dbsInfo, conn);
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		
		List<SyncTableConfiguration> configuredTables = getRelatedOperationController().getConfiguration()
		        .getTablesConfigurations();
		
		for (String dbName : dbInfo.getDbNames()) {
			logDebug("Validating DB '[" + dbName + "]");
			
			DBValidateInfo report = new DBValidateInfo(dbName);
			
			if (!DBUtilities.isResourceExist(dbName, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logWarn("DB '" + dbName + "' is missing!");
				
				report.addProblemType(MozartProblemType.MISSING_DB);
				
				continue;
			}
			
			for (SyncTableConfiguration configuredTable : configuredTables) {
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				if (!checkIfTableExists(configuredTable.getTableName(), dbName, srcConn)) {
					report.addMissingTable(configuredTable.getTableName());
					
					report.addProblemType(MozartProblemType.MISSING_TABLES);
				} else {
					
					String sql = "select count(*) as value from  " + dbName + "." + configuredTable.getTableName();
					
					SimpleValue result = DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn);
					
					if (result.intValue() == 0) {
						
						if (!configuredTable.getTableName().equals("key_vulnerable_pop")) {
							report.addProblemType(MozartProblemType.EMPTY_TABLES);
							report.addEmptyTable(configuredTable.getTableName());
						}
					}
					
					List<String> missingField = generateMissingFields(dbName, configuredTable, srcConn);
					
					if (utilities.arrayHasElement(missingField)) {
						report.addMissingFields(configuredTable.getTableName(), missingField);
						
						report.addProblemType(MozartProblemType.MISSIN_FIELDS);
					}
				}
			}
			
			if (report.hasProblem()) {
				report.setReport(this.reportOfProblematics);
			} else {
				report.setReport(this.reportOfNoIssue);
			}
		}
	}
}
