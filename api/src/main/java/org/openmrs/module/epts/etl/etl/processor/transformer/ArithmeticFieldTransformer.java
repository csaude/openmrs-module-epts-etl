package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;

import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.controller.conf.tablemapping.FieldsMapping;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * This transformer allow the transformation based on arithmetic expression. The expression should
 * be provided by {@link FieldsMapping#getSrcField()}
 */
public class ArithmeticFieldTransformer implements EtlFieldTransformer {
	
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
	public Object transform(List<EtlDatabaseObject> srcObjects, TransformableField field, Connection srcConn,
	        Connection dstConn) throws DBException, ForbiddenOperationException {
		
		if (field.getValueToTransform() == null) {
			throw new ForbiddenOperationException("Source value must be provided for String transformation.");
		}
		
		String srcValueWithParamsReplaced = tryToReplaceParametersOnSrcValue(srcObjects, field.getValueToTransform());
		
		try {
			return evaluateExpression(srcValueWithParamsReplaced);
		}
		catch (Exception e) {
			throw new EtlExceptionImpl("Failed to evaluate the arithmetic expression: " + field.getValueToTransform(), e);
		}
	}
	
	private Double evaluateExpression(String expression) throws Exception {
		Expression e = new ExpressionBuilder(expression).build();
		return e.evaluate();
	}
	
}
