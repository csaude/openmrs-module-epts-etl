package org.openmrs.module.epts.etl.dbquickload.controller;

import java.io.File;
import java.io.IOException;

import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.controller.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.dbquickload.engine.DBQuickLoadEngine;
import org.openmrs.module.epts.etl.dbquickload.model.DBQuickLoadSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class is responsible for control the loading of sync data to stage area.
 * <p>
 * This load consist on readding the JSON content from the sync directory and load them to temp
 * tables on sync stage.
 * 
 * @author jpboane
 */
public class DBQuickLoadController extends SiteOperationController {
	
	public DBQuickLoadController(ProcessController processController, SyncOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new DBQuickLoadEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlConfiguration config) {
		DBQuickLoadSearchParams searchParams = new DBQuickLoadSearchParams(this, config,
		        null);
		
		File[] files = getSyncDirectory(config.getSrcConf()).listFiles(searchParams);
		
		if (files == null || files.length == 0) {
			return 0;
		} else {
			return 1;
		}
	}
	
	@Override
	public long getMaxRecordId(EtlConfiguration config) {
		DBQuickLoadSearchParams searchParams = new DBQuickLoadSearchParams(this, config,
		        null);
		
		File[] files = getSyncDirectory(config.getSrcConf()).listFiles(searchParams);
		
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
