package org.openmrs.module.epts.etl.utilities.db.conn;

import java.util.Map;
import java.util.Map.Entry;

import org.openmrs.module.epts.etl.conf.ParentTableImpl;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

/**
 * Indicate that dstRecord is in inconsisten state which indicate that the dstRecord is orphaned of one or
 * more parents
 * 
 * @author jpboane
 */
public class InconsistentStateException extends EtlExceptionImpl {
	
	private static final long serialVersionUID = 4995578586770680131L;
	
	public static CommonUtilities utilities = CommonUtilities.getInstance();
	
	Map<ParentTableImpl, Integer> missingParents;
	
	public InconsistentStateException() {
		super("The dstRecord is in inconsistent state. There are missing some parents");
	}
	
	public InconsistentStateException(EtlDatabaseObject obj, Map<ParentTableImpl, Integer> missingParents) {
		super(generateMissingInfo(obj, missingParents));
		
		this.missingParents = missingParents;
	}
	
	public static String generateMissingInfo(EtlDatabaseObject obj, Map<ParentTableImpl, Integer> missingParents) {
		String missingInfo = "";
		
		for (Entry<ParentTableImpl, Integer> missing : missingParents.entrySet()) {
			missingInfo = utilities.concatStringsWithSeparator(missingInfo,
			    "[" + missing.getKey().getTableName() + ": " + missing.getValue() + "]", ";");
		}
		
		return "The dstRecord [" + obj.generateTableName() + " = " + obj.getObjectId()
		        + "] is in inconsistent state. There are missing these parents: " + missingInfo;
	}
}
