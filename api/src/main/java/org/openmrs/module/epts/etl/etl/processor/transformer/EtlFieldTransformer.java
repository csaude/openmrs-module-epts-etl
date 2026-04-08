package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
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
	
	static final Pattern PARAM_PATTERN = Pattern.compile("@(\\w+)");
	
	FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject, EtlDatabaseObject transformedRecord,
	        List<EtlDatabaseObject> additionalSrcObjects, TransformableField field, Connection srcConn, Connection dstConn)
	        throws DBException, EtlTransformationException;
	
	default void performFieldTransformation(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, FieldsMapping field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		EtlTransformationException transformationException = null;
		
		FieldTransformingInfo fieldTransformingInfo = null;
		Object dstValue = null;
		
		if (field.isMapToNullValue()) {
			fieldTransformingInfo = new FieldTransformingInfo(field, null, null);
		} else {
			
			if (field.getSrcValue() == null && field.hasDataSourceName()) {
				field.setSrcValue("@" + field.getSrcField());
			}
			
			try {
				fieldTransformingInfo = this.transform(processor, srcObject, transformedRecord, additionalSrcObjects, field,
				    srcConn, dstConn);
			}
			catch (EtlTransformationException e) {
				transformationException = e;
			}
			
			if (fieldTransformingInfo == null) {
				fieldTransformingInfo = new FieldTransformingInfo(field, null, null);
			}
			
			dstValue = fieldTransformingInfo != null ? fieldTransformingInfo.getTransformedValue() : null;
			
			//Override the value if it should be override
			if (dstValue != null && field.shouldOverrideValue(dstValue)) {
				dstValue = null;
			}
			
			if (dstValue == null) {
				dstValue = tryToLoadDefaultValue(field, additionalSrcObjects);
				
				if (dstValue != null) {
					fieldTransformingInfo.setLoadedWithDefaultValue(true);
				}
			}
			
			fieldTransformingInfo.setTransformedValue(dstValue);
		}
		
		transformedRecord.setFieldValue(field.getDstField(), fieldTransformingInfo.getTransformedValue());
		
		transformedRecord.getField(field.getDstField()).setTransformingInfo(fieldTransformingInfo);
		
		if (dstValue == null && transformationException != null) {
			throw transformationException;
		}
		
	}
	
	public static Object tryToLoadDefaultValue(TransformableField transformableField, List<EtlDatabaseObject> srcObjects) {
		if (transformableField.getDefaultValue() != null) {
			return EtlFieldTransformer.tryToReplaceParametersOnSrcValue(srcObjects,
			    transformableField.getDefaultValue().toString());
		}
		
		return null;
		
	}
	
	public static Object tryToReplaceParametersOnSrcValue(final List<EtlDatabaseObject> srcObjects, final Object srcValue)
	        throws EtlTransformationException {
		
		if (!(srcValue instanceof String) || srcValue == null || srcValue.toString().isBlank())
			return srcValue;
		
		String srcValueAsString = srcValue.toString();
		
		if (!srcValueAsString.contains("@")) {
			return srcValueAsString;
		}
		
		String expression = srcValueAsString;
		
		Matcher matcher = PARAM_PATTERN.matcher(expression);
		
		StringBuffer buffer = new StringBuffer();
		
		while (matcher.find()) {
			
			String paramName = matcher.group(1);
			Object paramValue = null;
			
			boolean found = false;
			
			EtlConfiguration tc = srcObjects.get(0).getRelatedConfiguration().getRelatedEtlConf();
			
			paramValue = tc.getParamValue(paramName);
			
			if (paramValue != null) {
				found = true;
			} else
				for (EtlDatabaseObject srcObject : srcObjects) {
					
					try {
						paramValue = srcObject.getFieldValue(paramName);
						found = true;
						break;
					}
					catch (ForbiddenOperationException e) {
						// continue
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
				        srcObjects.get(0), ActionOnEtlException.ABORT_PROCESS);
			}
			
			if (srcValueAsString.equals("@" + paramName)) {
				return paramValue;
			}
			
			matcher.appendReplacement(buffer, Matcher.quoteReplacement(paramValue.toString()));
		}
		
		matcher.appendTail(buffer);
		
		return buffer.toString();
	}
	
	Connection getOverrideConnection();
	
	void setOverrideConnection(Connection overrideConnection);
	
	default boolean hasOverrideConnection() {
		return this.getOverrideConnection() != null;
	}
	
}
