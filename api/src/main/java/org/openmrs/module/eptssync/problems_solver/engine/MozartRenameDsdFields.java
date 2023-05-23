package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.eptssync.problems_solver.model.mozart.ResolvedProblem;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DBQuickMergeController
 */
public class MozartRenameDsdFields extends MozartProblemSolver {
	
	public MozartRenameDsdFields(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
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
			
			DBValidateInfo report = new DBValidateInfo(this.reportOfResolvedProblems, dbName);
			
			if (!DBUtilities.isResourceExist(dbName, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logWarn("DB '" + dbName + "' is missing!");
				
				this.reportOfProblematics.addMissingDb(dbName);
				
				continue;
			}
			
			this.reportOfResolvedProblems.addReport(report);
			
			for (SyncTableConfiguration configuredTable : configuredTables) {
				
				if (!configuredTable.getTableName().equals("dsd"))
					continue;
				
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				List<String> missingField = generateMissingFields(dbName, configuredTable, srcConn);
				
				tryToRenameFields(report, dbName, missingField, srcConn);
			}
		}
	}
	
	private void tryToRenameFields(DBValidateInfo report, String dbName, List<String> missingField, OpenConnection conn_)
	        throws DBException {
		
		String table = dbName + ".dsd";
		
		DBConnectionService srcConnService = conn_.getConnService().clone("jdbc:mysql://10.10.2.2:53301/"+ dbName +"?autoReconnect=true&useSSL=false");
		
		OpenConnection srcConn = srcConnService.openConnection();
		
		if (!utilities.existOnArray(missingField, "dsd_uuid")) {
			logDebug("The dsd_uuid field exists");
			
			//TRy to generate unique key on dsd_uuid field
			
			tryToAddUniqueKeyOnDsdUuid(report, dbName, srcConn);
			
			//TRy to fill dsd_uuid field
			
			logDebug("Checking data");
			
			SimpleValue count = DatabaseObjectDAO.find(SimpleValue.class, "select count(*) from " + table + " where dsd_uuid is null", null, srcConn);
			
			if (count.integerValue() > 0) {
				logDebug("The field is empty! Update...");
				updateDsdField(report, dbName, srcConn);
			}
		}
		else
		if (utilities.arrayHasElement(missingField) && utilities.existOnArray(missingField, "dsd_uuid")) {
			missingField.remove("dsd_uuid");
			
			logWarn("Adding 'encounter_uuid' on " +table);
			
			String sql = "";
			
			sql += "ALTER TABLE " + table + " ADD `dsd_uuid` char(38) DEFAULT NULL;";
			
			
			ResolvedProblem resolvedProblem = ResolvedProblem.init("dsd");
			resolvedProblem.setProblemType(MozartProblemType.MISSIN_FIELDS);
			resolvedProblem.setColumnName("dsd_uuid");
			
			report.addResolvedProblem(resolvedProblem);
			
			report.getReport().saveOnFile();		
			
			DBUtilities.executeBatch(srcConn, sql);
			
			tryToAddUniqueKeyOnDsdUuid(report, dbName, srcConn);
			
			updateDsdField(report, dbName, srcConn);
		}		
		
		srcConn.markAsSuccessifullyTerminected();
		
		srcConn.finalizeConnection();
	}
	
	private void tryToAddUniqueKeyOnDsdUuid(DBValidateInfo report, String dbName, Connection conn) throws DBException {
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
			ResolvedProblem resolvedProblem = ResolvedProblem.init("dsd");
			resolvedProblem.setProblemType(MozartProblemType.MISSING_UNIQUE_KEY);
			resolvedProblem.setColumnName("dsd_uuid");
			
			report.addResolvedProblem(resolvedProblem);
			
			report.getReport().saveOnFile();
			
			logDebug("The key does not exist. Adding it..");
			
			String sql = "ALTER TABLE " + table + " ADD UNIQUE KEY `dsd_uniqueness_key` (`dsd_uuid`);";
			
			DBUtilities.executeBatch(conn, sql);
		}else {
			logDebug("The key exists");
		}		
	}
	
	private void updateDsdField(DBValidateInfo report, String dbName, Connection conn) throws DBException {
		String table = dbName + ".dsd";
		
		String sql = "SELECT id value FROM " + table + " WHERE dsd_uuid is null";
		
		List<SimpleValue> dsds = DatabaseObjectDAO.search(SimpleValue.class, sql, null, conn);
		
		for (int i =0; i < dsds.size(); i++) {
			SimpleValue dsd  = dsds.get(i);
			
			logDebug("Updating dsd ["+dsd + "] " + i+"/"+dsds.size());
			sql = "UPDATE " + table + " SET dsd_uuid =  ? WHERE id = ?";
			
			String uuid = utilities.generateUUID().toString();
			
			Object[] params = {uuid, dsd.integerValue()};
				
			ResolvedProblem resolvedProblem = ResolvedProblem.init("dsd");
			resolvedProblem.setProblemType(MozartProblemType.EMPTY_FIELD);
			resolvedProblem.setColumnName("dsd_uuid");
			resolvedProblem.setRecordId(dsd.integerValue());
			
			report.addResolvedProblem(resolvedProblem);
			
			
			BaseDAO.executeDBQuery(sql, params, conn);
		}
		
		report.getReport().saveOnFile();
	}

	
}
