package org.openmrs.module.epts.etl.exceptions;

import org.openmrs.module.epts.etl.conf.interfaces.TableConfiguration;

public class MissingJoiningElementsException extends EtlExceptionImpl {
	
	private static final long serialVersionUID = 1505624913800886849L;
	
	public MissingJoiningElementsException(TableConfiguration tabConf, TableConfiguration joinedTable) {
		super("No join fields were difined between " + tabConf.getTableName() + " And " + joinedTable.getTableName());
		
	}
	
}
