package org.openmrs.module.epts.etl.dbquickexport.model;

import org.openmrs.module.epts.etl.engine.Engine;
import org.openmrs.module.epts.etl.engine.record_intervals_manager.ThreadRecordIntervalsManager;
import org.openmrs.module.epts.etl.etl.model.EtlDatabaseObjectSearchParams;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public class DBQuickExportSearchParams extends EtlDatabaseObjectSearchParams {
	
	public DBQuickExportSearchParams(Engine<EtlDatabaseObject> engine, ThreadRecordIntervalsManager<EtlDatabaseObject> limits) {
		super(engine, limits);
		
		setOrderByFields(getSrcConf().getPrimaryKey().parseFieldNamesToArray());
	}
	
}
