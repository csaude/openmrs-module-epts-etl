package org.openmrs.module.eptssync.utilities.db.conn;

import java.util.Map;
import java.util.Map.Entry;

import org.openmrs.module.eptssync.controller.conf.ParentRefInfo;
import org.openmrs.module.eptssync.exceptions.SyncExeption;
import org.openmrs.module.eptssync.model.openmrs.generic.OpenMRSObject;
import org.openmrs.module.eptssync.utilities.CommonUtilities;

/**
 * Indicate that record is in inconsisten state which indicate that the record is orphaned of one or more parents
 * 
 * @author jpboane
 *
 */
public class InconsistentStateException extends SyncExeption {
	private static final long serialVersionUID = 4995578586770680131L;

	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	Map<ParentRefInfo, Integer> missingParents;
	
	public InconsistentStateException(){
		super("The record is in inconsistent state. There are missing some parents");
	}
	
	public InconsistentStateException(OpenMRSObject obj, Map<ParentRefInfo, Integer> missingParents){
		super(generateMissingInfo(obj, missingParents));
		
		this.missingParents = missingParents;
	}
	
	public static String generateMissingInfo(OpenMRSObject obj, Map<ParentRefInfo, Integer> missingParents) {
		String missingInfo = "";
		
		for (Entry<ParentRefInfo, Integer> missing : missingParents.entrySet()) {
			missingInfo = utilities.concatStrings(missingInfo, "[" +missing.getKey().getTableName() + ": " + missing.getValue() + "]", ";");
		}
		
		return "The record [" + obj.generateTableName() + " = " + obj.getObjectId() + "] is in inconsistent state. There are missing these parents: " + missingInfo;
	}	
}
