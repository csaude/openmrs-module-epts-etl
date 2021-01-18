package org.openmrs.module.eptssync.exceptions;

/**
 * This exception indicates a metadata Inconsistence state.
 * <pre>
 * 	An metada inconsistent state occure when one of this situations is observed:
 * 		1. The record from the remote site share a uuid from the central DB but both of them as diffents ID.
 * 		2. The record from the remote site share a "ID" from the central DB but both of them as diffents UUID.
 * </pre>
 * 
 * The metadata in Inconsistence state will be marked on the correspondent Stage table for averiguations by the data administrator. But all
 * parents related to this will be sincronized
 * 
 * @author jpboane
 *
 */

public class MetadataInconsistentException extends SyncExeption{
	private static final long serialVersionUID = -2623572759817537893L;

	public MetadataInconsistentException(String msg) {
		super(msg);
	}
}
