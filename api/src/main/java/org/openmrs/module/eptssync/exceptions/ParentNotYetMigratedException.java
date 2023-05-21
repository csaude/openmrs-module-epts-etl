package org.openmrs.module.eptssync.exceptions;

public class ParentNotYetMigratedException extends SyncExeption {
	private static final long serialVersionUID = 1L;

	public ParentNotYetMigratedException() {
		super("On or more parents not yet migrated");
	}
	
	public ParentNotYetMigratedException(Integer parentId, String parentTable, String originAppLocationConde) {
		super("Parent not yet migrated! Parent: [table: " + parentTable +", id: " + parentId + ", from:" +originAppLocationConde +  "]");
	}
	
	public ParentNotYetMigratedException(String msg) {
		super(msg);
	}	
}
