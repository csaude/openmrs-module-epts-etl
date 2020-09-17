package org.openmrs.module.eptssync.engine;

/**
 * Indicate the min and max record id to be processed by certain SyncEngine
 * 
 * @author jpboane
 *
 */
public class RecordLimits {
	private int firstRecordId;
	private int lastRecordId;
	
	public RecordLimits(int firstRecordId, int lastRecordId) {
		this.firstRecordId = firstRecordId;
		this.lastRecordId = lastRecordId;
	}
	
	public int getFirstRecordId() {
		return firstRecordId;
	}
	
	public int getLastRecordId() {
		return lastRecordId;
	}
}
