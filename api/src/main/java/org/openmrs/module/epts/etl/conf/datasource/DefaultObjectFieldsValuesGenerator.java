package org.openmrs.module.epts.etl.conf.datasource;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.interfaces.JavaObjectFieldsValuesGenerator;
import org.openmrs.module.epts.etl.conf.interfaces.ObjectDataSource;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class DefaultObjectFieldsValuesGenerator implements JavaObjectFieldsValuesGenerator {
	
	private static JavaObjectFieldsValuesGenerator defaultGenerator;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	@Override
	public Map<String, Object> generateObjectFields(ObjectDataSource dataSource, List<EtlDatabaseObject> avaliableSrcObjects,
	        Connection srcConn, Connection dstConn) throws DBException, ForbiddenOperationException {
		
		Map<String, Object> map = new HashMap<>();
		
		for (DataSourceField field : dataSource.getObjectFields()) {
			Object o = field.getTransformerInstance().transform(avaliableSrcObjects, field, srcConn, dstConn);
			
			map.put(field.getName(), o);
		}
		
		return map;
	}
	
	public static JavaObjectFieldsValuesGenerator getInstance() {
		if (defaultGenerator != null)
			return defaultGenerator;
		
		synchronized (LOCK_STRING) {
			if (defaultGenerator != null)
				return defaultGenerator;
			
			defaultGenerator = new DefaultObjectFieldsValuesGenerator();
			
			return defaultGenerator;
		}
	}
	
}
