package org.openmrs.module.epts.etl.merge.controller;

import org.openmrs.module.epts.etl.common.model.SyncImportInfoDAO;
import org.openmrs.module.epts.etl.common.model.SyncImportInfoVO;
import org.openmrs.module.epts.etl.conf.AppInfo;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.merge.engine.DataBasesMergeFromSourceEngine;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;

/**
 * This class is responsible for control the process of data bases merge
 * 
 * @author jpboane
 */
public class DataBaseMergeFromSourceDBController extends EtlController {
	
	private AppInfo mainApp;
	
	private AppInfo remoteApp;
	
	public DataBaseMergeFromSourceDBController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
		
		this.mainApp = getEtlConfiguration().find(AppInfo.init("main"));
		this.remoteApp = getEtlConfiguration().find(AppInfo.init("remote"));
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt,
	        Engine<EtlDatabaseObject> engine) {
		
		throw new ForbiddenOperationException("Review this method");
		
		/*AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new DataBaseMergeFromSourceDBSearchParams(engine, intervalsMgt);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getStartDate());
		
		return searchParams;*/
	}
	
	public AppInfo getMainApp() {
		return mainApp;
	}
	
	public AppInfo getRemoteApp() {
		return remoteApp;
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedEngine(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits) {
		return new DataBasesMergeFromSourceEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		Integer id = Integer.valueOf(0);
		
		try {
			conn = openSrcConnection();
			
			SyncImportInfoVO record = SyncImportInfoDAO.getFirstMissingRecordInDestination(engine.getSrcConf(), conn);
			
			id = record != null ? record.getId() : 0;
			
			return id;
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
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		Integer id = Integer.valueOf(0);
		
		try {
			conn = openSrcConnection();
			
			SyncImportInfoVO record = SyncImportInfoDAO.getLastMissingRecordInDestination(engine.getSrcConf(), conn);
			
			id = record != null ? record.getId() : 0;
			
			return id;
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
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
}
