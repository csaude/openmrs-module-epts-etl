package org.openmrs.module.eptssync.controller.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.changedrecordsdetector.controller.ChangedRecordsDetectorController;
import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.controller.DestinationOperationController;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.databasepreparation.controller.DatabasePreparationController;
import org.openmrs.module.eptssync.dbquickcopy.controller.DBQuickCopyController;
import org.openmrs.module.eptssync.dbquickexport.controller.DBQuickExportController;
import org.openmrs.module.eptssync.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.export.controller.DBExportController;
import org.openmrs.module.eptssync.inconsistenceresolver.controller.InconsistenceSolverController;
import org.openmrs.module.eptssync.load.controller.DataLoadController;
import org.openmrs.module.eptssync.pojogeneration.controller.PojoGenerationController;
import org.openmrs.module.eptssync.reconciliation.controller.CentralAndRemoteDataReconciliationController;
import org.openmrs.module.eptssync.resolveconflictsinstagearea.controller.ResolveConflictsInStageAreaController;
import org.openmrs.module.eptssync.synchronization.controller.SyncController;
import org.openmrs.module.eptssync.transport.controller.TransportController;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncOperationConfig {
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static String PROCESSING_MODE_SEQUENCIAL="sequencial";
	public static String PROCESSING_MODE_PARALLEL="parallel";
	
	private static final String[] supportedProcessingModes = {PROCESSING_MODE_SEQUENCIAL, PROCESSING_MODE_PARALLEL};

	private SyncOperationType operationType;

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
	
	public SyncOperationType getOperationType() {
		return operationType;
	}

	public void setOperationType(SyncOperationType operationType) {
		if (operationType != null && !operationType.isSupportedOperation()) throw new ForbiddenOperationException("Operation '" + operationType + "' nor supported!");
		
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
	
	public static SyncOperationConfig fastCreate(SyncOperationType operationType) {
		SyncOperationConfig op = new SyncOperationConfig();
		op.setOperationType(operationType);
	
		return op;
	}
	
	@JsonIgnore
	public boolean isExportOperation() {
		return this.operationType.isExport(); 
	}
	
	@JsonIgnore
	public boolean isSynchronizationOperation() {
		return this.operationType.isSynchronization();
	}
	
	@JsonIgnore
	public boolean isLoadOperation() {
		return this.operationType.isLoad();
	}
	
	@JsonIgnore
	public boolean isTransportOperation() {
		return this.operationType.isTransport();
	}
	
	@JsonIgnore
	public boolean isConsolidationOperation() {
		return this.operationType.isConsolidation();
	}
	
	@JsonIgnore
	public boolean isDatabasePreparationOperation() {
		return this.operationType.isDatabasePreparation();
	}
	
	@JsonIgnore
	public boolean isPojoGeneration() {
		return this.operationType.isPojoGeneration();
	}
	
	@JsonIgnore
	public boolean isInconsistenceSolver() {
		return this.operationType.isInconsistencySolver();
	}
	
	@JsonIgnore
	public boolean isChangedRecordsDetector() {
		return this.operationType.isChangedRecordsDetector();
	}
	
	public boolean isDBQuickExport() {
		return this.operationType.isDbQuickExport();
	}
	
	public boolean isDBQuickCopy() {
		return this.operationType.isDbQuickCopy();
	}
	
	@JsonIgnore
	public boolean isNewRecordsDetector() {
		return this.operationType.isNewRecordsDetector();
	}
	
	@JsonIgnore
	public boolean isDBQuickLoad() {
		return this.operationType.isDbQuickLoad();
	}
	
	@JsonIgnore
	public boolean isMissingRecordsDetector() {
		return this.operationType.isMissingRecordsDetector();
	}
	
	@JsonIgnore
	public boolean isOutdateRecordsDetector() {
		return this.operationType.isOutdatedRecordsDetector();
	}
	
	@JsonIgnore
	public boolean isPhantomRecordsDetector() {
		return this.operationType.isPhantomRecordsDetector();
	}
	
	@JsonIgnore
	public boolean isResolveConflictsInStageArea() {
		return this.operationType.isResolveConflicts();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		
		if (!(obj instanceof SyncOperationConfig)) return false;
		
		return this.operationType.equals(((SyncOperationConfig)obj).operationType);
	}
	
	public List<OperationController> generateRelatedController(ProcessController parent, String appOriginCode_, Connection conn) {
		this.relatedControllers = new ArrayList<OperationController>();
		
		if (getSourceFolders() == null) {
			this.relatedControllers.add(generateSingle(parent, appOriginCode_, conn));
		}
		else
		for (String appOriginCode : getSourceFolders()) {
			this.relatedControllers.add(generateSingle(parent, appOriginCode, conn));
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
	
	private OperationController generateSingle(ProcessController parent, String appOriginCode, Connection conn) {
		
		/*if (isDatabasePreparationOperation() && this.getRelatedSyncConfig().isDBQuickCopyProcess()) {
			return new OnlineQuickExportAndLoadDatabasePreparationController(parent, this);
		}
		else*/
		if (isDatabasePreparationOperation()) {
			return new DatabasePreparationController(parent, this);
		}
		else			
		if ( isPojoGeneration()) {
			return new PojoGenerationController(parent, this);
		}
		else	
		if (isExportOperation()) {
			return new DBExportController(parent, this);
		}
		else
		if (isTransportOperation()) {
			return new TransportController(parent, this);
		}
		else
		if (isInconsistenceSolver()) {
			return new InconsistenceSolverController(parent, this);
		}
		else
		if (isLoadOperation()) {
			return new DataLoadController(parent, this, appOriginCode);
		}
		else
		if (isSynchronizationOperation()) {
			return new SyncController(parent, this, appOriginCode);
		}
		else
		if (isConsolidationOperation()) {
			return new DatabaseIntegrityConsolidationController(parent, this, appOriginCode);
		}
		else
		if (isChangedRecordsDetector()) {
			return new ChangedRecordsDetectorController(parent, this);
		}
		else
		if (isNewRecordsDetector()) {
			return new ChangedRecordsDetectorController(parent, this);
		}		
		else
		if (isDBQuickLoad()) {
			return new DBQuickLoadController(parent, this, appOriginCode);
		}
		else
		if (isDBQuickExport()) {
			return new DBQuickExportController(parent, this);
		}
		else
		if (isMissingRecordsDetector() || isOutdateRecordsDetector() || isPhantomRecordsDetector())	{
			return new CentralAndRemoteDataReconciliationController(parent, this);
		}
		else
		if (isResolveConflictsInStageArea()) {
			return new ResolveConflictsInStageAreaController(parent, this);
		}
		else
		if (isDBQuickCopy()) {
			return new DBQuickCopyController(parent, this, appOriginCode);
		}
		else throw new ForbiddenOperationException("Operationtype [" + this.operationType + "]not supported!");		
	}
	
	public void validate () {
		String errorMsg = "";
		int errNum = 0;
		
		if (this.getRelatedSyncConfig().isDestinationSyncProcess()) {
			if (!this.canBeRunInDestinationSyncProcess()) errorMsg += ++errNum + ". This operation ["+ this.getOperationType() + "] Cannot be configured in destination sync process\n";
		
			if (this.isLoadOperation() && (this.getSourceFolders() == null || this.getSourceFolders().size() == 0))  errorMsg += ++errNum + ". There is no source folder defined";
		}
		else
		if (this.getRelatedSyncConfig().isSourceSyncProcess()) {
			if (!this.canBeRunInSourceSyncProcess()) errorMsg += ++errNum + ". This operation ["+ this.getOperationType() + "] Cannot be configured in source sync process\n";
		}
		else
		if (this.getRelatedSyncConfig().isDBReSyncProcess()) {
			if (!this.canBeRunInReSyncProcess()) errorMsg += ++errNum + ". This operation ["+ this.getOperationType() + "] Cannot be configured in re-sync process\n";
		}
		else
		if (this.getRelatedSyncConfig().isDBQuickExportProcess()) {
			if (!this.canBeRunInDBQuickExportProcess()) errorMsg += ++errNum + ". This operation ["+ this.getOperationType() + "] Cannot be configured in db quick export process\n";
		}
		else
		if (this.getRelatedSyncConfig().isDBQuickLoadProcess()) {
			if (!this.canBeRunInDBQuickLoadProcess()) errorMsg += ++errNum + ". This operation ["+ this.getOperationType() + "] Cannot be configured in db quick load process\n";
		}
		else
		if (this.getRelatedSyncConfig().isDataReconciliationProcess()) {
			if (!this.canBeRunInDataReconciliationProcess()) errorMsg += ++errNum + ". This operation ["+ this.getOperationType() + "] Cannot be configured in data reconciliation process\n";
		}
		else
		if (this.getRelatedSyncConfig().isDBQuickCopyProcess()) {
			if (!this.canBeRunInDBQuickCopyProcess()) errorMsg += ++errNum + ". This operation ["+ this.getOperationType() + "] Cannot be configured in data reconciliation process\n";
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
	public boolean canBeRunInSourceSyncProcess() {
		return utilities.existOnArray(getSupportedOperationsInSourceSyncProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInSourceSyncProcess() {
		SyncOperationType[] supported = {	SyncOperationType.EXPORT,
											SyncOperationType.TRANSPORT,
											SyncOperationType.INCONSISTENCY_SOLVER,
											SyncOperationType.DATABASE_PREPARATION,
											SyncOperationType.POJO_GENERATION};
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInReSyncProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBReSyncProcess(), this.operationType);
	}
	
	public static List<SyncOperationType>  getSupportedOperationsInDBReSyncProcess() {
		SyncOperationType[] supported = {SyncOperationType.NEW_RECORDS_DETECTOR,
							  SyncOperationType.CHANGED_RECORDS_DETECTOR,
							  SyncOperationType.DATABASE_PREPARATION};
		
		return utilities.parseArrayToList(supported);
	}
	
	
	
	@JsonIgnore
	public boolean canBeRunInDBQuickLoadProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBQuickLoadProcess(), this.operationType);
	}
		
	public static List<SyncOperationType>  getSupportedOperationsInDBQuickLoadProcess() {
		SyncOperationType[] supported = {SyncOperationType.DATABASE_PREPARATION,
							  SyncOperationType.DB_QUICK_LOAD};
		
		return utilities.parseArrayToList(supported);
	}
	
	
	@JsonIgnore
	public boolean canBeRunInDBQuickExportProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBQuickExportProcess(), this.operationType);
	}
		
	public static List<SyncOperationType> getSupportedOperationsInDBQuickExportProcess() {
		SyncOperationType[] supported = {SyncOperationType.DB_QUICK_EXPORT,
							  			 SyncOperationType.TRANSPORT};
		
		return utilities.parseArrayToList(supported);
	}
	
	
	@JsonIgnore
	public boolean canBeRunInDBQuickCopyProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBQuickCopyProcess(), this.operationType);
	}
		
	public static List<SyncOperationType>  getSupportedOperationsInDBQuickCopyProcess() {
		SyncOperationType[] supported = {SyncOperationType.DATABASE_PREPARATION,
							  			 SyncOperationType.DB_QUICK_COPY};
		
		return utilities.parseArrayToList(supported);
	}
	
	
	@JsonIgnore
	public boolean canBeRunInDataReconciliationProcess() {
		return utilities.existOnArray(getSupportedOperationsInDataReconciliationProcess(), this.operationType);
	}
		
	public static List<SyncOperationType>  getSupportedOperationsInDataReconciliationProcess() {
		SyncOperationType[] supported = { SyncOperationType.POJO_GENERATION,
										  SyncOperationType.RESOLVE_CONFLICTS,
										  SyncOperationType.MISSING_RECORDS_DETECTOR,
										  SyncOperationType.OUTDATED_RECORDS_DETECTOR,
										  SyncOperationType.PHANTOM_RECORDS_DETECTOR};
		
		return utilities.parseArrayToList(supported);
	}
		
	@JsonIgnore
	public boolean canBeRunInDestinationSyncProcess() {
		return utilities.existOnArray(getSupportedOperationsInDestinationSyncProcess(), this.operationType);
	}
	
	
	public static List<SyncOperationType>  getSupportedOperationsInDestinationSyncProcess() {
		SyncOperationType[] supported = {SyncOperationType.CONSOLIDATION,
							  SyncOperationType.SYNCHRONIZATION,
							  SyncOperationType.LOAD,
							  SyncOperationType.DATABASE_PREPARATION,
							  SyncOperationType.POJO_GENERATION};
		
		
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

	public boolean isSupposedToHaveOriginAppCode() {
		return this.getRelatedSyncConfig().isSupposedToHaveOriginAppCode();
	}
}
