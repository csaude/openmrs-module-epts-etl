package org.openmrs.module.eptssync.problems_solver.engine;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.controller.conf.UniqueKeyInfo;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.SimpleValue;
import org.openmrs.module.eptssync.model.base.BaseDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartRuntaskWithTimeCheck;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartTaskType;
import org.openmrs.module.eptssync.problems_solver.model.mozart.ResolvedProblem;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.LongTransactionException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DBQuickMergeController
 */
public class MozartRenameDsdFields extends MozartProblemSolver {
	int lasProcessedDBPos;
	
	public MozartRenameDsdFields(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		this.lasProcessedDBPos = 0;
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (done)
			return;
		
		logInfo("DETECTING PROBLEMS ON TABLE '" + getSyncTableConfiguration().getTableName() + "'");
		
		OpenConnection srcConn = this.dbsInfo.acquireConnection();
		
		try {
			performeOnServer(this.dbsInfo, conn, srcConn);
		}
		catch (LongTransactionException e) {
			logError(e.getLocalizedMessage());
			
			srcConn.finalizeConnection();
			
			performeSync(syncRecords, srcConn);
		}
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn, OpenConnection srcConn) throws DBException, LongTransactionException {
		
		List<SyncTableConfiguration> configuredTables = getRelatedOperationController().getConfiguration()
		        .getTablesConfigurations();
		
		for (int i = this.lasProcessedDBPos; i < dbInfo.getDbNames().size(); i++) {
			
			this.lasProcessedDBPos = i;
			
			String dbName = dbInfo.getDbNames().get(i);
			
			logDebug("Trying to rename fields on DSD table on DB " + i + "/" + dbInfo.getDbNames().size() + " [" + dbName
			        + "]");
			
			DBValidateInfo report = this.reportOfResolvedProblems.initDBValidatedInfo(dbName);
			
			if (!DBUtilities.isResourceExist(dbName, null, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logWarn("DB '" + dbName + "' is missing!");
				
				this.reportOfProblematics.addMissingDb(dbName);
				
				continue;
			}
			
			for (SyncTableConfiguration configuredTable : configuredTables) {
				
				if (!configuredTable.getTableName().equals("dsd"))
					continue;
				
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				//Check field
				
				if (checkIfDsdUuidFieldExists(dbName, configuredTable, srcConn)) {
					logDebug("The dsd.dsd_uuid field exists on DB " + dbName);
				} else {
					ResolvedProblem resolvedProblem = ResolvedProblem.init("dsd");
					resolvedProblem.setProblemType(MozartProblemType.MISSIN_FIELDS);
					resolvedProblem.setColumnName("dsd_uuid");
					
					report.addResolvedProblem(resolvedProblem);
					
					report.getReport().saveOnFile();
					
					addDsdUuidField(dbName, srcConn);
					
				}
				
				logDebug(dbName + " Checking key on DB " + dbName);
				
				if (!checkIfDsdUuidFieldHasUniqueKey(dbName, configuredTable, srcConn)) {
					
					logDebug("The key does not exist on DB" + dbName + ". Adding it..");
					
					ResolvedProblem resolvedProblem = null;
					
					resolvedProblem = ResolvedProblem.init("dsd");
					resolvedProblem.setProblemType(MozartProblemType.MISSING_UNIQUE_KEY);
					resolvedProblem.setColumnName("dsd_uuid");
					
					report.addResolvedProblem(resolvedProblem);
					
					addUniqueKeyOnDsdUuidField(dbName, configuredTable, srcConn);
				}
				else {
					logDebug("The key exists  on DB " + dbName);
				}
				
				//Check data
				if (checkIfDsdUuidHasMissingData(dbName, srcConn)) {
					logDebug("The are some records with empty value on the field! Update...");
					
					//Update Data
					updateDsdField(report, dbName, srcConn);
				}
				
				srcConn.commitCurrentWork();

			}
		}
	}
	
	private void addDsdUuidField(String dbName, Connection conn) throws DBException, LongTransactionException {
		String table = dbName + ".dsd";
		
		logWarn("Adding 'dsd_uuid' on " + table);
		
		String query = "";
		
		query += "ALTER TABLE " + table + " ADD `dsd_uuid` char(38) DEFAULT NULL;";
		
		MozartRuntaskWithTimeCheck.executeWithTimeCheck(MozartTaskType.BATCH, query, 60, conn);
	}
	
	private boolean checkIfDsdUuidHasMissingData(String dbName, Connection conn) throws DBException {
		String table = dbName + ".dsd";
		
		String query = "select count(*) from " + table + " where dsd_uuid is null";
		
		MozartRuntaskWithTimeCheck bg = MozartRuntaskWithTimeCheck.executeWithTimeCheck(MozartTaskType.QUERY, query, 60, conn);
		
		return bg.getValues().get(0).integerValue() > 0;
	}
	
	private boolean checkIfDsdUuidFieldExists(String dbName, SyncTableConfiguration configuredTable, Connection conn)
	        throws DBException {
		List<String> missingField = generateMissingFields(dbName, configuredTable, conn);
		
		if (!utilities.existOnArray(missingField, "dsd_uuid")) {
			return true;
		} else
			return false;
	}
	
	private boolean checkIfDsdUuidFieldHasUniqueKey(String dbName, SyncTableConfiguration configuredTable, Connection conn)
	        throws DBException, LongTransactionException {
		
		List<UniqueKeyInfo> uniqueKeys = DBUtilities.getUniqueKeys("dsd", dbName, conn);
		
		for (UniqueKeyInfo key : uniqueKeys) {
			
			if (key.generateListFromFieldsNames().contains("dsd_uuid")) {
				return true;
			}
		}
		
		return false;
	}
	
	private void addUniqueKeyOnDsdUuidField(String dbName, SyncTableConfiguration configuredTable, Connection conn)
	        throws DBException {
		
		String table = dbName + ".dsd";
		
		String query = "ALTER TABLE " + table + " ADD UNIQUE KEY `dsd_uniqueness_key` (`dsd_uuid`);";
		
		MozartRuntaskWithTimeCheck.executeWithTimeCheck(MozartTaskType.BATCH, query, 60, conn);
	}
	
	private void updateDsdField(DBValidateInfo report, String dbName, Connection conn) throws DBException {
		String table = dbName + ".dsd";
		
		String query = "SELECT id value FROM " + table + " WHERE dsd_uuid is null";
		
		MozartRuntaskWithTimeCheck bg = MozartRuntaskWithTimeCheck.executeWithTimeCheck(MozartTaskType.QUERY, query, 300, conn);
		
		List<SimpleValue> dsds = bg.getValues();
		
		for (int i = 0; i < dsds.size(); i++) {
			SimpleValue dsd = dsds.get(i);
			
			logDebug("Updating dsd [" + dbName + "." + dsd + "] " + i + "/" + dsds.size());
			query = "UPDATE " + table + " SET dsd_uuid =  ? WHERE id = ?";
			
			String uuid = utilities.generateUUID().toString();
			
			Object[] params = { uuid, dsd.integerValue() };
			
			ResolvedProblem resolvedProblem = ResolvedProblem.init("dsd");
			resolvedProblem.setProblemType(MozartProblemType.EMPTY_FIELD);
			resolvedProblem.setColumnName("dsd_uuid");
			resolvedProblem.setRecordId(dsd.integerValue());
			
			report.addResolvedProblem(resolvedProblem);
			
			BaseDAO.executeDBQuery(query, params, conn);
		}
		
		report.getReport().saveOnFile();
	}
}
