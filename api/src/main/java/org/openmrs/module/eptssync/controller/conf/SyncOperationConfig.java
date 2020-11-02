package org.openmrs.module.eptssync.controller.conf;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.databasepreparation.controller.DatabasePreparationController;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.export.controller.SyncExportController;
import org.openmrs.module.eptssync.load.controller.SyncDataLoadController;
import org.openmrs.module.eptssync.pojogeneration.controller.PojoGenerationController;
import org.openmrs.module.eptssync.synchronization.controller.SyncController;
import org.openmrs.module.eptssync.transport.controller.SyncTransportController;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

public class SyncOperationConfig {
	public static final String SYNC_OPERATION_DATABASE_PREPARATION = "database_preparation";
	public static final String SYNC_OPERATION_POJO_GENERATION = "pojo_generation";
	public static final String SYNC_OPERATION_EXPORT = "export";
	public static final String SYNC_OPERATION_SYNCHRONIZATION = "synchronization";
	public static final String SYNC_OPERATION_LOAD = "load";
	public static final String SYNC_OPERATION_TRANSPORT = "transport";
	public static final String SYNC_OPERATION_CONSOLIDATION= "consolidation";

	private static final String[] SUPPORTED_OPERATIONS = {	SYNC_OPERATION_CONSOLIDATION, 
			SYNC_OPERATION_EXPORT, 
			SYNC_OPERATION_LOAD, 
			SYNC_OPERATION_SYNCHRONIZATION, 
			SYNC_OPERATION_TRANSPORT,
			SYNC_OPERATION_DATABASE_PREPARATION,
			SYNC_OPERATION_POJO_GENERATION};

	public CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static String PROCESSING_MODE_SEQUENCIAL="sequencial";
	public static String PROCESSING_MODE_PARALLEL="parallel";
	
	private static final String[] supportedProcessingModes = {PROCESSING_MODE_SEQUENCIAL, PROCESSING_MODE_PARALLEL};

	private String operationType;

	private int maxRecordPerProcessing;
	private int maxSupportedEngines;
	private int minRecordsPerEngine;
	
	private boolean doIntegrityCheckInTheEnd;
	private SyncOperationConfig child;
	private SyncOperationConfig parent;
	private SyncConfiguration relatedSyncConfig;
	private boolean disabled;
	
	private String processingMode;
	
	private List<String> sourceFolders;
	
	public SyncOperationConfig() {
	}
	
	
	public String getDesignation() {
		return this.getRelatedSyncConfig().getDesignation() + "_" + this.getOperationType();
	}
	
	public List<String> getSourceFolders() {
		//if (this.sourceFolders == null) throw new ForbiddenOperationException("There is no source folder defined");
		
		return sourceFolders;
	}
	
	public void setSourceFolders(List<String> sourceFolders) {
		this.sourceFolders = sourceFolders;
	}
	
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

	public SyncOperationConfig getParent() {
		return parent;
	}
	
	public void setParent(SyncOperationConfig parent) {
		this.parent = parent;
	}
	
	public SyncOperationConfig getChild() {
		return child;
	}
	
	public void setChild(SyncOperationConfig child) {
		this.child = child;
		
		if (this.child != null) {
			this.child.setParent(this);
		}
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
	
	public boolean isDatabasePreparationOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_DATABASE_PREPARATION);
	}
	
	public boolean isPojoGeneration() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_POJO_GENERATION);
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (!(obj instanceof SyncOperationConfig)) return false;
		
		return this.operationType.equalsIgnoreCase(((SyncOperationConfig)obj).operationType);
	}
	
	public OperationController generateRelatedController(ProcessController parent, Connection conn) {
		OperationController controller = null;
	
		if (isDatabasePreparationOperation()) {
			controller = new DatabasePreparationController(parent, this);
		}
		else			
		if (isPojoGeneration()) {
			controller = new PojoGenerationController(parent, this);
		}
		else	
		if (isExportOperation()) {
			controller = new SyncExportController(parent, this);
		}
		else
		if (isTransportOperation()) {
			controller = new SyncTransportController(parent, this);
		}
		else
		if (isLoadOperation()) {
			controller = new SyncDataLoadController(parent, this);
		}
		else
		if (isSynchronizationOperation()) {
			controller = new SyncController(parent, this);
		}
		else
		if (isConsolidationOperation()) {
			controller = new DatabaseIntegrityConsolidationController(parent, this);
		}
		else throw new ForbiddenOperationException("Operationtype not supported!");
	
		if (this.getChild() != null) {
			controller.setChild(this.getChild().generateRelatedController(controller.getProcessController(), conn));
			controller.getChild().setParent(controller);
		}
		
		return controller;
	}
	
	public boolean canBeRunInDestinationInstallation() {
		return this.isConsolidationOperation() || 
				this.isSynchronizationOperation() ||
					this.isLoadOperation() ||
						this.isDatabasePreparationOperation() ||
							this.isPojoGeneration();
	}
	
	public boolean canBeRunInSourceInstallation() {
		return this.isExportOperation() || 
				this.isTransportOperation() ||
					this.isDatabasePreparationOperation() ||
						this.isPojoGeneration();
	}
	
	@Override
	public String toString() {
		return getRelatedSyncConfig().getDesignation() + "_" + this.operationType;
	}
}
