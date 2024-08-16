package org.openmrs.module.epts.etl.conf.interfaces;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.model.EtlDatabaseObject;

public interface JavaObjectDataSourceGenerator {
	
	public Map<String, Object> generateObjectFields(List<EtlDatabaseObject> avaliableSrcObjects, Connection srcConn,
	        Connection dstConn);
}
