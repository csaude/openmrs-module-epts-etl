package org.openmrs.module.epts.etl.load.model;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

import javax.ws.rs.ForbiddenException;

import org.openmrs.module.epts.etl.engine.AbstractEtlSearchParams;
import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.IntervalExtremeRecord;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.load.controller.DataLoadController;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.model.SearchClauses;
import org.openmrs.module.epts.etl.model.SyncJSONInfo;
import org.openmrs.module.epts.etl.model.base.VOLoaderHelper;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class LoadSyncDataSearchParams extends AbstractEtlSearchParams<EtlDatabaseObject> implements FilenameFilter {
	
	private File currJSONSourceFile;
	
	/*
	 * The current json info which is being processed
	 */
	private SyncJSONInfo currJSONInfo;
	
	private String firstFileName;
	
	private String lastFileName;
	
	private String fileNamePathern;
	
	public LoadSyncDataSearchParams(Engine<EtlDatabaseObject> engine, ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine, limits);
		
		if (limits != null) {
			this.firstFileName = getSrcTableConf().getTableName() + "_"
			        + utilities.garantirXCaracterOnNumber(limits.getCurrentFirstRecordId(), 10) + ".json";
			this.lastFileName = getSrcTableConf().getTableName() + "_"
			        + utilities.garantirXCaracterOnNumber(limits.getCurrentLastRecordId(), 10) + ".json";
		}
	}
	
	@Override
	public DataLoadController getRelatedController() {
		return (DataLoadController) super.getRelatedController();
	}
	
	private File getNextJSONFileToLoad() {
		File[] files = getSyncDirectory().listFiles(this);
		
		if (files != null && files.length > 0) {
			return files[0];
		}
		
		return null;
	}
	
	@Override
	public List<EtlDatabaseObject> search(IntervalExtremeRecord intervalExtremeRecord,
	        Connection srcConn, Connection dstCOnn) throws DBException {
		
		this.currJSONSourceFile = getNextJSONFileToLoad();
		
		if (this.currJSONSourceFile == null)
			return null;
		
		getRelatedController().logInfo("Loading content on JSON File " + this.currJSONSourceFile.getAbsolutePath());
		
		try {
			String json = new String(Files.readAllBytes(Paths.get(currJSONSourceFile.getAbsolutePath())));
			
			this.currJSONInfo = SyncJSONInfo.loadFromJSON(json);
			this.currJSONInfo.setFileName(currJSONSourceFile.getAbsolutePath());
			
			return utilities.parseList(this.currJSONInfo.getSyncInfo(), EtlDatabaseObject.class);
			
		}
		catch (Exception e) {
			getRelatedController().logInfo("Error performing " + this.currJSONSourceFile.getAbsolutePath());
			
			e.printStackTrace();
			
			throw new RuntimeException(e);
		}
	}
	
	public SyncJSONInfo getCurrJSONInfo() {
		return currJSONInfo;
	}
	
	public File getCurrJSONSourceFile() {
		return currJSONSourceFile;
	}
	
	public void setFileNamePathern(String fileNamePathern) {
		this.fileNamePathern = fileNamePathern;
	}
	
	public String getFileNamePathern() {
		return fileNamePathern;
	}
	
	@Override
	public SearchClauses<EtlDatabaseObject> generateSearchClauses(IntervalExtremeRecord recordLimits, Connection srcConn,
	        Connection dstConn) throws DBException {
		// TODO Auto-generated method stub
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
		
		throw new ForbiddenException();
		
		/*
		DataBaseMergeFromJSONSearchParams syncSearchParams = new DataBaseMergeFromJSONSearchParams(getRelatedEngine(), null,
		        getRelatedController().getAppOriginLocationCode());
		
		int processed = syncSearchParams.countAllRecords(conn);
		int notProcessed = countNotProcessedRecords(conn);
		
		return processed + notProcessed;
		*/
	}
	
	@Override
	public int countNotProcessedRecords(Connection conn) throws DBException {
		try {
			File[] files = getSyncDirectory().listFiles(new FilenameFilter() {
				
				@Override
				public boolean accept(File dir, String name) {
					boolean isJSON = name.toLowerCase().endsWith("json");
					boolean isMinimal = name.toLowerCase().contains("minimal");
					
					return isJSON && isMinimal;
				}
			});
			
			int notYetProcessed = 0;
			
			if (files == null)
				return 0;
			
			for (File file : files) {
				try {
					String json = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
					
					notYetProcessed += SyncJSONInfo.loadFromJSON(json).getQtyRecords();
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
		return this.getRelatedController().getSyncDirectory(getSrcTableConf());
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
		// TODO Auto-generated method stub
		return null;
	}
}
