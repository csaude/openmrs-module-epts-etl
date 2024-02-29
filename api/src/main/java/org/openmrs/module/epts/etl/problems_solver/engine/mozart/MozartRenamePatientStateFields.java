package org.openmrs.module.epts.etl.problems_solver.engine.mozart;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.engine.DatabasesInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.ResolvedProblem;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see EtlController
 */
public class MozartRenamePatientStateFields extends MozartProblemSolverEngine {
	
	public MozartRenamePatientStateFields(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (done)
			return;
		
		logInfo("DETECTING PROBLEMS ON TABLE '" + getSrcTableName() + "'");
		
		performeOnServer(this.dbsInfo, conn);
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		
		List<EtlConfiguration> configs = getRelatedOperationController().getConfiguration().getEtlConfiguration();
		
		int i = 0;
		for (String dbName : dbInfo.getDbNames()) {
			logDebug("Trying to rename fields on Patient_State on DB " + ++i + "/" + dbInfo.getDbNames().size() + " ["
			        + dbName + "]");
			
			DBValidateInfo report = this.reportOfResolvedProblems.initDBValidatedInfo(dbName);
			
			if (!DBUtilities.isResourceExist(dbName, null, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logWarn("DB '" + dbName + "' is missing!");
				
				this.reportOfProblematics.addMissingDb(dbName);
				
				continue;
			}
			for (EtlConfiguration config : configs) {
				SyncTableConfiguration configuredTable = config.getSrcTableConfiguration();
				
				if (!configuredTable.getTableName().equals("patient_state"))
					continue;
				
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				List<String> missingField = generateMissingFields(dbName, configuredTable, srcConn);
				
				tryToRenameFields(report, dbName, missingField, srcConn);
			}
		}
	}
	
	private void tryToRenameFields(DBValidateInfo report, String dbName, List<String> missingField, OpenConnection conn)
	        throws DBException {
		
		if (utilities.arrayHasElement(missingField) && utilities.existOnArray(missingField, "program_enrollment_date")) {
			missingField.remove("program_enrollment_date");
			
			String table = dbName + ".patient_state";
			
			logWarn("Renaming field 'program_enrolment_date' to 'program_enrollment_date' on " + table);
			
			String sql = "";
			
			sql += "ALTER TABLE " + table
			        + " CHANGE `program_enrolment_date` `program_enrollment_date` datetime DEFAULT NULL";
			
			ResolvedProblem resolvedProblem = ResolvedProblem.init("patient_state");
			resolvedProblem.setProblemType(MozartProblemType.WRONG_FIELD_NAME);
			resolvedProblem.setOriginalColumnName("program_enrolment_date");
			resolvedProblem.setColumnName("program_enrollment_date");
			
			report.addResolvedProblem(resolvedProblem);
			
			report.getReport().saveOnFile();
			
			DBUtilities.executeBatch(conn, sql);
		}
		
		if (utilities.arrayHasElement(missingField) && utilities.existOnArray(missingField, "enrollment_uuid")) {
			missingField.remove("enrollment_uuid");
			
			String table = dbName + ".patient_state";
			
			logWarn("Renaming field 'enrolment_uuid' to 'enrollment_uuid' on " + table);
			
			String sql = "";
			
			sql += "ALTER TABLE " + table + " CHANGE `enrolment_uuid` `enrollment_uuid` char(38) DEFAULT NULL";
			
			ResolvedProblem resolvedProblem = ResolvedProblem.init("patient_state");
			resolvedProblem.setProblemType(MozartProblemType.WRONG_FIELD_NAME);
			resolvedProblem.setOriginalColumnName("enrolment_uuid");
			resolvedProblem.setColumnName("enrollment_uuid");
			
			report.addResolvedProblem(resolvedProblem);
			
			report.getReport().saveOnFile();
			
			DBUtilities.executeBatch(conn, sql);
		}
		
		if (utilities.arrayHasElement(missingField) && utilities.existOnArray(missingField, "created_date")) {
			missingField.remove("created_date");
			
			String table = dbName + ".patient_state";
			
			logWarn("Adding 'created_date' on " + table);
			
			String sql = "";
			
			sql += "ALTER TABLE " + table + " ADD `created_date` datetime DEFAULT NULL";
			
			ResolvedProblem resolvedProblem = ResolvedProblem.init("patient_state");
			resolvedProblem.setProblemType(MozartProblemType.MISSIN_FIELDS);
			resolvedProblem.setColumnName("created_date");
			
			report.addResolvedProblem(resolvedProblem);
			
			report.getReport().saveOnFile();
			
			DBUtilities.executeBatch(conn, sql);
			
			sql = "UPDATE " + table + " SET created_date = state_date WHERE created_date is NULL";
			
			BaseDAO.executeQueryWithRetryOnError(sql, null, conn);
			
		}
		
		if (utilities.arrayHasElement(missingField) && utilities.existOnArray(missingField, "encounter_uuid")) {
			missingField.remove("encounter_uuid");
			
			String table = dbName + ".patient_state";
			
			logWarn("Adding 'encounter_uuid' on " + table);
			
			String sql = "";
			
			sql += "ALTER TABLE " + table + " ADD `encounter_uuid` char(38) DEFAULT NULL";
			
			ResolvedProblem resolvedProblem = ResolvedProblem.init("patient_state");
			resolvedProblem.setProblemType(MozartProblemType.MISSIN_FIELDS);
			resolvedProblem.setColumnName("encounter_uuid");
			
			report.addResolvedProblem(resolvedProblem);
			
			report.getReport().saveOnFile();
			
			DBUtilities.executeBatch(conn, sql);
		}
		
		conn.commitCurrentWork();
		
	}
}
