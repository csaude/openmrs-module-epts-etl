package org.openmrs.module.epts.etl.dbquickcopy.controller;

import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.dbquickcopy.engine.DBQuickCopyEngine;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class DBQuickCopyController extends SiteOperationController {
	
	private AppInfo dstConn;
	
	private AppInfo srcApp;
	
	public DBQuickCopyController(ProcessController processController, SyncOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
		
		this.srcApp = getConfiguration().find(AppInfo.init("main"));
		this.dstConn = getConfiguration().find(AppInfo.init("destination"));
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBQuickCopyEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlConfiguration config) {
		OpenConnection conn = openConnection();
		
		try {
			return DatabaseObjectDAO.getFirstRecord(config.getSrcTableConfiguration(), conn);
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
	public long getMaxRecordId(EtlConfiguration config) {
		OpenConnection conn = openConnection();
		
		try {
			return DatabaseObjectDAO.getLastRecord(config.getSrcTableConfiguration(), conn);
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
	
	public OpenConnection openDstConnection() {
		return dstConn.openConnection();
	}
	
	public AppInfo getDstApp() {
		return dstConn;
	}
	
	public AppInfo getSrcApp() {
		return srcApp;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
