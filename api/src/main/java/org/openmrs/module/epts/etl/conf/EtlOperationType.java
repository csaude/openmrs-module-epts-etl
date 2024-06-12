package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * Suported operation type in sync process
 * 
 * @author jpboane
 */
public enum EtlOperationType {
	
	CONSOLIDATION,
	EXPORT,
	DATABASE_PREPARATION,
	LOAD,
	DB_MERGE_FROM_JSON,
	TRANSPORT,
	PREPARATION,
	POJO_GENERATION,
	INCONSISTENCY_SOLVER,
	CHANGED_RECORDS_DETECTOR,
	NEW_RECORDS_DETECTOR,
	QUICK_EXPORT,
	QUICK_LOAD,
	MISSING_RECORDS_DETECTOR,
	OUTDATED_RECORDS_DETECTOR,
	PHANTOM_RECORDS_DETECTOR,
	RESOLVE_CONFLICTS,
	DB_QUICK_COPY,
	DB_MERGE_FROM_SOURCE_DB,
	GENERIC_OPERATION,
	DETECT_GAPES,
	ETL,
	RE_ETL,
	DETECT_MISSING_RECORDS,
	DB_EXTRACT;
	
	public static boolean isDetectMIssingRecords(String operationType) {
		return EtlOperationType.valueOf(operationType).isDetectMIssingRecords();
	}
	
	public static boolean isEtl(String operationType) {
		return EtlOperationType.valueOf(operationType).isEtl();
	}
	
	public static boolean isReEtl(String operationType) {
		return EtlOperationType.valueOf(operationType).isReEtl();
	}
	
	public static boolean isDetectGapesOperation(String operationType) {
		return EtlOperationType.valueOf(operationType).isDetectGapesOperation();
	}
	
	public static boolean isGenericOperation(String operationType) {
		return EtlOperationType.valueOf(operationType).isGenericOperation();
	}
	
	public static boolean isDBMerge(String operationType) {
		return EtlOperationType.valueOf(operationType).isDbMergeFromSourceDB();
	}
	
	public static boolean isDatabasePreparation(String operationType) {
		return EtlOperationType.valueOf(operationType).isDatabasePreparation();
	}
	
	public static boolean isPojoGeneration(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(POJO_GENERATION);
	}
	
	public static boolean isInconsistencySolver(String operationType) {
		return EtlOperationType.valueOf(operationType).isInconsistencySolver();
	}
	
	public static boolean isExport(String operationType) {
		return EtlOperationType.valueOf(operationType).isExport();
	}
	
	public static boolean isSynchronization(String operationType) {
		return EtlOperationType.valueOf(operationType).isDbMergeFromSourceDB();
	}
	
	public static boolean isLoad(String operationType) {
		return EtlOperationType.valueOf(operationType).isLoad();
	}
	
	public static boolean isTransport(String operationType) {
		return EtlOperationType.valueOf(operationType).isTransport();
	}
	
	public static boolean isConsolidation(String operationType) {
		return EtlOperationType.valueOf(operationType).isConsolidation();
	}
	
	public static boolean isChangedRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).isChangedRecordsDetector();
	}
	
	public static boolean isNewRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).isNewRecordsDetector();
	}
	
	public static boolean isDbQuickExport(String operationType) {
		return EtlOperationType.valueOf(operationType).isDbQuickExport();
	}
	
	public static boolean isDbQuickLoad(String operationType) {
		return EtlOperationType.valueOf(operationType).isDbQuickLoad();
	}
	
	public static boolean isResolveConflicts(String operationType) {
		return EtlOperationType.valueOf(operationType).isResolveConflicts();
	}
	
	public static boolean isMissingRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).isMissingRecordsDetector();
	}
	
	public static boolean isOutdatedRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).isOutdatedRecordsDetector();
	}
	
	public static boolean isPhantomRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).isPhantomRecordsDetector();
	}
	
	public static boolean isDbQuickCopy(String operationType) {
		return EtlOperationType.valueOf(operationType).isDbQuickCopy();
	}
	
	public static boolean isSupportedOperation(String operationType) {
		return CommonUtilities.getInstance().getPosOnArray(EtlOperationType.values(),
		    EtlOperationType.valueOf(operationType)) >= 0;
	}
	
	public boolean isDatabasePreparation() {
		return this.equals(DATABASE_PREPARATION);
	}
	
	public boolean isPojoGeneration() {
		return this.equals(POJO_GENERATION);
	}
	
	public boolean isInconsistencySolver() {
		return this.equals(INCONSISTENCY_SOLVER);
	}
	
	public boolean isExport() {
		return this.equals(EXPORT);
	}
	
	public boolean isSynchronization() {
		return this.equals(DB_MERGE_FROM_JSON);
	}
	
	public boolean isLoad() {
		return this.equals(LOAD);
	}
	
	public boolean isTransport() {
		return this.equals(TRANSPORT);
	}
	
	public boolean isConsolidation() {
		return this.equals(CONSOLIDATION);
	}
	
	public boolean isChangedRecordsDetector() {
		return this.equals(CHANGED_RECORDS_DETECTOR);
	}
	
	public boolean isNewRecordsDetector() {
		return this.equals(NEW_RECORDS_DETECTOR);
	}
	
	public boolean isDbQuickExport() {
		return this.equals(QUICK_EXPORT);
	}
	
	public boolean isDbQuickLoad() {
		return this.equals(QUICK_LOAD);
	}
	
	public boolean isResolveConflicts() {
		return this.equals(RESOLVE_CONFLICTS);
	}
	
	public boolean isMissingRecordsDetector() {
		return this.equals(MISSING_RECORDS_DETECTOR);
	}
	
	public boolean isOutdatedRecordsDetector() {
		return this.equals(OUTDATED_RECORDS_DETECTOR);
	}
	
	public boolean isPhantomRecordsDetector() {
		return this.equals(PHANTOM_RECORDS_DETECTOR);
	}
	
	public boolean isDbQuickCopy() {
		return this.equals(DB_QUICK_COPY);
	}
	
	public boolean isDbMergeFromSourceDB() {
		return this.equals(DB_MERGE_FROM_SOURCE_DB);
	}
	
	public boolean isGenericOperation() {
		return this.equals(GENERIC_OPERATION);
	}
	
	public boolean isDetectGapesOperation() {
		return this.equals(DETECT_GAPES);
	}
	
	public boolean isEtl() {
		return this.equals(ETL) || this.equals(DB_EXTRACT);
	}
	
	public boolean isReEtl() {
		return this.equals(RE_ETL);
	}
	
	public boolean isDetectMIssingRecords() {
		return this.equals(DETECT_MISSING_RECORDS);
	}
	
	public boolean isSupportedOperation() {
		return CommonUtilities.getInstance().getPosOnArray(EtlOperationType.values(), this) >= 0;
	}
}
