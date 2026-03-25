package org.openmrs.module.epts.etl.etl.processor.transformer;

import java.util.HashMap;
import java.util.Map;

public class TransformerRegistry {
	
	private static final Map<String, Class<? extends EtlFieldTransformer>> CUSTOM = new HashMap<>();
	
	public static void register(String key, Class<? extends EtlFieldTransformer> clazz) {
		CUSTOM.put(key, clazz);
	}
	
	public static EtlFieldTransformer create(String key) {
		try {
			return CUSTOM.get(key).getDeclaredConstructor().newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
