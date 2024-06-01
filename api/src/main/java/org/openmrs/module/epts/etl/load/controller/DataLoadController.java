package org.openmrs.module.epts.etl.load.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.ThreadLimitsManager;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.load.engine.DataLoadEngine;
import org.openmrs.module.epts.etl.load.model.LoadSyncDataSearchParams;
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
public class DataLoadController extends EtlController {
	
	public DataLoadController(ProcessController processController, EtlOperationConfig operationConfig,
	    String appOriginLocationCode) {
		super(processController, operationConfig, appOriginLocationCode);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, ThreadLimitsManager limits) {
		return new DataLoadEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlItemConfiguration config) {
		LoadSyncDataSearchParams searchParams = new LoadSyncDataSearchParams(this, config,
		        null);
		
		File[] files = getSyncDirectory(config.getSrcConf()).listFiles(searchParams);
		
		if (files == null || files.length == 0)
			return 0;
		
		Arrays.sort(files);
		
		File firstFile = files[0];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_FIRSTRECORDID_LASTRECORDID.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(firstFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 2]);
	}
	
	@Override
	public long getMaxRecordId(EtlItemConfiguration config) {
		LoadSyncDataSearchParams searchParams = new LoadSyncDataSearchParams(this, config,
		        null);
		
		File[] files = getSyncDirectory(config.getSrcConf()).listFiles(searchParams);
		
		if (files == null || files.length == 0)
			return 0;
		
		Arrays.sort(files);
		
		File lastFile = files[files.length - 1];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_FIRSTRECORDID_LASTRECORDID.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(lastFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 1]);
	}
	
	public File getSyncDirectory(AbstractTableConfiguration syncInfo) {
		String fileName = "";
		
		fileName += syncInfo.getRelatedSyncConfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		
		fileName += "import";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += this.appOriginLocationCode;
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
		
		fileName += this.appOriginLocationCode;
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
		return hasNestedController() ? false : true;
	}
	
	@Override
	public boolean canBeRunInMultipleEngines() {
		return false;
	}
	
}
