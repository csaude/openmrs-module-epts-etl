package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.etl.processor.EtlProcessor;
import org.openmrs.module.epts.etl.exceptions.ActionOnEtlException;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.EtlTransformationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

/**
 * Field transformer that evaluates string manipulation expressions using standard {@link String}
 * methods.
 * <p>
 * The transformer expects expressions that follow the format: <pre>
 * (value).methodName(arg1, arg2, ...)
 * </pre> where:
 * <ul>
 * <li><b>value</b> – the source string value</li>
 * <li><b>methodName</b> – the name of a method defined in {@link String}</li>
 * <li><b>arg1, arg2...</b> – optional arguments passed to the method</li>
 * </ul>
 * </p>
 * <p>
 * Before evaluation, any dynamic parameters in the expression are resolved using
 * {@link EtlFieldTransformer#tryToReplaceParametersOnSrcValue}.
 * </p>
 * <p>
 * The transformer uses Java reflection to invoke the specified method on the {@link String} class.
 * </p>
 * <p>
 * Example expressions:
 * </p>
 * <pre>
 * (John).toUpperCase()
 * (hello world).substring(0,5)
 * (abc123).replace(123,XYZ)
 * </pre>
 * <p>
 * If the expression cannot be parsed or the method invocation fails, an
 * {@link EtlTransformationException} is raised.
 * </p>
 */
public class StringTranformer extends AbstractEtlFieldTransformer{
	
	private static StringTranformer defaultTransformer;
	
	private static final Pattern STRING_EXPRESSION_PATTERN = Pattern.compile("\\(([^)]+)\\)\\.(\\w+)\\((.*)\\)");
	
	private static final Object LOCK = new Object();
	
	public StringTranformer(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		super(parameters, relatedDstConf, field);
	}
	
	public static StringTranformer getInstance(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			defaultTransformer = new StringTranformer(parameters, relatedDstConf, field);
			
			return defaultTransformer;
		}
	}
	
	@Override
	public FieldTransformingInfo transform(EtlProcessor processor, EtlDatabaseObject srcObject,
	        EtlDatabaseObject transformedRecord, List<EtlDatabaseObject> additionalSrcObjects, TransformableField field,
	        Connection srcConn, Connection dstConn) throws DBException, EtlTransformationException {
		
		if (additionalSrcObjects == null || additionalSrcObjects.isEmpty()) {
			throw new EtlTransformationException("StringTransformer requires at least one source object.", null, srcObject,
			        ActionOnEtlException.ABORT);
		}
		
		if (field.getValueToTransform() == null) {
			throw new EtlTransformationException("Source value must be provided for string transformation.", srcObject,
			        ActionOnEtlException.ABORT);
		}
		
		String srcValueWithParamsReplaced = EtlFieldTransformer
		        .tryToReplaceParametersOnSrcValue(additionalSrcObjects, field.getValueToTransform()).toString();
		
		try {
			
			Object result = evaluateStringExpression(srcValueWithParamsReplaced);
			
			FieldTransformingInfo transformingInfo = new FieldTransformingInfo(field, result, null);
			
			transformingInfo.setLoadedWithDefaultValue(true);
			
			return transformingInfo;
			
		}
		catch (Exception e) {
			
			throw new EtlTransformationException("Failed to evaluate string expression: " + field.getValueToTransform(), e,
			        srcObject, ActionOnEtlException.ABORT);
		}
	}
	
	private Object evaluateStringExpression(String expression) throws Exception {
		
		Matcher matcher = STRING_EXPRESSION_PATTERN.matcher(expression);
		
		if (!matcher.find()) {
			throw new EtlExceptionImpl("Invalid string expression: " + expression);
		}
		
		String param = matcher.group(1);
		String methodName = matcher.group(2);
		String methodArgs = matcher.group(3);
		
		String[] args = methodArgs.isEmpty() ? new String[0] : methodArgs.split("\\s*,\\s*");
		
		Object[] methodParameters = new Object[args.length];
		Class<?>[] paramTypes = new Class<?>[args.length];
		
		for (int i = 0; i < args.length; i++) {
			
			Object parsed = parseArgument(args[i]);
			
			methodParameters[i] = parsed;
			paramTypes[i] = parsed.getClass();
		}
		
		Method method = String.class.getMethod(methodName, paramTypes);
		
		return method.invoke(param, methodParameters);
	}
	
	private Object parseArgument(String arg) {
		
		if (arg.matches("-?\\d+")) {
			return Integer.parseInt(arg);
		}
		
		return arg;
	}
	
}
