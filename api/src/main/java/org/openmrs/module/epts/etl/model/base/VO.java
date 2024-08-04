package org.openmrs.module.epts.etl.model.base;

import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.openmrs.module.epts.etl.exceptions.ForbiddenOperationException;
import org.openmrs.module.epts.etl.model.Field;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface VO {
	public static final CommonUtilities utils = CommonUtilities.getInstance();
	
	void load(ResultSet rs) throws SQLException;
	
	String generateTableName();
	
	boolean isExcluded();
	
	void setExcluded(boolean excluded);
	
	List<Field> getFields();
	
	void setFields(List<Field> fields);
	
	/**
	 * Return a value of given field
	 * 
	 * @param fieldName of field to retrieve
	 * @return Return a value of given field
	 */
	default Object getFieldValue(String fieldName) throws ForbiddenOperationException {
		return CommonUtilities.getInstance().getFieldValue(this, fieldName);
	}
	
	void setFieldValue(String fieldName, Object value);
	
	default void generateFields() {
		List<Field> fields = new ArrayList<Field>();
		Class<?> cl = getClass();
		
		while (cl != null) {
			java.lang.reflect.Field[] in = cl.getDeclaredFields();
			for (int i = 0; i < in.length; i++) {
				java.lang.reflect.Field instanceField = in[i];
				if (Modifier.isStatic(instanceField.getModifiers()))
					continue;
				
				instanceField.setAccessible(true);
				
				Field f = Field.fastCreateWithType(instanceField.getName(), instanceField.getType().getTypeName());
				
				fields.add(f);
			}
			cl = cl.getSuperclass();
		}
		
		setFields(fields);
	}
	
	@JsonIgnore
	default List<String> getFieldValuesAsString() {
		List<String> fieldValues = new ArrayList<>();
		
		for (Object o : getFieldValues()) {
			fieldValues.add(o != null ? o.toString() : "");
		}
		
		return fieldValues;
		
	}
	
	@JsonIgnore
	default List<Object> getFieldValues() {
		List<Object> fieldsValues = new ArrayList<>(getFields().size());
		
		for (Field field : this.getFields()) {
			String fieldNameInSnakeCase = utils.parsetoSnakeCase(field.getName());
			String fieldNameInCameCase = utils.parsetoCamelCase(field.getName());
		
			try {
				fieldsValues.add(getFieldValue(fieldNameInSnakeCase));
			}
			catch (ForbiddenOperationException e) {
				fieldsValues.add(getFieldValue(fieldNameInCameCase));
			}
		}
		
		return fieldsValues;
	}
	
}
