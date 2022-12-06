package org.openmrs.module.eptssync.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * Indicate the min and max record id to be processed by certain SyncEngine
 * 
 * @author jpboane
 *
 */
public class RecordLimits {
	protected long currentFirstRecordId;
	protected long currentLastRecordId;
	
	protected String threadCode;
	protected long threadMinRecord;
	protected long threadMaxRecord;
	
	private Engine engine;
	private boolean loadedFromFile;
	
	private String lastSavedOn;
	private int qtyRecordsPerProcessing;
	
	public RecordLimits() {
	}
	
	public RecordLimits(long firstRecordId, long lastRecordId, int qtyRecordsPerProcessing, Engine engine) {
		this.threadMinRecord = firstRecordId;
		this.threadMaxRecord = lastRecordId;
	
		this.qtyRecordsPerProcessing = qtyRecordsPerProcessing;
	
		this.engine = engine;
		
		if (this.engine != null) this.threadCode = engine.getEngineId();
		
		this.reset();
	}
	
	public void reset() {
		this.setCurrentLimits(this.threadMinRecord);
	}

	public void setEngine(Engine engine) {
		this.engine = engine;
		
		if (this.engine != null) this.threadCode = engine.getEngineId();
	}
	
	public boolean isLoadedFromFile() {
		return loadedFromFile;
	}
	
	private void setCurrentLimits(long currentFirstRecordId) {
		this.currentFirstRecordId = currentFirstRecordId;
		this.currentLastRecordId = this.currentFirstRecordId + this.qtyRecordsPerProcessing - 1;
		
		if (this.currentLastRecordId > this.threadMaxRecord) {
			this.currentLastRecordId = this.threadMaxRecord;
		}
	}
	
	public long getCurrentFirstRecordId() {
		return currentFirstRecordId;
	}

	public long getCurrentLastRecordId() {
		return currentLastRecordId;
	}
	
	public int getQtyRecordsPerProcessing() {
		return qtyRecordsPerProcessing;
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
	
	public static RecordLimits loadFromJSON (String json) {
		try {
			RecordLimits limits = new ObjectMapperProvider().getContext(RecordLimits.class).readValue(json, RecordLimits.class);
		
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
		String subFolder = this.engine.getRelatedOperationController().generateOperationStatusFolder();
		
		subFolder += FileUtilities.getPathSeparator() + "threads";
		
		return subFolder + FileUtilities.getPathSeparator() + this.threadCode;
	}
	
	@JsonIgnore
	public String parseToJSON(){
		try {
			return new ObjectMapperProvider().getContext(RecordLimits.class).writeValueAsString(this);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasSameEngineInfo(RecordLimits limits) {
		return this.equals(limits)  && this.getThreadMaxRecord() == limits.getThreadMaxRecord() && this.getThreadMinRecord() == limits.getThreadMinRecord();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RecordLimits) || obj == null) return false;
		
		RecordLimits cr = (RecordLimits)obj;
		
		return this.getThreadCode().equals(cr.getThreadCode());
	}

	/**
	 * Verifica se os limites ainda permitem pesquisa.
	 * Os limites deixam de permitir pesquisa quando o valor de {@link #getCurrentFirstRecordId()} for inferior ou igual ao valor de {@link #getCurrentLastRecordId()}
	 * 
	 * @return
	 */
	public boolean canGoNext() {
		boolean canGo = this.getCurrentLastRecordId() < this.getThreadMaxRecord();
		
		if (!canGo) {
			System.out.println("Sys");
		}
		
		return canGo;
	}
	
	public synchronized void moveNext(int qtyRecords) {
		if (canGoNext()) {
			this.setCurrentLimits(this.getCurrentFirstRecordId() + qtyRecords);
		}
		else throw new ForbiddenOperationException("You reached the max record. Curr Status: ["  + this.threadMinRecord + " - " + this.threadMaxRecord + "] Curr [" + this.currentFirstRecordId + " - " + this.currentLastRecordId + "]");
	}

	public boolean hasThreadCode() {
		return Engine.utilities.stringHasValue(this.getThreadCode());
	}
	
	public static RecordLimits loadFromFile(File file, Engine engine) {
		try {
			RecordLimits limits = RecordLimits.loadFromJSON(new String(Files.readAllBytes(file.toPath())));
		
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
	@Override
	public String toString() {
		return getThreadCode() + " : Thread ["  + this.threadMinRecord + " - " + this.threadMaxRecord + "] Curr [" + this.currentFirstRecordId + " - " + this.currentLastRecordId + "]";
	}

}
