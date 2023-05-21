package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
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
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

/**
 * @author jpboane
 * @see DBQuickMergeController
 */
public class MozartRenameDsdFields extends ProblemsSolverEngine {
	static List<DBValidateReport> reportsProblematicDBs;
	
	static List<DBValidateReport> reportsNoIssueDBs;
	
	DatabasesInfo[] DB_INFOs = {
	        new DatabasesInfo("EGPAF_GZ", DatabasesInfo.EGPAF_DB_NAMES_GAZA, new DBConnectionInfo("root", "root",
	                "jdbc:mysql://10.10.2.2:53301/mysql?autoReconnect=true&useSSL=false", "com.mysql.jdbc.Driver")) };
	
	public MozartRenameDsdFields(EngineMonitor monitor, RecordLimits limits) {
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
			
			if (dbName.equals("egpaf_gz_manjangue")) {
				logDebug("STOP");
			}
			
			DBValidateReport report = new DBValidateReport(dbInfo.getServerName(), dbName);
			
			if (!DBUtilities.isResourceExist(dbName, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logWarn("DB '" + dbName + "' is missing!");
				
				continue;
			}
			
			for (SyncTableConfiguration configuredTable : configuredTables) {
				
				if (!configuredTable.getTableName().equals("dsd"))
					continue;
				
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				
				List<String> missingField = generateMissingFields(dbName, configuredTable, srcConn);
				
				tryToRenameFields(dbName, missingField, srcConn);
				
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
	
	private void tryToRenameFields(String dbName, List<String> missingField, OpenConnection conn_)
	        throws DBException {
		
		String table = dbName + ".dsd";
		
		DBConnectionService srcConnService = conn_.getConnService().clone("jdbc:mysql://10.10.2.2:53301/"+ dbName +"?autoReconnect=true&useSSL=false");
		
		OpenConnection srcConn = srcConnService.openConnection();
		
		if (!utilities.existOnArray(missingField, "dsd_uuid")) {
			logDebug("The dsd_uuid field exists");
			
			//TRy to generate unique key on dsd_uuid field
			
			tryToAddUniqueKeyOnDsdUuid(dbName, srcConn);
			
			//TRy to fill dsd_uuid field
			
			logDebug("Checking data");
			
			SimpleValue count = DatabaseObjectDAO.find(SimpleValue.class, "select count(*) from " + table + " where dsd_uuid is null", null, srcConn);
			
			if (count.integerValue() > 0) {
				logDebug("The field is empty! Update...");
				updateDsdField(dbName, srcConn);
			}
		}
		else
		if (utilities.arrayHasElement(missingField) && utilities.existOnArray(missingField, "dsd_uuid")) {
			missingField.remove("dsd_uuid");
			
			
			logWarn("Adding 'encounter_uuid' on " +table);
			
			String sql = "";
			
			sql += "ALTER TABLE " + table + " ADD `dsd_uuid` char(38) DEFAULT NULL;";
			
			DBUtilities.executeBatch(srcConn, sql);
			
			tryToAddUniqueKeyOnDsdUuid(dbName, srcConn);
			
			updateDsdField(dbName, srcConn);
		}		
		
		srcConn.markAsSuccessifullyTerminected();
		
		srcConn.finalizeConnection();
	}
	
	private void tryToAddUniqueKeyOnDsdUuid(String dbName, Connection conn) throws DBException {
		logDebug("Checking key");
		
		String table = dbName + ".dsd";
		
		List<List<String>> uniqueKeys = DBUtilities.getUniqueKeys("dsd", dbName, conn);
		
		boolean hasKeyOnDdsUuid = false;
		
		for (List<String> keyElements : uniqueKeys) {
			if (keyElements.contains("dsd_uuid")) {
				hasKeyOnDdsUuid = true;
				break;
			}
		}
		
		if (!hasKeyOnDdsUuid) {
			logDebug("The key does not exist. Adding it..");
			
			String sql = "ALTER TABLE " + table + " ADD UNIQUE KEY `dsd_uniqueness_key` (`dsd_uuid`);";
			
			DBUtilities.executeBatch(conn, sql);
		}else {
			logDebug("The key exists");
		}		
	}
	
	private void updateDsdField(String dbName, Connection conn) throws DBException {
		String table = dbName + ".dsd";
		
		String sql = "SELECT id value FROM " + table;
		
		List<SimpleValue> dsds = DatabaseObjectDAO.search(SimpleValue.class, sql, null, conn);
		
		for (int i =0; i < dsds.size(); i++) {
			SimpleValue dsd  = dsds.get(i);
			
			logDebug("Updating dsd ["+dsd + "] " + i+"/"+dsds.size());
			sql = "UPDATE " + table + " SET dsd_uuid =  ? WHERE id = ?";
			
			Object[] params = {utilities.generateUUID().toString(), dsd.integerValue()};
			
		
			
			BaseDAO.executeDBQuery(sql, params, conn);
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
