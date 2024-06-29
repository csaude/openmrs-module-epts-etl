package org.openmrs.module.epts.etl.export.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.export.engine.DBExportEngine;
import org.openmrs.module.epts.etl.export.model.ExportSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObjectDAO;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.db.conn.OpenConnection;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class is responsible for control the data export in the synchronization processs
 * 
 * @author jpboane
 */
public class DBExportController extends OperationController<EtlDatabaseObject> {
	
	public DBExportController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		return new DBExportEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt,
	        Engine<EtlDatabaseObject> engine) {
		
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new ExportSearchParams(engine, intervalsMgt);
		searchParams.setQtdRecordPerSelected(getQtyRecordsPerProcessing());
		searchParams.setSyncStartDate(this.getProgressInfo().getStartTime());
		
		return searchParams;
	}
	
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		
		if (!engine.getSrcConf().getPrimaryKey().isSimpleNumericKey()) {
			throw new ForbiddenOperationException("Not supported composite primary key");
		}
		
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			EtlDatabaseObject obj = DatabaseObjectDAO.getFirstConsistentRecordInOrigin(engine.getSrcConf(), conn);
			
			if (obj != null)
				return obj.getObjectId().getSimpleValueAsInt();
			
			return 0;
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
		if (!engine.getSrcConf().getPrimaryKey().isSimpleNumericKey()) {
			throw new ForbiddenOperationException("Not supported composite primary key");
		}
		
		OpenConnection conn = null;
		
		try {
			conn = openSrcConnection();
			
			EtlDatabaseObject obj = DatabaseObjectDAO.getLastConsistentRecordOnOrigin(engine.getSrcConf(), conn);
			
			if (obj != null)
				return obj.getObjectId().getSimpleValueAsInt();
			
			return 0;
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
	
	public synchronized File generateJSONTempFile(SyncJSONInfo jsonInfo, AbstractTableConfiguration tableInfo,
	        Integer startRecord, Integer lastRecord) throws IOException {
		String fileName = "";
		
		fileName += tableInfo.getRelatedEtlConf().getEtlRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getRelatedEtlConf().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		fileName += tableInfo.getRelatedEtlConf().getOriginAppLocationCode() + "_";
		fileName += tableInfo.getTableName();
		fileName += FileUtilities.getPathSeparator();
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
	
	@Override
	public boolean mustRestartInTheEnd() {
		return false;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return true;
	}
	
	@Override
	public void afterEtl(List<EtlDatabaseObject> objs, Connection srcConn, Connection dstConn) throws DBException {
	}
}
