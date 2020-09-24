/**
 * 
 */
package org.openmrs.module.eptssync.utilities;

import javax.ws.rs.ext.ContextResolver;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	final ObjectMapper defaultObjectMapper;

	public ObjectMapperProvider() {
		defaultObjectMapper = createDefaultMapper();
	}

	@Override
	public ObjectMapper getContext(Class<?> type) {
		return defaultObjectMapper;
	}

	private static ObjectMapper createDefaultMapper() {
		final ObjectMapper result = new ObjectMapper();
		result.configure(SerializationFeature.INDENT_OUTPUT, true);
		result.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		result.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		result.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES , true);
		//result.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS , true);
		
		return result;
	}
}
