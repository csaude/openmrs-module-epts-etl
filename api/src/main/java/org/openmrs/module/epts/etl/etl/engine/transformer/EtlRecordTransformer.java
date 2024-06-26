package org.openmrs.module.epts.etl.etl.engine.transformer;

import java.sql.Connection;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public interface EtlRecordTransformer {
	
	EtlDatabaseObject transform(EtlDatabaseObject srcObject, DstConf dstConf, Connection srcConn, Connection dstConn);
}
