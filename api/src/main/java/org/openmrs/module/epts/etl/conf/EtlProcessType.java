package org.openmrs.module.epts.etl.conf;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public enum EtlProcessType {
	
	SOURCE_SYNC,
	DATABASE_MERGE_FROM_JSON,
	DB_RE_SYNC,
	DB_QUICK_EXPORT,
	DB_QUICK_LOAD,
	DATA_RECONCILIATION,
	DB_QUICK_COPY,
	DATABASE_MERGE_FROM_SOURCE_DB,
	DB_QUICK_MERGE,
	DB_QUICK_MERGE_WITH_ENTITY_GENERATION,
	DB_QUICK_MERGE_WITH_DATABASE_GENERATION,
	DB_INCONSISTENCY_CHECK,
	GENERIC_PROCESS,
	DETECT_GAPES_ON_DB_TABLES,
	POJO_GENERATION,
	ETL,
	RE_ETL,
	DB_EXTRACT,
	DETECT_MISSING_RECORDS;
	
	public boolean isDetectMissingRecords() {
		return this.equals(DETECT_MISSING_RECORDS);
	}
	
	public boolean isDbExtract() {
		return this.equals(DB_EXTRACT);
	}
	
	public boolean isReEtl() {
		return this.equals(RE_ETL);
	}
	
	
	public boolean isEtl() {
		return this.equals(ETL);
	}
	
	public boolean isPojoGeneration() {
		return this.equals(POJO_GENERATION);
	}
	
	public boolean isDetectGapesOnDbTables() {
		return this.equals(DETECT_GAPES_ON_DB_TABLES);
	}
	
	public boolean isGenericProcess() {
		return this.equals(GENERIC_PROCESS);
	}
	
	public boolean isdDBInconsistencyCheck() {
		return this.equals(DB_INCONSISTENCY_CHECK);
	}
	
	public boolean isDBQuickMerge() {
		return this.equals(DB_QUICK_MERGE);
	}
	
	public boolean isQuickMergeWithEntityGeneration() {
		return this.equals(DB_QUICK_MERGE_WITH_ENTITY_GENERATION);
	}
	
	public boolean isQuickMergeWithDatabaseGeneration() {
		return this.equals(DB_QUICK_MERGE_WITH_DATABASE_GENERATION);
	}
	
	public boolean isDBQuickCopy() {
		return this.equals(DB_QUICK_COPY);
	}
	
	public boolean isDBQuickLoad() {
		return this.equals(DB_QUICK_LOAD);
	}
	
	public boolean isSourceSync() {
		return this.equals(SOURCE_SYNC);
	}
	
	public boolean isDataBaseMergeFromJSON() {
		return this.equals(DATABASE_MERGE_FROM_JSON);
	}
	
	public boolean isDBResync() {
		return this.equals(DB_RE_SYNC);
	}
	
	public boolean isDBQuickExport() {
		return this.equals(DB_QUICK_EXPORT);
	}
	
	public boolean isDataReconciliation() {
		return this.equals(DATA_RECONCILIATION);
	}
	
	public boolean isDataBaseMergeFromSourceDB() {
		return this.equals(DATABASE_MERGE_FROM_SOURCE_DB);
	}
	
	public boolean isSupportedProcessType() {
		return CommonUtilities.getInstance().getPosOnArray(EtlProcessType.values(), this) >= 0;
	}
	
	public static boolean isDBQuickCopy(String processType) {
		return EtlProcessType.valueOf(processType).isDBQuickCopy();
	}
	
	public static boolean isDBQuickLoad(String processType) {
		return EtlProcessType.valueOf(processType).isDBQuickLoad();
	}
	
	public static boolean isSourceSync(String processType) {
		return EtlProcessType.valueOf(processType).isSourceSync();
	}
	
	public static boolean isDataBaseMergeFromJSON(String processType) {
		return EtlProcessType.valueOf(processType).isDataBaseMergeFromJSON();
	}
	
	public static boolean isDBResync(String processType) {
		return EtlProcessType.valueOf(processType).isDBResync();
	}
	
	public static boolean isDBQuickExport(String processType) {
		return EtlProcessType.valueOf(processType).isDBQuickExport();
	}
	
	public static boolean isDataReconciliation(String processType) {
		return EtlProcessType.valueOf(processType).isDataReconciliation();
	}
	
	public static boolean isDataBasesMergeFromSourceDB(String processType) {
		return EtlProcessType.valueOf(processType).isDataBaseMergeFromSourceDB();
	}
	
	public static boolean isQuickMergeWithoutEntityGeneration(String processType) {
		return EtlProcessType.valueOf(processType).isDBQuickMerge();
	}
	
	public static boolean isQuickMergeWithEntityGeneration(String processType) {
		return EtlProcessType.valueOf(processType).isQuickMergeWithEntityGeneration();
	}
	
	public static boolean isDBInconsistencyCheck(String processType) {
		return EtlProcessType.valueOf(processType).isdDBInconsistencyCheck();
	}
	
	public static boolean isGenericProcess(String processType) {
		return EtlProcessType.valueOf(processType).isGenericProcess();
	}
	
	public static boolean isEtl(String processType) {
		return EtlProcessType.valueOf(processType).isEtl();
	}
	
	public static boolean isReEtl(String processType) {
		return EtlProcessType.valueOf(processType).isReEtl();
	}
	
	public static boolean isDetectGapesOnDbTables(String processType) {
		return EtlProcessType.valueOf(processType).isDetectGapesOnDbTables();
	}
	
	public static boolean isSupportedProcessType(String processType) {
		return EtlProcessType.valueOf(processType).isSupportedProcessType();
	}
	
	public static boolean isDetectMissingRecords(String processType) {
		return EtlProcessType.valueOf(processType).isDetectMissingRecords();
	}
	
}
