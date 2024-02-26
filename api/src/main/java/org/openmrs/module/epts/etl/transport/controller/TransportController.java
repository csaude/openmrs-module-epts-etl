package org.openmrs.module.epts.etl.transport.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.SyncOperationConfig;
import org.openmrs.module.epts.etl.controller.conf.SyncTableConfiguration;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.transport.engine.TransportEngine;
import org.openmrs.module.epts.etl.transport.model.TransportSyncSearchParams;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class is responsible for control the transpor of sync files from origin to destination site
 * 
 * @author jpboane
 */
public class TransportController extends OperationController {
	
	public TransportController(ProcessController processController, SyncOperationConfig operationConfig) {
		super(processController, operationConfig);
	}
	
	@Override
	public Engine initRelatedEngine(EngineMonitor monitor, RecordLimits limits) {
		return new TransportEngine(monitor, limits);
	}
	
	@Override
	public long getMinRecordId(EtlConfiguration config) {
		File[] files = getSyncDirectory(config.getSrcTableConfiguration()).listFiles(new TransportSyncSearchParams(this, config, null));
		
		if (files == null || files.length == 0)
			return 0;
		
		Arrays.sort(files);
		
		File firstFile = files[0];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_MINRECORD_MAXRECORD.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(firstFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 2]);
	}
	
	@Override
	public long getMaxRecordId(EtlConfiguration config) {
		File[] files = getSyncDirectory(config.getSrcTableConfiguration())
		        .listFiles(new TransportSyncSearchParams(this, config, null));
		
		if (files == null || files.length == 0)
			return 0;
		
		Arrays.sort(files);
		
		File lastFile = files[files.length - 1];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_MINRECORD_MAXRECORD.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(lastFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 1]);
	}
	
	public File getSyncDirectory(SyncTableConfiguration syncInfo) {
		String fileName = "";
		
		fileName += syncInfo.getRelatedSyncConfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getRelatedSyncConfiguration().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getTableName();
		
		return new File(fileName);
	}
	
	public File getSyncBkpDirectory(SyncTableConfiguration syncInfo) throws IOException {
		String fileName = "";
		
		fileName += syncInfo.getRelatedSyncConfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getRelatedSyncConfiguration().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export_bkp";
		fileName += FileUtilities.getPathSeparator();
		
		fileName += syncInfo.getTableName();
		
		File bkpDirectory = new File(fileName);
		
		if (!bkpDirectory.exists()) {
			FileUtilities.tryToCreateDirectoryStructure(bkpDirectory.getAbsolutePath());
		}
		
		return bkpDirectory;
	}
	
	public File getSyncDestinationDirectory(SyncTableConfiguration syncInfo) throws IOException {
		String fileName = "";
		
		fileName += syncInfo.getRelatedSyncConfiguration().getSyncRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += "import";
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getRelatedSyncConfiguration().getOriginAppLocationCode().toLowerCase();
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
