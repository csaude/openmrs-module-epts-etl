package org.openmrs.module.epts.etl.conf;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.openmrs.module.epts.etl.conf.interfaces.EtlDataConfiguration;
import org.openmrs.module.epts.etl.conf.types.EtlActionType;
import org.openmrs.module.epts.etl.exceptions.EtlExceptionImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TemplateOverride {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	private String path;
	
	private JsonNode match;
	
	private JsonNode value;
	
	private EtlActionType type;
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public JsonNode getMatch() {
		return match;
	}
	
	public void setMatch(JsonNode match) {
		this.match = match;
	}
	
	public JsonNode getValue() {
		return value;
	}
	
	public void setValue(JsonNode value) {
		this.value = value;
	}
	
	public EtlActionType getType() {
		return type;
	}
	
	public void setType(EtlActionType type) {
		this.type = type;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void applyOverride(EtlDataConfiguration etlDataConfiguration) {
		
		if (etlDataConfiguration == null) {
			throw new EtlExceptionImpl("etlDataConfiguration cannot be null");
		}
		
		if (this.path == null || this.path.isBlank()) {
			throw new EtlExceptionImpl("Override path cannot be null or empty");
		}
		
		if (this.type == null) {
			throw new EtlExceptionImpl("Override type cannot be null");
		}
		
		try {
			ResolvedPath resolvedPath = resolveParentAndField(etlDataConfiguration, this.path);
			Object parentObject = resolvedPath.parentObject;
			Field targetField = resolvedPath.field;
			
			targetField.setAccessible(true);
			
			Object currentFieldValue = targetField.get(parentObject);
			
			if (List.class.isAssignableFrom(targetField.getType())) {
				List targetList = (List) currentFieldValue;
				
				if (targetList == null) {
					throw new EtlExceptionImpl(
					        "List field '" + targetField.getName() + "' is null. Initialize it before applying override.");
				}
				
				Class<?> itemType = resolveListItemType(targetField);
				
				if (this.type == EtlActionType.CREATE) {
					if (this.value == null || this.value.isNull()) {
						throw new EtlExceptionImpl("CREATE override requires a non-null value for path: " + this.path);
					}
					
					Object newItem = MAPPER.treeToValue(this.value, itemType);
					targetList.add(newItem);
					return;
				}
				
				int matchedIndex = findMatchingElementIndex(targetList, this.match);
				
				if (matchedIndex < 0) {
					throw new EtlExceptionImpl("No matching element found in list '" + targetField.getName()
					        + "' for override path: " + this.path);
				}
				
				if (this.type == EtlActionType.UPDATE) {
					if (this.value == null || this.value.isNull()) {
						throw new EtlExceptionImpl("UPDATE override requires a non-null value for path: " + this.path);
					}
					
					Object replacement = MAPPER.treeToValue(this.value, itemType);
					targetList.set(matchedIndex, replacement);
					return;
				}
				
				if (this.type == EtlActionType.DELETE) {
					targetList.remove(matchedIndex);
					return;
				}
				
				throw new EtlExceptionImpl("Unsupported override type: " + this.type);
			}
			
			if (this.type == EtlActionType.DELETE) {
				targetField.set(parentObject, null);
				return;
			}
			
			if (this.value == null || this.value.isNull()) {
				throw new EtlExceptionImpl(this.type + " override requires a non-null value for path: " + this.path);
			}
			
			Object newValue = MAPPER.treeToValue(this.value, targetField.getType());
			targetField.set(parentObject, newValue);
			
		}
		catch (EtlExceptionImpl e) {
			throw e;
		}
		catch (Exception e) {
			throw new EtlExceptionImpl("Error applying override on path '" + this.path + "'", e);
		}
	}
	
	private int findMatchingElementIndex(List<?> list, JsonNode matchNode) {
		if (matchNode == null || matchNode.isNull() || !matchNode.isObject()) {
			throw new EtlExceptionImpl("A valid match object is required for UPDATE/DELETE on list path: " + this.path);
		}
		
		for (int i = 0; i < list.size(); i++) {
			Object currentItem = list.get(i);
			
			if (matches(currentItem, matchNode)) {
				return i;
			}
		}
		
		return -1;
	}
	
	private boolean matches(Object candidate, JsonNode matchNode) {
		if (candidate == null) {
			return false;
		}
		
		Iterator<String> fieldNames = matchNode.fieldNames();
		
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode expectedNode = matchNode.get(fieldName);
			
			Object actualValue = readFieldValue(candidate, fieldName);
			JsonNode actualNode = MAPPER.valueToTree(actualValue);
			
			if (!jsonValuesEqual(actualNode, expectedNode)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean jsonValuesEqual(JsonNode actualNode, JsonNode expectedNode) {
		if (actualNode == null || actualNode.isNull()) {
			return expectedNode == null || expectedNode.isNull();
		}
		
		if (expectedNode == null || expectedNode.isNull()) {
			return actualNode.isNull();
		}
		
		if (actualNode.isNumber() && expectedNode.isNumber()) {
			return Objects.equals(actualNode.decimalValue(), expectedNode.decimalValue());
		}
		
		if (actualNode.isBoolean() && expectedNode.isBoolean()) {
			return actualNode.booleanValue() == expectedNode.booleanValue();
		}
		
		return actualNode.asText().equals(expectedNode.asText());
	}
	
	private Object readFieldValue(Object target, String fieldName) {
		try {
			Field field = findField(target.getClass(), fieldName);
			
			if (field == null) {
				throw new EtlExceptionImpl("Field '" + fieldName + "' not found on class " + target.getClass().getName());
			}
			
			field.setAccessible(true);
			return field.get(target);
		}
		catch (IllegalAccessException e) {
			throw new EtlExceptionImpl("Error reading field '" + fieldName + "' from class " + target.getClass().getName(),
			        e);
		}
	}
	
	private ResolvedPath resolveParentAndField(Object root, String path) {
		String[] parts = path.split("\\.");
		
		if (parts.length == 0) {
			throw new EtlExceptionImpl("Invalid override path: " + path);
		}
		
		Object currentObject = root;
		
		for (int i = 0; i < parts.length - 1; i++) {
			String part = parts[i];
			Field field = findField(currentObject.getClass(), part);
			
			if (field == null) {
				throw new EtlExceptionImpl("Field '" + part + "' not found while resolving path '" + path + "'");
			}
			
			try {
				field.setAccessible(true);
				currentObject = field.get(currentObject);
			}
			catch (IllegalAccessException e) {
				throw new EtlExceptionImpl("Error accessing field '" + part + "' on path '" + path + "'", e);
			}
			
			if (currentObject == null) {
				throw new EtlExceptionImpl("Intermediate object '" + part + "' is null while resolving path '" + path + "'");
			}
		}
		
		Field targetField = findField(currentObject.getClass(), parts[parts.length - 1]);
		
		if (targetField == null) {
			throw new EtlExceptionImpl("Target field '" + parts[parts.length - 1] + "' not found on path '" + path + "'");
		}
		
		return new ResolvedPath(currentObject, targetField);
	}
	
	private Field findField(Class<?> clazz, String fieldName) {
		Class<?> current = clazz;
		
		while (current != null && current != Object.class) {
			try {
				return current.getDeclaredField(fieldName);
			}
			catch (NoSuchFieldException e) {
				current = current.getSuperclass();
			}
		}
		
		return null;
	}
	
	private Class<?> resolveListItemType(Field field) {
		Type genericType = field.getGenericType();
		
		if (!(genericType instanceof ParameterizedType)) {
			throw new EtlExceptionImpl("List field '" + field.getName() + "' does not declare a generic item type");
		}
		
		Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
		
		if (args.length != 1) {
			throw new EtlExceptionImpl("Unable to resolve item type for list field '" + field.getName() + "'");
		}
		
		Type itemType = args[0];
		
		if (itemType instanceof Class<?>) {
			return (Class<?>) itemType;
		}
		
		if (itemType instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) itemType).getRawType();
		}
		
		throw new EtlExceptionImpl("Unsupported generic item type for list field '" + field.getName() + "': " + itemType);
	}
	
	private static class ResolvedPath {
		
		private final Object parentObject;
		
		private final Field field;
		
		private ResolvedPath(Object parentObject, Field field) {
			this.parentObject = parentObject;
			this.field = field;
		}
	}
}
