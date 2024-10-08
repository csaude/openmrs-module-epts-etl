/**
 * 
 */
package org.openmrs.module.epts.etl.utilities;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	final ObjectMapper defaultObjectMapper;

	Class<?> clazz;
	
	public ObjectMapperProvider() {
		defaultObjectMapper = createDefaultMapper();
	}
	
	public ObjectMapperProvider(Class<?> ...types) {
		defaultObjectMapper = createDefaultMapper(types);
	}
	
	/*
	public ObjectMapperProvider(File openMrsTargetDirectory, String classesPackage) {
		defaultObjectMapper = createDefaultMapper(openMrsTargetDirectory, classesPackage);
	}*/

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return defaultObjectMapper;
	}

	private static ObjectMapper createDefaultMapper(Class<?> ...types) {
		final ObjectMapper result = new ObjectMapper();
		result.configure(SerializationFeature.INDENT_OUTPUT, true);
		result.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
		result.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		result.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES , true);
		result.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS , true);
		result.setSerializationInclusion(Include.NON_NULL);
	
		result.registerSubtypes(types);
		
		return result;
	}
	
}
