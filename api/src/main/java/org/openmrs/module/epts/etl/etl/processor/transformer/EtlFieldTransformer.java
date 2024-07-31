package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Allow the custom field transformation.
 */
public interface EtlFieldTransformer {
	
	static final String DEFAULT_TRANSFORMER = DefaultFieldTransformer.class.getCanonicalName();
	
	static final String ARITHMETIC_TRANSFORMER = ArithmeticFieldTransformer.class.getCanonicalName();
	
	static final String STRING_TRANSFORMER = StringTranformer.class.getCanonicalName();
	
	static final String SRC_VALUE_TRANSFORMER = SimpleValueTransformer.class.getCanonicalName();
	
	/**
	 * Generates the transformed value for a given dtsField and set it to dstObject.
	 * 
	 * @param transformedRecord the transformed record were the field will be set to
	 * @param srcObjects the available src objects
	 * @param fieldsMapping the field mapping containing the mapping information
	 * @param srcConn
	 * @param dstConn
	 * @return the transformed value for dstField
	 */
	void transform(EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> srcObjects, FieldsMapping fieldsMapping,
	        Connection srcConn, Connection dstConn) throws DBException, ForbiddenOperationException;
	
	default String tryToReplaceParametersOnSrcValue(final List<EtlDatabaseObject> srcObjects,
	        final FieldsMapping fieldsMapping) throws ForbiddenOperationException {
		
		if (fieldsMapping.getSrcValue() == null || fieldsMapping.getSrcValue().isEmpty()) {
			throw new ForbiddenOperationException("The srcValue is empty");
		}
		
		String expression = fieldsMapping.getSrcValue();
		
		Pattern pattern = Pattern.compile("@(\\w+)");
		Matcher matcher = pattern.matcher(expression);
		
		StringBuffer buffer = new StringBuffer();
		
		while (matcher.find()) {
			String paramName = matcher.group(1);
			Object paramValue = null;
			
			boolean found = false;
			
			for (EtlDatabaseObject srcObject : srcObjects) {
				
				try {
					paramValue = srcObject.getFieldValue(paramName);
					
					found = true;
					
					break;
				}
				catch (ForbiddenOperationException e) {
					//Continue
				}
				
			}
			
			if (!found) {
				EtlConfiguration conf = srcObjects.get(0).getRelatedConfiguration().getRelatedEtlConf();
				
				paramValue = conf.getParamValue(paramName);
			}
			
			if (paramValue == null) {
				throw new ForbiddenOperationException("Parameter '" + paramName + "' not found in source objects.");
			}
			
			matcher.appendReplacement(buffer, paramValue.toString());
		}
		
		matcher.appendTail(buffer);
		
		return buffer.toString();
	}
	
}
