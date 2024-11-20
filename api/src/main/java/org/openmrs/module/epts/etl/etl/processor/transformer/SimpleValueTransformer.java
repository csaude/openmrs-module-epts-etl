package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class SimpleValueTransformer implements EtlFieldTransformer {
	
	private static SimpleValueTransformer defaultTransformer;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	public SimpleValueTransformer() {
	}
	
	public static SimpleValueTransformer getInstance() {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK_STRING) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			defaultTransformer = new SimpleValueTransformer();
			
			return defaultTransformer;
		}
	}
	
	@Override
	public Object transform(List<EtlDatabaseObject> srcObjects, TransformableField field, Connection srcConn,
	        Connection dstConn) throws DBException, ForbiddenOperationException {
		
		if (field == null || field.getValueToTransform().toString().isEmpty()
		        || field.getValueToTransform().equals("null")) {
			return null;
		} else {
			return tryToReplaceParametersOnSrcValue(srcObjects, field.getValueToTransform());
		}
	}
	
}
