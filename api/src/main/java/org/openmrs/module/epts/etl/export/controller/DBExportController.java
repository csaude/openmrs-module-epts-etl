package org.openmrs.module.epts.etl.export.controller;

import java.io.File;
import java.io.IOException;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.controller.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.export.engine.DBExportEngine;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class is responsible for control the data export in the synchronization processs
 * 
 * @author jpboane
 *
 */
public class DBExportController extends OperationController {
	
	public DBExportController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}

	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBExportEngine(monitor, limits);
	}

	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		
		if (!config.getSrcConf().getPrimaryKey().isSimpleNumericKey()) {
			throw new ForbiddenOperationException("Not supported composite primary key");
		}
		
		OpenConnection conn = openConnection();
		
		try {
			DatabaseObject obj = DatabaseObjectDAO.getFirstConsistentRecordInOrigin(config.getSrcConf(), conn);
		
			if (obj != null) return obj.getObjectId().getSimpleValueAsInt();
			
			return 0;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}

	@Override
	public long getMaxRecordId(EtlItemConfiguration config) {
		if (!config.getSrcConf().getPrimaryKey().isSimpleNumericKey()) {
			throw new ForbiddenOperationException("Not supported composite primary key");
		}
		
		OpenConnection conn = openConnection();
		
		try {
			DatabaseObject obj = DatabaseObjectDAO.getLastConsistentRecordOnOrigin(config.getSrcConf(), conn);
		
			if (obj != null) return obj.getObjectId().getSimpleValueAsInt();
			
			return 0;
		} catch (DBException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		finally {
			conn.finalizeConnection();
		}
	}
	
	public synchronized File generateJSONTempFile(SyncJSONInfo jsonInfo, AbstractTableConfiguration tableInfo, Integer startRecord, Integer lastRecord) throws IOException {
		String fileName = "";
		
		fileName += tableInfo.getRelatedSyncConfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getRelatedSyncConfiguration().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getRelatedSyncConfiguration().getOriginAppLocationCode() + "_";
		fileName += tableInfo.getTableName();
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getTableName();
		
		fileName += "_" + utilities().garantirXCaracterOnNumber(startRecord, 10);
		fileName += "_" + utilities().garantirXCaracterOnNumber(lastRecord, 10);
	
		if(new File(fileName).exists() ) {
			logInfo("The file '" + fileName + "' is already exists!!! Removing it...");
			new File(fileName).delete();
		}
		
		if(new File(fileName+".json").exists() ) {
			logInfo("The file '" + fileName  + ".json' is already exists!!! Removing it...");
			new File(fileName+".json").delete();
		}
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		File file = new File(fileName);
		file.createNewFile();
		
		return file;
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