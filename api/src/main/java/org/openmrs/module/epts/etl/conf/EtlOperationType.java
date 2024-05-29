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
	DB_QUICK_MERGE,
	GENERIC_OPERATION,
	DETECT_GAPES,
	ETL,
	DB_EXTRACT,
	DETECT_MISSING_RECORDS;
	
	public static boolean isDetectMIssingRecords(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(DETECT_MISSING_RECORDS);
	}
	
	public static boolean isDbExtract(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(DB_EXTRACT);
	}
	
	public static boolean isEtl(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(ETL);
	}
	
	public static boolean isDetectGapesOperation(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(DETECT_GAPES);
	}
	
	public static boolean isGenericOperation(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(GENERIC_OPERATION);
	}
	
	public static boolean isDBQuickMerge(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(DB_QUICK_MERGE);
	}
	
	public static boolean isDBMerge(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(DB_MERGE_FROM_SOURCE_DB);
	}
	
	public static boolean isDatabasePreparation(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(DATABASE_PREPARATION);
	}
	
	public static boolean isPojoGeneration(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(POJO_GENERATION);
	}
	
	public static boolean isInconsistencySolver(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(INCONSISTENCY_SOLVER);
	}
	
	public static boolean isExport(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(EXPORT);
	}
	
	public static boolean isSynchronization(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(DB_MERGE_FROM_JSON);
	}
	
	public static boolean isLoad(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(LOAD);
	}
	
	public static boolean isTransport(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(TRANSPORT);
	}
	
	public static boolean isConsolidation(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(CONSOLIDATION);
	}
	
	public static boolean isChangedRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(CHANGED_RECORDS_DETECTOR);
	}
	
	public static boolean isNewRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(NEW_RECORDS_DETECTOR);
	}
	
	public static boolean isDbQuickExport(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(QUICK_EXPORT);
	}
	
	public static boolean isDbQuickLoad(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(QUICK_LOAD);
	}
	
	public static boolean isResolveConflicts(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(RESOLVE_CONFLICTS);
	}
	
	public static boolean isMissingRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(MISSING_RECORDS_DETECTOR);
	}
	
	public static boolean isOutdatedRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(OUTDATED_RECORDS_DETECTOR);
	}
	
	public static boolean isPhantomRecordsDetector(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(PHANTOM_RECORDS_DETECTOR);
	}
	
	public static boolean isDbQuickCopy(String operationType) {
		return EtlOperationType.valueOf(operationType).equals(DB_QUICK_COPY);
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
	
	public boolean isDBQuickMerge() {
		return this.equals(DB_QUICK_MERGE);
	}
	
	public boolean isGenericOperation() {
		return this.equals(GENERIC_OPERATION);
	}
	
	public boolean isDetectGapesOperation() {
		return this.equals(DETECT_GAPES);
	}
	
	public boolean isEtl() {
		return this.equals(ETL);
	}
	
	public boolean isDbExtract() {
		return this.equals(DB_EXTRACT);
	}
	
	public boolean isDetectMIssingRecords() {
		return this.equals(DETECT_MISSING_RECORDS);
	}
	
	public boolean isSupportedOperation() {
		return CommonUtilities.getInstance().getPosOnArray(EtlOperationType.values(), this) >= 0;
	}
}
