package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;

import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public interface EtlFieldTransformer {
	
	Object transform(EtlDatabaseObject srcObject, String srcField, String dstField, Connection srcConn, Connection dstConn);
}
