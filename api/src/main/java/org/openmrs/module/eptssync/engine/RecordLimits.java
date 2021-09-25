package org.openmrs.module.eptssync.engine;

import java.io.File;

import org.openmrs.module.eptssync.utilities.ObjectMapperProvider;
import org.openmrs.module.eptssync.utilities.io.FileUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Indicate the min and max record id to be processed by certain SyncEngine
 * 
 * @author jpboane
 *
 */
public class RecordLimits {
	private long firstRecordId;
	private long lastRecordId;
	
	private String threadCode;
	private long threadMinRecord;
	private long threadMaxRecord;
	
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
	

	@Override
	public String toString() {
		return this.firstRecordId + " - " + this.lastRecordId;
	}
}
