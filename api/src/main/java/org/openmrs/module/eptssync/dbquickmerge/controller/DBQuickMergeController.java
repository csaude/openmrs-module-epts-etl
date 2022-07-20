package org.openmrs.module.eptssync.dbquickmerge.controller;

import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.SiteOperationController;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickmerge.engine.DBQuickMergeEngine;
import org.openmrs.module.eptssync.dbquickmerge.model.MergeType;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the quick merge process. The quick merge process imediatly merge records from the source to the destination db
 * This process assume that the source and destination are located in the same network
 * 
 * @author jpboane
 *
 */
public class DBQuickMergeController extends SiteOperationController {
	private AppInfo mainApp;
	private AppInfo remoteApp;
	
	public DBQuickMergeController(ProcessController processController, SyncOperationConfig operationConfig, String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
		
		this.mainApp = getConfiguration().find(AppInfo.init("main"));
		this.remoteApp = getConfiguration().find(AppInfo.init("remote"));
	}
	
	public MergeType getMergeType() {
		if (getOperationConfig().isDBQuickMergeExistingRecords()) return MergeType.EXISTING;
		if (getOperationConfig().isDBQuickMergeMissingRecords()) return MergeType.MISSING;
		
		throw new ForbiddenOperationException("Not supported operation '" + getOperationConfig().getDesignation() + "'");
	}
	
	public AppInfo getMainApp() {
		return mainApp;
	}
	
	public AppInfo getRemoteApp() {
		return remoteApp;
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBQuickMergeEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openSrcConnection();
		
		try {
			return OpenMRSObjectDAO.getFirstRecord(tableInfo, conn);
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
		OpenConnection conn = openSrcConnection();
		
		try {
			return OpenMRSObjectDAO.getLastRecord(tableInfo, conn);
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

	public OpenConnection openSrcConnection() {
		return remoteApp.openConnection();
	}	
}
