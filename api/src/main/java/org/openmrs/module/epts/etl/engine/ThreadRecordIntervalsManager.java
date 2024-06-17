package org.openmrs.module.epts.etl.engine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.EtlException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The manager of records limits for a specific {@link TaskProcessor}
 * 
 * @author jpboane
 */
public class ThreadRecordIntervalsManager<T extends EtlDatabaseObject> implements Comparable<ThreadRecordIntervalsManager<T>> {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected String threadCode;
	
	private Engine<T> engine;
	
	private boolean loadedFromFile;
	
	private String lastSavedOn;
	
	protected int qtyRecordsPerProcessing;
	
	private IntervalExtremeRecord currentLimits;
	
	private IntervalExtremeRecord maxLimits;
	
	private List<IntervalExtremeRecord> excludedIntervals;
	
	private ThreadLImitsManagerStatusType status;
	
	public ThreadRecordIntervalsManager() {
		this.status = ThreadLImitsManagerStatusType.NOT_INITIALIZED;
		
		this.setCurrentLimits(new IntervalExtremeRecord());
		this.setMaxLimits(new IntervalExtremeRecord());
	}
	
	public ThreadRecordIntervalsManager(long firstRecordId, long lastRecordId, int qtyRecordsPerProcessing) {
		this();
		
		this.maxLimits = new IntervalExtremeRecord(firstRecordId, lastRecordId);
		
		this.qtyRecordsPerProcessing = qtyRecordsPerProcessing;
		
		this.reset();
	}
	
	public ThreadRecordIntervalsManager(long firstRecordId, long lastRecordId, int qtyRecordsPerProcessing,
	    String threadCode, Engine<T> engine) {
		this(firstRecordId, lastRecordId, qtyRecordsPerProcessing);
		
		this.engine = engine;
		
		if (this.engine != null)
			this.threadCode = engine.getEngineId();
	}
	
	public ThreadLImitsManagerStatusType getStatus() {
		return status;
	}
	
	public List<IntervalExtremeRecord> getExcludedIntervals() {
		return excludedIntervals;
	}
	
	public void setExcludedIntervals(List<IntervalExtremeRecord> excludedIntervals) {
		this.excludedIntervals = excludedIntervals;
	}
	
	public IntervalExtremeRecord getMaxLimits() {
		return maxLimits;
	}
	
	public void setMaxLimits(IntervalExtremeRecord maxLimits) {
		this.maxLimits = maxLimits;
	}
	
	public IntervalExtremeRecord getCurrentLimits() {
		return currentLimits;
	}
	
	public void setCurrentLimits(IntervalExtremeRecord currentLimits) {
		this.currentLimits = currentLimits;
	}
	
	/**
	 * Put the currentLimits#minRecord to maxLimits#minRecordId - {@link #qtyRecordsPerProcessing}
	 */
	public void reset() {
		//Set To allow a secure #moveNext, put the threadMinRecord behind
		//this.getCurrentLimits().setMinRecordId(this.getMaxLimits().getMinRecordId() - this.getQtyRecordsPerProcessing());
		
		defineCurrentLimits(this.getMaxLimits().getMinRecordId() - this.getQtyRecordsPerProcessing());
		
		this.setStatus(ThreadLImitsManagerStatusType.BETWEEN_LIMITS);
	}
	
	public void setStatus(ThreadLImitsManagerStatusType status) {
		this.status = status;
	}
	
	public void setEngine(Engine<T> engine) {
		this.engine = engine;
	}
	
	public boolean isLoadedFromFile() {
		return loadedFromFile;
	}
	
	public boolean hasExcludedIntervals() {
		return utilities.arrayHasElement(getExcludedIntervals());
	}
	
	private void defineCurrentLimits(long currentFirstRecordId) {
		long currentLastRecordId = -1;
		
		if (hasExcludedIntervals()) {
			for (IntervalExtremeRecord intervalExtremeRecord : getExcludedIntervals()) {
				if (currentFirstRecordId >= intervalExtremeRecord.getMinRecordId()) {
					//Try to go out of already processed intervals
					while (currentFirstRecordId <= intervalExtremeRecord.getMaxRecordId()) {
						currentFirstRecordId++;
					}
				}
			}
		}
		
		if (currentFirstRecordId > this.getThreadMaxRecordId()) {
			this.setStatus(ThreadLImitsManagerStatusType.OUT_OF_LIMITS);
		} else {
			currentLastRecordId = currentFirstRecordId + this.getQtyRecordsPerProcessing() - 1;
			
			if (currentLastRecordId > this.getThreadMaxRecordId()) {
				currentLastRecordId = this.getThreadMaxRecordId();
			}
			
			if (hasExcludedIntervals()) {
				for (IntervalExtremeRecord intervalExtremeRecord : getExcludedIntervals()) {
					if (currentLastRecordId < intervalExtremeRecord.getMaxRecordId()) {
						//Try to go out of already processed intervals
						while (currentLastRecordId >= intervalExtremeRecord.getMinRecordId()) {
							currentLastRecordId--;
						}
					}
				}
			}
			
			if (currentLastRecordId < currentFirstRecordId) {
				this.setStatus(ThreadLImitsManagerStatusType.OUT_OF_LIMITS);
			} else {
				this.getCurrentLimits().setMaxRecordId(currentLastRecordId);
				this.getCurrentLimits().setMinRecordId(currentFirstRecordId);
			}
		}
	}
	
	public long getCurrentFirstRecordId() {
		return getCurrentLimits().getMinRecordId();
	}
	
	public long getCurrentLastRecordId() {
		return getCurrentLimits().getMaxRecordId();
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
	
	public long getThreadMinRecordId() {
		return getMaxLimits().getMinRecordId();
	}
	
	public long getThreadMaxRecordId() {
		return getMaxLimits().getMaxRecordId();
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
	
	public void save(Engine<T> monitor) {
		
		if (!hasThreadCode())
			throw new ForbiddenOperationException("You cannot save limits without threadCode");
		
		if (isOutOfLimits() || isNotInitialized()) {
			monitor.logWarn("You cannot save out of limit/not initialized thread limits manager");
		} else {
			String fileName = generateFilePath(monitor);
			
			if (new File(fileName).exists()) {
				FileUtilities.removeFile(fileName);
			}
			
			setLastSavedOn(TaskProcessor.utilities.formatDateToDDMMYYYY_HHMISS(TaskProcessor.utilities.getCurrentDate()));
			
			String desc = this.parseToJSON();
			
			FileUtilities.tryToCreateDirectoryStructureForFile(fileName);
			
			FileUtilities.write(fileName, desc);
			
			this.loadedFromFile = true;
		}
	}
	
	public String generateFilePath(Engine<T> monitor) {
		return generateFilePath(this.getThreadCode(), monitor);
	}
	
	public static <T extends EtlDatabaseObject> String generateFilePath(String threadCode, Engine<T> monitor) {
		String subFolder = monitor.getRelatedOperationController().generateOperationStatusFolder();
		
		subFolder += FileUtilities.getPathSeparator() + "threads";
		
		return subFolder + FileUtilities.getPathSeparator() + threadCode;
	}
	
	@JsonIgnore
	public String parseToJSON() {
		return utilities.parseToJSON(this);
	}
	
	public boolean hasSameEngineInfo(ThreadRecordIntervalsManager<T> limits) {
		return this.getMaxLimits().equals(limits.getMaxLimits());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ThreadRecordIntervalsManager) || obj == null)
			return false;
		
		ThreadRecordIntervalsManager<T> cr = (ThreadRecordIntervalsManager<T>) obj;
		
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
		return !isOutOfLimits() && this.getCurrentLastRecordId() < this.getThreadMaxRecordId();
	}
	
	public synchronized void moveNext() {
		if (canGoNext()) {
			this.defineCurrentLimits(this.getCurrentLastRecordId()+1);
		} else
			throw new ForbiddenOperationException("You reached the max record. Curr Status: [" + this.getThreadMinRecordId()
			        + " - " + this.getThreadMaxRecordId() + "] Curr [" + this.getCurrentFirstRecordId() + " - "
			        + this.getCurrentLastRecordId() + "]");
	}
	
	public boolean hasThreadCode() {
		return TaskProcessor.utilities.stringHasValue(this.getThreadCode());
	}
	
	/**
	 * Tries to load data for this engine from file. If there is no saved limits then will keeped
	 * the current limits info
	 * 
	 * @param file
	 * @param engine
	 */
	public static <T extends EtlDatabaseObject> ThreadRecordIntervalsManager<T> tryToLoadFromFile(String threadCode,
	        Engine<T> engine) {
		
		ThreadRecordIntervalsManager<T> limits = null;
		
		File file = new File(generateFilePath(threadCode, engine));
		
		try {
			limits = loadFromJSON(new String(Files.readAllBytes(file.toPath())));
			
			if (limits != null) {
				int qtyRecordsPerProcessing = engine.getQtyRecordsPerProcessing();
				
				limits.setQtyRecordsPerProcessing(qtyRecordsPerProcessing);
				
				limits.loadedFromFile = true;
				limits.setExcludedIntervals(engine.getExcludedRecordsIntervals());
				
				limits.setEngine(engine);
			}
			
		}
		catch (NoSuchFileException e) {}
		catch (IOException e) {
			throw new RuntimeException();
		}
		
		return limits;
	}
	
	public void copy(ThreadRecordIntervalsManager<T> copyFrom) {
		this.threadCode = copyFrom.threadCode;
		this.engine = copyFrom.engine;
		this.loadedFromFile = copyFrom.loadedFromFile;
		this.lastSavedOn = copyFrom.lastSavedOn;
		this.qtyRecordsPerProcessing = copyFrom.qtyRecordsPerProcessing;
		this.excludedIntervals = copyFrom.excludedIntervals;
		this.status = copyFrom.status;
		
		this.setCurrentLimits(new IntervalExtremeRecord(copyFrom.getCurrentLimits().getMinRecordId(),
		        copyFrom.getCurrentLimits().getMaxRecordId()));
		this.setMaxLimits(
		    new IntervalExtremeRecord(copyFrom.getMaxLimits().getMinRecordId(), copyFrom.getMaxLimits().getMaxRecordId()));
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends EtlDatabaseObject> ThreadRecordIntervalsManager<T> loadFromJSON(String json) {
		return utilities.loadObjectFormJSON(ThreadRecordIntervalsManager.class, json);
	}
	
	@Override
	public String toString() {
		return getThreadCode() + " : Thread [" + this.getMaxLimits() + "] Curr [" + this.getCurrentLimits() + "]";
	}
	
	public boolean isInitialized() {
		return this.getStatus().isInitialized();
	}
	
	public boolean isNotInitialized() {
		return this.getStatus().isNotInitialized();
	}
	
	public boolean isBetweenLimits() {
		return this.getStatus().isBetweenLimits();
	}
	
	public boolean isOutOfLimits() {
		return this.getStatus().isOutOfLimits();
	}
	
	public void refreshCode() {
		threadCode = null;
		
		if (this.engine != null)
			this.threadCode = engine.getEngineId();
	}
	
	public static <T extends EtlDatabaseObject> void removeAll(List<ThreadRecordIntervalsManager<T>> generatedLimits,
	        Engine<T> monitor) {
		if (generatedLimits != null) {
			
			for (ThreadRecordIntervalsManager<T> limits : generatedLimits) {
				limits.remove(monitor);
			}
		}
	}
	
	public void remove(Engine<T> monitor) {
		String fileName = generateFilePath(monitor);
		
		if (new File(fileName).exists()) {
			FileUtilities.removeFile(fileName);
		}
	}
	
	public static <T extends EtlDatabaseObject> List<ThreadRecordIntervalsManager<T>> getAllSavedLimitsOfOperation(
	        Engine<T> monitor) {
		
		try {
			String threadsFolder = monitor.getRelatedOperationController().generateOperationStatusFolder();
			
			threadsFolder += FileUtilities.getPathSeparator() + "threads";
			
			File[] files = new File(threadsFolder).listFiles(new LimitSearcher(monitor));
			
			List<ThreadRecordIntervalsManager<T>> allLImitsOfEngine = null;
			
			if (files != null) {
				allLImitsOfEngine = new ArrayList<>();
				
				for (File file : files) {
					allLImitsOfEngine.add(loadFromJSON(FileUtilities.realAllFileAsString(file)));
				}
				
			}
			
			return allLImitsOfEngine;
		}
		catch (IOException e) {
			throw new EtlException(e);
		}
		
	}
	
	@Override
	public int compareTo(ThreadRecordIntervalsManager<T> other) {
		return this.getThreadCode().compareTo(other.getThreadCode());
	}
	
}

class LimitSearcher implements FilenameFilter {
	
	Engine<? extends EtlDatabaseObject> monitor;
	
	public LimitSearcher(Engine<? extends EtlDatabaseObject> monitor) {
		this.monitor = monitor;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().startsWith(monitor.getEngineId());
	}
}
