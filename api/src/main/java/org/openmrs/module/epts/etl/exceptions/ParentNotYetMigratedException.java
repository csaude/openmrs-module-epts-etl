package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class ParentNotYetMigratedException extends DBException {
	
	private static final long serialVersionUID = 1L;
	
	public ParentNotYetMigratedException(DBException e) {
		super("On or more parents not yet migrated", e);
	}
	
	public ParentNotYetMigratedException(Integer parentId, String parentTable, String originAppLocationConde,
	    DBException e) {
		super("Parent not yet migrated! Parent: [table: " + parentTable + ", id: " + parentId + ", from:"
		        + originAppLocationConde + "]", e);
	}
	
	public ParentNotYetMigratedException(String msg, DBException e) {
		super(msg, e);
	}
	
	@Override
	public boolean isDuplicatePrimaryOrUniqueKeyException() throws DBException {
		return true;
	}
	
	@Override
	public boolean isIntegrityConstraintViolationException() throws DBException {
		return false;
	}
}
