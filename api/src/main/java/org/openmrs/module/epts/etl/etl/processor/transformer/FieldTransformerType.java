package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.epts.etl.conf.DstConf;
import org.openmrs.module.epts.etl.conf.interfaces.TransformableField;
import org.openmrs.module.epts.etl.conf.types.RelationshipResolutionStrategy;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;

public enum FieldTransformerType {
	
	DEFAULT_TRANSFORMER(DefaultFieldTransformer.class.getCanonicalName(), DefaultFieldTransformer::getInstance),
	ARITHMETIC_TRANSFORMER(ArithmeticFieldTransformer.class.getCanonicalName(), ArithmeticFieldTransformer::getInstance),
	STRING_TRANSFORMER(StringTranformer.class.getCanonicalName(), StringTranformer::getInstance),
	SIMPLE_VALUE_TRANSFORMER(SimpleValueTransformer.class.getCanonicalName(), SimpleValueTransformer::getInstance),
	MAPPING_TRANSFORMER(MappingFieldTransformer.class.getCanonicalName(), MappingFieldTransformer::getInstance),
	FAST_SQL_TRANSFORMER(FastSqlFieldTransformer.class.getCanonicalName(), FastSqlFieldTransformer::getInstance),
	COALESCE_TRANSFORMER(CoalesceFieldTransformer.class.getCanonicalName(), CoalesceFieldTransformer::getInstance),
	PARENT_ON_DEMAND_TRANSFORMER(
	        ParentOnDemandLoadTransformer.class.getCanonicalName(),
	        ParentOnDemandLoadTransformer::getInstance),
	CUSTOM_TRANSFORMER(null, null);
	
	private final String className;
	
	private final TransformerFactory factory;
	
	private static final Map<String, Method> METHOD_CACHE = new HashMap<>();
	
	FieldTransformerType(String className, TransformerFactory factory) {
		this.className = className;
		this.factory = factory;
	}
	
	public String getClassName() {
		return className;
	}
	
	public boolean isDefault() {
		return this == DEFAULT_TRANSFORMER;
	}
	
	public boolean isArithmetic() {
		return this == ARITHMETIC_TRANSFORMER;
	}
	
	public boolean isString() {
		return this == STRING_TRANSFORMER;
	}
	
	public boolean isSrcValue() {
		return this == SIMPLE_VALUE_TRANSFORMER;
	}
	
	public boolean isMapping() {
		return this == MAPPING_TRANSFORMER;
	}
	
	public boolean isFastSql() {
		return this == FAST_SQL_TRANSFORMER;
	}
	
	public boolean isCoalesce() {
		return this == COALESCE_TRANSFORMER;
	}
	
	public boolean isParentOnDemand() {
		return this == PARENT_ON_DEMAND_TRANSFORMER;
	}
	
	public boolean isCustom() {
		return this == CUSTOM_TRANSFORMER;
	}
	
	private static Method getFactoryMethod(Class<?> clazz) throws Exception {
		
		return METHOD_CACHE.computeIfAbsent(clazz.getName(), k -> {
			try {
				return clazz.getDeclaredMethod("getInstance", List.class, DstConf.class, TransformableField.class);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	public EtlFieldTransformer create(List<Object> parameters, DstConf relatedDstConf, TransformableField field) {
		
		if (this.isCustom()) {
			try {
				Class<?> clazz = Class.forName(extractClassName(field.getTransformer()));
				
				Method m = getFactoryMethod(clazz);
				
				return (EtlFieldTransformer) m.invoke(null, parameters, relatedDstConf, field);
				
			}
			catch (Exception e) {
				throw new EtlExceptionImpl(
				        "Error resolving custum transformer on transformer definition: " + field.getTransformer(), e);
			}
		}
		
		if (factory == null) {
			throw new IllegalStateException("No factory defined for this type");
		}
		
		return factory.create(parameters, relatedDstConf, field);
	}
	
	/**
	 * Resolve enum pelo className
	 */
	public static FieldTransformerType fromClassName(String transformerDefinition) {
		
		String className = extractClassName(transformerDefinition);
		
		for (FieldTransformerType type : values()) {
			if (type.className != null && type.className.equals(className)) {
				return type;
			}
		}
		
		throw new IllegalArgumentException("Unknown transformer: " + transformerDefinition);
	}
	
	public static FieldTransformerType resolveType(TransformableField field) {
		
		String def = field.getTransformer();
		FieldTransformerType transformer;
		
		if (def == null || def.isBlank()) {
			transformer = field.getValueToTransform() != null ? SIMPLE_VALUE_TRANSFORMER : DEFAULT_TRANSFORMER;
		} else {
			
			String selector = extractClassName(def);
			
			if (selector.contains(".")) {
				try {
					transformer = fromClassName(def);
				}
				catch (Exception e) {
					transformer = CUSTOM_TRANSFORMER;
				}
			} else
				try {
					transformer = FieldTransformerType.valueOf(selector.toUpperCase().replace("-", "_"));
				}
				catch (Exception ignored) {
					transformer = null;
				}
		}
		
		if (transformer != null) {
			if (!field.hasTransformer()) {
				field.setTransformer(transformer.className);
			}
			
			return transformer;
		}
		
		throw new IllegalArgumentException("Unknown transformer: " + def);
	}
	
	public static void tryToLoadTransformerToField(TransformableField field, DstConf dstConf) {
		
		if (field.getTransformerInstance() != null)
			return;
		
		FieldTransformerType type = resolveType(field);
		
		List<Object> parameters = tryToLoadTransformerParameters(field.getTransformer());
		
		field.setTransformerInstance(type.create(parameters, dstConf, field));
		
		if (field.getTransformerType().isMapping()) {
			field.setRelationshipResolutionStrategy(RelationshipResolutionStrategy.SKIP);
		}
	}
	
	public static List<Object> tryToLoadTransformerParameters(String def) {
		
		List<Object> params = new ArrayList<>();
		
		if (def == null || !def.contains("(") || !def.endsWith(")")) {
			return params;
		}
		
		String inside = def.substring(def.indexOf("(") + 1, def.lastIndexOf(")")).trim();
		
		if (inside.isEmpty())
			return params;
		
		int depth = 0;
		StringBuilder current = new StringBuilder();
		
		for (char c : inside.toCharArray()) {
			
			if (c == ',' && depth == 0) {
				params.add(current.toString().trim());
				current.setLength(0);
				continue;
			}
			
			if (c == '(')
				depth++;
			if (c == ')')
				depth--;
			
			current.append(c);
		}
		
		if (!current.isEmpty()) {
			params.add(current.toString().trim());
		}
		
		return params;
	}
	
	private static String extractClassName(String transformerDefinition) {
		
		String def = transformerDefinition.trim();
		
		if (def.contains("(")) {
			return def.substring(0, def.indexOf("(")).trim();
		}
		
		return def;
	}
}
