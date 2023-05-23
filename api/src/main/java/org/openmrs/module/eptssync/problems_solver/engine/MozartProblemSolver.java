package org.openmrs.module.eptssync.problems_solver.engine;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.Extension;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.Field;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.model.mozart.MozartValidateInfoReport;
import org.openmrs.module.eptssync.utilities.DateAndTimeUtilities;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.DBUtilities;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;


public abstract class MozartProblemSolver extends GenericEngine {
	
	public MozartValidateInfoReport reportOfProblematics;
	public MozartValidateInfoReport reportOfNoIssue;
	public MozartValidateInfoReport reportOfResolvedProblems;
	
	protected DatabasesInfo dbsInfo;
	
	protected File reportProblematicsFile;
	protected File reportNoIssueFile;
	protected File reportResolvedProblemsFile;
	
	protected String partner;
	protected String province;
	
	public MozartProblemSolver(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		try {
			Extension exItem = this.getRelatedOperationController().getOperationConfig().findExtesion("databaseListFile");
			
			List<String> dbsName = FileUtilities.readAllFileAsListOfString(exItem.getValueString());
			
			exItem = this.getRelatedOperationController().getOperationConfig().findExtesion("partner");
				
			this.partner = exItem.getValueString();
			
			exItem = this.getRelatedOperationController().getOperationConfig().findExtesion("province");
			
			this.province = exItem.getValueString();
			
			DBConnectionInfo connInfo = getDefaultApp().getConnInfo();
		
			this.dbsInfo = new DatabasesInfo(partner, dbsName, connInfo) ;
			
			String reportDirectoryPath = getSyncTableConfiguration().getRelatedSynconfiguration().getSyncRootDirectory();
			reportDirectoryPath += FileUtilities.getPathSeparator() + "mozart" + FileUtilities.getPathSeparator() + partner + FileUtilities.getPathSeparator() + province;
			
			String timeStamp = DateAndTimeUtilities.formatAsPartOfFileName(DateAndTimeUtilities.getCurrentDate());
			
			this.reportProblematicsFile = new File(reportDirectoryPath + FileUtilities.getPathSeparator() + timeStamp + "_problematicsDB.json");
			this.reportNoIssueFile = new File(reportDirectoryPath + FileUtilities.getPathSeparator() + timeStamp + "_noIssueDB.json");
			this.reportResolvedProblemsFile = new File(reportDirectoryPath + FileUtilities.getPathSeparator() + "resolvedProblemsDB.json");
		
			this.reportOfResolvedProblems = MozartValidateInfoReport.loadFromFile(this.reportResolvedProblemsFile);
			
			if (this.reportOfResolvedProblems == null) {
				this.reportOfResolvedProblems = new MozartValidateInfoReport(this.partner, this.province, reportResolvedProblemsFile);
			}
		
			this.reportOfProblematics = new MozartValidateInfoReport(this.partner, this.province, reportProblematicsFile);
			this.reportOfNoIssue = new MozartValidateInfoReport(this.partner, this.province, reportNoIssueFile);
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
	

	protected List<String> generateMissingFields(String dbName, SyncTableConfiguration configuredTable, Connection conn)
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
			return DBUtilities.isResourceExist(schema, DBUtilities.RESOURCE_TYPE_TABLE, tableName, conn);
		}
		catch (SQLException e) {
			throw new DBException(e);
		}
	}
	
}
