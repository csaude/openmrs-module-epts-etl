package org.openmrs.module.eptssync.controller.conf;

import org.openmrs.module.eptssync.utilities.CommonUtilities;

public class SyncOperationConfig {
	
	public static final String SYNC_OPERATION_EXPORT = "EXPORT";
	public static final String SYNC_OPERATION_SYNCHRONIZATION = "SYNCHRONIZATION";
	public static final String SYNC_OPERATION_LOAD = "LOAD";
	public static final String SYNC_OPERATION_TRANSPORT = "TRANSPORT";
	public static final String SYNC_OPERATION_CONSOLIDATION= "CONSOLIDATION";
	
	private String operationType;
	private int defaultQtyRecordsPerSelect;
	private int defaultQtyRecordsPerEngine;
	private boolean doIntegrityCheckInTheEnd;
	
	private static final String[] SUPPORTED_OPERATIONS = {	SYNC_OPERATION_CONSOLIDATION, 
															SYNC_OPERATION_EXPORT, 
															SYNC_OPERATION_LOAD, 
															SYNC_OPERATION_SYNCHRONIZATION, 
															SYNC_OPERATION_TRANSPORT};
	
	public CommonUtilities utilities = CommonUtilities.getInstance();
	
	public SyncOperationConfig() {
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		if (utilities.isStringIn(operationType.toUpperCase(), SUPPORTED_OPERATIONS))
		
		this.operationType = operationType;
	}

	public int getDefaultQtyRecordsPerSelect() {
		return defaultQtyRecordsPerSelect;
	}

	public void setDefaultQtyRecordsPerSelect(int defaultQtyRecordsPerSelect) {
		this.defaultQtyRecordsPerSelect = defaultQtyRecordsPerSelect;
	}

	public int getDefaultQtyRecordsPerEngine() {
		return defaultQtyRecordsPerEngine;
	}

	public void setDefaultQtyRecordsPerEngine(int defaultQtyRecordsPerEngine) {
		this.defaultQtyRecordsPerEngine = defaultQtyRecordsPerEngine;
	}

	public void setDoIntegrityCheckInTheEnd(boolean doIntegrityCheckInTheEnd) {
		this.doIntegrityCheckInTheEnd = doIntegrityCheckInTheEnd;
	}
	
	public boolean isDoIntegrityCheckInTheEnd() {
		return doIntegrityCheckInTheEnd;
	}
	
	public static SyncOperationConfig fastCreate(String operationType) {
		SyncOperationConfig op = new SyncOperationConfig();
		op.setOperationType(operationType);
	
		return op;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (!(obj instanceof SyncOperationConfig)) return false;
		
		return this.operationType.equalsIgnoreCase(((SyncOperationConfig)obj).operationType);
	}
}
