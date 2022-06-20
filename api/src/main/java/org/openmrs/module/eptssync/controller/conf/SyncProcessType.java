package org.openmrs.module.eptssync.controller.conf;

import org.openmrs.module.eptssync.utilities.CommonUtilities;

public enum SyncProcessType {
	SOURCE_SYNC,
	DESTINATION_SYNC,
	DB_RE_SYNC,
	DB_QUICK_EXPORT,
	DB_QUICK_LOAD,
	DATA_RECONCILIATION,
	DB_QUICK_COPY;
	
	public static boolean isDBQuickCopy(String processType){
		return  SyncProcessType.valueOf(processType).equals(DB_QUICK_COPY);
	}
	
	public static boolean isDBQuickLoad(String processType){
		return  SyncProcessType.valueOf(processType).equals(DB_QUICK_LOAD); 
	}
	
	public static boolean isSourceSync(String processType){
		return  SyncProcessType.valueOf(processType).equals(SOURCE_SYNC);
	}
	
	public static boolean isDestinationSync(String processType){
		return  SyncProcessType.valueOf(processType).equals(DESTINATION_SYNC);
	}
	
	public static boolean isDBResync(String processType){
		return  SyncProcessType.valueOf(processType).equals(DB_RE_SYNC);
	}
	
	public static boolean isDBQuickExport(String processType){
		return  SyncProcessType.valueOf(processType).equals(DB_QUICK_EXPORT);
	}
	
	public static boolean isDataReconciliation(String processType){
		return  SyncProcessType.valueOf(processType).equals(DATA_RECONCILIATION);
	}
	
	public static boolean isSupportedProcessType(String processType) {
		return CommonUtilities.getInstance().getPosOnArray(SyncProcessType.values(), SyncProcessType.valueOf(processType)) >= 0;
	}
}
