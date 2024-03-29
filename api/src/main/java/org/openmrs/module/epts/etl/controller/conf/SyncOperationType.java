package org.openmrs.module.epts.etl.controller.conf;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * Suported operation type in sync process
 * 
 * @author jpboane
 */
public enum SyncOperationType {
	
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
	DB_COPY,
	DB_MERGE_FROM_SOURCE_DB,
	DB_QUICK_MERGE,
	GENERIC_OPERATION,
	DETECT_GAPES,
	ETL;
	
	public static boolean isEtl(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(ETL);
	}
	
	public static boolean isDetectGapesOperation(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(DETECT_GAPES);
	}
	
	public static boolean isDbCopyOperation(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(DB_COPY);
	}
	
	public static boolean isGenericOperation(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(GENERIC_OPERATION);
	}
	
	public static boolean isDBQuickMerge(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(DB_QUICK_MERGE);
	}
	
	public static boolean isDBMerge(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(DB_MERGE_FROM_SOURCE_DB);
	}
	
	public static boolean isDatabasePreparation(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(DATABASE_PREPARATION);
	}
	
	public static boolean isPojoGeneration(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(POJO_GENERATION);
	}
	
	public static boolean isInconsistencySolver(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(INCONSISTENCY_SOLVER);
	}
	
	public static boolean isExport(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(EXPORT);
	}
	
	public static boolean isSynchronization(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(DB_MERGE_FROM_JSON);
	}
	
	public static boolean isLoad(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(LOAD);
	}
	
	public static boolean isTransport(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(TRANSPORT);
	}
	
	public static boolean isConsolidation(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(CONSOLIDATION);
	}
	
	public static boolean isChangedRecordsDetector(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(CHANGED_RECORDS_DETECTOR);
	}
	
	public static boolean isNewRecordsDetector(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(NEW_RECORDS_DETECTOR);
	}
	
	public static boolean isDbQuickExport(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(QUICK_EXPORT);
	}
	
	public static boolean isDbQuickLoad(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(QUICK_LOAD);
	}
	
	public static boolean isResolveConflicts(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(RESOLVE_CONFLICTS);
	}
	
	public static boolean isMissingRecordsDetector(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(MISSING_RECORDS_DETECTOR);
	}
	
	public static boolean isOutdatedRecordsDetector(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(OUTDATED_RECORDS_DETECTOR);
	}
	
	public static boolean isPhantomRecordsDetector(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(PHANTOM_RECORDS_DETECTOR);
	}
	
	public static boolean isDbQuickCopy(String operationType) {
		return SyncOperationType.valueOf(operationType).equals(DB_QUICK_COPY);
	}
	
	public static boolean isSupportedOperation(String operationType) {
		return CommonUtilities.getInstance().getPosOnArray(SyncOperationType.values(),
		    SyncOperationType.valueOf(operationType)) >= 0;
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
	
	public boolean isDbCopyOperation() {
		return this.equals(DB_COPY);
	}
	
	public boolean isDetectGapesOperation() {
		return this.equals(DETECT_GAPES);
	}
	
	public boolean isEtl() {
		return this.equals(ETL);
	}
	
	public boolean isSupportedOperation() {
		return CommonUtilities.getInstance().getPosOnArray(SyncOperationType.values(), this) >= 0;
	}
}
