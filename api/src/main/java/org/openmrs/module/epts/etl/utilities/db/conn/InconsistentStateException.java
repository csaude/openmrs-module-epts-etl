package org.openmrs.module.epts.etl.utilities.db.conn;

import java.util.Map;
import java.util.Map.Entry;

import org.openmrs.module.epts.etl.conf.RefInfo;
import org.openmrs.module.epts.etl.exceptions.SyncExeption;
import org.openmrs.module.epts.etl.model.pojo.generic.DatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * Indicate that record is in inconsisten state which indicate that the record is orphaned of one or
 * more parents
 * 
 * @author jpboane
 */
public class InconsistentStateException extends SyncExeption {
	
	private static final long serialVersionUID = 4995578586770680131L;
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	Map<RefInfo, Integer> missingParents;
	
	public InconsistentStateException() {
		super("The record is in inconsistent state. There are missing some parents");
	}
	
	public InconsistentStateException(DatabaseObject obj, Map<RefInfo, Integer> missingParents) {
		super(generateMissingInfo(obj, missingParents));
		
		this.missingParents = missingParents;
	}
	
	public static String generateMissingInfo(DatabaseObject obj, Map<RefInfo, Integer> missingParents) {
		String missingInfo = "";
		
		for (Entry<RefInfo, Integer> missing : missingParents.entrySet()) {
			missingInfo = utilities.concatStringsWithSeparator(missingInfo,
			    "[" + missing.getKey().getParentTableName() + ": " + missing.getValue() + "]", ";");
		}
		
		return "The record [" + obj.generateTableName() + " = " + obj.getObjectId()
		        + "] is in inconsistent state. There are missing these parents: " + missingInfo;
	}
}
