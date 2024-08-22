package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;
import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.db.conn.DBException;

public class StringTranformer implements EtlFieldTransformer {
	
	private static StringTranformer defaultTransformer;
	
	private static final String LOCK_STRING = "LOCK_STRING";
	
	private StringTranformer() {
	}
	
	public static StringTranformer getInstance() {
		if (defaultTransformer != null)
			return defaultTransformer;
		
		synchronized (LOCK_STRING) {
			if (defaultTransformer != null)
				return defaultTransformer;
			
			defaultTransformer = new StringTranformer();
			
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
			return evaluateStringExpression(srcValueWithParamsReplaced);
		}
		catch (Exception e) {
			throw new EtlExceptionImpl("Failed to evaluate the string expression: " + field.getValueToTransform(), e);
		}
		
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
