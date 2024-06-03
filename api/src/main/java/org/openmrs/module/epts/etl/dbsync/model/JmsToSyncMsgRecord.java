package org.openmrs.module.epts.etl.dbsync.model;

import org.openmrs.module.epts.etl.conf.AbstractTableConfiguration;
import org.openmrs.module.epts.etl.etl.model.EtlRecord;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public class JmsToSyncMsgRecord extends EtlRecord{

	public JmsToSyncMsgRecord(EtlDatabaseObject record, AbstractTableConfiguration config, boolean writeOperationHistory) {
		super(record, config, writeOperationHistory);
	}
	
	
	
}
