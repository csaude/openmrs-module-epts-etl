package org.openmrs.module.epts.etl.dbquickexport.controller;

import java.io.File;
import java.io.IOException;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.dbquickexport.engine.DBQuickExportEngine;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.DBUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class is responsible for control record changes process
 * 
 * @author jpboane
 */
public class DBQuickExportController extends OperationController {
	
	private final String stringLock = new String("LOCK_STRING");
	
	public DBQuickExportController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBQuickExportEngine(monitor, limits);
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
	
	@Override
	public OpenConnection openConnection() {
		OpenConnection conn = super.openConnection();
		
		if (getOperationConfig().isDoIntegrityCheckInTheEnd()) {
			try {
				DBUtilities.disableForegnKeyChecks(conn);
			}
			catch (DBException e) {
				e.printStackTrace();
				
				throw new RuntimeException(e);
			}
		}
		
		return conn;
	}
	
	public File generateJSONTempFile(SyncJSONInfo jsonInfo, SyncTableConfiguration tableInfo, Integer startRecord,
	        Integer lastRecord) throws IOException {
		
		synchronized (stringLock) {
			
			String fileName = "";
			
						fileName += tableInfo.getRelatedSyncConfiguration().getSyncRootDirectory();
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
	
}
