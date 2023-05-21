package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.engine.SyncSearchParams;
import org.openmrs.module.eptssync.model.Field;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.controller.ProblemsSolverController;
import org.openmrs.module.eptssync.problems_solver.model.ProblemsSolverSearchParams;
import org.openmrs.module.eptssync.problems_solver.model.mozart.DBValidateReport;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * @author jpboane
 * @see DBQuickMergeController
 */
public class DetectProblematicMozartDB extends ProblemsSolverEngine {
	
	static List<DBValidateReport> reportsProblematicDBs;
	
	static List<DBValidateReport> reportsNoIssueDBs;
	
	DatabasesInfo[] DB_INFOs_01 = {
	        new DatabasesInfo("CCS_MAPUTO", DatabasesInfo.CCS_DB_NAMES_MAPUTO, new DBConnectionInfo("root", "root",
	                "jdbc:mysql://10.10.2.2:53301/mysql?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")) };
	
	DatabasesInfo[] DB_INFOs_02 = {
	        new DatabasesInfo("ARIEL_MAPUTO", DatabasesInfo.ARIEL_DB_NAMES_MAPUTO, new DBConnectionInfo("root", "root",
	                "jdbc:mysql://10.10.2.2:53301/mysql?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")) };
	
	DatabasesInfo[] DB_INFOs_03 = { new DatabasesInfo("ARIEL_CD", DatabasesInfo.ARIEL_DB_NAMES_CD, new DBConnectionInfo("root",
	        "root", "jdbc:mysql://10.10.2.2:53301/mysql?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")) };
	
	
	DatabasesInfo[] DB_INFOs = { new DatabasesInfo("EGPAF_GZ", DatabasesInfo.EGPAF_DB_NAMES_GAZA, new DBConnectionInfo("root",
	        "root", "jdbc:mysql://10.10.2.2:53301/mysql?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")) };
	
	public DetectProblematicMozartDB(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public ProblemsSolverController getRelatedOperationController() {
		return (ProblemsSolverController) super.getRelatedOperationController();
	}
	
	@Override
	protected void restart() {
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (done)
			return;
		
		logInfo("DETECTING PROBLEMS ON TABLE '" + getSyncTableConfiguration().getTableName() + "'");
		
		for (DatabasesInfo dbsInfo : DB_INFOs) {
			performeOnServer(dbsInfo, conn);
		}
		
		String fileNameProblematicDBs = getSyncTableConfiguration().getRelatedSynconfiguration().getSyncRootDirectory()
		        + FileUtilities.getPathSeparator() + "problematicDBs.json";
		String fileNameNoIssueDBs = getSyncTableConfiguration().getRelatedSynconfiguration().getSyncRootDirectory()
		        + FileUtilities.getPathSeparator() + "noIssueDBs.json";
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileNameProblematicDBs);
		
		if (utilities.arrayHasElement(reportsProblematicDBs)) {
			FileUtilities.write(fileNameProblematicDBs, utilities.parseToJSON(reportsProblematicDBs));
		}
		if (utilities.arrayHasElement(reportsNoIssueDBs)) {
			FileUtilities.write(fileNameNoIssueDBs, utilities.parseToJSON(reportsNoIssueDBs));
		}
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		
		List<SyncTableConfiguration> configuredTables = getRelatedOperationController().getConfiguration()
		        .getTablesConfigurations();
		
		for (String dbName : dbInfo.getDbNames()) {
			logDebug("Validating DB '[" + dbName + "]");
			
			DBValidateReport report = new DBValidateReport(dbInfo.getServerName(), dbName);
			
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
					if (report == null)
						report = new DBValidateReport(dbInfo.getServerName(), dbName);
					
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
				if (reportsProblematicDBs == null)
					reportsProblematicDBs = new ArrayList<DBValidateReport>();
				
				reportsProblematicDBs.add(report);
			} else {
				if (reportsNoIssueDBs == null)
					reportsNoIssueDBs = new ArrayList<DBValidateReport>();
				
				reportsNoIssueDBs.add(report);
			}
		}
	}
	
	private List<String> generateMissingFields(String dbName, SyncTableConfiguration configuredTable, Connection conn)
	        throws DBException {
		List<Field> fields = DBUtilities.getTableFields(configuredTable.getTableName(), dbName, conn);
		List<Field> configuredFields = configuredTable.getFields();
		
		List<String> missingFields = new ArrayList<String>();
		
		for (Field configuredField : configuredFields) {
			Field tableField = utilities.findOnArray(fields, configuredField);
			
			if (tableField == null) {
				missingFields.add(configuredField.getName());
			}
		}
		
		return missingFields;
	}
	
	private boolean checkIfTableExists(String tableName, String schema, Connection conn) throws DBException {
		try {
			return DBUtilities.isResourceExist(schema, DBUtilities.RESOURCE_TYPE_TABLE, tableName, conn);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	@Override
	public void requestStop() {
	}
	
	@Override
	protected SyncSearchParams<? extends SyncRecord> initSearchParams(RecordLimits limits, Connection conn) {
		SyncSearchParams<? extends SyncRecord> searchParams = new ProblemsSolverSearchParams(
		        this.getSyncTableConfiguration(), null);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getSyncTableConfiguration().getRelatedSynconfiguration().getObservationDate());
		
		return searchParams;
	}
	
}
