package org.openmrs.module.epts.etl.conf.types;

/**
 * Defines how the ETL process should behave when a field transformation results in a null value.
 * <p>
 * This behavior is applied at field level and overrides the default ETL behavior when a null value
 * is produced during transformation.
 * </p>
 * <p>
 * Typical use cases include:
 * </p>
 * <ul>
 * <li>Enforcing mandatory fields</li>
 * <li>Allowing optional fields to be null</li>
 * <li>Controlling whether null values should interrupt or flag the ETL process</li>
 * </ul>
 */
public enum EtlNullBehavior {
	
	/**
	 * The transformation is considered valid even if the resulting value is null.
	 * <p>
	 * No action is taken and the null value is assigned to the destination field.
	 * </p>
	 */
	ALLOW,
	
	/**
	 * The transformation is considered invalid if the resulting value is null.
	 * <p>
	 * The record will be marked as failed, but the ETL process will continue.
	 * </p>
	 */
	MARK_RECORD_AS_FAILED,
	
	/**
	 * The transformation is considered invalid if the resulting value is null.
	 * <p>
	 * An exception will be thrown and the ETL process will be interrupted according to the
	 * configured exception handling strategy.
	 * </p>
	 */
	ABORT_PROCESS;
	
	public boolean abort() {
		return this.equals(ABORT_PROCESS);
	}
	
	public boolean allow() {
		return this.equals(ALLOW);
	}
	
	public boolean markAsFailed() {
		return this.equals(MARK_RECORD_AS_FAILED);
	}
}
