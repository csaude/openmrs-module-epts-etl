package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * The default field transformer which is based on fields mapping
 */
public class DefaultFieldTransformer implements EtlFieldTransformer {
	
	private static DefaultFieldTransformer defaultTransformer;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	private DefaultFieldTransformer() {
	}
	
	public static DefaultFieldTransformer getInstance() {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK_STRING) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			defaultTransformer = new DefaultFieldTransformer();
			
			return defaultTransformer;
		}
	}
	
	@Override
	public Object transform(List<EtlDatabaseObject> srcObjects, TransformableField field, Connection srcConn,
	        Connection dstConn) throws DBException, ForbiddenOperationException {
		
		Object dstValue = null;
		
		if (field.getValueToTransform() == null) {
			dstValue = null;
		} else {
			
			boolean found = false;
			
			for (EtlDatabaseObject srcObject : srcObjects) {
				if (field.getDataSourceName().equals(srcObject.getRelatedConfiguration().getAlias())) {
					found = true;
					
					String fieldNameInSnakeCase = utilities.parsetoSnakeCase(field.getName());
					String fieldNameInCameCase = utilities.parsetoCamelCase(field.getName());
					
					try {
						dstValue = srcObject.getFieldValue(fieldNameInSnakeCase);
					}
					catch (ForbiddenOperationException e) {
						dstValue = srcObject.getFieldValue(fieldNameInCameCase);
					}
				}
			}
			
			if (!found) {
				throw new ForbiddenOperationException(
				        "The field '" + field.getName() + " does not belong to any configured source table");
			}
		}
		
		return dstValue;
	}
	
}
