package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public interface JavaObjectFieldsValuesGenerator {
	
	public Map<String, Object> generateObjectFields(ObjectDataSource dataSource, List<EtlDatabaseObject> avaliableSrcObjects,
	        Connection srcConn, Connection dstConn) throws DBException;
	
}
