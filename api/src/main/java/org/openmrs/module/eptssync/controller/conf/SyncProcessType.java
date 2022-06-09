package org.openmrs.module.eptssync.controller.conf;


public enum SyncProcessType {
	SOURCE_SYNC,
	DESTINATION_SYNC,
	DB_RE_SYNC,
	DB_QUICK_EXPORT,
	DB_QUICK_LOAD,
	DATA_RECONCILIATION;
	
	public static SyncProcessType find(String processType) {
		for (SyncProcessType type : values()) {
	        if (type.name().equalsIgnoreCase(processType)) {
	           return type;
	        }
	    }
	    
	    return null;
	}
	
	public static boolean isDBQuickLoad(String processType){
		return find(processType).equals(DB_QUICK_LOAD);
	}
	
	public static boolean isSourceSync(String processType){
		return find(processType).equals(SOURCE_SYNC);
	}
	
	public static boolean isDestinationSync(String processType){
		return find(processType).equals(DESTINATION_SYNC);
	}
	
	public static boolean isDBResync(String processType){
		return find(processType).equals(DB_RE_SYNC);
	}
	
	
	public static boolean isDBQuickExport(String processType){
		return find(processType).equals(DB_QUICK_EXPORT);
	}
	
	
	
	public static boolean isDataReconciliation(String processType){
		return find(processType).equals(DATA_RECONCILIATION);
	}
	
	public static boolean isSupportedProcessType(String processType) {
		return find(processType) != null;
	}
}
