package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.EtlConfiguration;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * This transformer allow the transformation based on arithmetic expression. The expression should
 * be provided by {@link FieldsMapping#getSrcField()}
 */
public class ArithmeticFieldTransformer implements EtlFieldTransformer {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private static ArithmeticFieldTransformer defaultTransformer;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	private ArithmeticFieldTransformer() {
	}
	
	public static ArithmeticFieldTransformer getInstance() {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK_STRING) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			defaultTransformer = new ArithmeticFieldTransformer();
			
			return defaultTransformer;
		}
	}
	
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
				dstValue = evaluateExpression(fieldsMapping.getSrcValue());
				
				dstValue = utilities.parseValue(dstValue.toString(),
				    transformedRecord.getFieldType(fieldsMapping.getDstField()));
			}
			catch (Exception e) {
				throw new EtlExceptionImpl("Failed to evaluate the arithmetic expression: " + fieldsMapping.getSrcValue(),
				        e);
			}
		} else {
			throw new ForbiddenOperationException("Source value must be provided for arithmetic transformation.");
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
	
	private Double evaluateExpression(String expression) throws Exception {
		Expression e = new ExpressionBuilder(expression).build();
		return e.evaluate();
	}
	
}
