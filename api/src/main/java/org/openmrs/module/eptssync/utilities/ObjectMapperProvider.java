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

	Class<?> clazz;
	
	public ObjectMapperProvider() {
		defaultObjectMapper = createDefaultMapper();
	}
	
	/*
	public ObjectMapperProvider(File openMrsTargetDirectory, String classesPackage) {
		defaultObjectMapper = createDefaultMapper(openMrsTargetDirectory, classesPackage);
	}*/

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
		result.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS , true);
		
		
		return result;
	}
	
	/*
	private static ObjectMapper createDefaultMapper(File targetDirectory, String classesPackage) {
		final ObjectMapper result = new ObjectMapper();
		result.configure(SerializationFeature.INDENT_OUTPUT, true);
		result.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		result.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		result.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
		result.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES , true);
		result.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS , true);
		
		
		if (targetDirectory != null) {
			Class<?>[] classes = new Class<?>[targetDirectory.list().length];
			
			File[] files = targetDirectory.listFiles();
			
			for (int i =0; i < files.length; i++) {
				classes[i] = OpenMRSClassGenerator.tryToGetExistingCLass(targetDirectory, classesPackage + "." + FileUtilities.generateFileNameFromRealPathWithoutExtension(files[i].getAbsolutePath()));
			}
			
			result.registerSubtypes(classes);
		}
		
		return result;
	}*/
}
