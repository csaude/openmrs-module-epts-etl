package org.openmrs.module.epts.etl.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.openmrs.module.epts.etl.utilities.AttDefinedElements;
import org.openmrs.module.epts.etl.utilities.CommonUtilities;
import org.openmrs.module.epts.etl.utilities.DateAndTimeUtilities;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Esta classe representa o field de um formulário. ATENCAO que o formField pode representar também
 * o file.
 * 
 * @author JP. Boane
 * @version 1.0 29/10/2012
 */
public class Field implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	
	private Object value;
	
	private String type;
	
	private AttDefinedElements attDefinedElements;
	
	public Field() {
	}
	
	public AttDefinedElements getAttDefinedElements() {
		return attDefinedElements;
	}
	
	public void setAttDefinedElements(AttDefinedElements attDefinedElements) {
		this.attDefinedElements = attDefinedElements;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Field(String name) {
		this.name = name;
	}
	
	public Field(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNameAsClassAtt() {
		return AttDefinedElements.convertTableAttNameToClassAttName(this.name);
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}
	
	public static Object getParameter(List<? extends Field> fields, String name) {
		Field field = CommonUtilities.getInstance().findOnArray(fields, new Field(name, null));
		
		return field != null ? field.value : null;
	}
	
	public static void printAll(List<? extends Field> fields) {
		for (Object field : fields)
			System.err.println(field);
	}
	
	@Override
	@JsonIgnore
	public String toString() {
		return "[Name: " + getName() + ", Value " + value + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		
		if (obj instanceof Field) {
			if (this.name.equals(((Field) obj).name))
				return true;
		}
		
		if (obj instanceof String) {
			return this.name.equals(obj);
		}
		
		return super.equals(obj);
	}
	
	/**
	 * Retorna o valor Integer correspondente ao valor do parametro identificado por 'paramName'; Se
	 * o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static Integer getInteger(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return 0;
		
		try {
			return Integer.parseInt((String) value);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Retorna o valor date correspondente ao valor do parametro identificado por 'paramName'; Se o
	 * parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static Date getDate(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return null;
		
		try {
			return DateAndTimeUtilities.createDate((String) value, DateAndTimeUtilities.DATE_FORMAT);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Retorna o valor int correspondente ao valor do parametro identificado por 'paramName'; Se o
	 * parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static int getInt(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return 0;
		
		try {
			return Integer.parseInt((String) value);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Retorna o valor double correspondente ao valor do parametro identificado por 'paramName'; Se
	 * o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static double getDouble(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return 0;
		
		try {
			return Double.parseDouble((String) value);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}
	
	/**
	 * Retorna o valor String correspondente ao valor do parametro identificado por 'paramName'; Se
	 * o parametro nao existir ou se o valor nao for compativel, retorna '0'
	 * 
	 * @param fields
	 * @param paramName
	 * @return
	 */
	public static String getString(List<? extends Field> fields, String paramName) {
		Object value = getParameter(fields, paramName);
		
		if (value == null)
			return "";
		
		return (String) value;
	}
	
	public boolean isDateField() {
		return AttDefinedElements.isDateType(this.type);
	}
	
	public boolean isNumericColumnType() {
		return AttDefinedElements.isNumeric(this.type);
	}
	
	public Field createACopy() {
		Field f = new Field(this.name);
		
		f.setType(this.type);
		
		return f;
	}
}
