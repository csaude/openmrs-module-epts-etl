package org.openmrs.module.eptssync.problems_solver.engine;

import java.io.IOException;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.Extension;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.model.mozart.DBValidateReport;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionInfo;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;


public abstract class MozartProblemSolver extends GenericEngine {
	
	static List<DBValidateReport> reportsProblematicDBs;
	
	static List<DBValidateReport> reportsNoIssueDBs;
	
	protected DatabasesInfo dbsInfo;

	public MozartProblemSolver(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		try {
			Extension exItem = this.getRelatedOperationController().getOperationConfig().findExtesion("databaseListFile");
			
			List<String> dbsName = FileUtilities.readAllFileAsListOfString(exItem.getValueString());
			
			exItem = this.getRelatedOperationController().getOperationConfig().findExtesion("partner");
				
			String partner = exItem.getValueString();
			
			DBConnectionInfo connInfo = getDefaultApp().getConnInfo();
			
			this.dbsInfo = new DatabasesInfo(partner, dbsName, connInfo) ;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
}
