package org.openmrs.module.eptssync.controller.conf;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.eptssync.changedrecordsdetector.controller.ChangedRecordsDetectorController;
import org.openmrs.module.eptssync.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.eptssync.controller.OperationController;
import org.openmrs.module.eptssync.controller.ProcessController;
import org.openmrs.module.eptssync.controller.SiteOperationController;
import org.openmrs.module.eptssync.databasepreparation.controller.DatabasePreparationController;
import org.openmrs.module.eptssync.dbquickcopy.controller.DBQuickCopyController;
import org.openmrs.module.eptssync.dbquickexport.controller.DBQuickExportController;
import org.openmrs.module.eptssync.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.eptssync.dbquickmerge.controller.DBQuickMergeController;
import org.openmrs.module.eptssync.engine.Engine;
import org.openmrs.module.eptssync.exceptions.ForbiddenOperationException;
import org.openmrs.module.eptssync.export.controller.DBExportController;
import org.openmrs.module.eptssync.inconsistenceresolver.controller.InconsistenceSolverController;
import org.openmrs.module.eptssync.load.controller.DataLoadController;
import org.openmrs.module.eptssync.merge.controller.DataBaseMergeFromSourceDBController;
import org.openmrs.module.eptssync.pojogeneration.controller.PojoGenerationController;
import org.openmrs.module.eptssync.problems_solver.controller.ProblemsSolverController;
import org.openmrs.module.eptssync.problems_solver.engine.ProblemsSolverEngine;
import org.openmrs.module.eptssync.reconciliation.controller.CentralAndRemoteDataReconciliationController;
import org.openmrs.module.eptssync.resolveconflictsinstagearea.controller.ResolveConflictsInStageAreaController;
import org.openmrs.module.eptssync.synchronization.controller.DatabaseMergeFromJSONController;
import org.openmrs.module.eptssync.transport.controller.TransportController;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SyncOperationConfig {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	public static String PROCESSING_MODE_SEQUENCIAL = "sequencial";
	
	public static String PROCESSING_MODE_PARALLEL = "parallel";
	
	private static final String[] supportedProcessingModes = { PROCESSING_MODE_SEQUENCIAL, PROCESSING_MODE_PARALLEL };
	
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
	
	private boolean mustRunToAllApps;
	
	private String engineFullClassName;
	
	public Class<Engine> engineClazz;
	
	public SyncOperationConfig() {
	}
	
	public String getEngineFullClassName() {
		return engineFullClassName;
	}
	
	public Class<Engine> getEngineClazz() {
		return engineClazz;
	}
	
	public void setEngineFullClassName(String engineFullClassName) {
		this.engineFullClassName = engineFullClassName;
	}
	
	public boolean isMustRunToAllApps() {
		return mustRunToAllApps;
	}
	
	public void setMustRunToAllApps(boolean mustRunToAllApps) {
		this.mustRunToAllApps = mustRunToAllApps;
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
		if (relatedControllers == null)
			return null;
		
		if (appOriginCode == null) {
			OperationController activeController = this.relatedControllers.get(0);
			
			return activeController;
		}
		
		for (OperationController controller : this.relatedControllers) {
			
			if (controller instanceof SiteOperationController
			        && ((SiteOperationController) controller).getAppOriginLocationCode().equalsIgnoreCase(appOriginCode)) {
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
			throw new ForbiddenOperationException("The processing mode '" + processingMode
			        + "' is not supported. Supported modes are: " + supportedProcessingModes);
		
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
		if (operationType != null && !operationType.isSupportedOperation())
			throw new ForbiddenOperationException("Operation '" + operationType + "' nor supported!");
		
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
	public boolean isDataBaseMergeFromJSONOperation() {
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
	
	@JsonIgnore
	public boolean isDBMergeFromSourceDB() {
		return this.operationType.isDbMergeFromSourceDB();
	}
	
	@JsonIgnore
	public boolean isDBQuickMergeMissingRecords() {
		return this.operationType.isDBQuickMergeMissingRecords();
	}
	
	@JsonIgnore
	public boolean isDBQuickMergeExistingRecords() {
		return this.operationType.isDBQuickMergeExistingRecords();
	}
	
	@JsonIgnore
	public boolean isResolveProblem() {
		return this.operationType.isResolveProblem();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (!(obj instanceof SyncOperationConfig))
			return false;
		
		return this.operationType.equals(((SyncOperationConfig) obj).operationType);
	}
	
	public List<OperationController> generateRelatedController(ProcessController parent, String appOriginCode_,
	        Connection conn) {
		this.relatedControllers = new ArrayList<OperationController>();
		
		if (getSourceFolders() == null) {
			this.relatedControllers.add(generateSingle(parent, appOriginCode_, conn));
		} else
			for (String appOriginCode : getSourceFolders()) {
				this.relatedControllers.add(generateSingle(parent, appOriginCode, conn));
			}
		
		if (this.getChild() != null) {
			for (OperationController controller : this.relatedControllers) {
				controller.setChildren(
				    this.getChild().generateRelatedController(controller.getProcessController(), appOriginCode_, conn));
				
				for (OperationController child : controller.getChildren()) {
					child.setParent(controller);
				}
			}
		}
		
		return this.relatedControllers;
	}
	
	private OperationController generateSingle(ProcessController parent, String appOriginCode, Connection conn) {
		
		if (isDatabasePreparationOperation()) {
			return new DatabasePreparationController(parent, this);
		} else if (isPojoGeneration()) {
			return new PojoGenerationController(parent, this);
		} else if (isExportOperation()) {
			return new DBExportController(parent, this);
		} else if (isTransportOperation()) {
			return new TransportController(parent, this);
		} else if (isInconsistenceSolver()) {
			return new InconsistenceSolverController(parent, this);
		} else if (isLoadOperation()) {
			return new DataLoadController(parent, this, appOriginCode);
		} else if (isDataBaseMergeFromJSONOperation()) {
			return new DatabaseMergeFromJSONController(parent, this);
		} else if (isConsolidationOperation()) {
			return new DatabaseIntegrityConsolidationController(parent, this);
		} else if (isChangedRecordsDetector()) {
			return new ChangedRecordsDetectorController(parent, this);
		} else if (isNewRecordsDetector()) {
			return new ChangedRecordsDetectorController(parent, this);
		} else if (isDBQuickLoad()) {
			return new DBQuickLoadController(parent, this, appOriginCode);
		} else if (isDBQuickExport()) {
			return new DBQuickExportController(parent, this);
		} else if (isMissingRecordsDetector() || isOutdateRecordsDetector() || isPhantomRecordsDetector()) {
			return new CentralAndRemoteDataReconciliationController(parent, this);
		} else if (isResolveConflictsInStageArea()) {
			return new ResolveConflictsInStageAreaController(parent, this);
		} else if (isDBQuickCopy()) {
			return new DBQuickCopyController(parent, this, appOriginCode);
		} else if (isDBMergeFromSourceDB()) {
			return new DataBaseMergeFromSourceDBController(parent, this);
		} else if (isDBQuickMergeMissingRecords() || isDBQuickMergeExistingRecords()) {
			return new DBQuickMergeController(parent, this, appOriginCode);
		} else if (isResolveProblem()) {
			return new ProblemsSolverController(parent, this);
		}
		
		else
			throw new ForbiddenOperationException("Operationtype [" + this.operationType + "]not supported!");
	}
	
	public void validate() {
		String errorMsg = "";
		int errNum = 0;
		
		if (this.getRelatedSyncConfig().isDataBaseMergeFromJSONProcess()) {
			if (!this.canBeRunInDestinationSyncProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in destination sync process\n";
			
			if (this.isLoadOperation() && (this.getSourceFolders() == null || this.getSourceFolders().size() == 0))
				errorMsg += ++errNum + ". There is no source folder defined";
		} else if (this.getRelatedSyncConfig().isSourceSyncProcess()) {
			if (!this.canBeRunInSourceSyncProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in source sync process\n";
		} else if (this.getRelatedSyncConfig().isDBReSyncProcess()) {
			if (!this.canBeRunInReSyncProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in re-sync process\n";
		} else if (this.getRelatedSyncConfig().isDBQuickExportProcess()) {
			if (!this.canBeRunInDBQuickExportProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in db quick export process\n";
		} else if (this.getRelatedSyncConfig().isDBQuickLoadProcess()) {
			if (!this.canBeRunInDBQuickLoadProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in db quick load process\n";
		} else if (this.getRelatedSyncConfig().isDataReconciliationProcess()) {
			if (!this.canBeRunInDataReconciliationProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in data reconciliation process\n";
		} else if (this.getRelatedSyncConfig().isDBQuickCopyProcess()) {
			if (!this.canBeRunInDBQuickCopyProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in data reconciliation process\n";
		} else if (this.getRelatedSyncConfig().isDataBaseMergeFromSourceDBProcess()) {
			if (!this.canBeRunInDataBasesMergeFromSourceDBProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in data reconciliation process\n";
		} else if (this.getRelatedSyncConfig().isQuickMergeUniformeDBProcess()) {
			if (!this.canBeRunInQuickMergeUniformeDBProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in db quick merge process\n";
		} else if (this.getRelatedSyncConfig().isQuickMergeNonUniformeDBProcess()) {
			if (!this.canBeRunInQuickMergeNonUniformeDBProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in db quick merge process\n";
		} else if (this.getRelatedSyncConfig().isDBInconsistencyCheckProcess()) {
			if (!this.canBeRunInDBInconsistencyCheckProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in db inconsistency check process\n";
		} else if (this.getRelatedSyncConfig().isResolveProblems()) {
			if (!this.canBeRunInResolveProblemsProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in db problems resolution process\n";
			
			if (!utilities.stringHasValue(this.getEngineFullClassName())) {
				errorMsg += ++errNum + ". You should specifie the engine full class on this type of operation "
				        + this.getOperationType() + "\n";
			} else {
				loadEngine();
				
				if (this.engineClazz == null) {
					errorMsg += ++errNum + ". The engine class [" + this.getEngineFullClassName() + "] cannot be found\n";
				} else if (!ProblemsSolverEngine.class.isAssignableFrom(this.engineClazz)) {
					errorMsg += ++errNum + ". The engine class [" + this.getEngineFullClassName()
					        + "] is not any org.openmrs.module.eptssync.problems_solver.engine.ProblemsSolverEngine \n";
				}
			}
		}
		
		if (utilities.stringHasValue(errorMsg)) {
			errorMsg = "There are errors on config operation configuration " + this.getOperationType() + "[File:  "
			        + this.getRelatedSyncConfig().getRelatedConfFile().getAbsolutePath() + "]\n" + errorMsg;
			throw new ForbiddenOperationException(errorMsg);
		} else if (this.getChild() != null) {
			this.getChild().validate();
		}
	}
	
	@JsonIgnore
	public boolean canBeRunInResolveProblemsProcess() {
		return utilities.existOnArray(getSupportedOperationsInResolveProblemsProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInResolveProblemsProcess() {
		SyncOperationType[] supported = { SyncOperationType.RESOLVE_PROBLEM };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInSourceSyncProcess() {
		return utilities.existOnArray(getSupportedOperationsInSourceSyncProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInSourceSyncProcess() {
		SyncOperationType[] supported = { SyncOperationType.EXPORT, SyncOperationType.TRANSPORT,
		        SyncOperationType.INCONSISTENCY_SOLVER, SyncOperationType.DATABASE_PREPARATION,
		        SyncOperationType.POJO_GENERATION };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDBInconsistencyCheckProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBInconsistencyCheckProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInDBInconsistencyCheckProcess() {
		SyncOperationType[] supported = { SyncOperationType.INCONSISTENCY_SOLVER, SyncOperationType.DATABASE_PREPARATION,
		        SyncOperationType.POJO_GENERATION };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInReSyncProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBReSyncProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInDBReSyncProcess() {
		SyncOperationType[] supported = { SyncOperationType.NEW_RECORDS_DETECTOR, SyncOperationType.CHANGED_RECORDS_DETECTOR,
		        SyncOperationType.DATABASE_PREPARATION };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDBQuickLoadProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBQuickLoadProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInDBQuickLoadProcess() {
		SyncOperationType[] supported = { SyncOperationType.DATABASE_PREPARATION, SyncOperationType.DB_QUICK_LOAD };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDBQuickExportProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBQuickExportProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInDBQuickExportProcess() {
		SyncOperationType[] supported = { SyncOperationType.DB_QUICK_EXPORT, SyncOperationType.TRANSPORT };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDBQuickCopyProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBQuickCopyProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInDBQuickCopyProcess() {
		SyncOperationType[] supported = { SyncOperationType.DATABASE_PREPARATION, SyncOperationType.DB_QUICK_COPY };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDataReconciliationProcess() {
		return utilities.existOnArray(getSupportedOperationsInDataReconciliationProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInDataReconciliationProcess() {
		SyncOperationType[] supported = { SyncOperationType.POJO_GENERATION, SyncOperationType.RESOLVE_CONFLICTS,
		        SyncOperationType.MISSING_RECORDS_DETECTOR, SyncOperationType.OUTDATED_RECORDS_DETECTOR,
		        SyncOperationType.PHANTOM_RECORDS_DETECTOR };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInQuickMergeUniformeDBProcess() {
		return utilities.existOnArray(getSupportedOperationsInQuickMergeUniformeDBProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInQuickMergeUniformeDBProcess() {
		SyncOperationType[] supported = { SyncOperationType.DB_QUICK_MERGE_EXISTING_RECORDS,
		        SyncOperationType.DB_QUICK_MERGE_MISSING_RECORDS };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInQuickMergeNonUniformeDBProcess() {
		return utilities.existOnArray(getSupportedOperationsInQuickMergeNonUniformeDBProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInQuickMergeNonUniformeDBProcess() {
		SyncOperationType[] supported = { SyncOperationType.DB_QUICK_MERGE_EXISTING_RECORDS,
		        SyncOperationType.DB_QUICK_MERGE_MISSING_RECORDS, SyncOperationType.DATABASE_PREPARATION,
		        SyncOperationType.POJO_GENERATION };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDataBasesMergeFromSourceDBProcess() {
		return utilities.existOnArray(getSupportedOperationsInDataBasesMergeFromSourceDBProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInDataBasesMergeFromSourceDBProcess() {
		SyncOperationType[] supported = { SyncOperationType.POJO_GENERATION, SyncOperationType.RESOLVE_CONFLICTS,
		        SyncOperationType.DB_MERGE_FROM_SOURCE_DB };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDestinationSyncProcess() {
		return utilities.existOnArray(getSupportedOperationsInDestinationSyncProcess(), this.operationType);
	}
	
	public static List<SyncOperationType> getSupportedOperationsInDestinationSyncProcess() {
		SyncOperationType[] supported = { SyncOperationType.CONSOLIDATION, SyncOperationType.DB_MERGE_FROM_JSON,
		        SyncOperationType.LOAD, SyncOperationType.DATABASE_PREPARATION, SyncOperationType.POJO_GENERATION };
		
		return utilities.parseArrayToList(supported);
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		return (getRelatedSyncConfig().getDesignation() + "_" + this.operationType).toLowerCase();
	}
	
	public String generateControllerId() {
		String controllerId = relatedSyncConfig.generateControllerId() + "_" + getOperationType();
		
		return controllerId.toLowerCase();
	}
	
	public boolean isSupposedToHaveOriginAppCode() {
		return this.getRelatedSyncConfig().isSupposedToHaveOriginAppCode();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Engine> void loadEngine() {
		
		try {
			URL[] classPaths = new URL[] { getRelatedSyncConfig().getClassPathAsFile().toURI().toURL() };
			
			URLClassLoader loader = URLClassLoader.newInstance(classPaths);
			
			Class<T> c = (Class<T>) loader.loadClass(this.getEngineFullClassName());
			
			loader.close();
			
			this.engineClazz = (Class<Engine>) c;
		}
		catch (ClassNotFoundException e) {
		}
		catch (IOException e) {
		}
	}
	
}
