package org.openmrs.module.epts.etl.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Indicate the min and max record id to be processed by certain SyncEngine
 * 
 * @author jpboane
 */
public class RecordLimits {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected long currentFirstRecordId;
	
	protected long currentLastRecordId;
	
	protected String threadCode;
	
	protected long threadMinRecord;
	
	protected long threadMaxRecord;
	
	private Engine engine;
	
	private boolean loadedFromFile;
	
	private String lastSavedOn;
	
	protected int qtyRecordsPerProcessing;
	
	public RecordLimits() {
	}
	
	public RecordLimits(long firstRecordId, long lastRecordId, int qtyRecordsPerProcessing) {
		this.threadMinRecord = firstRecordId;
		this.threadMaxRecord = lastRecordId;
		this.qtyRecordsPerProcessing = qtyRecordsPerProcessing;
		
		this.reset();
	}
	
	public RecordLimits(long firstRecordId, long lastRecordId, int qtyRecordsPerProcessing, Engine engine) {
		this(firstRecordId, lastRecordId, qtyRecordsPerProcessing);
		
		this.engine = engine;
		
		if (this.engine != null)
			this.threadCode = engine.getEngineId();
	}
	
	public void reset() {
		//Set To allow a secure #moveNext, put the threadMinRecord behind
		this.setCurrentLimits(this.threadMinRecord - this.qtyRecordsPerProcessing);
	}
	
	public void setEngine(Engine engine) {
		threadCode = null;
		
		this.engine = engine;
		
		if (this.engine != null)
			this.threadCode = engine.getEngineId();
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
	
	public void setQtyRecordsPerProcessing(int qtyRecordsPerProcessing) {
		this.qtyRecordsPerProcessing = qtyRecordsPerProcessing;
	}
	
	public void save() {
		String fileName = generateFilePath();
		
		if (new File(fileName).exists()) {
			FileUtilities.removeFile(fileName);
		}
		
		setLastSavedOn(Engine.utilities.formatDateToDDMMYYYY_HHMISS(Engine.utilities.getCurrentDate()));
		
		String desc = this.parseToJSON();
		
		FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
		
		FileUtilities.write(fileName, desc);
		
		this.loadedFromFile = true;
	}
	
	public String generateFilePath() {
		String subFolder = this.engine.getRelatedOperationController().generateOperationStatusFolder();
		
		subFolder += FileUtilities.getPathSeparator() + "threads";
		
		return subFolder + FileUtilities.getPathSeparator() + this.threadCode;
	}
	
	@JsonIgnore
	public String parseToJSON() {
		return utilities.parseToJSON(this);
	}
	
	public boolean hasSameEngineInfo(RecordLimits limits) {
		return this.equals(limits) && this.getThreadMaxRecord() == limits.getThreadMaxRecord()
		        && this.getThreadMinRecord() == limits.getThreadMinRecord();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RecordLimits) || obj == null)
			return false;
		
		RecordLimits cr = (RecordLimits) obj;
		
		return this.getThreadCode().equals(cr.getThreadCode());
	}
	
	/**
	 * Verifica se os limites ainda permitem pesquisa. Os limites deixam de permitir pesquisa quando
	 * o valor de {@link #getCurrentFirstRecordId()} for inferior ou igual ao valor de
	 * {@link #getCurrentLastRecordId()}
	 * 
	 * @return
	 */
	public boolean canGoNext() {
		return this.getCurrentLastRecordId() < this.getThreadMaxRecord();
	}
	
	public synchronized void moveNext(int qtyRecords) {
		if (canGoNext()) {
			this.setCurrentLimits(this.getCurrentFirstRecordId() + qtyRecords);
		} else
			throw new ForbiddenOperationException(
			        "You reached the max record. Curr Status: [" + this.threadMinRecord + " - " + this.threadMaxRecord
			                + "] Curr [" + this.currentFirstRecordId + " - " + this.currentLastRecordId + "]");
	}
	
	public boolean hasThreadCode() {
		return Engine.utilities.stringHasValue(this.getThreadCode());
	}
	
	/**
	 * Tries to load data for this engine from file. If there is no saved limits then will keeped
	 * the current limits info
	 * 
	 * @param file
	 * @param engine
	 */
	public void tryToLoadFromFile(File file, Engine engine) {
		try {
			RecordLimits limits = loadFromJSON(new String(Files.readAllBytes(file.toPath())));
			
			if (limits != null && limits.hasSameEngineInfo(engine.getLimits())) {
				
				copy(limits);
				
				this.loadedFromFile = true;
			}
			
			this.engine = engine;
			this.threadCode = engine.getEngineId();
		}
		catch (NoSuchFileException e) {}
		catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public void copy(RecordLimits copyFrom) {
		this.currentFirstRecordId = copyFrom.currentFirstRecordId;
		this.currentLastRecordId = copyFrom.currentLastRecordId;
		this.threadCode = copyFrom.threadCode;
		this.threadMinRecord = copyFrom.threadMinRecord;
		this.threadMaxRecord = copyFrom.threadMaxRecord;
		this.engine = copyFrom.engine;
		this.loadedFromFile = copyFrom.loadedFromFile;
		this.lastSavedOn = copyFrom.lastSavedOn;
		this.qtyRecordsPerProcessing = copyFrom.qtyRecordsPerProcessing;
	}
	
	private static RecordLimits loadFromJSON(String json) {
		return utilities.loadObjectFormJSON(RecordLimits.class, json);
	}
	
	@Override
	public String toString() {
		return getThreadCode() + " : Thread [" + this.threadMinRecord + " - " + this.threadMaxRecord + "] Curr ["
		        + this.currentFirstRecordId + " - " + this.currentLastRecordId + "]";
	}
	
	public boolean isDefined() {
		return getCurrentFirstRecordId() > 0 && getCurrentLastRecordId() > 0;
	}
	
	public void refreshCode() {
		threadCode = null;
		
		if (this.engine != null)
			this.threadCode = engine.getEngineId();
	}
	
	public static void removeAll(List<RecordLimits> generatedLimits) {
		if (generatedLimits != null) {
			
			for (RecordLimits limits : generatedLimits) {
				limits.remove();
			}
		}
	}
	
	public void remove() {
		String fileName = generateFilePath();
		
		if (new File(fileName).exists()) {
			FileUtilities.removeFile(fileName);
		}
	}
	
}
