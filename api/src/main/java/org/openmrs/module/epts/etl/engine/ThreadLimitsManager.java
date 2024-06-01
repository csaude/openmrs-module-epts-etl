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
import org.openmrs.module.epts.etl.monitor.EngineMonitor;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The manager of records limits for a specific {@link Engine}
 * 
 * @author jpboane
 */
public class ThreadLimitsManager {
	
	protected static CommonUtilities utilities = CommonUtilities.getInstance();
	
	protected String threadCode;
	
	private Engine engine;
	
	private boolean loadedFromFile;
	
	private String lastSavedOn;
	
	protected int qtyRecordsPerProcessing;
	
	private Limit currentLimits;
	
	private Limit maxLimits;
	
	private List<Limit> excludedIntervals;
	
	private ThreadLImitsManagerStatusType status;
	
	public ThreadLimitsManager() {
		this.status = ThreadLImitsManagerStatusType.NOT_INITIALIZED;
		
		this.setCurrentLimits(new Limit());
		this.setMaxLimits(new Limit());
	}
	
	public ThreadLimitsManager(long firstRecordId, long lastRecordId, int qtyRecordsPerProcessing) {
		this();
		
		this.maxLimits = new Limit(firstRecordId, lastRecordId);
		
		this.qtyRecordsPerProcessing = qtyRecordsPerProcessing;
		
		this.reset();
	}
	
	public ThreadLImitsManagerStatusType getStatus() {
		return status;
	}
	
	public List<Limit> getExcludedIntervals() {
		return excludedIntervals;
	}
	
	public void setExcludedIntervals(List<Limit> excludedIntervals) {
		this.excludedIntervals = excludedIntervals;
	}
	
	public Limit getMaxLimits() {
		return maxLimits;
	}
	
	public void setMaxLimits(Limit maxLimits) {
		this.maxLimits = maxLimits;
	}
	
	public Limit getCurrentLimits() {
		return currentLimits;
	}
	
	public void setCurrentLimits(Limit currentLimits) {
		this.currentLimits = currentLimits;
	}
	
	public ThreadLimitsManager(long firstRecordId, long lastRecordId, int qtyRecordsPerProcessing, Engine engine) {
		this(firstRecordId, lastRecordId, qtyRecordsPerProcessing);
		
		this.engine = engine;
		
		if (this.engine != null)
			this.threadCode = engine.getEngineId();
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
	
	public void setEngine(Engine engine) {
		threadCode = null;
		
		this.engine = engine;
		
		if (this.engine != null)
			this.threadCode = engine.getEngineId();
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
			for (Limit limit : getExcludedIntervals()) {
				if (currentFirstRecordId >= limit.getMinRecordId()) {
					//Try to go out of already processed intervals
					while (currentFirstRecordId <= limit.getMaxRecordId()) {
						currentFirstRecordId++;
					}
				}
			}
		}
		
		if (currentFirstRecordId > this.getThreadMaxRecord()) {
			this.setStatus(ThreadLImitsManagerStatusType.OUT_OF_LIMITS);
		} else {
			currentLastRecordId = currentFirstRecordId + this.getQtyRecordsPerProcessing() - 1;
			
			if (currentLastRecordId > this.getThreadMaxRecord()) {
				currentLastRecordId = this.getThreadMaxRecord();
			}
			
			if (hasExcludedIntervals()) {
				for (Limit limit : getExcludedIntervals()) {
					if (currentLastRecordId < limit.getMaxRecordId()) {
						//Try to go out of already processed intervals
						while (currentLastRecordId >= limit.getMinRecordId()) {
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
	
	public long getThreadMaxRecord() {
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
	
	public void save() {
		if (isOutOfLimits() || isNotInitialized())
			throw new ForbiddenOperationException("You cannot save out of limit/not initialized thread limits manager");
		
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
	
	public boolean hasSameEngineInfo(ThreadLimitsManager limits) {
		return this.equals(limits) && this.getThreadMaxRecord() == limits.getThreadMaxRecord()
		        && this.getThreadMinRecordId() == limits.getThreadMinRecordId();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ThreadLimitsManager) || obj == null)
			return false;
		
		ThreadLimitsManager cr = (ThreadLimitsManager) obj;
		
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
		return !isOutOfLimits() && this.getCurrentLastRecordId() < this.getThreadMaxRecord();
	}
	
	public synchronized void moveNext() {
		if (canGoNext()) {
			this.defineCurrentLimits(this.getCurrentFirstRecordId() + getQtyRecordsPerProcessing());
		} else
			throw new ForbiddenOperationException("You reached the max record. Curr Status: [" + this.getThreadMinRecordId()
			        + " - " + this.getThreadMaxRecord() + "] Curr [" + this.getCurrentFirstRecordId() + " - "
			        + this.getCurrentLastRecordId() + "]");
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
			ThreadLimitsManager limits = loadFromJSON(new String(Files.readAllBytes(file.toPath())));
			
			if (limits != null && limits.hasSameEngineInfo(engine.getLimits())) {
				
				copy(limits);
				
				this.loadedFromFile = true;
			}else {
				this.setExcludedIntervals(engine.getMonitor().getExcludedRecordsIntervals());
			}
			
			this.engine = engine;
			this.threadCode = engine.getEngineId();
		}
		catch (NoSuchFileException e) {}
		catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public void copy(ThreadLimitsManager copyFrom) {
		this.threadCode = copyFrom.threadCode;
		this.engine = copyFrom.engine;
		this.loadedFromFile = copyFrom.loadedFromFile;
		this.lastSavedOn = copyFrom.lastSavedOn;
		this.qtyRecordsPerProcessing = copyFrom.qtyRecordsPerProcessing;
		
		this.setCurrentLimits(
		    new Limit(copyFrom.getCurrentLimits().getMinRecordId(), copyFrom.getCurrentLimits().getMaxRecordId()));
		this.setMaxLimits(new Limit(copyFrom.getMaxLimits().getMinRecordId(), copyFrom.getMaxLimits().getMaxRecordId()));
	}
	
	private static ThreadLimitsManager loadFromJSON(String json) {
		return utilities.loadObjectFormJSON(ThreadLimitsManager.class, json);
	}
	
	@Override
	public String toString() {
		return getThreadCode() + " : Thread [" + this.getThreadMinRecordId() + " - " + this.getThreadMaxRecord() + "] Curr ["
		        + this.getCurrentFirstRecordId() + " - " + this.getCurrentLastRecordId() + "]";
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
	
	public static void removeAll(List<ThreadLimitsManager> generatedLimits) {
		if (generatedLimits != null) {
			
			for (ThreadLimitsManager limits : generatedLimits) {
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
	
	public static List<ThreadLimitsManager> getAllSavedLimitsOfOperation(EngineMonitor monitor) {
		
		try {
			String threadsFolder = monitor.getRelatedOperationController().generateOperationStatusFolder();
			
			threadsFolder += FileUtilities.getPathSeparator() + "threads";
			
			File[] files = new File(threadsFolder).listFiles(new LimitSearcher(monitor));
			
			List<ThreadLimitsManager> allLImitsOfEngine = null;
			
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
}

class LimitSearcher implements FilenameFilter {
	
	EngineMonitor monitor;
	
	public LimitSearcher(EngineMonitor monitor) {
		this.monitor = monitor;
	}
	
	@Override
	public boolean accept(File dir, String name) {
		return name.toLowerCase().startsWith(monitor.getEngineId());
	}
}
