package org.openmrs.module.eptssync.engine;

/**
 * Indicate the min and max record id to be processed by certain SyncEngine
 * 
 * @author jpboane
 *
 */
public class RecordLimits {
	protected long firstRecordId;
	protected long lastRecordId;
	
	protected String threadCode;
	protected long threadMinRecord;
	protected long threadMaxRecord;
	
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

	@Override
	public String toString() {
		return this.firstRecordId + " - " + this.lastRecordId;
	}
}
