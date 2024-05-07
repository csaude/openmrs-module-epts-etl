package org.openmrs.module.epts.etl.problems_solver.engine.mozart;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.SimpleValue;
import org.openmrs.module.epts.etl.model.base.BaseDAO;
import org.openmrs.module.epts.etl.model.base.SyncRecord;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.engine.DatabasesInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.DBValidateInfo;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.MozartFieldToFillType;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.MozartProblemType;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.ResolvedProblem;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see DbExtractController
 */
public class MozartFillEmptyFields extends MozartProblemSolverEngine {
	
	private String tableToFill;
	
	private String columnToFill;
	
	private MozartFieldToFillType type;
	
	public MozartFillEmptyFields(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		Extension exItem1 = this.getRelatedOperationController().getOperationConfig().findExtension("tableToFill");
		this.tableToFill = exItem1.getValueString();
		
		Extension exItem2 = this.getRelatedOperationController().getOperationConfig().findExtension("columnToFill");
		this.columnToFill = exItem2.getValueString();
		
		Extension exItem3 = this.getRelatedOperationController().getOperationConfig().findExtension("fieldType");
		
		this.type = MozartFieldToFillType.valueOf(exItem3.getValueString());
		
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
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
			logDebug("Trying to fill empty fields on Table " + ++i + "/" + dbInfo.getDbNames().size() + " [" + dbName + "]");
			
			DBValidateInfo report = this.reportOfResolvedProblems.initDBValidatedInfo(dbName);
			
			if (!DBUtilities.isResourceExist(dbName, null, DBUtilities.RESOURCE_TYPE_SCHEMA, dbName, srcConn)) {
				logDebug("DB '" + dbName + "' is missing!");
				
				this.reportOfProblematics.addMissingDb(dbName);
				
				continue;
			}
			
			for (EtlItemConfiguration conf : configs) {
				AbstractTableConfiguration configuredTable = conf.getSrcConf();
				
				if (!configuredTable.getTableName().equals(tableToFill))
					continue;
				
				if (!configuredTable.isFullLoaded()) {
					configuredTable.fullLoad();
				}
				
				fillTableField(report, dbName, srcConn);
			}
			
		}
		
		this.reportOfResolvedProblems.saveOnFile();
		
		srcConn.markAsSuccessifullyTerminated();
		srcConn.finalizeConnection();
	}
	
	private void fillTableField(DBValidateInfo report, String dbName, Connection conn) throws DBException {
		String table = dbName + "." + this.tableToFill;
		
		String sql = "SELECT id value FROM " + table + " WHERE " + columnToFill + " IS null";
		
		List<SimpleValue> records = DatabaseObjectDAO.search(SimpleValue.class, sql, null, conn);
		
		for (int i = 0; i < records.size(); i++) {
			SimpleValue record = records.get(i);
			
			logDebug("Updating " + dbName + "." + this.tableToFill + " [" + record + "] " + i + "/" + records.size());
			sql = "UPDATE " + table + " SET " + columnToFill + " =  ? WHERE id = ?";
			
			Object value = null;
			
			if (type.isUuid()) {
				value = utilities.generateUUID().toString();
			} else
				throw new ForbiddenOperationException("Unsupported type for auto generation! [" + this.type + "]");
			
			Object[] params = { value, record.integerValue() };
			
			BaseDAO.executeQueryWithRetryOnError(sql, params, conn);
			
			ResolvedProblem rp = ResolvedProblem.init(this.tableToFill);
			rp.setColumnName(this.columnToFill);
			rp.setNewColumnValue(value);
			rp.setRecordId(record.integerValue());
			rp.setProblemType(MozartProblemType.EMPTY_FIELD);
			
			report.addResolvedProblem(rp);
		}
		
		report.getReport().saveOnFile();
	}
	
}
