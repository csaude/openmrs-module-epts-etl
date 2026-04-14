package org.openmrs.module.epts.etl.dbquickload.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.common.model.EtlStageRecordVO;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.epts.etl.dbquickload.processor.QuickLoadLimits;
import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.SyncJSONInfoMinimal;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DBQuickLoadSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> implements FilenameFilter {
	
	private File currJSONSourceFile;
	
	/*
	 * The current json info which is being processed
	 */
	private SyncJSONInfo currJSONInfo;
	
	private Engine<EtlDatabaseObject> engine;
	
	public DBQuickLoadSearchParams(Engine<EtlDatabaseObject> engine, QuickLoadLimits limits) {
		super(engine.getSrcConf(), limits);
		
		this.engine = engine;
	}
	
	public SyncJSONInfo getCurrJSONInfo() {
		return currJSONInfo;
	}
	
	public File getCurrJSONSourceFile() {
		return currJSONSourceFile;
	}
	
	public DBQuickLoadController getRelatedController() {
		return (DBQuickLoadController) engine.getController();
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord recordLimits, Connection srcConn,
	        Connection dstConn) throws DBException {
		return null;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().endsWith("json");
	}
	
	private File getNextJSONFileToLoad() {
		File[] files = getSyncDirectory().listFiles(this);
		
		if (files != null && files.length > 0) {
			return files[0];
		}
		
		return null;
	}
	
	@Override
	public List<EtlDatabaseObject> search(IntervalExtremeRecord intervalExtremeRecord, Connection srcConn,
	        Connection dstCOnn) throws DBException {
		this.currJSONSourceFile = getNextJSONFileToLoad();
		
		if (this.currJSONSourceFile == null)
			return null;
		
		getRelatedController().logInfo("Loading content on JSON File " + this.currJSONSourceFile.getAbsolutePath());
		
		try {
			String json = new String(Files.readAllBytes(Paths.get(currJSONSourceFile.getAbsolutePath())));
			
			this.currJSONInfo = SyncJSONInfo.loadFromJSON(json);
			this.currJSONInfo.setFileName(currJSONSourceFile.getAbsolutePath());
			
			for (EtlStageRecordVO rec : this.currJSONInfo.getSyncInfo()) {
				rec.setRecordOriginLocationCode(this.currJSONInfo.getOriginAppLocationCode());
			}
			
			return utilities.parseList(this.currJSONInfo.getSyncInfo(), EtlDatabaseObject.class);
			
		}
		catch (Exception e) {
			getRelatedController().logInfo("Error performing " + this.currJSONSourceFile.getAbsolutePath());
			
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
		
	}
	
	@Override
	public int countAllRecords(OperationController<EtlDatabaseObject> controller, Connection conn) throws DBException {
		LoadedRecordsSearchParams syncSearchParams = new LoadedRecordsSearchParams(engine, null,
		        getRelatedController().getAppOriginLocationCode());
		
		int processed = syncSearchParams.countAllRecords(controller, conn);
		
		int notProcessed = countNotProcessedRecords(controller, conn);
		
		return processed + notProcessed;
	}
	
	@Override
	public int countNotProcessedRecords(OperationController<EtlDatabaseObject> controller, Connection conn)
	        throws DBException {
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
		return this.getRelatedController().getSyncDirectory(getSrcConf());
	}
	
	@Override
	protected VOLoaderHelper getLoaderHealper() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public AbstractEtlSearchParams<EtlDatabaseObject> cloneMe() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String generateDestinationExclusionClause(Connection srcConn, Connection dstConn) throws DBException {
		return null;
	}
}
