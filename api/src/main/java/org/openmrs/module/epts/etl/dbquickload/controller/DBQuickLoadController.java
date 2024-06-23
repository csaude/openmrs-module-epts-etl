package org.openmrs.module.epts.etl.dbquickload.controller;

import java.io.File;
import java.io.IOException;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.dbquickload.engine.DBQuickLoadEngine;
import org.openmrs.module.epts.etl.dbquickload.engine.QuickLoadLimits;
import org.openmrs.module.epts.etl.dbquickload.model.DBQuickLoadSearchParams;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class is responsible for control the loading of sync data to stage area.
 * <p>
 * This load consist on readding the JSON content from the sync directory and load them to temp
 * tables on sync stage.
 * 
 * @author jpboane
 */
public class DBQuickLoadController extends EtlController {
	
	public DBQuickLoadController(ProcessController processController, EtlOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
	}
	
	@Override
	public TaskProcessor<EtlDatabaseObject> initRelatedTaskProcessor(Engine<EtlDatabaseObject> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		
		return new DBQuickLoadEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> initMainSearchParams(
	        ThreadRecordIntervalsManager<EtlDatabaseObject> intervalsMgt, Engine<EtlDatabaseObject> engine) {
		
		QuickLoadLimits loadLimits = new QuickLoadLimits();
		loadLimits.copy(intervalsMgt);
		
		AbstractEtlSearchParams<EtlDatabaseObject> searchParams = new DBQuickLoadSearchParams(engine, loadLimits);
		
		searchParams.setQtdRecordPerSelected(1);
		
		loadLimits.setRelatedSearchParams((DBQuickLoadSearchParams) searchParams);
		
		return searchParams;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		DBQuickLoadSearchParams searchParams = new DBQuickLoadSearchParams((Engine<EtlDatabaseObject>) engine, null);
		
		File[] files = getSyncDirectory(engine.getSrcConf()).listFiles(searchParams);
		
		if (files == null || files.length == 0) {
			return 0;
		} else {
			return 1;
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		DBQuickLoadSearchParams searchParams = new DBQuickLoadSearchParams((Engine<EtlDatabaseObject>) engine, null);
		
		File[] files = getSyncDirectory(engine.getSrcConf()).listFiles(searchParams);
		
		if (files == null || files.length == 0) {
			return 0;
		} else {
			return files.length;
		}
	}
	
	public File getSyncDirectory(AbstractTableConfiguration syncInfo) {
		String fileName = "";
		
		fileName += syncInfo.getRelatedSyncConfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += this.getAppOriginLocationCode();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += syncInfo.getTableName();
		
		return new File(fileName);
	}
	
	public File getSyncBkpDirectory(AbstractTableConfiguration syncInfo) throws IOException {
		String fileName = "";
		
		fileName += syncInfo.getRelatedSyncConfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import_bkp";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += this.getAppOriginLocationCode();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += syncInfo.getTableName();
		
		File bkpDirectory = new File(fileName);
		
		if (!bkpDirectory.exists()) {
			FileUtilities.tryToCreateDirectoryStructure(bkpDirectory.getAbsolutePath());
		}
		
		return bkpDirectory;
	}
	
	@Override
	public boolean mustRestartInTheEnd() {
		return hasChild() ? false : true;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return false;
	}
}
