package org.openmrs.module.eptssync.controller.conf;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.controller.AbstractSyncController;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.export.controller.SyncExportController;
import org.openmrs.module.eptssync.load.controller.SyncDataLoadController;
import org.openmrs.module.eptssync.synchronization.controller.SynchronizationController;
import org.openmrs.module.eptssync.transport.controller.SyncTransportController;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

public class SyncOperationConfig {
	
	public static final String SYNC_OPERATION_EXPORT = "export";
	public static final String SYNC_OPERATION_SYNCHRONIZATION = "synchronization";
	public static final String SYNC_OPERATION_LOAD = "load";
	public static final String SYNC_OPERATION_TRANSPORT = "transport";
	public static final String SYNC_OPERATION_CONSOLIDATION= "consolidation";
	
	private String operationType;
	private int defaultQtyRecordsPerSelect;
	private int defaultQtyRecordsPerEngine;
	private boolean doIntegrityCheckInTheEnd;
	private SyncOperationConfig relatedOperationToBeRunInTheEnd;
	private SyncConfig relatedSyncConfig;
	private boolean disabled;
	
	private static final String[] SUPPORTED_OPERATIONS = {	SYNC_OPERATION_CONSOLIDATION, 
															SYNC_OPERATION_EXPORT, 
															SYNC_OPERATION_LOAD, 
															SYNC_OPERATION_SYNCHRONIZATION, 
															SYNC_OPERATION_TRANSPORT};
	
	public CommonUtilities utilities = CommonUtilities.getInstance();
	
	public SyncOperationConfig() {
	}
	
	public SyncOperationConfig getRelatedOperationToBeRunInTheEnd() {
		return relatedOperationToBeRunInTheEnd;
	}
	
	public void setRelatedOperationToBeRunInTheEnd(SyncOperationConfig relatedOperationToBeRunInTheEnd) {
		this.relatedOperationToBeRunInTheEnd = relatedOperationToBeRunInTheEnd;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return disabled;
	}
	
	public SyncConfig getRelatedSyncConfig() {
		return relatedSyncConfig;
	}
	
	public void setRelatedSyncConfig(SyncConfig relatedSyncConfig) {
		this.relatedSyncConfig = relatedSyncConfig;
	}
	
	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		if (!utilities.isStringIn(operationType.toLowerCase(), SUPPORTED_OPERATIONS)) throw new ForbiddenOperationException("Operation '" + operationType + "' nor supported!");
		
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
	
	
	public boolean isExportOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_EXPORT);
	}
	
	public boolean isSynchronizationOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_SYNCHRONIZATION);
	}
	
	public boolean isLoadOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_LOAD);
	}
	
	public boolean isTransportOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_TRANSPORT);
	}
	
	public boolean isConsolidationOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_CONSOLIDATION);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (!(obj instanceof SyncOperationConfig)) return false;
		
		return this.operationType.equalsIgnoreCase(((SyncOperationConfig)obj).operationType);
	}
	
	public List<AbstractSyncController> generateRelatedController() {
		List<AbstractSyncController> controllers = new ArrayList<AbstractSyncController>();
		
		if (isSynchronizationOperation()) {
			controllers.add(new SynchronizationController(getRelatedSyncConfig()));
		}
		else
		if (isTransportOperation()) {
			controllers.add(new SyncTransportController(getRelatedSyncConfig()));
		}
		else
		if (isConsolidationOperation()) {
			controllers.add(new DatabaseIntegrityConsolidationController(getRelatedSyncConfig()));
		}
		else
		if (isExportOperation()) {
			controllers.add(new SyncExportController(getRelatedSyncConfig()));
		}
		else
		if (isLoadOperation()) {
			String[] allAvaliableOrigins = SyncDataLoadController.discoveryAllAvaliableOrigins(getRelatedSyncConfig());
			
			for (String appOriginCode : allAvaliableOrigins) {
				controllers.add(new SyncDataLoadController(getRelatedSyncConfig(), appOriginCode));
			}
		}
		else throw new ForbiddenOperationException("Operationtype not supported!");
		
		if (this.relatedOperationToBeRunInTheEnd != null) {
			controllers.get(0).setRelatedOperationToBeRunInTheEnd(relatedOperationToBeRunInTheEnd.generateRelatedController().get(0));
		}
		
		return controllers;
	}
	
	public boolean canBeRunInDestinationInstallation() {
		return this.isConsolidationOperation() || 
				this.isSynchronizationOperation() ||
					this.isLoadOperation();
	}
	
	public boolean canBeRunInSourceInstallation() {
		return this.isExportOperation() || 
				this.isTransportOperation();
	}
}
