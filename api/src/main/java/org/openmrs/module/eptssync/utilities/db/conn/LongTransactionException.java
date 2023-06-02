package org.openmrs.module.eptssync.utilities.db.conn;

public class LongTransactionException extends RuntimeException{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LongTransactionException() {
		super("The current transaction is taking more that excepted time");
	}
	
}
