package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class StringTranformer implements EtlFieldTransformer {
	
	@Override
	public void transform(EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> srcObjects,
	        FieldsMapping fieldsMapping, Connection srcConn, Connection dstConn)
	        throws DBException, ForbiddenOperationException {
		
		tryToReplaceParametersOnExpression(srcObjects, fieldsMapping);
		
		Object dstValue = null;
		
		if (fieldsMapping.isMapToNullValue()) {
			dstValue = null;
		} else if (fieldsMapping.getSrcValue() != null) {
			
			try {
				dstValue = evaluateStringExpression(fieldsMapping.getSrcValue());
			}
			catch (Exception e) {
				throw new EtlExceptionImpl("Failed to evaluate the string expression: " + fieldsMapping.getSrcValue(), e);
			}
		} else {
			throw new ForbiddenOperationException("Source value must be provided for String transformation.");
		}
		
		transformedRecord.setFieldValue(fieldsMapping.getDstField(), dstValue);
	}
	
	private void tryToReplaceParametersOnExpression(List<EtlDatabaseObject> srcObjects, FieldsMapping fieldsMapping)
	        throws ForbiddenOperationException {
		
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
				}
				catch (ForbiddenOperationException e) {
					//Continue
				}
				
				break;
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
		
		fieldsMapping.setSrcValue(buffer.toString());
	}
	
	private Object evaluateStringExpression(String expression) throws Exception {
		Pattern pattern = Pattern.compile("\\(([^)]+)\\)\\.(\\w+)\\((.*)\\)");
		Matcher matcher = pattern.matcher(expression);
		
		if (matcher.find()) {
			String param = matcher.group(1);
			String methodName = matcher.group(2);
			String methodArgs = matcher.group(3);
			
			String[] args = methodArgs.isEmpty() ? new String[0] : methodArgs.split(",");
			Object[] methodParameters = new Object[args.length];
			
			for (int i = 0; i < args.length; i++) {
				methodParameters[i] = args[i].trim();
			}
			
			Method method;
			
			if (methodParameters.length == 0) {
				method = String.class.getMethod(methodName);
				return method.invoke(param);
			} else {
				Class<?>[] paramTypes = new Class<?>[methodParameters.length];
				for (int i = 0; i < methodParameters.length; i++) {
					paramTypes[i] = String.class;
				}
				method = String.class.getMethod(methodName, paramTypes);
				return method.invoke(param, methodParameters);
			}
		}
		
		throw new Exception("Invalid string expression: " + expression);
	}
	
}
