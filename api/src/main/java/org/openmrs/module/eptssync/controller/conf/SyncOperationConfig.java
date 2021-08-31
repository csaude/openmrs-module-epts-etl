package org.openmrs.module.eptssync.controller.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.controller.DestinationOperationController;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.databasepreparation.controller.DatabasePreparationController;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.export.controller.SyncExportController;
import org.openmrs.module.eptssync.inconsistenceresolver.controller.InconsistenceSolverController;
import org.openmrs.module.eptssync.load.controller.SyncDataLoadController;
import org.openmrs.module.eptssync.pojogeneration.controller.PojoGenerationController;
import org.openmrs.module.eptssync.synchronization.controller.SyncController;
import org.openmrs.module.eptssync.transport.controller.SyncTransportController;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncOperationConfig {
	public static final String SYNC_OPERATION_DATABASE_PREPARATION = "database_preparation";
	public static final String SYNC_OPERATION_POJO_GENERATION = "pojo_generation";
	public static final String SYNC_OPERATION_INCONSISTENCY_SOLVER= "inconsistency_solver";
	public static final String SYNC_OPERATION_EXPORT = "export";
	public static final String SYNC_OPERATION_SYNCHRONIZATION = "synchronization";
	public static final String SYNC_OPERATION_LOAD = "load";
	public static final String SYNC_OPERATION_TRANSPORT = "transport";
	public static final String SYNC_OPERATION_CONSOLIDATION= "consolidation";
	public static final String SYNC_OPERATION_CHANGES_DETECTOR = "changes_detector";
	
	public static final String[] SUPPORTED_OPERATIONS = {	SYNC_OPERATION_CONSOLIDATION, 
			SYNC_OPERATION_EXPORT, 
			SYNC_OPERATION_LOAD, 
			SYNC_OPERATION_SYNCHRONIZATION, 
			SYNC_OPERATION_TRANSPORT,
			SYNC_OPERATION_DATABASE_PREPARATION,
			SYNC_OPERATION_POJO_GENERATION,
			SYNC_OPERATION_INCONSISTENCY_SOLVER};

	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
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
	
	private List<OperationController> relatedControllers;
	
	public SyncOperationConfig() {
	}
	
	@JsonIgnore
	public boolean isSourceFoldersRequired() {
		return isLoadOperation();
	}
	
	@JsonIgnore
	public String getDesignation() {
		return this.getRelatedSyncConfig().getDesignation() + "_" + this.getOperationType();
	}
	
	public List<String> getSourceFolders() {
		return sourceFolders;
	}
	
	public void setSourceFoldersAsString(String foldersStr) {
		this.sourceFolders = new ArrayList<String>();
		
		String[] folders = utilities.stringHasValue(foldersStr) ? foldersStr.split(",") : null;
		
		if (folders != null) {
			for (String f : folders) {
				this.sourceFolders.add(f);
			}
		}
	}
	
	@JsonIgnore
	public List<OperationController> getRelatedControllers() {
		return relatedControllers;
	}
	
	public OperationController getRelatedController(String appOriginCode) {
		if (relatedControllers == null) return null;
		
		if (appOriginCode == null || !(this.relatedControllers.get(0) instanceof DestinationOperationController)) {
			OperationController activeController = this.relatedControllers.get(0);
			
			return activeController;
		}
		
		for (OperationController controller : this.relatedControllers) {
			if ( ((DestinationOperationController)controller).getAppOriginLocationCode().equalsIgnoreCase(appOriginCode)) {
				return controller;
			}
		}
		
		return null;
	}
	
	@JsonIgnore
	public String getSourceFoldersAsString() {
		String sourceFoldersAsString = "";
		
		if (utilities.arrayHasElement(this.getSourceFolders())) {
			for (int i = 0; i < this.getSourceFolders().size() - 1; i++) {
				sourceFoldersAsString += this.getSourceFolders().get(i) + ",";
			}
			
			sourceFoldersAsString += this.getSourceFolders().get(this.getSourceFolders().size() - 1);
		}
		
		return sourceFoldersAsString;
	}
	
	public void setSourceFolders(List<String> sourceFolders) {
		this.sourceFolders = sourceFolders;
	}
	
	@JsonIgnore
	public boolean isParallelModeProcessing() {
		return this.processingMode.equalsIgnoreCase(SyncConfiguration.PROCESSING_MODE_PARALLEL);
	}
	
	@JsonIgnore
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

	@JsonIgnore
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
	
	@JsonIgnore
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
	
	@JsonIgnore
	public boolean isExportOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_EXPORT);
	}
	
	@JsonIgnore
	public boolean isSynchronizationOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_SYNCHRONIZATION);
	}
	
	@JsonIgnore
	public boolean isLoadOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_LOAD);
	}
	
	@JsonIgnore
	public boolean isTransportOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_TRANSPORT);
	}
	
	@JsonIgnore
	public boolean isConsolidationOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_CONSOLIDATION);
	}
	
	@JsonIgnore
	public boolean isDatabasePreparationOperation() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_DATABASE_PREPARATION);
	}
	
	@JsonIgnore
	public boolean isPojoGeneration() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_POJO_GENERATION);
	}
	
	@JsonIgnore
	public boolean isInconsistenceSolver() {
		return this.operationType.equalsIgnoreCase(SyncOperationConfig.SYNC_OPERATION_INCONSISTENCY_SOLVER);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (!(obj instanceof SyncOperationConfig)) return false;
		
		return this.operationType.equalsIgnoreCase(((SyncOperationConfig)obj).operationType);
	}
	
	public List<OperationController> generateRelatedController(ProcessController parent, String appOriginCode_, Connection conn) {
		this.relatedControllers = new ArrayList<OperationController>();
		
		if (getSourceFolders() == null) {
			this.relatedControllers.add(generateSigle(parent, appOriginCode_, conn));
		}
		else
		for (String appOriginCode : getSourceFolders()) {
			this.relatedControllers.add(generateSigle(parent, appOriginCode, conn));
		}
		
		if (this.getChild() != null) {
			for (OperationController controller : this.relatedControllers){
				String appOrigin = controller instanceof DestinationOperationController ? ((DestinationOperationController)controller).getAppOriginLocationCode() : null;
				
				controller.setChildren(this.getChild().generateRelatedController(controller.getProcessController(), appOrigin, conn));
				
				for (OperationController child : controller.getChildren()) {
					child.setParent(controller);
				}
			}
		}
		
		return this.relatedControllers;
	}
	
	private OperationController generateSigle(ProcessController parent, String appOriginCode, Connection conn) {
		if (isDatabasePreparationOperation()) {
			return new DatabasePreparationController(parent, this);
		}
		else			
		if ( isPojoGeneration()) {
			return new PojoGenerationController(parent, this);
		}
		else	
		if (isExportOperation()) {
			return new SyncExportController(parent, this);
		}
		else
		if (isTransportOperation()) {
			return new SyncTransportController(parent, this);
		}
		else
		if (isInconsistenceSolver()) {
			return new InconsistenceSolverController(parent, this);
		}
		else
		if (isLoadOperation()) {
			return new SyncDataLoadController(parent, this, appOriginCode);
		}
		else
		if (isSynchronizationOperation()) {
			return new SyncController(parent, this, appOriginCode);
		}
		else
		if (isConsolidationOperation()) {
			return new DatabaseIntegrityConsolidationController(parent, this, appOriginCode);
		}
			
		else throw new ForbiddenOperationException("Operationtype [" + this.operationType + "]not supported!");
		
	}
	
	public void validate () {
		String errorMsg = "";
		int errNum = 0;
		
		if (this.getRelatedSyncConfig().isDestinationInstallationType()) {
			if (!this.canBeRunInDestinationInstallation()) errorMsg += ++errNum + ". This operation ["+ this.getOperationType() + "] Cannot be configured in destination installation\n";
		
			if (this.isLoadOperation() && (this.getSourceFolders() == null || this.getSourceFolders().size() == 0))  errorMsg += ++errNum + ". There is no source folder defined";
		}
		else {
			if (!this.canBeRunInSourceInstallation()) errorMsg += ++errNum + ". This operation ["+ this.getOperationType() + "] Cannot be configured in source installation\n";
		}
		
		if (utilities.stringHasValue(errorMsg)) {
			errorMsg = "There are errors on config operation configuration " + this.getOperationType() +  "[File:  " + this.getRelatedSyncConfig().getRelatedConfFile().getAbsolutePath() + "]\n" + errorMsg;
			throw new ForbiddenOperationException(errorMsg);
		}
		else
		if (this.getChild() != null){
			this.getChild().validate();
		}
	}
	
	@JsonIgnore
	public boolean canBeRunInSourceInstallation() {
		return utilities.existOnArray(getSupportedOperationsInSourceInstallation(), this.operationType);
	}
	
	public static List<String> getSupportedOperationsInSourceInstallation() {
		String[] supported = {SyncOperationConfig.SYNC_OPERATION_EXPORT,
							  SyncOperationConfig.SYNC_OPERATION_TRANSPORT,
							  SyncOperationConfig.SYNC_OPERATION_INCONSISTENCY_SOLVER,
							  SyncOperationConfig.SYNC_OPERATION_DATABASE_PREPARATION,
							  SyncOperationConfig.SYNC_OPERATION_POJO_GENERATION,
							  SyncOperationConfig.SYNC_OPERATION_CHANGES_DETECTOR};
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDestinationInstallation() {
		return utilities.existOnArray(getSupportedOperationsInDestinationInstallation(), this.operationType);
	}
	
	
	public static List<String> getSupportedOperationsInDestinationInstallation() {
		String[] supported = {SyncOperationConfig.SYNC_OPERATION_CONSOLIDATION,
							  SyncOperationConfig.SYNC_OPERATION_SYNCHRONIZATION,
							  SyncOperationConfig.SYNC_OPERATION_LOAD,
							  SyncOperationConfig.SYNC_OPERATION_DATABASE_PREPARATION,
							  SyncOperationConfig.SYNC_OPERATION_POJO_GENERATION};
		
		
		return utilities.parseArrayToList(supported);
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		return getRelatedSyncConfig().getDesignation() + "_" + this.operationType;
	}

	public String generateControllerId() {
		return relatedSyncConfig.generateControllerId() + "_" + getOperationType();
	}
}
