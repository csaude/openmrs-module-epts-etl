package org.openmrs.module.epts.etl.problems_solver.engine.mozart;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.Extension;
import org.openmrs.module.epts.etl.conf.Key;
import org.openmrs.module.epts.etl.conf.UniqueKeyInfo;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.problems_solver.engine.DatabasesInfo;
import org.openmrs.module.epts.etl.problems_solver.engine.GenericEngine;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.MozartReportType;
import org.openmrs.module.epts.etl.problems_solver.model.mozart.MozartValidateInfoReport;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.LongTransactionException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

public abstract class MozartProblemSolverEngine extends GenericEngine {
	
	public MozartValidateInfoReport reportOfProblematics;
	
	public MozartValidateInfoReport reportOfNoIssue;
	
	public MozartValidateInfoReport reportOfResolvedProblems;
	
	protected DatabasesInfo dbsInfo;
	
	protected File reportProblematicsFile;
	
	protected File reportNoIssueFile;
	
	protected File reportResolvedProblemsFile;
	
	protected String partner;
	
	protected String province;
	
	public MozartProblemSolverEngine(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		try {
			Extension exItem = this.getRelatedOperationController().getOperationConfig().findExtension("databaseListFile");
			
			List<String> dbsName = FileUtilities.readAllFileAsListOfString(exItem.getValueString());
			
			exItem = this.getRelatedOperationController().getOperationConfig().findExtension("partner");
			
			this.partner = exItem.getValueString();
			
			exItem = this.getRelatedOperationController().getOperationConfig().findExtension("province");
			
			this.province = exItem.getValueString();
			
			DBConnectionInfo connInfo = getDefaultApp().getConnInfo();
			
			this.dbsInfo = new DatabasesInfo(partner, dbsName, connInfo);
			
			String reportDirectoryPath = getEtlConfiguration().getRelatedSyncConfiguration().getSyncRootDirectory();
			reportDirectoryPath += FileUtilities.getPathSeparator() + "mozart" + FileUtilities.getPathSeparator() + partner
			        + FileUtilities.getPathSeparator() + province;
			
			String timeStamp = DateAndTimeUtilities.formatAsPartOfFileName(DateAndTimeUtilities.getCurrentDate());
			
			this.reportProblematicsFile = new File(
			        reportDirectoryPath + FileUtilities.getPathSeparator() + timeStamp + "_problematicsDB.json");
			this.reportNoIssueFile = new File(
			        reportDirectoryPath + FileUtilities.getPathSeparator() + timeStamp + "_noIssueDB.json");
			this.reportResolvedProblemsFile = new File(
			        reportDirectoryPath + FileUtilities.getPathSeparator() + "resolvedProblemsDB.json");
			
			this.reportOfResolvedProblems = MozartValidateInfoReport.loadFromFile(this.reportResolvedProblemsFile);
			
			if (this.reportOfResolvedProblems == null) {
				this.reportOfResolvedProblems = new MozartValidateInfoReport(MozartReportType.RESOLVED_PROBLEMS,
				        this.partner, this.province, reportResolvedProblemsFile);
			}
			
			this.reportOfProblematics = new MozartValidateInfoReport(MozartReportType.DETECTED_PROBLEMS, this.partner,
			        this.province, reportProblematicsFile);
			
			this.reportOfNoIssue = new MozartValidateInfoReport(MozartReportType.NO_ISSUE, this.partner, this.province,
			        reportNoIssueFile);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public void saveReport() {
		if (this.reportOfProblematics != null) {
			this.reportOfProblematics.saveOnFile();
		}
		if (this.reportOfNoIssue != null) {
			this.reportOfNoIssue.saveOnFile();
		}
		if (this.reportOfResolvedProblems != null) {
			this.reportOfResolvedProblems.saveOnFile();
		}
	}
	
	@Override
	public void onFinish() {
		super.onFinish();
		
		this.saveReport();
	}
	
	protected List<String> generateMissingFields(String dbName, AbstractTableConfiguration configuredTable, Connection conn)
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
	
	public boolean checkIfTableExists(String tableName, String schema, Connection conn) throws DBException {
		try {
			return DBUtilities.isResourceExist(schema, null, DBUtilities.RESOURCE_TYPE_TABLE, tableName, conn);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
	private static List<UniqueKeyInfo> generateKnownUk(AbstractTableConfiguration configuredTable) {
		try {
			List<UniqueKeyInfo> knownKeys_ = new ArrayList<UniqueKeyInfo>();
			
			Extension knownKeys = configuredTable.findExtension("knownKeys");
			
			for (Extension keyInfo: knownKeys.getExtension()) {
				UniqueKeyInfo uk = new UniqueKeyInfo();
				
				for (Extension keyPart : keyInfo.getExtension()) {
					uk.addKey(new Key(keyPart.getValueString()));
				}
				
				knownKeys_.add(uk);
			}
			
			return knownKeys_;
		}
		catch (ForbiddenOperationException e1) {
			return  null;
		}
	}
	
	protected List<UniqueKeyInfo> generateMissingUniqueKeys(String dbName, AbstractTableConfiguration configuredTable, Connection conn)
	        throws DBException, LongTransactionException {
		
		List<UniqueKeyInfo> missing = new ArrayList<UniqueKeyInfo>();
		
		List<UniqueKeyInfo> knownKeys = generateKnownUk(configuredTable);
		
		if (knownKeys == null) return missing;
		
		List<UniqueKeyInfo> existingKeys = DBUtilities.getUniqueKeys(configuredTable.getTableName(), dbName, conn);
		
		for (UniqueKeyInfo knownKey : knownKeys) {
			if (!existingKeys.contains(knownKey)) {
				missing.add(knownKey);
			}
		}
		
		return missing;
	}
	
}
