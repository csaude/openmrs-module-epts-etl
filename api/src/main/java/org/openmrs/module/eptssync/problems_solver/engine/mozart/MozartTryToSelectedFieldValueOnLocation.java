package org.openmrs.module.eptssync.problems_solver.engine.mozart;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.controller.conf.tablemapping.MappedTableInfo;
import org.openmrs.module.eptssync.dbquickmerge.model.MergingRecord;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.SearchParamsDAO;
import org.openmrs.module.eptssync.model.base.SyncRecord;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObject;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectSearchParams;
import org.openmrs.module.eptssync.model.pojo.mozart.LocationVO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.problems_solver.controller.GenericOperationController;
import org.openmrs.module.eptssync.problems_solver.engine.DatabasesInfo;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * @author jpboane
 * @see MozartProblemSolverEngine
 */
public class MozartTryToSelectedFieldValueOnLocation extends MozartProblemSolverEngine {
	
	private AppInfo dstApp;
	
	public MozartTryToSelectedFieldValueOnLocation(EngineMonitor monitor, RecordLimits limits) {
		super(monitor, limits);
		
		try {
			this.dstApp = getRelatedSyncConfiguration().find(AppInfo.init("destination"));
		}
		catch (ForbiddenOperationException e) {
			throw new ForbiddenOperationException("You must configure 'destination' app for destination database!");
		}
	}
	
	@Override
	public GenericOperationController getRelatedOperationController() {
		return (GenericOperationController) super.getRelatedOperationController();
	}
	
	@Override
	public void performeSync(List<SyncRecord> syncRecords, Connection conn) throws DBException {
		if (done)
			return;
		
		logInfo("STARTING PROBLEMS RESOLUTION...'");
		
		performeOnServer(this.dbsInfo, conn);
		
		done = true;
	}
	
	private void performeOnServer(DatabasesInfo dbInfo, Connection conn) throws DBException {
		OpenConnection srcConn = dbInfo.acquireConnection();
		OpenConnection dstConn = dstApp.openConnection();
		
		int i = 0;
		
		try {
			
			for (String dbName : dbInfo.getDbNames()) {
				logDebug(
				    "Trying to update 'selected' field " + ++i + "/" + dbInfo.getDbNames().size() + " [" + dbName + "]");
				
				for (SyncTableConfiguration configuredTable : getRelatedSyncConfiguration().getTablesConfigurations()) {
					
					if (!configuredTable.getTableName().equals("location"))
						continue;
					
					if (!configuredTable.isFullLoaded()) {
						configuredTable.fullLoad();
					}
					
					DatabaseObjectSearchParams searchParams = new DatabaseObjectSearchParams(configuredTable, null);
					
					List<LocationVO> syncRecords = utilities.parseList(SearchParamsDAO.search(searchParams, conn),
					    LocationVO.class);
					
					for (LocationVO syncRecord : syncRecords) {
						MappedTableInfo mappingInfo = utilities.getFirstRecordOnArray(configuredTable.getDestinationTableMappingInfo());
						
						DatabaseObject destObject = mappingInfo.generateMappedObject(syncRecord, this.dstApp, conn);
						
						MergingRecord mergingData = new MergingRecord(destObject, configuredTable, getDefaultApp(),
						        this.dstApp, false);
						
						mergingData.merge(srcConn, dstConn);
					}
					
					dstConn.markAsSuccessifullyTerminated();
				}
			}
		}
		finally {
			dstConn.finalizeConnection();
			srcConn.finalizeConnection();
		}
	}
}
