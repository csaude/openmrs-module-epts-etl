package org.openmrs.module.eptssync.changedrecordsdetector.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.openmrs.module.eptssync.changedrecordsdetector.engine.ChangedRecordsDetectorEngine;
import org.openmrs.module.eptssync.controller.DestinationOperationController;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.engine.RecordLimits;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class ChangedRecordSearchLimits extends RecordLimits {

	private String threadCode;
	private long threadMinRecord;
	private long threadMaxRecord;
	
	private ChangedRecordsDetectorEngine engine;
	private boolean loadedFromFile;
	
	public ChangedRecordSearchLimits() {
		super(0, 0);
	}
	
	public ChangedRecordSearchLimits(long firstRecordId, long lastRecordId, ChangedRecordsDetectorEngine engine) {
		super(firstRecordId, lastRecordId);
		
		this.engine = engine;
		this.threadCode = engine.getEngineId();
	}
	
	
	public boolean isLoadedFromFile() {
		return loadedFromFile;
	}


	@JsonIgnore
	public ChangedRecordsDetectorEngine getEngine() {
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

	public void save() {
		String fileName =  generateFilePath();
		
		if (new File(fileName).exists()) {
			FileUtilities.removeFile(fileName);
		}	
		
		String desc = this.parseToJSON();
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		FileUtilities.write(fileName, desc);
		
		this.loadedFromFile = true;
	}
	
	public static ChangedRecordSearchLimits loadFromFile(File file, ChangedRecordsDetectorEngine engine) {
		try {
			ChangedRecordSearchLimits limits = ChangedRecordSearchLimits.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		
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
	
	public static ChangedRecordSearchLimits loadFromJSON (String json) {
		try {
			ChangedRecordSearchLimits limits = new ObjectMapperProvider().getContext(ChangedRecordSearchLimits.class).readValue(json, ChangedRecordSearchLimits.class);
		
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
		
		if (this.engine.getRelatedOperationController().getConfiguration().isSourceInstallationType()) {
			subFolder = "source" + FileUtilities.getPathSeparator() + this.engine.getRelatedOperationController().getOperationType() + FileUtilities.getPathSeparator() + this.engine.getRelatedOperationController().getConfiguration().getOriginAppLocationCode(); 
		}
		else
		if (this.engine.getRelatedOperationController().getConfiguration().isDestinationInstallationType()) {
			String appOrigin =  this instanceof DestinationOperationController ?  FileUtilities.getPathSeparator() + ((DestinationOperationController)this).getAppOriginLocationCode() : "";
					
			subFolder = "destination" + FileUtilities.getPathSeparator() + this.engine.getRelatedOperationController().getOperationType() + appOrigin; 
		}
		else
		if (this.engine.getRelatedOperationController().getConfiguration().isNeutralInstallationType()) {
			subFolder = "neutral" + FileUtilities.getPathSeparator() + this.engine.getRelatedOperationController().getOperationType() + FileUtilities.getPathSeparator() + this.engine.getRelatedOperationController().getConfiguration().getOriginAppLocationCode(); 
		}
		
		subFolder += FileUtilities.getPathSeparator() + "threads";
		
		return this.engine.getRelatedOperationController().getConfiguration().getSyncRootDirectory() + FileUtilities.getPathSeparator() +  "process_status" + FileUtilities.getPathSeparator()  + subFolder + FileUtilities.getPathSeparator() + this.threadCode;
	
	}
	
	@JsonIgnore
	public String parseToJSON(){
		try {
			return new ObjectMapperProvider().getContext(RecordLimits.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasSameEngineInfo(ChangedRecordSearchLimits limits) {
		return this.equals(limits)  && this.getThreadMaxRecord() == limits.getThreadMaxRecord() && this.getThreadMinRecord() == limits.getThreadMinRecord();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChangedRecordsDetectorEngine) || obj == null) return false;
		
		ChangedRecordSearchLimits cr = (ChangedRecordSearchLimits)obj;
		
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
