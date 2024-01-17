package org.openmrs.module.epts.etl.controller.conf;

import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public enum SyncProcessType {
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
	DB_COPY,
	DETECT_GAPES_ON_DB_TABLES;
	
	public boolean isDetectGapesOnDbTables(){
		return  this.equals(DETECT_GAPES_ON_DB_TABLES);
	}
	
	public boolean isGenericProcess(){
		return  this.equals(GENERIC_PROCESS);
	}
	
	public boolean isDbCopy(){
		return  this.equals(DB_COPY);
	}
	
	public boolean isdDBInconsistencyCheck(){
		return  this.equals(DB_INCONSISTENCY_CHECK);
	}
	
	public boolean isDBQuickMerge(){
		return  this.equals(DB_QUICK_MERGE);
	}
	
	public boolean isQuickMergeWithEntityGeneration(){
		return  this.equals(DB_QUICK_MERGE_WITH_ENTITY_GENERATION);
	}
	
	public boolean isQuickMergeWithDatabaseGeneration(){
		return  this.equals(DB_QUICK_MERGE_WITH_DATABASE_GENERATION);
	}
		
	public boolean isDBQuickCopy(){
		return  this.equals(DB_QUICK_COPY);
	}
	
	public boolean isDBQuickLoad(){
		return  this.equals(DB_QUICK_LOAD); 
	}
	
	public boolean isSourceSync(){
		return  this.equals(SOURCE_SYNC);
	}
	
	public boolean isDataBaseMergeFromJSON(){
		return  this.equals(DATABASE_MERGE_FROM_JSON);
	}
	
	public boolean isDBResync(){
		return  this.equals(DB_RE_SYNC);
	}
	
	public boolean isDBQuickExport(){
		return  this.equals(DB_QUICK_EXPORT);
	}
	
	public boolean isDataReconciliation(){
		return  this.equals(DATA_RECONCILIATION);
	}
	
	public boolean isDataBaseMergeFromSourceDB(){
		return  this.equals(DATABASE_MERGE_FROM_SOURCE_DB);
	}
	
	public boolean isSupportedProcessType() {
		return CommonUtilities.getInstance().getPosOnArray(SyncProcessType.values(), this) >= 0;
	}
	
	public static boolean isDBQuickCopy(String processType){
		return  SyncProcessType.valueOf(processType).isDBQuickCopy();
	}
	
	public static boolean isDBQuickLoad(String processType){
		return  SyncProcessType.valueOf(processType).isDBQuickLoad(); 
	}
	
	public static boolean isSourceSync(String processType){
		return  SyncProcessType.valueOf(processType).isSourceSync();
	}
	
	public static boolean isDataBaseMergeFromJSON(String processType){
		return  SyncProcessType.valueOf(processType).isDataBaseMergeFromJSON();
	}
	
	public static boolean isDBResync(String processType){
		return  SyncProcessType.valueOf(processType).isDBResync();
	}
	
	public static boolean isDBQuickExport(String processType){
		return  SyncProcessType.valueOf(processType).isDBQuickExport();
	}
	
	public static boolean isDataReconciliation(String processType){
		return  SyncProcessType.valueOf(processType).isDataReconciliation();
	}
	
	public static boolean isDataBasesMergeFromSourceDB(String processType){
		return  SyncProcessType.valueOf(processType).isDataBaseMergeFromSourceDB();
	}
	
	public static boolean isQuickMergeWithoutEntityGeneration(String processType){
		return  SyncProcessType.valueOf(processType).isDBQuickMerge();
	}
	
	public static boolean isQuickMergeWithEntityGeneration(String processType){
		return  SyncProcessType.valueOf(processType).isQuickMergeWithEntityGeneration();
	}
	
	public static boolean isDBInconsistencyCheck(String processType){
		return  SyncProcessType.valueOf(processType).isdDBInconsistencyCheck();
	}
	
	public static boolean isGenericProcess(String processType){
		return  SyncProcessType.valueOf(processType).isGenericProcess();
	}
	
	public static boolean isDbCopy(String processType){
		return  SyncProcessType.valueOf(processType).isDbCopy();
	}
	
	public static boolean isDetectGapesOnDbTables(String processType){
		return  SyncProcessType.valueOf(processType).isDetectGapesOnDbTables();
	}
	
	public static boolean isSupportedProcessType(String processType) {
		return SyncProcessType.valueOf(processType).isSupportedProcessType();
	}
}
