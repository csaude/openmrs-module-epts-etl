package org.openmrs.module.epts.etl.dbquickload.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.EtlItemConfiguration;
import org.openmrs.module.epts.etl.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.epts.etl.dbquickload.engine.QuickLoadLimits;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SyncJSONInfoMinimal;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DBQuickLoadSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> implements FilenameFilter {
	
	private DBQuickLoadController controller;
	
	public DBQuickLoadSearchParams(DBQuickLoadController controller, EtlItemConfiguration config, QuickLoadLimits limits) {
		super(config, limits);
		
		this.controller = controller;
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(Connection conn) throws DBException {
		return null;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith("json");
	}
	
	@Override
	public int countAllRecords(Connection conn) throws DBException {
		LoadedRecordsSearchParams syncSearchParams = new LoadedRecordsSearchParams(getConfig(), null,
		        controller.getAppOriginLocationCode());
		
		int processed = syncSearchParams.countAllRecords(conn);
		
		int notProcessed = countNotProcessedRecords(conn);
		
		return processed + notProcessed;
	}
	
	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		try {
			File[] files = getSyncDirectory().listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith("json");
				}
			});
			
			int notYetProcessed = 0;
			
			if (files == null)
				return 0;
			
			for (File file : files) {
				try {
					String json = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
					
					notYetProcessed += SyncJSONInfoMinimal.loadFromJSON(json).getQtyRecords();
				}
				catch (NoSuchFileException e) {}
			}
			
			return notYetProcessed;
		}
		catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	private File getSyncDirectory() {
		return this.controller.getSyncDirectory(getSrcTableConf());
	}
}
