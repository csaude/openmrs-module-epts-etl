package org.openmrs.module.epts.etl.engine.record_intervals_manager;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents the current intervals to be processed in an {@link ThreadRecordIntervalsManager}
 */
public class ThreadCurrentIntervals extends IntervalExtremeRecord {
	
	static final CommonUtilities utilities = CommonUtilities.getInstance();
	
	private List<IntervalExtremeRecord> internalIntervals;;
	
	private boolean initialized;
	
	private int qtyProcessors;
	
	private boolean skippedRecordProcessed;
	
	public ThreadCurrentIntervals() {
	}
	
	public ThreadCurrentIntervals(long minRecordId, long maxRecordId, int qtyProcessors) {
		super(minRecordId, maxRecordId);
		
		this.qtyProcessors = qtyProcessors;
		
		init();
	}
	
	public boolean isSkippedRecordProcessed() {
		return skippedRecordProcessed;
	}
	
	public void setSkippedRecordProcessed(boolean skippedRecordProcessed) {
		this.skippedRecordProcessed = skippedRecordProcessed;
	}
	
	public List<IntervalExtremeRecord> getInternalIntervals() {
		return internalIntervals;
	}
	
	public void setInternalIntervals(List<IntervalExtremeRecord> internalIntervals) {
		this.internalIntervals = internalIntervals;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	public int getQtyProcessors() {
		return qtyProcessors;
	}
	
	private void init() {
		long qtyAvaliablerecords = this.getMaxRecordId() - this.getMinRecordId() + 1;
		
		if (qtyAvaliablerecords < getQtyProcessors()) {
			this.qtyProcessors = (int) qtyAvaliablerecords;
		}
		
		long engineAlocatedRecs = qtyAvaliablerecords / this.getQtyProcessors();
		
		this.internalIntervals = new ArrayList<>(qtyProcessors);
		
		IntervalExtremeRecord currLimits = null;
		
		for (int i = 0; i < this.getQtyProcessors(); i++) {
			IntervalExtremeRecord limits;
			
			if (currLimits == null) {
				limits = new IntervalExtremeRecord(this.getMinRecordId(), this.getMinRecordId() + engineAlocatedRecs - 1);
				
				currLimits = limits;
				
			} else {
				// Last processor
				if (i == this.getQtyProcessors() - 1) {
					limits = new IntervalExtremeRecord(currLimits.getMaxRecordId() + 1, this.getMaxRecordId());
				} else {
					limits = new IntervalExtremeRecord(currLimits.getMaxRecordId() + 1,
					        currLimits.getMaxRecordId() + engineAlocatedRecs);
				}
				currLimits = limits;
			}
			
			this.getInternalIntervals().add(limits);
		}
	}
	
	@JsonIgnore
	public List<IntervalExtremeRecord> getAllNotProcessed() {
		List<IntervalExtremeRecord> notProcessed = new ArrayList<>();
		
		for (IntervalExtremeRecord i : this.getInternalIntervals()) {
			if (!i.isProcessed()) {
				notProcessed.add(i);
			}
		}
		
		return notProcessed;
	}
	
	@JsonIgnore
	public ThreadCurrentIntervals cloneMe() {
		ThreadCurrentIntervals cloned = new ThreadCurrentIntervals(this.getMinRecordId(), this.getMaxRecordId(),
		        getQtyProcessors());
		
		cloned.setInternalIntervals(new ArrayList<>());
		
		if (hasInternalIntervals()) {
			for (IntervalExtremeRecord er : this.getInternalIntervals()) {
				cloned.getInternalIntervals().add(er.cloneMe());
			}
		}
		
		cloned.setInternalIntervals(this.getInternalIntervals());
		
		return cloned;
	}
	
	@JsonIgnore
	private boolean hasInternalIntervals() {
		return utilities.arrayHasElement(getInternalIntervals());
	}
	
	public boolean isFullProcessed() {
		return utilities.arrayHasNoElement(getAllNotProcessed()) && isSkippedRecordProcessed();
	}
	
	@Override
	public void markAsProcessed() {
		for (IntervalExtremeRecord i : getInternalIntervals()) {
			i.markAsProcessed();
		}
	}
	
	public void markSkippedRecordsAsProcessed() {
		setSkippedRecordProcessed(true);
	}
}
