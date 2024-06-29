package org.openmrs.module.epts.etl.transport.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.conf.EtlOperationConfig;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.transport.engine.TransportEngine;
import org.openmrs.module.epts.etl.transport.model.TransportRecord;
import org.openmrs.module.epts.etl.transport.model.TransportSyncSearchParams;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

/**
 * This class is responsible for control the transpor of sync files from origin to destination site
 * 
 * @author jpboane
 */
public class TransportController extends SiteOperationController<TransportRecord> {
	
	public TransportController(ProcessController processController, EtlOperationConfig operationConfig) {
		super(processController, operationConfig, null);
	}
	
	@Override
	public TaskProcessor<TransportRecord> initRelatedTaskProcessor(Engine<TransportRecord> monitor,
	        IntervalExtremeRecord limits, boolean runningInConcurrency) {
		return new TransportEngine(monitor, limits, runningInConcurrency);
	}
	
	@Override
	public AbstractEtlSearchParams<TransportRecord> initMainSearchParams(
	        ThreadRecordIntervalsManager<TransportRecord> intervalsMgt, Engine<TransportRecord> engine) {
		AbstractEtlSearchParams<TransportRecord> searchParams = new TransportSyncSearchParams(engine, intervalsMgt);
		searchParams.setQtdRecordPerSelected(2500);
		
		return searchParams;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public long getMinRecordId(Engine<? extends EtlDatabaseObject> engine) {
		File[] files = getSyncDirectory(engine.getSrcConf())
		        .listFiles(new TransportSyncSearchParams((Engine<TransportRecord>) engine, null));
		
		if (files == null || files.length == 0)
			return 0;
		
		Arrays.sort(files);
		
		File firstFile = files[0];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_MINRECORD_MAXRECORD.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(firstFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 2]);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public long getMaxRecordId(Engine<? extends EtlDatabaseObject> engine) {
		File[] files = getSyncDirectory(engine.getSrcConf())
		        .listFiles(new TransportSyncSearchParams((Engine<TransportRecord>) engine, null));
		
		if (files == null || files.length == 0)
			return 0;
		
		Arrays.sort(files);
		
		File lastFile = files[files.length - 1];
		
		//THIS ASSUME THAT THE FILE NAME USE THIS PATHERN TABLENAME_MINRECORD_MAXRECORD.JSON
		
		String[] pats = FileUtilities.generateFileNameFromRealPathWithoutExtension(lastFile.getName()).split("_");
		
		return Long.parseLong(pats[pats.length - 1]);
	}
	
	public File getSyncDirectory(AbstractTableConfiguration syncInfo) {
		String fileName = "";
		
		fileName += syncInfo.getRelatedSyncConfiguration().getEtlRootDirectory();
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getRelatedSyncConfiguration().getOriginAppLocationCode().toLowerCase();
		fileName += FileUtilities.getPathSeparator();
		fileName += "export";
		fileName += FileUtilities.getPathSeparator();
		fileName += syncInfo.getTableName();
		
		return new File(fileName);
	}
	
	public File getSyncBkpDirectory(AbstractTableConfiguration syncInfo) throws IOException {
		String fileName = "";
		
		fileName += syncInfo.getRelatedSyncConfiguration().getEtlRootDirectory();
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
	
	public File getSyncDestinationDirectory(AbstractTableConfiguration syncInfo) throws IOException {
		String fileName = "";
		
		fileName += syncInfo.getRelatedSyncConfiguration().getEtlRootDirectory();
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
	
	@Override
	public void afterEtl(List<TransportRecord> objs, Connection srcConn, Connection dstConn) throws DBException {
	}
}
