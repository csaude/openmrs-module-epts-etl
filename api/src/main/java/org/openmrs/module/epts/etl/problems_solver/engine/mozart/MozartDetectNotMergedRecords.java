package org.openmrs.module.epts.etl.problems_solver.engine.mozart;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.engine.DatabasesInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see EtlController
 */
public class MozartDetectNotMergedRecords extends MozartProblemSolverEngine {
	
	public MozartDetectNotMergedRecords(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (done)
			return;
		
		logInfo("DETECTING NOT MERGED RECORDS ON TABLE '" + getMainSrcTableName() + "'");
		
		performeOnServer(this.dbsInfo, conn);
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		
		List<EtlConfiguration> configuredTables = getRelatedOperationController().getConfiguration().getEtlConfiguration();
		
		int i = 0;
		for (String dbName : dbInfo.getDbNames()) {
			logDebug("[" + dbName + "] Detecting not merged records " + ++i + "/" + dbInfo.getDbNames().size() + "!");
			
			DBValidateInfo report = this.reportOfProblematics.initDBValidatedInfo(dbName);
			
			if (!DBUtilities.isResourceExist(dbName, null, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logDebug("DB '" + dbName + "' is missing!");
				
				this.reportOfProblematics.addMissingDb(dbName);
				
				continue;
			}
			
			for (EtlConfiguration conf : configuredTables) {
				SyncTableConfiguration configuredTable = conf.getMainSrcTableConf();
				
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				logDebug("[" + dbName + "] Checking table " + configuredTable.getTableName() + "...");
				
				if (!utilities.arrayHasElement(configuredTable.getUniqueKeys()))
					continue;
				
				int notMergedRecord = determineNotMergedRecord(configuredTable, dbName, srcConn, conn);
				
				if (notMergedRecord > 0) {
					logDebug(dbName + "." + configuredTable.getTableName() + " miss " + notMergedRecord + "records");
					
					report.addNotFullMergedTables(configuredTable.getTableName(), notMergedRecord);
				}
			}
			
			if (utilities.arrayHasElement(report.getNotFullMergedTables())) {
				report.addProblemType(MozartProblemType.NOT_FULL_MERGED_DB);
			}
		}
		
		logDebug("Saving report on " + this.reportOfProblematics.getJsonFile().getAbsolutePath());
		
		this.reportOfProblematics.saveOnFile();
		
		logDebug("Report saved");
		
		srcConn.markAsSuccessifullyTerminated();
		srcConn.finalizeConnection();
	}
	
	private int determineNotMergedRecord(SyncTableConfiguration tableInfo, String dbName, Connection srcConn,
	        Connection destConn) throws DBException {
		String table = dbName + "." + tableInfo.getTableName();
		
		String sql = " SELECT count(*) value \n";
		sql += " FROM   " + table + " src_\n";
		sql += " WHERE  NOT EXISTS (	SELECT * \n";
		sql += " 				   	FROM   " + tableInfo.getTableName() + " dest_\n";
		sql += "						WHERE  " + tableInfo.generateUniqueKeysJoinCondition("src_", "dest_") + ")";
		
		SimpleValue record = DatabaseObjectDAO.find(SimpleValue.class, sql, null, destConn);
		
		return record.intValue();
	}
	
}
