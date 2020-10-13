package org.openmrs.module.eptssync.controller.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.export.controller.SyncExportController;
import org.openmrs.module.eptssync.load.controller.SyncDataLoadController;
import org.openmrs.module.eptssync.synchronization.controller.SyncController;
import org.openmrs.module.eptssync.transport.controller.SyncTransportController;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

public class SyncOperationConfig {
	
	public static final String SYNC_OPERATION_EXPORT = "export";
	public static final String SYNC_OPERATION_SYNCHRONIZATION = "synchronization";
	public static final String SYNC_OPERATION_LOAD = "load";
	public static final String SYNC_OPERATION_TRANSPORT = "transport";
	public static final String SYNC_OPERATION_CONSOLIDATION= "consolidation";
	
	private String operationType;

	private int maxRecordPerProcessing;
	private int maxSupportedEngines;
	private int minRecordsPerEngine;
	
	private boolean doIntegrityCheckInTheEnd;
	private SyncOperationConfig child;
	private SyncConfiguration relatedSyncConfig;
	private boolean disabled;
	
	private String processingMode;
	
	private static final String[] SUPPORTED_OPERATIONS = {	SYNC_OPERATION_CONSOLIDATION, 
															SYNC_OPERATION_EXPORT, 
															SYNC_OPERATION_LOAD, 
															SYNC_OPERATION_SYNCHRONIZATION, 
															SYNC_OPERATION_TRANSPORT};
	
	public CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static String PROCESSING_MODE_SEQUENCIAL="sequencial";
	public static String PROCESSING_MODE_PARALLEL="parallel";
	
	private static final String[] supportedProcessingModes = {PROCESSING_MODE_SEQUENCIAL, PROCESSING_MODE_PARALLEL};
	
	public boolean isParallelModeProcessing() {
		return this.processingMode.equalsIgnoreCase(SyncConfiguration.PROCESSING_MODE_PARALLEL);
	}
	
	public boolean isSequencialModeProcessing() {
		return this.processingMode.equalsIgnoreCase(SyncConfiguration.PROCESSING_MODE_SEQUENCIAL);
	}
	
	public String getProcessingMode() {
		return processingMode;
	}
	
	public void setProcessingMode(String processingMode) {
		
		if (!utilities.isStringIn(processingMode, supportedProcessingModes)) 
			throw new ForbiddenOperationException("The processing mode '" + processingMode + "' is not supported. Supported modes are: " + supportedProcessingModes);
		
		this.processingMode = processingMode;
	}
	
	
	public SyncOperationConfig() {
	}

	public SyncOperationConfig getChild() {
		return child;
	}
	
	public void setChild(SyncOperationConfig child) {
		this.child = child;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean isDisabled() {
		return disabled;
	}
	
	public SyncConfiguration getRelatedSyncConfig() {
		return relatedSyncConfig;
	}
	
	public void setRelatedSyncConfig(SyncConfiguration relatedSyncConfig) {
		this.relatedSyncConfig = relatedSyncConfig;
	}
	
	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		if (!utilities.isStringIn(operationType.toLowerCase(), SUPPORTED_OPERATIONS)) throw new ForbiddenOperationException("Operation '" + operationType + "' nor supported!");
		
		this.operationType = operationType;
	}


	public int getMaxRecordPerProcessing() {
		return maxRecordPerProcessing;
	}

	public void setMaxRecordPerProcessing(int maxRecordPerProcessing) {
		this.maxRecordPerProcessing = maxRecordPerProcessing;
	}

	public int getMaxSupportedEngines() {
		return maxSupportedEngines;
	}

	public void setMaxSupportedEngines(int maxSupportedEngines) {
		this.maxSupportedEngines = maxSupportedEngines;
	}

	public int getMinRecordsPerEngine() {
		return minRecordsPerEngine;
	}

	public void setMinRecordsPerEngine(int minRecordsPerEngine) {
		this.minRecordsPerEngine = minRecordsPerEngine;
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
	
	public List<OperationController> generateRelatedController(ProcessController processController, Connection conn) {
		List<OperationController> controllers = new ArrayList<OperationController>();
		
		if (isSynchronizationOperation()) {
			controllers.add(new SyncController(processController, this));
		}
		else
		if (isTransportOperation()) {
			controllers.add(new SyncTransportController(processController, this));
		}
		else
		if (isConsolidationOperation()) {
			controllers.add(new DatabaseIntegrityConsolidationController(processController, this));
		}
		else
		if (isExportOperation()) {
			controllers.add(new SyncExportController(processController, this));
		}
		else
		if (isLoadOperation()) {
			String[] allAvaliableOrigins = SyncDataLoadController.discoveryAllAvaliableOrigins(getRelatedSyncConfig());
			
			for (String appOriginCode : allAvaliableOrigins) {
				controllers.add(new SyncDataLoadController(processController, this, appOriginCode));
			}
		}
		else throw new ForbiddenOperationException("Operationtype not supported!");
		
		if (this.child != null) {
			controllers.get(0).setChild(child.generateRelatedController(processController, conn).get(0));
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
