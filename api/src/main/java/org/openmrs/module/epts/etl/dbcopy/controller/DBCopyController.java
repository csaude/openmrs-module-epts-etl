package org.openmrs.module.epts.etl.dbcopy.controller;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.dbcopy.engine.DBCopyEngine;
import org.openmrs.module.epts.etl.dbcopy.model.DBCopySearchParams;
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
public class DBCopyController extends OperationController {
	
	private AppInfo srcAppInfo;
	
	private AppInfo dstAppInfo;
	
	public DBCopyController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
		
		this.srcAppInfo = getConfiguration().getMainApp();
		this.dstAppInfo = getConfiguration().exposeAllAppsNotMain().get(0);
	}
	
	public AppInfo getSrcAppInfo() {
		return srcAppInfo;
	}
	
	public AppInfo getDstAppInfo() {
		return dstAppInfo;
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBCopyEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		OpenConnection conn = openSrcConnection();
		
		try {
			DBCopySearchParams searchParams = new DBCopySearchParams(config, null, this);
			
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
	public long getMaxRecordId(EtlItemConfiguration config) {
		OpenConnection conn = openSrcConnection();
		
		DBCopySearchParams searchParams = new DBCopySearchParams(config, null, this);
		
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
	
	public OpenConnection openDstConnection() {
		return dstAppInfo.openConnection();
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
