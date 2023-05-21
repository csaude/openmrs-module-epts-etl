package org.openmrs.module.eptssync.merge.controller;

import org.openmrs.module.eptssync.common.model.SyncImportInfoDAO;
import org.openmrs.module.eptssync.common.model.SyncImportInfoVO;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.merge.engine.DataBasesMergeFromSourceEngine;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the process of data bases merge
 * 
 * 
 * 
 * @author jpboane
 *
 */
public class DataBaseMergeFromSourceDBController extends OperationController {
		
	private AppInfo mainApp;
	private AppInfo remoteApp;
	
	public DataBaseMergeFromSourceDBController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
		
		this.mainApp = getConfiguration().find(AppInfo.init("main"));
		this.remoteApp = getConfiguration().find(AppInfo.init("remote"));
	}
	
	public AppInfo getMainApp() {
		return mainApp;
	}
	
	public AppInfo getRemoteApp() {
		return remoteApp;
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DataBasesMergeFromSourceEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {	
		OpenConnection conn = openConnection();
		
		Integer id = Integer.valueOf(0);
			
		try {
			SyncImportInfoVO record = SyncImportInfoDAO.getFirstMissingRecordInDestination(tableInfo, conn);
			
			id = record != null ? record.getId() : 0;
			
			return id;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	public long getMaxRecordId(SyncTableConfiguration tableInfo) {	
		OpenConnection conn = openConnection();
		
		Integer id = Integer.valueOf(0);
		
		try {
			SyncImportInfoVO record = SyncImportInfoDAO.getLastMissingRecordInDestination(tableInfo, conn);
			
			id = record != null ? record.getId() : 0;
		
			return id;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
}
