package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Allow the custom field transformation.
 */
public interface EtlFieldTransformer {
	
	public static final CommonUtilities utilities = CommonUtilities.getInstance();
	
	static final String DEFAULT_TRANSFORMER = DefaultFieldTransformer.class.getCanonicalName();
	
	static final String ARITHMETIC_TRANSFORMER = ArithmeticFieldTransformer.class.getCanonicalName();
	
	static final String STRING_TRANSFORMER = StringTranformer.class.getCanonicalName();
	
	static final String SRC_VALUE_TRANSFORMER = SimpleValueTransformer.class.getCanonicalName();
	
	static final String MAPPING_TRANSFORMER = MappingFieldTransformer.class.getCanonicalName();
	
	static final String FAST_SQL_TRANSFORMER = FastSqlFieldTransformer.class.getCanonicalName();
	
	Object transform(List<EtlDatabaseObject> srcObjects, TransformableField field, Connection srcConn, Connection dstConn)
	        throws DBException, EtlTransformationException;
	
	default void transform(EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> srcObjects, FieldsMapping field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		if (field.getSrcValue() == null && field.hasDataSourceName()) {
			field.setSrcValue("@" + field.getSrcField());
		}
		
		Object dstValue = this.transform(srcObjects, field, srcConn, dstConn);
		
		//Override the value if it should be override
		if (dstValue != null && field.shouldOverrideValue(dstValue)) {
			dstValue = null;
		}
		
		//The mappingTransformer assumes destination value
		if (this instanceof MappingFieldTransformer) {
			transformedRecord.getField(field.getDstField()).setLoadedWithDefaultValue(true);
		}
		
		if (dstValue == null) {
			dstValue = tryToLoadDefaultValue(field, srcObjects);
			
			if (dstValue != null) {
				transformedRecord.getField(field.getDstField()).setLoadedWithDefaultValue(true);
			}
		}
		
		if (dstValue != null && utilities.isNumericType(transformedRecord.getFieldType(field.getDstField()))) {
			dstValue = utilities.parseValue(dstValue.toString(), transformedRecord.getFieldType(field.getDstField()));
		} else if (dstValue != null && utilities.isBooleanType(transformedRecord.getFieldType(field.getDstField()))) {
			dstValue = utilities.parseValue(dstValue.toString(), transformedRecord.getFieldType(field.getDstField()));
		}
		
		transformedRecord.setFieldValue(field.getDstField(), dstValue);
	}
	
	public static Object tryToLoadDefaultValue(TransformableField transformableField, List<EtlDatabaseObject> srcObjects) {
		if (transformableField.getDefaultValue() != null) {
			return EtlFieldTransformer.tryToReplaceParametersOnSrcValue(srcObjects,
			    transformableField.getDefaultValue().toString());
		}
		
		return null;
		
	}
	
	public static Object tryToReplaceParametersOnSrcValue(final List<EtlDatabaseObject> srcObjects, final String srcValue)
	        throws EtlTransformationException {
		
		if (srcValue == null || srcValue.isEmpty()) {
			throw new EtlTransformationException("The srcValue is empty", srcObjects.get(0), ActionOnEtlException.ABORT);
		}
		
		String expression = srcValue;
		
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
				
				if (paramValue != null) {
					found = true;
				}
			}
			
			if (!found) {
				throw new EtlTransformationException("Parameter '" + paramName + "' not found in source objects.",
				        srcObjects.get(0), ActionOnEtlException.ABORT);
			}
			
			//The param represent the whole srcValue
			if (srcValue.equals("@" + paramName)) {
				return paramValue;
			}
			
			matcher.appendReplacement(buffer, paramValue.toString());
		}
		
		matcher.appendTail(buffer);
		
		return buffer.toString();
	}
	
}
