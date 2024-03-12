package org.openmrs.module.epts.etl.transport.model;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;

import org.openmrs.module.epts.etl.controller.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.engine.RecordLimits;
import org.openmrs.module.epts.etl.engine.SyncSearchParams;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.transport.controller.TransportController;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class TransportSyncSearchParams extends SyncSearchParams<DatabaseObject> implements FilenameFilter {
	
	private String firstFileName;
	
	private String lastFileName;
	
	private String fileNamePathern;
	
	private TransportController controller;
	
	public TransportSyncSearchParams(TransportController controller, EtlConfiguration config, RecordLimits limits) {
		super(config, limits);
		
		this.controller = controller;
		
		if (limits != null) {
			this.firstFileName = getMainSrcTableConf().getTableName() + "_"
			        + utilities.garantirXCaracterOnNumber(limits.getCurrentFirstRecordId(), 10) + "_"
			        + utilities.garantirXCaracterOnNumber(limits.getCurrentFirstRecordId(), 10) + ".json";
			this.lastFileName = getMainSrcTableConf().getTableName() + "_"
			        + utilities.garantirXCaracterOnNumber(limits.getCurrentLastRecordId(), 10) + "_"
			        + utilities.garantirXCaracterOnNumber(limits.getCurrentLastRecordId(), 10) + ".json";
		}
	}
	
	public String getFileNamePathern() {
		return fileNamePathern;
	}
	
	public void setFileNamePathern(String fileNamePathern) {
		this.fileNamePathern = fileNamePathern;
	}
	
	@Override
	public SearchClauses<DatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		return null;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		boolean isJSON = name.toLowerCase().endsWith("json");
		boolean isNotMinimal = !name.toLowerCase().contains("minimal");
		
		boolean isInInterval = true;
		
		if (hasLimits()) {
			isInInterval = isInInterval && name.compareTo(this.firstFileName) >= 0;
			isInInterval = isInInterval && name.compareTo(this.lastFileName) <= 0;
		}
		
		boolean pathernOk = true;
		
		if (utilities.stringHasValue(this.fileNamePathern)) {
			pathernOk = name.contains(this.fileNamePathern);
		}
		
		return isJSON && isNotMinimal && isInInterval && pathernOk;
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		return countNotProcessedRecords(conn);
	}
	
	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		File[] files = getSyncDirectory().listFiles(this);
		
		if (files != null)
			return files.length;
		
		return 0;
	}
	
	private File getSyncDirectory() {
		return controller.getSyncDirectory(getMainSrcTableConf());
	}
}
