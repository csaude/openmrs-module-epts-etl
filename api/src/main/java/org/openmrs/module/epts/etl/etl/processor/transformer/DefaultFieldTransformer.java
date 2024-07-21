package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * The default field transformer which is based on fields mapping
 */
public class DefaultFieldTransformer implements EtlFieldTransformer {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
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
	public void transform(EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> srcObjects,
	        FieldsMapping fieldsMapping, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException {
		
		Object dstValue = null;
		
		if (fieldsMapping.isMapToNullValue()) {
			dstValue = null;
		} else if (fieldsMapping.getSrcValue() != null) {
			
			//We assume that all the parameters refers to configuration params because if not then the value will be configured as "srcField"
			if (fieldsMapping.getSrcValue().startsWith("@")) {
				String paramName = utilities.removeCharactersOnString(fieldsMapping.getSrcValue(), "@");
				
				EtlConfiguration conf = srcObjects.get(0).getRelatedConfiguration().getRelatedEtlConf();
				
				fieldsMapping.setSrcValue(conf.getParamValue(paramName));
			}
			
			dstValue = utilities.parseValue(fieldsMapping.getSrcValue(),
			    transformedRecord.getFieldType(fieldsMapping.getDstField()));
		} else {
			
			boolean found = false;
			
			for (EtlDatabaseObject srcObject : srcObjects) {
				if (fieldsMapping.getDataSourceName().equals(srcObject.getRelatedConfiguration().getAlias())) {
					found = true;
					
					try {
						dstValue = srcObject.getFieldValue(fieldsMapping.getSrcField());
					}
					catch (ForbiddenOperationException e) {
						dstValue = srcObject.getFieldValue(fieldsMapping.getSrcFieldAsClassField());
					}
				}
			}
			
			if (!found) {
				throw new ForbiddenOperationException(
				        "The field '" + fieldsMapping.getSrcField() + " does not belong to any configured source table");
			}
		}
		
		transformedRecord.setFieldValue(fieldsMapping.getDstField(), dstValue);
	}
	
}
