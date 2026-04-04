package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 * Field transformer that evaluates arithmetic expressions to produce the destination field value.
 * <p>
 * The arithmetic expression is defined in the source value of the field configuration and may
 * contain numeric literals and mathematical operators supported by the
 * {@link net.objecthunter.exp4j} expression engine.
 * </p>
 * <p>
 * Before evaluation, any dynamic parameters present in the expression are resolved using
 * {@link EtlFieldTransformer#tryToReplaceParametersOnSrcValue}.
 * </p>
 * <p>
 * The resulting expression is evaluated using the {@code exp4j} library and the computed numeric
 * result is returned as the destination field value.
 * </p>
 * <p>
 * If the expression cannot be evaluated due to syntax errors or invalid values, an
 * {@link EtlTransformationException} is thrown and the ETL processing behavior will follow the
 * configured exception handling policy.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 * valueToTransform = "(weight / (height * height))"
 * </pre> If {@code weight=70} and {@code height=1.75}, the evaluated result will be: <pre>
 * 22.857142857142858
 * </pre> which will be assigned to the destination field.
 */
public class ArithmeticFieldTransformer extends AbstractEtlFieldTransformer {
	
	public static ArithmeticFieldTransformer defaultTransformer;
	
	private static final Object LOCK = new Object();
	
	private static final Map<String, Expression> CACHE = new ConcurrentHashMap<>();
	
	public ArithmeticFieldTransformer(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		super(parameters, relatedDstConf, field);
	}
	
	public static ArithmeticFieldTransformer getInstance(List<Object> parameters, DstConf relatedDstConf,
	        TransformableField field) {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			defaultTransformer = new ArithmeticFieldTransformer(parameters, relatedDstConf, field);
			
			return defaultTransformer;
		}
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		if (additionalSrcObjects == null || additionalSrcObjects.isEmpty()) {
			throw new EtlExceptionImpl("ArithmeticFieldTransformer requires at least one source object.");
		}
		if (field.getValueToTransform() == null) {
			throw new EtlTransformationException("Source value must be provided for arithmetic transformation.", srcObject,
			        ActionOnEtlException.ABORT_PROCESS);
		}
		
		String srcValueWithParamsReplaced = EtlFieldTransformer
		        .tryToReplaceParametersOnSrcValue(additionalSrcObjects, field.getValueToTransform()).toString();
		
		try {
			
			Double result = evaluateExpression(srcValueWithParamsReplaced);
			
			FieldTransformingInfo transformingInfo = new FieldTransformingInfo(field, result, null);
			
			transformingInfo.setLoadedWithDefaultValue(true);
			
			return transformingInfo;
			
		}
		catch (Exception e) {
			
			throw new EtlTransformationException("Failed to evaluate arithmetic expression: " + field.getValueToTransform(),
			        e, srcObject, ActionOnEtlException.ABORT_PROCESS);
		}
	}
	
	private Double evaluateExpression(String expression) {
		
		Expression expr = CACHE.computeIfAbsent(expression, e -> new ExpressionBuilder(e).build());
		
		return expr.evaluate();
	}
	
}
