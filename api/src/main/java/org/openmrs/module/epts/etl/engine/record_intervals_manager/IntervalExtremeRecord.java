package org.openmrs.module.epts.etl.engine.record_intervals_manager;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;

/**
 * Indicate the min and max dstRecord id to be processed in a certain thread or thread iteration
 */
public class IntervalExtremeRecord {
	
	private boolean processed;
	
	private long minRecordId;
	
	private long maxRecordId;
	
	public IntervalExtremeRecord() {
	}
	
	public IntervalExtremeRecord(long minRecordId, long maxRecordId) {
		
		if (minRecordId > maxRecordId)
			throw new ForbiddenOperationException("The minRecordId cannot be greater that the maxRecordId [minRecordId: "
			        + minRecordId + ", maxRecordId: " + maxRecordId);
		
		this.minRecordId = minRecordId;
		this.maxRecordId = maxRecordId;
	}
	
	public long getMinRecordId() {
		return minRecordId;
	}
	
	public void setMinRecordId(long minRecordId) {
		this.minRecordId = minRecordId;
	}
	
	public long getMaxRecordId() {
		return maxRecordId;
	}
	
	public void setMaxRecordId(long maxRecordId) {
		this.maxRecordId = maxRecordId;
	}
	
	public void reset(long minRecordId, long maxRecordId) {
		setMinRecordId(minRecordId);
		setMaxRecordId(maxRecordId);
	}
	
	@Override
	public String toString() {
		return getMinRecordId() + " - " + getMaxRecordId();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IntervalExtremeRecord))
			return false;
		
		IntervalExtremeRecord intervalExtremeRecord = (IntervalExtremeRecord) obj;
		
		return this.getMinRecordId() == intervalExtremeRecord.getMinRecordId()
		        && this.getMaxRecordId() == intervalExtremeRecord.getMaxRecordId();
	}
	
	public boolean isProcessed() {
		return processed;
	}
	
	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
	
	public void markAsProcessed() {
		
		if (getMinRecordId() == 876) {
			System.out.println("Stop");
		}
		
		this.processed = true;
	}
	
	public IntervalExtremeRecord cloneMe() {
		IntervalExtremeRecord i = new IntervalExtremeRecord(minRecordId, maxRecordId);
		
		i.setProcessed(processed);
		
		return i;
	}
}
