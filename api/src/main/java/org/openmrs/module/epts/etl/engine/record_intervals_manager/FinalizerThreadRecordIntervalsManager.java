package org.openmrs.module.epts.etl.engine.record_intervals_manager;

import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

/**
 * This represents a {@link ThreadRecordIntervalsManager} responsible for final check o etl process
 */
public class FinalizerThreadRecordIntervalsManager<T extends EtlDatabaseObject> extends ThreadRecordIntervalsManager<T> {
	
	ThreadRecordIntervalsManager<T> parent;
	
	public FinalizerThreadRecordIntervalsManager() {
	}
	
	public FinalizerThreadRecordIntervalsManager(ThreadRecordIntervalsManager<T> parent, long firstRecordId,
	    long lastRecordId, int qtyRecordsPerProcessing, int maxAllowedProcessors) {
		
		super(firstRecordId, lastRecordId, qtyRecordsPerProcessing, 1);
		
		this.parent = parent;
	}
	
	public void setParent(ThreadRecordIntervalsManager<T> parent) {
		this.parent = parent;
	}
	
	@Override
	public void save() {
		this.parent.save();
	}
}
