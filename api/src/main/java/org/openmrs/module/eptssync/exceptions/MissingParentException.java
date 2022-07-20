package org.openmrs.module.eptssync.exceptions;

public class MissingParentException extends SyncExeption {
	private static final long serialVersionUID = 1L;

	public MissingParentException() {
		super("On or more parents are missing");
	}
	
	public MissingParentException(int parentId, String parentTable, String originAppLocationConde) {
		super("Missing parent! Parent: [table: " + parentTable +", id: " + parentId + ", from:" +originAppLocationConde +  "]");
	}
	
	public MissingParentException(String msg) {
		super(msg);
	}	
}
