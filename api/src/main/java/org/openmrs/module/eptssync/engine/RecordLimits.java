package org.openmrs.module.eptssync.engine;

/**
 * Indicate the min and max record id to be processed by certain SyncEngine
 * 
 * @author jpboane
 *
 */
public class RecordLimits {
	private long firstRecordId;
	private long lastRecordId;
	
	public RecordLimits(long firstRecordId, long lastRecordId) {
		this.firstRecordId = firstRecordId;
		this.lastRecordId = lastRecordId;
	}
	
	public void setFirstRecordId(long firstRecordId) {
		this.firstRecordId = firstRecordId;
	}
	
	public void setLastRecordId(long lastRecordId) {
		this.lastRecordId = lastRecordId;
	}
	
	public long getFirstRecordId() {
		return firstRecordId;
	}
	
	public long getLastRecordId() {
		return lastRecordId;
	}
}
