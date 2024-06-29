package org.openmrs.module.epts.etl.dbquickexport.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.dbquickexport.engine.DBQuickExportEngine;
import org.openmrs.module.epts.etl.dbquickexport.model.DBQuickExportSearchParams;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class DBQuickExportController extends OperationController<EtlDatabaseObject> {
	
	private final String stringLock = new String("LOCK_STRING");
	
	public DBQuickExportController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		return new DBQuickExportEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			return DatabaseObjectDAO.getFirstRecord(engine.getSrcConf(), conn);
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
		
		try {
			conn = openSrcConnection();
			
			return DatabaseObjectDAO.getLastRecord(engine.getSrcConf(), conn);
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
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(
	        ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt, Engine<EtlDatabaseObject> engine) {
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new DBQuickExportSearchParams(engine, intervalsMgt);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(getEtlConfiguration().getStartDate());
		
		return searchParams;
	}
	
	public File generateJSONTempFile(SyncJSONInfo jsonInfo, AbstractTableConfiguration tableInfo, Integer startRecord,
	        Integer lastRecord) throws IOException {
		
		synchronized (stringLock) {
			
			String fileName = "";
			
			fileName += tableInfo.getParentConf().getRelatedSyncConfiguration().getSyncRootDirectory();
			fileName += FileUtilities.getPathSeparator();
			
			//Use "_" at begining of folder name to avoid situation were the starting character cause escape (ex: 't' on '\t')
			
			fileName += "_" + tableInfo.getRelatedSyncConfiguration().getOriginAppLocationCode().toLowerCase();
			fileName += FileUtilities.getPathSeparator();
			fileName += "export";
			fileName += FileUtilities.getPathSeparator();
			fileName += tableInfo.getTableName();
			fileName += FileUtilities.getPathSeparator();
			fileName += "_" + tableInfo.getRelatedSyncConfiguration().getOriginAppLocationCode().toLowerCase() + "_";
			fileName += tableInfo.getTableName();
			fileName += "_" + utilities().garantirXCaracterOnNumber(startRecord, 10);
			fileName += "_" + utilities().garantirXCaracterOnNumber(lastRecord, 10);
			
			if (new File(fileName).exists()) {
				logInfo("The file '" + fileName + "' is already exists!!! Removing it...");
				new File(fileName).delete();
			}
			
			if (new File(fileName + ".json").exists()) {
				logInfo("The file '" + fileName + ".json' is already exists!!! Removing it...");
				new File(fileName + ".json").delete();
			}
			
			FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
			
			File file = new File(fileName);
			file.createNewFile();
			
			return file;
		}
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
	
	@Override
	public void afterEtl(List<EtlDatabaseObject> objs, Connection srcConn, Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
		
	}
	
}
