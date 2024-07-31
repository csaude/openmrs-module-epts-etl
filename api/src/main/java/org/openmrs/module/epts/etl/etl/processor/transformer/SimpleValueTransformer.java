package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class SimpleValueTransformer implements EtlFieldTransformer {
	
	private static SimpleValueTransformer defaultTransformer;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	private SimpleValueTransformer() {
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
	public void transform(EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> srcObjects,
	        FieldsMapping fieldsMapping, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException {
		
		if (fieldsMapping.isMapToNullValue() || fieldsMapping.getSrcValue().isEmpty()
		        || fieldsMapping.getSrcValue().equals("null")) {
			transformedRecord.setFieldValue(fieldsMapping.getDstField(), null);
		}
		
		String srcValueWithParamsReplaced = tryToReplaceParametersOnSrcValue(srcObjects, fieldsMapping);
		
		transformedRecord.setFieldValue(fieldsMapping.getDstField(), srcValueWithParamsReplaced);
	}
	
}
