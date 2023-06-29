package org.openmrs.module.eptssync.dbcopy.controller;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbcopy.engine.DBCopyEngine;
import org.openmrs.module.eptssync.dbcopy.model.DBCopySearchParams;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class DBCopyController extends OperationController {
	
	private AppInfo srcAppInfo;
	
	private AppInfo destAppInfo;
	
	public DBCopyController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
		
		this.srcAppInfo = getConfiguration().getMainApp();
		this.destAppInfo = getConfiguration().exposeAllAppsNotMain().get(0);
	}
	
	public AppInfo getSrcAppInfo() {
		return srcAppInfo;
	}
	
	public AppInfo getDestAppInfo() {
		return destAppInfo;
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBCopyEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openSrcConnection();
		
		try {
			DBCopySearchParams searchParams = new DBCopySearchParams(tableInfo, null, this);
			
			return DatabaseObjectDAO.getFirstRecord(searchParams, conn);
		}
		catch (DBException e) {
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
		
		DBCopySearchParams searchParams = new DBCopySearchParams(tableInfo, null, this);
		
		try {
			return DatabaseObjectDAO.getLastRecord(searchParams, conn);
		}
		catch (DBException e) {
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
		return srcAppInfo.openConnection();
	}
	
	public OpenConnection openDestConnection() {
		return destAppInfo.openConnection();
	}
}
