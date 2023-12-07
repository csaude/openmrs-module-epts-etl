package org.openmrs.module.epts.etl.dbquickcopy.controller;

import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.dbquickcopy.engine.DBQuickCopyEngine;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBConnectionService;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class DBQuickCopyController extends SiteOperationController {
	
	private DBConnectionService srcDBService;
	
	public DBQuickCopyController(ProcessController processController, SyncOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
		
		AppInfo srcApp = getConfiguration().exposeAllAppsNotMain().get(0);
		
		this.srcDBService = DBConnectionService.init(srcApp.getConnInfo());
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBQuickCopyEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(SyncTableConfiguration tableInfo) {
		OpenConnection conn = openSrcConnection();
		
		try {
			return DatabaseObjectDAO.getFirstRecord(tableInfo, conn);
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
		
		try {
			return DatabaseObjectDAO.getLastRecord(tableInfo, conn);
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
		return srcDBService.openConnection();
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
