package org.openmrs.module.epts.etl.engine;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;

/**
 * Indicate the min and max record id to be processed in a certain thread or thread iteration
 */
public class IntervalExtremeRecord {
	
	private long minRecordId;
	
	private long maxRecordId;
	
	public IntervalExtremeRecord() {
	}
	
	public IntervalExtremeRecord(long minRecordId, long maxRecordId) {
		
		if (minRecordId > maxRecordId)
			throw new ForbiddenOperationException("The minRecordId cannot be greater that the maxRecordId");
		
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
}
