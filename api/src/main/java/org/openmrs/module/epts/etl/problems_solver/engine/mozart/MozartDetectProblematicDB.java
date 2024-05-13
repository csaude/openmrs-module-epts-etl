package org.openmrs.module.epts.etl.problems_solver.engine.mozart;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.EtlObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.problems_solver.engine.DatabasesInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see MozartProblemSolverEngine
 */
public class MozartDetectProblematicDB extends MozartProblemSolverEngine {
	
	public MozartDetectProblematicDB(EngineMonitor monitor, RecordLimits limits) {
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
	public void performeSync(List<EtlObject> etlObjects, Connection conn) throws DBException {
		if (done)
			return;
		
		logInfo("DETECTING PROBLEMS ON TABLE '" + this.getMainSrcTableName() + "'");
		
		performeOnServer(this.dbsInfo, conn);
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		
		List<EtlItemConfiguration> configs = getRelatedOperationController().getConfiguration().getEtlItemConfiguration();
		
		int i = 0;
		
		for (String dbName : dbInfo.getDbNames()) {
			logDebug("Decteting problems on DB " + ++i + "/" + dbInfo.getDbNames().size() + " [" + dbName + "]");
			
			DBValidateInfo report = new DBValidateInfo(dbName);
			
			if (!DBUtilities.isResourceExist(dbName, null, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logWarn("DB '" + dbName + "' is missing!");
				
				report.addProblemType(MozartProblemType.MISSING_DB);
				
				continue;
			}
			
			for (EtlItemConfiguration conf : configs) {
				
				AbstractTableConfiguration configuredTable = conf.getSrcConf();
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				//Check if the table is missing
				if (!checkIfTableExists(configuredTable.getTableName(), dbName, srcConn)) {
					logDebug("The table " + dbName + "." + configuredTable.getTableName() + " does not exists");
					
					report.addMissingTable(configuredTable.getTableName());
					
					report.addProblemType(MozartProblemType.MISSING_TABLES);
				} else {
					
					//Check if table is empty. Start checking if table allow empty values
					boolean canBeEmpty = false;
					
					try {
						Extension emptyAllowed = configuredTable.findExtension("emptyAllowed");
						
						if (emptyAllowed.getValueString().equals("true")) {
							canBeEmpty = true;
						}
					}
					catch (ForbiddenOperationException e) {}
					
					if (!canBeEmpty) {
						String sql = "select count(*) as value from  " + dbName + "." + configuredTable.getTableName();
						
						SimpleValue result = DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn);
						
						if (result.intValue() == 0) {
							logDebug("The table " + dbName + "." + configuredTable.getTableName() + " Is empty");
							
							report.addProblemType(MozartProblemType.EMPTY_TABLES);
							report.addEmptyTable(configuredTable.getTableName());
						}
					}
					
					//Check if there are missing fields
					List<String> missingField = generateMissingFields(dbName, configuredTable, srcConn);
					
					if (utilities.arrayHasElement(missingField)) {
						logDebug("There are missing field on table " + dbName + "." + configuredTable.getTableName());
						report.addMissingFields(configuredTable.getTableName(), missingField);
						
						report.addProblemType(MozartProblemType.MISSIN_FIELDS);
					}
					
					//Check missing keys
					
					List<UniqueKeyInfo> missingKeys = generateMissingUniqueKeys(dbName, configuredTable, srcConn);
					
					if (missingKeys.size() > 0) {
						missingKeys = generateMissingUniqueKeys(dbName, configuredTable, srcConn);
						
						logDebug("There are some missing UKs " + missingKeys);
						report.addProblemType(MozartProblemType.MISSING_UNIQUE_KEY);
						
						for (UniqueKeyInfo key : missingKeys) {
							report.addMissingUniqueKeys(configuredTable.getTableName(), key.generateListFromFieldsNames());
						}
					}
					
					//Check if there are empty fields which does not allow empty value
					//boolean canBeEmpty = false;
					
					try {
						Extension fieldsNotAllowingEmptyValue = configuredTable.findExtension("emptyForbidenFields");
						
						List<String> emptyFields = new ArrayList<String>();
						
						for (Extension field : fieldsNotAllowingEmptyValue.getExtension()) {
							if (utilities.existOnArray(missingField, field.getValueString()))
								continue;
							
							String sql = "select count(*) as value from  " + dbName + "." + configuredTable.getTableName();
							sql += " where " + field.getValueString() + " is null";
							
							SimpleValue result = DatabaseObjectDAO.find(SimpleValue.class, sql, null, conn);
							
							if (result.intValue() > 0) {
								logDebug("The table " + dbName + "." + configuredTable.getTableName()
								        + " Contains empty data on field " + field.getValueString());
								
								emptyFields.add(field.getValueString());
							}
						}
						
						if (utilities.arrayHasElement(emptyFields)) {
							report.addProblemType(MozartProblemType.EMPTY_FIELD);
							report.addEmptyFields(configuredTable.getTableName(), emptyFields);
						}
					}
					catch (ForbiddenOperationException e) {}
				}
			}
			
			if (report.hasProblem()) {
				this.reportOfProblematics.addReport(report);
			} else {
				this.reportOfNoIssue.addReport(report);
			}
		}
	}
	
}
