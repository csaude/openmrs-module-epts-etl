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
import org.openmrs.module.eptssync.model.base.BaseDAO;
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
public class MozartRenamePrimaryToPReferredFieldOnIdentifierTable extends ProblemsSolverEngine {
	
	private static boolean done;
	
	static List<DBValidateReport> reportsProblematicDBs;
	
	static List<DBValidateReport> reportsNoIssueDBs;
	
	DatabasesInfo[] DB_INFOs = { new DatabasesInfo("ARIEL_CD", DatabasesInfo.ARIEL_DB_NAMES_CD, new DBConnectionInfo("root",
	        "root", "jdbc:mysql://10.10.2.2:53301/mysql?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")) };
	
	public MozartRenamePrimaryToPReferredFieldOnIdentifierTable(EngineMonitor monitor, RecordLimits limits) {
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
		
		logInfo("STARTING PROBLEMS RESOLUTION...'");
		
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
				continue;
			}
			
			for (SyncTableConfiguration configuredTable : configuredTables) {
				
				if (!configuredTable.getTableName().equals("identifier"))
					continue;
				
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				List<String> missingField = generateMissingFields(dbName, configuredTable, srcConn);
				
				tryToRenamePrimaryFieldToPreferred(dbName, missingField, srcConn);
				
				if (utilities.arrayHasElement(missingField)) {
					report.addMissingFields(configuredTable.getTableName(), missingField);
					
					report.addProblemType(MozartProblemType.MISSIN_FIELDS);
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
	
	private void tryToAddPReferredFieldOnIdentifiedTable(String dbName, List<String> missingField, OpenConnection conn)
	        throws DBException {
		if (utilities.arrayHasElement(missingField) && utilities.existOnArray(missingField, "preferred")) {
			missingField.remove("preferred");
			
			String table = dbName + ".identifier";
			
			logWarn("Generating preferred field on " + dbName + ".identifier");
			
			DBUtilities.executeBatch(conn, "alter table " + table + " add preferred tinyint(4) DEFAULT 0; ");
			
			String query = "select max(identifier_seq) value from " + table + " group by patient_uuid";
			
			List<SimpleValue> maxIdentifiers = BaseDAO.search(SimpleValue.class, query, null, conn);
			
			if (!utilities.arrayHasElement(maxIdentifiers))
				return;
			
			for (SimpleValue v : maxIdentifiers) {
				query = "update " + table + " set preferred = 1 where identifier_seq = ? ";
				Object[] params = { v.intValue() };
				
				BaseDAO.executeQuery(query, params, conn);
			}
			
			conn.commitCurrentWork();
		}
	}
	
	private void tryToRenamePrimaryFieldToPreferred(String dbName, List<String> missingField, OpenConnection conn)
	        throws DBException {
		
		if (utilities.arrayHasElement(missingField) && utilities.existOnArray(missingField, "preferred")) {
			missingField.remove("preferred");
			
			String table = dbName + ".identifier";
			
			logWarn("Renaming field 'primary' to 'preferred' on " + table);
			
			String sql = "";
			
			sql += "ALTER TABLE " + table + " CHANGE `primary` `preferred` tinyint(4) DEFAULT NULL";
			
			DBUtilities.executeBatch(conn, sql);
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
