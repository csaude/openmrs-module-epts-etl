package org.openmrs.module.epts.etl.dbquickexport.model;

import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.monitor.Engine;

public class DBQuickExportSearchParams extends EtlDatabaseObjectSearchParams {
	
	public DBQuickExportSearchParams(Engine<EtlDatabaseObject> engine, ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine, limits);
		
		setOrderByFields(getSrcTableConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
}
