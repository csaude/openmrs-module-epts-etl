package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

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
		
		if (fieldsMapping.getSrcValue() == null) {
			throw new ForbiddenOperationException("Source value must be provided for String transformation.");
		}
		
		String srcValueWithParamsReplaced = tryToReplaceParametersOnSrcValue(srcObjects, fieldsMapping);
		
		Object dstValue = null;
		
		try {
			dstValue = evaluateExpression(srcValueWithParamsReplaced);
			
			dstValue = utilities.parseValue(dstValue.toString(),
			    transformedRecord.getFieldType(fieldsMapping.getDstField()));
		}
		catch (Exception e) {
			throw new EtlExceptionImpl("Failed to evaluate the arithmetic expression: " + fieldsMapping.getSrcValue(), e);
		}
		
		transformedRecord.setFieldValue(fieldsMapping.getDstField(), dstValue);
	}
	
	private Double evaluateExpression(String expression) throws Exception {
		Expression e = new ExpressionBuilder(expression).build();
		return e.evaluate();
	}
	
}
