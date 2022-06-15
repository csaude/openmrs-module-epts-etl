package org.openmrs.module.eptssync.dbquickexport.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.openmrs.module.eptssync.changedrecordsdetector.engine.ChangedRecordsDetectorEngine;
import org.openmrs.module.eptssync.controller.DestinationOperationController;
import org.openmrs.module.eptssync.controller.conf.SyncConfiguration;
import org.openmrs.module.eptssync.dbquickexport.engine.SyncDBQuickExportEngine;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class DBQuickExportSearchLimits extends RecordLimits {
	
	private SyncDBQuickExportEngine engine;
	private boolean loadedFromFile;
	
	private String lastSavedOn;
	
	public DBQuickExportSearchLimits() {
		super(0, 0);
	}
	
	public DBQuickExportSearchLimits(long firstRecordId, long lastRecordId, SyncDBQuickExportEngine engine) {
		super(firstRecordId, lastRecordId);
		
		this.engine = engine;
		this.threadCode = engine.getEngineId();
	}
	
	
	public boolean isLoadedFromFile() {
		return loadedFromFile;
	}


	@JsonIgnore
	public SyncDBQuickExportEngine getEngine() {
		return engine;
	}
	
	public String getThreadCode() {
		return threadCode;
	}
	
	public void setThreadCode(String threadCode) {
		this.threadCode = threadCode;
	}
	
	public long getThreadMinRecord() {
		return threadMinRecord;
	}

	public void setThreadMinRecord(long threadMinRecord) {
		this.threadMinRecord = threadMinRecord;
	}

	public long getThreadMaxRecord() {
		return threadMaxRecord;
	}

	public void setThreadMaxRecord(long threadMaxRecord) {
		this.threadMaxRecord = threadMaxRecord;
	}

	public String getLastSavedOn() {
		return lastSavedOn;
	}

	public void setLastSavedOn(String lastSavedOn) {
		this.lastSavedOn = lastSavedOn;
	}

	public void save() {
		String fileName =  generateFilePath();
		
		if (new File(fileName).exists()) {
			FileUtilities.removeFile(fileName);
		}	
		
		setLastSavedOn(Engine.utilities.formatDateToDDMMYYYY_HHMISS(Engine.utilities.getCurrentDate()));
		
		String desc = this.parseToJSON();
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		FileUtilities.write(fileName, desc);
		
		this.loadedFromFile = true;
	}
	
	public static DBQuickExportSearchLimits loadFromFile(File file, SyncDBQuickExportEngine engine) {
		try {
			DBQuickExportSearchLimits limits = DBQuickExportSearchLimits.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		
			if (limits != null  && limits.hasSameEngineInfo(engine.getLimits())) {
				limits.loadedFromFile = true;
				limits.engine = engine;
				limits.threadCode = engine.getEngineId();
				return limits;
			}
			else return null;
		}
		catch (NoSuchFileException e) {
			return null;
		}
		catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static DBQuickExportSearchLimits loadFromJSON (String json) {
		try {
			DBQuickExportSearchLimits limits = new ObjectMapperProvider().getContext(DBQuickExportSearchLimits.class).readValue(json, DBQuickExportSearchLimits.class);
		
			return limits;
		} catch (JsonParseException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			
			throw new RuntimeException(e);
		} 
	}	

	
	public String generateFilePath() {
		String subFolder = "";
		
		SyncConfiguration config = this.engine.getRelatedOperationController().getConfiguration();
		
		if (config.isSourceSyncProcess() || config.isDBReSyncProcess() || config.isDBQuickExportProcess()) {
			subFolder = "source" + FileUtilities.getPathSeparator() + this.engine.getRelatedOperationController().getOperationType() + FileUtilities.getPathSeparator() +config.getOriginAppLocationCode(); 
		}
		else
		if (config.isDestinationSyncProcess()) {
			String appOrigin =  this instanceof DestinationOperationController ?  FileUtilities.getPathSeparator() + ((DestinationOperationController)this).getAppOriginLocationCode() : "";
					
			subFolder = "destination" + FileUtilities.getPathSeparator() + this.engine.getRelatedOperationController().getOperationType() + appOrigin; 
		}
		
		subFolder += FileUtilities.getPathSeparator() + "threads";
		
		return config.getSyncRootDirectory() + FileUtilities.getPathSeparator() +  "process_status" + FileUtilities.getPathSeparator()  + subFolder + FileUtilities.getPathSeparator() + this.threadCode;
	
	}
	
	@JsonIgnore
	public String parseToJSON(){
		try {
			return new ObjectMapperProvider().getContext(RecordLimits.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasSameEngineInfo(DBQuickExportSearchLimits limits) {
		return this.equals(limits)  && this.getThreadMaxRecord() == limits.getThreadMaxRecord() && this.getThreadMinRecord() == limits.getThreadMinRecord();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChangedRecordsDetectorEngine) || obj == null) return false;
		
		DBQuickExportSearchLimits cr = (DBQuickExportSearchLimits)obj;
		
		return this.getThreadCode().equals(cr.getThreadCode());
	}

	/**
	 * Verifica se os limites ainda permitem pesquisa.
	 * Os limites deixam de permitir pesquisa quando o valor de {@link #getFirstRecordId()} for inferior ou igual ao valor de {@link #getLastRecordId()}
	 * 
	 * @return
	 */
	public boolean canGoNext() {
		return this.getFirstRecordId() <= this.getLastRecordId();
	}
	
	
	public void moveNext(int qtyRecords) {
		this.setFirstRecordId(this.getFirstRecordId() + qtyRecords);
	}

	public boolean hasThreadCode() {
		return Engine.utilities.stringHasValue(this.getThreadCode());
	}
	
}