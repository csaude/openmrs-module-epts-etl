package org.openmrs.module.epts.etl.conf;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.changedrecordsdetector.controller.ChangedRecordsDetectorController;
import org.openmrs.module.epts.etl.conf.types.EtlProcessingModeType;
import org.openmrs.module.epts.etl.consolitation.controller.DatabaseIntegrityConsolidationController;
import org.openmrs.module.epts.etl.controller.OperationController;
import org.openmrs.module.epts.etl.controller.ProcessController;
import org.openmrs.module.epts.etl.controller.SiteOperationController;
import org.openmrs.module.epts.etl.data.validation.missingrecords.controller.DetectMissingRecordsController;
import org.openmrs.module.epts.etl.databasepreparation.controller.DatabasePreparationController;
import org.openmrs.module.epts.etl.dbquickexport.controller.DBQuickExportController;
import org.openmrs.module.epts.etl.dbquickload.controller.DBQuickLoadController;
import org.openmrs.module.epts.etl.detectgapes.controller.DetectGapesController;
import org.openmrs.module.epts.etl.engine.TaskProcessor;
import org.openmrs.module.epts.etl.etl.controller.EtlController;
import org.openmrs.module.epts.etl.etl.re_etl.controller.ReEtlController;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.export.controller.DBExportController;
import org.openmrs.module.epts.etl.inconsistenceresolver.controller.InconsistenceSolverController;
import org.openmrs.module.epts.etl.load.controller.DataLoadController;
import org.openmrs.module.epts.etl.merge.controller.DataBaseMergeFromSourceDBController;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.pojogeneration.controller.PojoGenerationController;
import org.openmrs.module.epts.etl.problems_solver.controller.GenericOperationController;
import org.openmrs.module.epts.etl.problems_solver.engine.GenericEngine;
import org.openmrs.module.epts.etl.reconciliation.controller.CentralAndRemoteDataReconciliationController;
import org.openmrs.module.epts.etl.resolveconflictsinstagearea.controller.ResolveConflictsInStageAreaController;
import org.openmrs.module.epts.etl.synchronization.controller.DatabaseMergeFromJSONController;
import org.openmrs.module.epts.etl.transport.controller.TransportController;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class EtlOperationConfig extends AbstractBaseConfiguration {
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private EtlOperationType operationType;
	
	private int processingBatch;
	
	private int maxSupportedEngines;
	
	private boolean doIntegrityCheckInTheEnd;
	
	private EtlOperationConfig child;
	
	private EtlOperationConfig parent;
	
	private EtlConfiguration relatedSyncConfig;
	
	private boolean disabled;
	
	private EtlProcessingModeType processingMode;
	
	private List<String> sourceFolders;
	
	private List<OperationController<? extends EtlDatabaseObject>> relatedControllers;
	
	private boolean mustRunToAllApps;
	
	private String engineFullClassName;
	
	private Class<TaskProcessor<? extends EtlDatabaseObject>> engineClazz;
	
	private boolean skipFinalDataVerification;
	
	private boolean writeOperationHistory;
	
	private boolean useSharedConnectionPerThread;
	
	/**
	 * Indicates whether this operation will executed over all tables configured under
	 * {@link #relatedSyncConfig}. If true, the operation will be run only once (for the first
	 * table)
	 */
	private boolean runOnce;
	
	private boolean nonResumable;
	
	private EtlDstType dstType;
	
	public EtlOperationConfig() {
		this.dstType = EtlDstType.db;
		this.processingMode = EtlProcessingModeType.SERIAL;
	}
	
	public EtlDstType getDstType() {
		return dstType;
	}
	
	public void setDstType(EtlDstType dstType) {
		this.dstType = dstType;
	}
	
	public boolean isDbDst() {
		return getDstType().isDb();
	}
	
	public boolean isJsonDst() {
		return getDstType().isJson();
	}
	
	public boolean isDumpDst() {
		return getDstType().isDump();
	}
	
	public boolean isCsvDst() {
		return getDstType().isCsv();
	}
	
	public boolean isFileDst() {
		return getDstType().isFile();
	}
	
	public boolean isUseSharedConnectionPerThread() {
		return useSharedConnectionPerThread;
	}
	
	public void setUseSharedConnectionPerThread(boolean useSharedConnectionPerThread) {
		this.useSharedConnectionPerThread = useSharedConnectionPerThread;
	}
	
	public boolean isNonResumable() {
		return nonResumable;
	}
	
	public void setNonResumable(boolean nonResumable) {
		this.nonResumable = nonResumable;
	}
	
	public boolean writeOperationHistory() {
		return writeOperationHistory;
	}
	
	public void setWriteOperationHistory(boolean writeOperationHistory) {
		this.writeOperationHistory = writeOperationHistory;
	}
	
	public boolean skipFinalDataVerification() {
		return skipFinalDataVerification;
	}
	
	public void setSkipFinalDataVerification(boolean skipFinalDataVerification) {
		this.skipFinalDataVerification = skipFinalDataVerification;
	}
	
	public boolean isRunOnce() {
		return runOnce;
	}
	
	public void setRunOnce(boolean runOnce) {
		this.runOnce = runOnce;
	}
	
	public String getEngineFullClassName() {
		return engineFullClassName;
	}
	
	public Class<TaskProcessor<? extends EtlDatabaseObject>> getEngineClazz() {
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
	public List<OperationController<? extends EtlDatabaseObject>> getRelatedControllers() {
		return relatedControllers;
	}
	
	public OperationController<? extends EtlDatabaseObject> getRelatedController(String appOriginCode) {
		if (relatedControllers == null)
			return null;
		
		if (appOriginCode == null) {
			OperationController<? extends EtlDatabaseObject> activeController = this.relatedControllers.get(0);
			
			return activeController;
		}
		
		for (OperationController<? extends EtlDatabaseObject> controller : this.relatedControllers) {
			
			if (controller instanceof SiteOperationController
			        && ((SiteOperationController<? extends EtlDatabaseObject>) controller).getAppOriginLocationCode()
			                .equalsIgnoreCase(appOriginCode)) {
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
	
	public EtlProcessingModeType getProcessingMode() {
		return processingMode;
	}
	
	public void setProcessingMode(EtlProcessingModeType processingMode) {
		this.processingMode = processingMode;
	}
	
	@JsonIgnore
	public boolean isParallelModeProcessing() {
		return this.processingMode.isParallel();
	}
	
	@JsonIgnore
	public boolean isSerialModeProcessing() {
		return this.processingMode.isSerial();
	}
	
	@JsonIgnore
	public EtlOperationConfig getParentConf() {
		return parent;
	}
	
	public void setParent(EtlOperationConfig parent) {
		this.parent = parent;
	}
	
	public EtlOperationConfig getChild() {
		return child;
	}
	
	public void setChild(EtlOperationConfig child) {
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
	public EtlConfiguration getRelatedSyncConfig() {
		return relatedSyncConfig;
	}
	
	public void setRelatedSyncConfig(EtlConfiguration relatedSyncConfig) {
		this.relatedSyncConfig = relatedSyncConfig;
	}
	
	public EtlOperationType getOperationType() {
		return operationType;
	}
	
	public void setOperationType(EtlOperationType operationType) {
		if (operationType != null && !operationType.isSupportedOperation())
			throw new ForbiddenOperationException("Operation '" + operationType + "' nor supported!");
		
		this.operationType = operationType;
	}
	
	public int getProcessingBatch() {
		return processingBatch;
	}
	
	public void setProcessingBatch(int processingBatch) {
		this.processingBatch = processingBatch;
	}
	
	public int getMaxSupportedEngines() {
		return maxSupportedEngines;
	}
	
	public void setMaxSupportedEngines(int maxSupportedEngines) {
		this.maxSupportedEngines = maxSupportedEngines;
	}
	
	public void setDoIntegrityCheckInTheEnd(boolean doIntegrityCheckInTheEnd) {
		this.doIntegrityCheckInTheEnd = doIntegrityCheckInTheEnd;
	}
	
	public boolean isDoIntegrityCheckInTheEnd() {
		return doIntegrityCheckInTheEnd;
	}
	
	public static <T extends EtlDatabaseObject> EtlOperationConfig fastCreate(EtlOperationType operationType) {
		EtlOperationConfig op = new EtlOperationConfig();
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
	public boolean isEtl() {
		return this.operationType.isEtl();
	}
	
	@JsonIgnore
	public boolean isReEtl() {
		return this.operationType.isReEtl();
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
	
	public boolean isDetectGapes() {
		return this.operationType.isDetectGapesOperation();
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
	public boolean isDetectMissingRecords() {
		return this.operationType.isDetectMIssingRecords();
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
	public boolean isResolveProblem() {
		return this.operationType.isGenericOperation();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (!(obj instanceof EtlOperationConfig))
			return false;
		
		EtlOperationConfig otherObj = (EtlOperationConfig) obj;
		
		return this.operationType.equals(otherObj.operationType);
	}
	
	public List<OperationController<? extends EtlDatabaseObject>> generateRelatedController(ProcessController parent,
	        String appOriginCode_, Connection conn) {
		this.relatedControllers = new ArrayList<>();
		
		if (getSourceFolders() == null) {
			this.relatedControllers.add(generateSingle(parent, appOriginCode_, conn));
		} else {
			for (String appOriginCode : getSourceFolders()) {
				this.relatedControllers.add(generateSingle(parent, appOriginCode, conn));
			}
		}
		
		if (this.getChild() != null) {
			for (OperationController<? extends EtlDatabaseObject> controller : this.relatedControllers) {
				controller.setChildren(
				    this.getChild().generateRelatedController(controller.getProcessController(), appOriginCode_, conn));
				
				for (OperationController<? extends EtlDatabaseObject> child : controller.getChildren()) {
					child.setParent(controller);
				}
			}
		}
		
		return this.relatedControllers;
	}
	
	private OperationController<? extends EtlDatabaseObject> generateSingle(ProcessController parent, String appOriginCode,
	        Connection conn) {
		
		if (isReEtl()) {
			return new ReEtlController(parent, this, appOriginCode);
		} else if (isDetectMissingRecords()) {
			return new DetectMissingRecordsController(parent, this, appOriginCode);
		} else if (isEtl()) {
			return new EtlController(parent, this, appOriginCode);
		} else if (isDatabasePreparationOperation()) {
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
		} else if (isDBMergeFromSourceDB()) {
			return new DataBaseMergeFromSourceDBController(parent, this);
		} else if (isResolveProblem()) {
			return new GenericOperationController(parent, this);
		} else if (isDetectGapes()) {
			return new DetectGapesController(parent, this);
		} else
			throw new ForbiddenOperationException("Operationtype [" + this.operationType + "]not supported!");
	}
	
	public void validate() {
		String errorMsg = "";
		int errNum = 0;
		
		if (this.getRelatedSyncConfig().isReEtlProcess()) {
			if (!this.canBeRunInReEtlProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in Re Etl process\n";
		} else if (this.getRelatedSyncConfig().isEtlProcess()) {
			if (!this.canBeRunInEtlProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in Etl process\n";
		} else if (this.getRelatedSyncConfig().isDetectMissingRecords()) {
			if (!this.canBeRunInDetectMissingRecordsProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in Detect Missing Records process\n";
		} else if (this.getRelatedSyncConfig().isDataBaseMergeFromJSONProcess()) {
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
		} else if (this.getRelatedSyncConfig().isDataBaseMergeFromSourceDBProcess()) {
			if (!this.canBeRunInDataBasesMergeFromSourceDBProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in data reconciliation process\n";
		} else if (this.getRelatedSyncConfig().isDBInconsistencyCheckProcess()) {
			if (!this.canBeRunInDBInconsistencyCheckProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in db inconsistency check process\n";
		} else if (this.getRelatedSyncConfig().isPojoGeneration()) {
			if (!this.canBeRunInDbPojoGenerationProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in pojo generation process\n";
		}
		
		else if (this.getRelatedSyncConfig().isDetectGapesOnDbTables()) {
			if (!this.canBeRunInDetectGapesOnDBTables())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in detect gapes on db tables process\n";
		} else if (this.getRelatedSyncConfig().isResolveProblems()) {
			if (!this.canBeRunInResolveProblemsProcess())
				errorMsg += ++errNum + ". This operation [" + this.getOperationType()
				        + "] Cannot be configured in db problems resolution process\n";
			
		}
		
		try {
			tryToLoadEngine();
			
			if (this.getRelatedSyncConfig().isResolveProblems() && !GenericEngine.class.isAssignableFrom(this.engineClazz)) {
				errorMsg += ++errNum + ". The engine class [" + this.getEngineFullClassName()
				        + "] is not any org.openmrs.module.epts.etl.problems_solver.engine.GenericEngine \n";
			}
			
		}
		catch (ForbiddenOperationException e) {
			errorMsg += ++errNum + "." + e.getLocalizedMessage() + "\n";
		}
		
		if (utilities.stringHasValue(errorMsg)) {
			errorMsg = "There are errors on dstConf operation configuration " + this.getOperationType() + "[File:  "
			        + this.getRelatedSyncConfig().getRelatedConfFile().getAbsolutePath() + "]\n" + errorMsg;
			throw new ForbiddenOperationException(errorMsg);
		} else if (this.getChild() != null) {
			this.getChild().validate();
		}
		
	}
	
	public boolean requireEngine() {
		return this.getRelatedSyncConfig().isResolveProblems();
	}
	
	@JsonIgnore
	public boolean canBeRunInResolveProblemsProcess() {
		return utilities.existOnArray(getSupportedOperationsInResolveProblemsProcess(), this.operationType);
	}
	
	@JsonIgnore
	public boolean canBeRunInDetectMissingRecordsProcess() {
		return utilities.existOnArray(getSupportedOperationsInDetectMissingRecordsProcess(), this.operationType);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInResolveProblemsProcess() {
		EtlOperationType[] supported = { EtlOperationType.GENERIC_OPERATION };
		
		return utilities.parseArrayToList(supported);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInDetectGapesOnDbTables() {
		EtlOperationType[] supported = { EtlOperationType.DETECT_GAPES };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInSourceSyncProcess() {
		return utilities.existOnArray(getSupportedOperationsInSourceSyncProcess(), this.operationType);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInSourceSyncProcess() {
		EtlOperationType[] supported = { EtlOperationType.EXPORT, EtlOperationType.TRANSPORT,
		        EtlOperationType.INCONSISTENCY_SOLVER, EtlOperationType.DATABASE_PREPARATION,
		        EtlOperationType.POJO_GENERATION };
		
		return utilities.parseArrayToList(supported);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInPojoGenerationProcess() {
		EtlOperationType[] supported = { EtlOperationType.POJO_GENERATION };
		
		return utilities.parseArrayToList(supported);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInEtlProcess() {
		EtlOperationType[] supported = { EtlOperationType.ETL, EtlOperationType.DB_EXTRACT };
		
		return utilities.parseArrayToList(supported);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInReEtlProcess() {
		EtlOperationType[] supported = { EtlOperationType.RE_ETL };
		
		return utilities.parseArrayToList(supported);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInDetectMissingRecordsProcess() {
		EtlOperationType[] supported = { EtlOperationType.DETECT_MISSING_RECORDS };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDbPojoGenerationProcess() {
		return utilities.existOnArray(getSupportedOperationsInPojoGenerationProcess(), this.operationType);
	}
	
	@JsonIgnore
	public boolean canBeRunInDetectGapesOnDBTables() {
		return utilities.existOnArray(getSupportedOperationsInDetectGapesOnDbTables(), this.operationType);
	}
	
	@JsonIgnore
	public boolean canBeRunInDBInconsistencyCheckProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBInconsistencyCheckProcess(), this.operationType);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInDBInconsistencyCheckProcess() {
		EtlOperationType[] supported = { EtlOperationType.INCONSISTENCY_SOLVER, EtlOperationType.DATABASE_PREPARATION,
		        EtlOperationType.POJO_GENERATION };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInReSyncProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBReSyncProcess(), this.operationType);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInDBReSyncProcess() {
		EtlOperationType[] supported = { EtlOperationType.NEW_RECORDS_DETECTOR, EtlOperationType.CHANGED_RECORDS_DETECTOR };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDBQuickLoadProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBQuickLoadProcess(), this.operationType);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInDBQuickLoadProcess() {
		EtlOperationType[] supported = { EtlOperationType.DATABASE_PREPARATION, EtlOperationType.QUICK_LOAD };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDBQuickExportProcess() {
		return utilities.existOnArray(getSupportedOperationsInDBQuickExportProcess(), this.operationType);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInDBQuickExportProcess() {
		EtlOperationType[] supported = { EtlOperationType.QUICK_EXPORT };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDataReconciliationProcess() {
		return utilities.existOnArray(getSupportedOperationsInDataReconciliationProcess(), this.operationType);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInDataReconciliationProcess() {
		EtlOperationType[] supported = { EtlOperationType.POJO_GENERATION, EtlOperationType.RESOLVE_CONFLICTS,
		        EtlOperationType.MISSING_RECORDS_DETECTOR, EtlOperationType.OUTDATED_RECORDS_DETECTOR,
		        EtlOperationType.PHANTOM_RECORDS_DETECTOR };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDataBasesMergeFromSourceDBProcess() {
		return utilities.existOnArray(getSupportedOperationsInDataBasesMergeFromSourceDBProcess(), this.operationType);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInDataBasesMergeFromSourceDBProcess() {
		EtlOperationType[] supported = { EtlOperationType.POJO_GENERATION, EtlOperationType.RESOLVE_CONFLICTS,
		        EtlOperationType.DB_MERGE_FROM_SOURCE_DB };
		
		return utilities.parseArrayToList(supported);
	}
	
	@JsonIgnore
	public boolean canBeRunInDestinationSyncProcess() {
		return utilities.existOnArray(getSupportedOperationsInDestinationSyncProcess(), this.operationType);
	}
	
	@JsonIgnore
	public boolean canBeRunInEtlProcess() {
		return utilities.existOnArray(getSupportedOperationsInEtlProcess(), this.operationType);
	}
	
	@JsonIgnore
	public boolean canBeRunInReEtlProcess() {
		return utilities.existOnArray(getSupportedOperationsInReEtlProcess(), this.operationType);
	}
	
	public static List<EtlOperationType> getSupportedOperationsInDestinationSyncProcess() {
		EtlOperationType[] supported = { EtlOperationType.CONSOLIDATION, EtlOperationType.DB_MERGE_FROM_JSON,
		        EtlOperationType.LOAD, EtlOperationType.DATABASE_PREPARATION, EtlOperationType.POJO_GENERATION };
		
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
	public synchronized void tryToLoadEngine() throws ForbiddenOperationException {
		try {
			if (engineClazz == null) {
				
				if (utilities.stringHasValue(this.getEngineFullClassName())) {
					
					ClassLoader loader = TaskProcessor.class.getClassLoader();
					
					Class<TaskProcessor<? extends EtlDatabaseObject>> c = (Class<TaskProcessor<? extends EtlDatabaseObject>>) loader
					        .loadClass(this.getEngineFullClassName());
					
					this.engineClazz = (Class<TaskProcessor<? extends EtlDatabaseObject>>) c;
					
					if (this.engineClazz == null) {
						throw new ForbiddenOperationException(
						        "The engine class [" + this.getEngineFullClassName() + "] cannot be found");
					}
				} else if (requireEngine()) {
					throw new ForbiddenOperationException(
					        "You should specifie the engine full class on this type of operation "
					                + this.getOperationType());
				}
			}
		}
		catch (
		
		ClassNotFoundException e) {}
	}
	
}
