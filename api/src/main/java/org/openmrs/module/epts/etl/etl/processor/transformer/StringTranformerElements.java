package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openmrs.module.epts.etl.model.EtlDatabaseObject;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

public class StringTranformerElements {
	
	private static CommonUtilities utilities = CommonUtilities.getInstance();
	
	private Object valueToTransform;
	
	private String function;
	
	private List<Object> params;
	
	private StringTranformerElements nextElements;
	
	private Method method;
	
	public Object getValueToTransform() {
		return valueToTransform;
	}
	
	public void setValueToTransform(Object valueToTransform) {
		this.valueToTransform = valueToTransform;
	}
	
	public String getFunction() {
		return function;
	}
	
	public void setFunction(String function) {
		this.function = function;
	}
	
	public List<Object> getParams() {
		return params;
	}
	
	public void setParams(List<Object> params) {
		this.params = params;
	}
	
	public StringTranformerElements getNextElements() {
		return nextElements;
	}
	
	public void setNextElements(StringTranformerElements nextElements) {
		this.nextElements = nextElements;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		buildString(sb, 0);
		return sb.toString();
	}
	
	private void buildString(StringBuilder sb, int level) {
		
		for (int i = 0; i < level; i++) {
			sb.append("\t");
		}
		
		sb.append("valueToTransform=").append(valueToTransform).append(", function=").append(function).append(", params=")
		        .append(params).append("\n");
		
		if (nextElements != null) {
			nextElements.buildString(sb, level + 1);
		}
	}
	
	public void init() {
		resolveBestMethod();
	}
	
	private void resolveBestMethod() {
		
		String methodName = this.function;
		int paramCount = utilities.arraySize(params);
		
		Method[] methods = String.class.getMethods();
		
		for (Method m : methods) {
			if (m.getName().equals(methodName) && m.getParameterCount() == paramCount) {
				this.method = m;
				
				return;
			}
		}
		
		throw new RuntimeException("No matching method found: " + methodName + " with " + paramCount + " params");
	}
	
	public Object evaluate(List<EtlDatabaseObject> additionalSrcObjects) throws Exception {
		
		StringTranformerElements element = this;
		
		if (element.getFunction() != null) {
			List<Object> params = element.getParams();
			
			Class<?>[] paramTypes = method.getParameterTypes();
			Object[] methodParams = new Object[paramTypes.length];
			
			for (int i = 0; i < paramTypes.length; i++) {
				
				Object rawValue = EtlFieldTransformer.tryToReplaceParametersOnSrcValue(additionalSrcObjects,
				    params.get(i).toString());
				
				methodParams[i] = convertToType(rawValue, paramTypes[i]);
			}
			
			Object valueToTransform = EtlFieldTransformer.tryToReplaceParametersOnSrcValue(additionalSrcObjects,
			    element.getValueToTransform().toString());
			
			Object currentValue = method.invoke(valueToTransform.toString(), methodParams);
			
			if (element.getNextElements() != null) {
				element.getNextElements().setValueToTransform(currentValue);
				return element.getNextElements().evaluate(additionalSrcObjects);
			}
			
			return currentValue;
		}
		
		return element.getValueToTransform();
	}
	
	private Object convertToType(Object value, Class<?> targetType) {
		
		if (value == null)
			return null;
		
		String str = value.toString();
		
		if (targetType == String.class) {
			return str;
		}
		
		if (targetType == int.class || targetType == Integer.class) {
			return Integer.parseInt(str);
		}
		
		if (targetType == long.class || targetType == Long.class) {
			return Long.parseLong(str);
		}
		
		if (targetType == double.class || targetType == Double.class) {
			return Double.parseDouble(str);
		}
		
		if (targetType == boolean.class || targetType == Boolean.class) {
			return Boolean.parseBoolean(str);
		}
		
		if (targetType == char.class || targetType == Character.class) {
			return str.charAt(0);
		}
		
		return value;
	}
	
	public static StringTranformerElements buildChain(Object value, String remaining) {
		
		StringTranformerElements element = new StringTranformerElements();
		element.setValueToTransform(value);
		
		if (remaining == null || remaining.isBlank()) {
			return null;
		}
		
		Pattern pattern = Pattern.compile("^\\.(\\w+)\\(([^)]*)\\)(.*)$");
		Matcher matcher = pattern.matcher(remaining);
		
		if (!matcher.find()) {
			return element;
		}
		
		String methodName = matcher.group(1);
		String argsStr = matcher.group(2);
		String next = matcher.group(3);
		
		element.setFunction(methodName);
		
		List<Object> params = new ArrayList<>();
		
		if (!argsStr.isBlank()) {
			String[] args = splitArguments(argsStr);
			for (String arg : args) {
				params.add(parseArgument(arg));
			}
		}
		
		element.setParams(params);
		
		element.init();
		
		element.setNextElements(buildChain(null, next));
		
		return element;
	}
	
	private static String[] splitArguments(String args) {
		
		List<String> result = new ArrayList<>();
		
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		
		for (char c : args.toCharArray()) {
			
			if (c == '"' || c == '\'') {
				inQuotes = !inQuotes;
			}
			
			if (c == ',' && !inQuotes) {
				result.add(current.toString().trim());
				current.setLength(0);
				continue;
			}
			
			current.append(c);
		}
		
		if (!current.isEmpty()) {
			result.add(current.toString().trim());
		}
		
		return result.toArray(new String[0]);
	}
	
	private static Object parseArgument(String arg) {
		
		arg = arg.trim();
		
		// Strings com aspas
		if ((arg.startsWith("\"") && arg.endsWith("\"")) || (arg.startsWith("'") && arg.endsWith("'"))) {
			return arg.substring(1, arg.length() - 1);
		}
		
		// Integer
		if (arg.matches("-?\\d+")) {
			return Integer.parseInt(arg);
		}
		
		// Double
		if (arg.matches("-?\\d+\\.\\d+")) {
			return Double.parseDouble(arg);
		}
		
		return arg;
	}
	
}
