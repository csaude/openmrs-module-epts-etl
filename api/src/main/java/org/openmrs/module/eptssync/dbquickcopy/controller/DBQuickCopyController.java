package org.openmrs.module.eptssync.dbquickcopy.controller;

import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.conf.AppInfo;
import org.openmrs.module.eptssync.controller.conf.SyncOperationConfig;
import org.openmrs.module.eptssync.controller.conf.SyncTableConfiguration;
import org.openmrs.module.eptssync.dbquickcopy.engine.DBQuickCopyEngine;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.model.pojo.generic.OpenMRSObjectDAO;
import org.openmrs.module.eptssync.monitor.EngineMonitor;
import org.openmrs.module.eptssync.utilities.db.conn.DBConnectionService;
import org.openmrs.module.eptssync.utilities.db.conn.DBException;
import org.openmrs.module.eptssync.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 *
 */
public class DBQuickCopyController extends OperationController {
	private DBConnectionService srcDBService;
	private String appOriginLocationCode;
	
	public DBQuickCopyController(ProcessController processController, SyncOperationConfig operationConfig, String appOriginLocationCode) {
		super(processController, operationConfig);
		
		this.controllerId = processController.getControllerId() + "_" + getOperationType();	
		
		this.appOriginLocationCode = operationConfig.getRelatedSyncConfig().getOriginAppLocationCode();
		
		AppInfo srcInfo = getConfiguration().exposeAllAppsNotMain().get(0); 
		
		this.srcDBService = DBConnectionService.init(srcInfo.getConnInfo());
	}
	
	public String getAppOriginLocationCode() {
		return appOriginLocationCode;
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBQuickCopyEngine(monitor, limits);
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

	@Override
	public String getOperationType() {
		return  this.operationConfig.getOperationType();
	}
	
	public OpenConnection openSrcConnection() {
		return srcDBService.openConnection();
	}	
}
