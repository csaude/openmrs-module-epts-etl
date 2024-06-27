package org.openmrs.module.epts.etl.exceptions;

import java.io.File;

/**
 * This exception is thrown when there is an attempt to acess a Sync File which is being created and not alredy finished
 * @author jpboane
 *
 */
public class SyncFileNotFullGenerated extends EtlExceptionImpl{
	private static final long serialVersionUID = 1L;
	
	public SyncFileNotFullGenerated(File file) {
		super("The file " + file.getAbsolutePath() + " is on the creation process. It cannot be accessed by now!");
	}
}
