package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.datasource.ObjectDataSource;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.etl.processor.transformer.FieldTransformingInfo;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public interface JavaObjectFieldsValuesGenerator {
	
	Map<String, FieldTransformingInfo> generateObjectFields(EtlProcessor processor, EtlDatabaseObject srcObject,
	        ObjectDataSource dataSource, List<EtlDatabaseObject> avaliableSrcObjects, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException;
	
}
