package org.openmrs.module.epts.etl.engine.record_intervals_manager;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;
import org.openmrs.module.epts.etl.utilities.parseToCSV;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The manager of records intervals for any {@link Engine}. An instance of
 * {@link ThreadRecordIntervalsManager} define the extreme records as stated on
 * {@link IntervalExtremeRecord}. For each processing iteration, there is current processing
 * interval defined by {@link ThreadCurrentIntervals} and this allow easy moving to the next
 * interval
 * 
 * @author jpboane
 */
public class ThreadRecordIntervalsManager<T extends EtlDatabaseObject> extends IntervalExtremeRecord implements Comparable<ThreadRecordIntervalsManager<T>> {
	
	protected static parseToCSV utilities = parseToCSV.getInstance();
	
	protected String threadCode;
	
	private Engine<T> engine;
	
	private boolean loadedFromFile;
	
	private String lastSavedOn;
	
	protected int qtyRecordsPerProcessing;
	
	protected int maxSupportedProcessors;
	
	private ThreadCurrentIntervals currentLimits;
	
	private List<IntervalExtremeRecord> excludedIntervals;
	
	private ThreadIntervalsManagerStatusType status;
	
	private FinalizerThreadRecordIntervalsManager<T> finalCheckIntervalsManager;
	
	public ThreadRecordIntervalsManager() {
		this.status = ThreadIntervalsManagerStatusType.NOT_INITIALIZED;
		
		this.setCurrentLimits(new ThreadCurrentIntervals());
	}
	
	public ThreadRecordIntervalsManager(long firstRecordId, long lastRecordId, int qtyRecordsPerProcessing,
	    int maxAllowedProcessors) {
		super(firstRecordId, lastRecordId);
		
		this.setCurrentLimits(new ThreadCurrentIntervals());
		
		this.qtyRecordsPerProcessing = qtyRecordsPerProcessing;
		this.maxSupportedProcessors = maxAllowedProcessors;
		
		this.reset();
	}
	
	public int getMaxSupportedProcessors() {
		return maxSupportedProcessors;
	}
	
	public void setMaxSupportedProcessors(int maxSupportedProcessors) {
		this.maxSupportedProcessors = maxSupportedProcessors;
	}
	
	@JsonIgnore
	public Engine<T> getEngine() {
		return engine;
	}
	
	public FinalizerThreadRecordIntervalsManager<T> getFinalCheckIntervalsManager() {
		return finalCheckIntervalsManager;
	}
	
	public void setFinalCheckIntervalsManager(FinalizerThreadRecordIntervalsManager<T> finalCheckIntervalsManager) {
		this.finalCheckIntervalsManager = finalCheckIntervalsManager;
	}
	
	public void setCurrentLimits(ThreadCurrentIntervals currentLimits) {
		this.currentLimits = currentLimits;
	}
	
	public ThreadRecordIntervalsManager(Engine<T> engine) {
		this(engine.getMinRecordId(), engine.getMaxRecordId(), engine.getMaxRecordsPerProcessing(),
		        engine.getMaxSupportedProcessors());
		
		this.engine = engine;
		
		this.threadCode = engine.getEngineId();
	}
	
	public ThreadIntervalsManagerStatusType getStatus() {
		return status;
	}
	
	public List<IntervalExtremeRecord> getExcludedIntervals() {
		return excludedIntervals;
	}
	
	public void setExcludedIntervals(List<IntervalExtremeRecord> excludedIntervals) {
		this.excludedIntervals = excludedIntervals;
	}
	
	public ThreadCurrentIntervals getCurrentLimits() {
		return currentLimits;
	}
	
	/**
	 * Put the currentLimits#minRecord to maxLimits#minRecordId
	 */
	public void reset() {
		defineCurrentLimits(this.getMinRecordId());
		
		this.setStatus(ThreadIntervalsManagerStatusType.BETWEEN_LIMITS);
	}
	
	public void setStatus(ThreadIntervalsManagerStatusType status) {
		this.status = status;
	}
	
	public void setEngine(Engine<T> engine) {
		this.engine = engine;
	}
	
	@JsonIgnore
	public boolean isLoadedFromFile() {
		return loadedFromFile;
	}
	
	@JsonIgnore
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
		
		if (currentFirstRecordId > this.getMaxRecordId()) {
			this.setStatus(ThreadIntervalsManagerStatusType.OUT_OF_LIMITS);
		} else {
			currentLastRecordId = currentFirstRecordId + this.getQtyRecordsPerProcessing() - 1;
			
			if (currentLastRecordId > this.getMaxRecordId()) {
				currentLastRecordId = this.getMaxRecordId();
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
				this.setStatus(ThreadIntervalsManagerStatusType.OUT_OF_LIMITS);
			} else {
				
				this.setCurrentLimits(
				    new ThreadCurrentIntervals(currentFirstRecordId, currentLastRecordId, this.maxSupportedProcessors));
			}
		}
	}
	
	@JsonIgnore
	public long getCurrentFirstRecordId() {
		return getCurrentLimits().getMinRecordId();
	}
	
	@JsonIgnore
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
	
	public String getLastSavedOn() {
		return lastSavedOn;
	}
	
	public void setLastSavedOn(String lastSavedOn) {
		this.lastSavedOn = lastSavedOn;
	}
	
	public void setQtyRecordsPerProcessing(int qtyRecordsPerProcessing) {
		this.qtyRecordsPerProcessing = qtyRecordsPerProcessing;
	}
	
	public synchronized void save() {
		
		if (this.engine == null)
			throw new ForbiddenOperationException("You cannot save limits without engine");
		
		if (!hasThreadCode())
			throw new ForbiddenOperationException("You cannot save limits without threadCode");
		
		if (isOutOfLimits() || isNotInitialized()) {
			engine.logWarn("You cannot save out of limit/not initialized thread limits manager");
		} else {
			String fileName = generateFilePath(engine);
			
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
		return !isOutOfLimits() && this.getCurrentLastRecordId() < this.getMaxRecordId();
	}
	
	public synchronized void moveNext() {
		if (canGoNext()) {
			this.defineCurrentLimits(this.getCurrentLastRecordId() + 1);
		} else
			throw new ForbiddenOperationException(
			        "You reached the max record. Curr Status: [" + this.getMinRecordId() + " - " + this.getMaxRecordId()
			                + "] Curr [" + this.getCurrentFirstRecordId() + " - " + this.getCurrentLastRecordId() + "]");
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
				int qtyRecordsPerProcessing = engine.getMaxRecordsPerProcessing();
				
				limits.setQtyRecordsPerProcessing(qtyRecordsPerProcessing);
				
				limits.loadedFromFile = true;
				limits.setExcludedIntervals(engine.getExcludedRecordsIntervals());
				
				limits.setEngine(engine);
				
				limits.setMaxSupportedProcessors(engine.getMaxSupportedProcessors());
				limits.setQtyRecordsPerProcessing(engine.getMaxRecordsPerProcessing());
				
				if (limits.hasFinalCheckIntervalsManager()) {
					limits.getFinalCheckIntervalsManager().setEngine(engine);
					limits.getFinalCheckIntervalsManager().setParent(limits);
					
					limits.getFinalCheckIntervalsManager().setMaxSupportedProcessors(engine.getMaxSupportedProcessors());
					limits.getFinalCheckIntervalsManager().setQtyRecordsPerProcessing(engine.getMaxRecordsPerProcessing());
				}
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
		this.currentLimits = copyFrom.getCurrentLimits().cloneMe();
		this.maxSupportedProcessors = copyFrom.maxSupportedProcessors;
		this.finalCheckIntervalsManager = copyFrom.finalCheckIntervalsManager;
		
		this.setMinRecordId(copyFrom.getMinRecordId());
		this.setMaxRecordId(copyFrom.getMaxRecordId());
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends EtlDatabaseObject> ThreadRecordIntervalsManager<T> loadFromJSON(String json) {
		return utilities.loadObjectFormJSON(ThreadRecordIntervalsManager.class, json);
	}
	
	@Override
	public String toString() {
		return getThreadCode() + " : Thread [" + this.getMinRecordId() + " - " + this.getMaxRecordId() + "] Curr ["
		        + this.getCurrentLimits() + "]";
	}
	
	@JsonIgnore
	public boolean isInitialized() {
		return this.getStatus().isInitialized();
	}
	
	@JsonIgnore
	public boolean isNotInitialized() {
		return this.getStatus().isNotInitialized();
	}
	
	@JsonIgnore
	public boolean isBetweenLimits() {
		return this.getStatus().isBetweenLimits();
	}
	
	@JsonIgnore
	public boolean isOutOfLimits() {
		return this.getStatus().isOutOfLimits();
	}
	
	public boolean hasFinalCheckIntervalsManager() {
		return getFinalCheckIntervalsManager() != null;
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
			throw new EtlExceptionImpl(e);
		}
		
	}
	
	@Override
	public int compareTo(ThreadRecordIntervalsManager<T> other) {
		return this.getThreadCode().compareTo(other.getThreadCode());
	}
	
	public void initializeFinalCheckIntervalManager() {
		this.finalCheckIntervalsManager = new FinalizerThreadRecordIntervalsManager<>(this, this.getMinRecordId(),
		        this.getMaxRecordId(), this.getQtyRecordsPerProcessing(), 1);
	}
	
	public static void main(String[] args) {
		long min = 52764646;
		long max = 53097646;
		
		ThreadRecordIntervalsManager<EtlDatabaseObject> tm = new ThreadRecordIntervalsManager<>(min, max, 2000, 8);
		 
		System.out.println(parseToCSV.getInstance().parseToJSON(tm));
		
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
