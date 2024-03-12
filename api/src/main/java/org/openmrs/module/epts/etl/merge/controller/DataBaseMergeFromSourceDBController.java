package org.openmrs.module.epts.etl.merge.controller;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.AppInfo;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.merge.engine.DataBasesMergeFromSourceEngine;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the process of data bases merge
 * 
 * @author jpboane
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
	public long getMinRecordId(EtlConfiguration config) {
		OpenConnection conn = openConnection();
		
		Integer id = Integer.valueOf(0);
		
		try {
			SyncImportInfoVO record = SyncImportInfoDAO.getFirstMissingRecordInDestination(config.getMainSrcTableConf(), conn);
			
			id = record != null ? record.getId() : 0;
			
			return id;
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
		
		Integer id = Integer.valueOf(0);
		
		try {
			SyncImportInfoVO record = SyncImportInfoDAO.getLastMissingRecordInDestination(config.getMainSrcTableConf(), conn);
			
			id = record != null ? record.getId() : 0;
			
			return id;
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
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
