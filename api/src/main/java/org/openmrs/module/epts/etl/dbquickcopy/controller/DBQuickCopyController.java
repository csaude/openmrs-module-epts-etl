package org.openmrs.module.epts.etl.dbquickcopy.controller;

import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.dbquickcopy.engine.DBQuickCopyEngine;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class DBQuickCopyController extends EtlController {
	
	private AppInfo dstConn;
	
	private AppInfo srcApp;
	
	public DBQuickCopyController(ProcessController processController, EtlOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
		
		this.srcApp = getConfiguration().find(AppInfo.init("main"));
		this.dstConn = getConfiguration().find(AppInfo.init("destination"));
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		return new DBQuickCopyEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		OpenConnection conn = null;
		
		try {
			conn = openConnection();
			
			return DatabaseObjectDAO.getFirstRecord(config.getSrcConf(), conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	@Override
	public long getMaxRecordId(EtlItemConfiguration config) {
		OpenConnection conn = null;
		
		try {
			conn = openConnection();
			
			return DatabaseObjectDAO.getLastRecord(config.getSrcConf(), conn);
		}
		catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			if (conn != null)
				conn.finalizeConnection();
		}
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	public OpenConnection openDstConnection() throws DBException {
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
